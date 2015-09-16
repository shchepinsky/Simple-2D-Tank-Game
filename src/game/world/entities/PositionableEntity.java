package game.world.entities;

import game.world.Board;
import game.world.BoardCell;

import java.nio.ByteBuffer;
import java.util.UUID;

abstract class PositionableEntity extends EntityBase implements Positionable {
    private double x;                                       // coordinates
    private double y;                                       //
    private double heading;                                 // direction of facing
    private BoardCell cell;                                 // occupied cell

    PositionableEntity(UUID ownerUniqueID, Board board) {
        super(ownerUniqueID, board);
    }

    PositionableEntity(UUID ownerUniqueID, Board board, ByteBuffer buf) {
        super(ownerUniqueID, board, buf);
    }

    @Override
    public BoardCell getCell() {
        return (getBoard() == null)? null : cell;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public void setPos(Point newPos) {

        BoardCell old_cell = getCell();
        BoardCell new_cell = null;

        this.x = newPos.x;
        this.y = newPos.y;

        int new_col = BoardCell.xToCol(newPos.x);
        int new_row = BoardCell.yToRow(newPos.y);

        if (getBoard()!= null && getBoard().coordinatesInBounds(newPos.x, newPos.y)) {
            new_cell = getBoard().getCell(new_row, new_col);
        }

        cell = new_cell;

        if (old_cell != new_cell && old_cell != null) {     // remove only if cell changed and was valid
            old_cell.removeEntity(this);
        }

        if (new_cell != old_cell && new_cell != null) {     // insert only if cell changed and is valid
            new_cell.insertEntity(this);
        }

    }

    @Override
    public void setPos(double x, double y) {
        setPos(new Point(x, y));
    }

    @Override
    public Point getPos() {
        return new Point(getX(), getY());
    }

    protected final void readPositionable(ByteBuffer buf) {
        try {
            short xPos = buf.getShort();
            short yPos = buf.getShort();
            setPos(xPos, yPos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Utility method to keep heading in range 0f 0..359
     * @param heading input heading
     * @return corrected output heading in range.
     */
    protected static double makeHeadingInRange(double heading) {
        return Point.makeHeadingInRange(heading);
    }

    @Override
    public double getDistanceTo(Positionable other) {
        if (other == null) throw new IllegalArgumentException("Can not calculate distance to null Entity");
        return getPos().getDistanceTo(other.getPos());
    }

    @Override
    public double getDistanceTo(Point pos) {
        if (pos == null) throw new IllegalArgumentException("Can not calculate distance to null Point");
        return getPos().getDistanceTo(pos);
    }

    @Override
    public double getHeadingTo(Positionable other) {
        if (other == null) throw new IllegalArgumentException("Can not calculate heading to null Entity");
        return getPos().getHeadingTo(other.getPos());
    }

    @Override
    public double getHeadingTo(Point pos) {
        return getPos().getHeadingTo(pos);
    }

    @Override
    public void setHeading(double heading) {
        this.heading = makeHeadingInRange(heading);
    }

    @Override
    public double getHeading() {
        return this.heading;
    }

    public void writePosition(ByteBuffer dst) {
        dst.putShort( (short) getX() );
        dst.putShort( (short) getY() );
    }

    public void readPosition(ByteBuffer src) {
        setPos(src.getShort(), src.getShort());
    }

    public void writeHeading(ByteBuffer dst) {
        dst.putShort((short) getHeading());
    }

    public void readHeading(ByteBuffer src) {
        setHeading( src.getShort() );
    }
}
