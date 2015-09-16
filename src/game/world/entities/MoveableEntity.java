package game.world.entities;

import game.world.Board;
import game.world.BoardCell;

import java.nio.ByteBuffer;
import java.util.UUID;

import static java.lang.Math.*;

public abstract class MoveableEntity extends PositionableEntity implements Moveable {
    private double orderedHeading;                          // this is heading that requested by player

    private double moveSpeed;                               // current moving speed
    private double orderedSpeed;                            // this is speed we must accelerate to

    private double distanceTravelled = -1;                  // counts distance this entity had travelled

    MoveableEntity(UUID ownerUniqueID, Board board) {
        super(ownerUniqueID, board);
    }

    MoveableEntity(UUID ownerUniqueID, Board board, ByteBuffer src) {
        super(ownerUniqueID, board, src);
    }

    /**
     * Utility method that calculates minimum signed difference between headings.
     *
     * @param from initial heading.
     * @param to   target heading.
     * @return degrees of shortest turn, with sign.
     */
    public static double getHeadingDelta(double from, double to) {
        from = makeHeadingInRange(from);
        to = makeHeadingInRange(to);

        if (from < to) {
            double diff1 = (to - from);
            double diff2 = (to - from) - 360;

            return Math.abs(diff1) < Math.abs(diff2) ? diff1 : diff2;
        }

        if (from > to) {
            double diff1 = (to - from);
            double diff2 = (to - from) + 360;

            return Math.abs(diff1) < Math.abs(diff2) ? diff1 : diff2;
        }

        double diff = Math.min(to - from, 180 + (from - to));

        if (diff > 0) return Math.min(diff, 360 - diff);
        if (diff < 0) return Math.max(diff, 360 + diff);

        return 0;
    }

    public double distanceTravelled() {
        return distanceTravelled;
    }

    @Override
    public void setPos(Point newPos) {
        // when entity is created it's coordinates are 0,0. When it is placed on board and it's coordinates are set
        // this method may calculate distance from 0,0 to first init point but this is wrong and should not be counted
        // so distanceTravelled is initialized to -1 and this value indicates first-time condition here.
        Point oldPos = getPos();
        if (newPos.sameAs(oldPos)) return;

        if (distanceTravelled < 0) {
            distanceTravelled = 0;
        } else {
            distanceTravelled += oldPos.getDistanceTo(newPos);
        }

        super.setPos(newPos);
    }

    public void forward() {
        setOrderedSpeed(+getTypeInfo().maxForwardSpeed);
    }

    public void reverse() {
        setOrderedSpeed(-getTypeInfo().maxReverseSpeed);
    }

    public void stop() {
        setOrderedSpeed(0);
    }

    @Override
    public double getMoveSpeed() {
        return moveSpeed;
    }

    @Override
    public void setMoveSpeed(double moveSpeed) {
        if (moveSpeed > 0) moveSpeed = Math.min(moveSpeed, +getTypeInfo().maxForwardSpeed);
        if (moveSpeed < 0) moveSpeed = Math.max(moveSpeed, -getTypeInfo().maxReverseSpeed);

        this.moveSpeed = moveSpeed;
    }

    @Override
    public double getOrderedSpeed() {
        return orderedSpeed;
    }

    @Override
    public void setOrderedSpeed(double orderedSpeed) {
        if (orderedSpeed < 0) orderedSpeed = Math.min(-getTypeInfo().maxReverseSpeed, orderedSpeed);
        if (orderedSpeed > 0) orderedSpeed = Math.min(+getTypeInfo().maxForwardSpeed, orderedSpeed);
        this.orderedSpeed = orderedSpeed;
    }

    @Override
    public double getOrderedHeading() {
        return orderedHeading;
    }

    @Override
    public void setOrderedHeading(double orderedHeading) {
        this.orderedHeading = makeHeadingInRange(orderedHeading);
    }

    public void update() {
        setHeading(getNextHeading());
        setMoveSpeed(getNextSpeed());

        Point newPos = getNextPosition();

        int col = BoardCell.xToCol(newPos.x);
        int row = BoardCell.yToRow(newPos.y);

        if (!getBoard().coordinatesInBounds(newPos.x, newPos.y) && !canCrossBoardBounds()) {
            return;
        }

        if (!cellHasObstacle(getBoard().getCell(row, col))) {
            setPos(newPos);
        }
    }

    protected double getNextSpeed() {
        double newSpeed = 0.0;
        if (getHeading() != getOrderedHeading()) return newSpeed;

        if (getOrderedSpeed() != 0) {
            if (getOrderedSpeed() > 0) newSpeed = (getMoveSpeed() + getTypeInfo().maxAcceleration);
            if (getOrderedSpeed() < 0) newSpeed = (getMoveSpeed() - getTypeInfo().maxAcceleration);
        } else {
            if (getMoveSpeed() > 0) newSpeed = (Math.max(getMoveSpeed() - getTypeInfo().maxDeceleration, 0));
            if (getMoveSpeed() < 0) newSpeed = (Math.min(getMoveSpeed() + getTypeInfo().maxDeceleration, 0));
        }

        return newSpeed;
    }

    protected double getNextHeading() {
        return doTurn(getHeading(), getOrderedHeading(), getTypeInfo().maxTurnSpeed);
    }

    protected Point getNextPosition() {
        double speed = getMoveSpeed();

        double nx = getX() + sin(toRadians(getHeading())) * speed;
        double ny = getY() - cos(toRadians(getHeading())) * speed;

        return new Point(nx, ny);
    }

    public abstract boolean cellHasObstacle(BoardCell cell);

    /**
     * Utility method to get shortest turn direction.
     *
     * @param from current direction in degrees
     * @param to   target direction in degrees.
     * @return +1 if best it is to turn clockwise, -1 if counterclockwise or 0 no turn required.
     */
    int getBestTurnDirection(double from, double to) {
        from = makeHeadingInRange(from);
        to = makeHeadingInRange(to);

        if (to > from) {
            if ((to - from) < +180) {
                return +1;
            } else {
                return -1;
            }
        }

        if (to < from) {
            if ((to - from) < -180) {
                return +1;
            } else {
                return -1;
            }
        }

        return 0;
    }

    /**
     * Utility method to do actual turning
     *
     * @param heading        current heading.
     * @param orderedHeading target heading.
     * @param MAX_TURN_SPEED maximum turn speed for this step.
     * @return returns new heading in range.
     */
    protected double doTurn(double heading, double orderedHeading, final double MAX_TURN_SPEED) {
        switch (getBestTurnDirection(heading, orderedHeading)) {
            case +1: {
                double newHeading = makeHeadingInRange(heading + MAX_TURN_SPEED);
                double diff = getHeadingDelta(heading, orderedHeading);

                if (Math.abs(diff) < Math.abs(MAX_TURN_SPEED)) {
                    newHeading = orderedHeading;
                }

                return makeHeadingInRange(newHeading);
            }
            case -1: {
                double newHeading = makeHeadingInRange(heading - MAX_TURN_SPEED);
                double diff = getHeadingDelta(heading, orderedHeading);

                if (Math.abs(diff) < Math.abs(MAX_TURN_SPEED)) {
                    newHeading = orderedHeading;
                }

                return makeHeadingInRange(newHeading);
            }
            default:
                return heading;
        }
    }

    protected abstract boolean canCrossBoardBounds();

    public void writeMoveSpeed(ByteBuffer dst) {
        dst.putShort((short) (getMoveSpeed() * 1000));
    }

    public void readMoveSpeed(ByteBuffer src) {
        setMoveSpeed(src.getShort() / 1000.0);
    }


    public void writeOrderedSpeed(ByteBuffer dst) {
        dst.putShort((short) (getOrderedSpeed() * 1000));
    }

    public void readOrderedSpeed(ByteBuffer src) {
        setOrderedSpeed(src.getShort() / 1000.0);
    }

    public void writeOrderedHeading(ByteBuffer dst) {
        dst.putShort((short) getOrderedHeading());
    }

    public void readOrderedHeading(ByteBuffer src) {
        setOrderedHeading(src.getShort());
    }

    public boolean rangeExceeded() {
        return distanceTravelled() > getTypeInfo().maxRange;
    }
}
