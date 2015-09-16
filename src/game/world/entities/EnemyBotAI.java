package game.world.entities;

import game.util.Timeout;
import game.world.Board;
import game.world.BoardCell;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static game.util.Debug.log;
import static java.lang.Math.*;

public class EnemyBotAI {
    private final int MAX_TARGET_DECISION_TIMEOUT = 1000;
    private final int MAX_PURSUE_DECISION_TIMEOUT = 1000;
    private final int MAX_ROAMING_DECISION_TIMEOUT = 5000;

    private final Timeout targetDecisionTimeout = new Timeout(MAX_TARGET_DECISION_TIMEOUT);
    private final Timeout pursueDecisionTimeout = new Timeout(MAX_PURSUE_DECISION_TIMEOUT);
    private final Timeout roamingDecisionTimeout = new Timeout(MAX_ROAMING_DECISION_TIMEOUT);

    private final double EPSILON = 0.3;

    private final Board board;

    private final Tank controlled;

    private short targetKey;

    private Random random = new Random();
    private PathList<BoardCell> path = new PathList<>();

    public EnemyBotAI(Board board, Tank controlled) {
        this.board = board;
        this.controlled = controlled;
        targetKey = EntityBase.INVALID_UNIQUE_ID;
    }

    private boolean hasTarget() {
        return targetKey != EntityBase.INVALID_UNIQUE_ID && board.getEntity(targetKey) != null;
    }

    private Tank getTarget() {
        return (Tank) board.getEntity(targetKey);
    }

    public void update() {
        // 1. search for target in range
        if (targetDecisionTimeout.occurred()) {
            makeTargetDecision();                           // recalculate target decision
            targetDecisionTimeout.reset();                  // reset timeout
        }

        if (hasTarget() && pursueDecisionTimeout.occurred()) {
            // this is important: we have to recalculate path only when aligned in cell otherwise tank may
            // take wrong decision and collide with adjacent obstacles
            if (alignedInCell(controlled.getCell()) || path.isEmpty()) {
                makePathToTarget();
            }

            pursueDecisionTimeout.reset();
        }

        // 2 if no target found - go roaming
        if (!hasTarget() && alignedInCell(controlled.getCell())) {
            if (path.isEmpty() || roamingDecisionTimeout.occurred()) {
                makeRandomPathDecision();
                roamingDecisionTimeout.reset();
            }
        }


        makeFireDecision();

        if (makeTurnDecision() && !path.isEmpty()) {
            followPath();
        }

    }

    private void makeRandomPathDecision() {
        BoardCell cell;

        do {
            // get random cell as roaming destination
            int row = random.nextInt(board.getRowCount());
            int col = random.nextInt(board.getColCount());

            cell = board.getCell(row, col);

            path = board.getPathFinder().find(controlled, cell);
            // is destination is unreachable - try again
        } while (path.isEmpty());

    }

    public boolean alignedInCell(BoardCell cell) {
        double x = controlled.getX();
        double y = controlled.getY();
        double cx = cell.getCenterX();
        double cy = cell.getCenterY();

        return (abs(x - cx) < EPSILON && abs(y - cy) < EPSILON);
    }

    private void makePathToTarget() {

        Entity entity = board.getEntity(targetKey);
        if (entity == null || !(entity instanceof Tank)) return;

        Tank target = (Tank) entity;
        BoardCell targetCell = target.getCell();
        if (targetCell == null) return;

        path = board.getPathFinder().find(controlled, targetCell);

        if (path.isEmpty()) return;

        if (path.size() < 3) {                             // destination too close
            path.clear();                                   // discard path
            return;
        }

        if (path.size() > 1) {                              // remove last step - we don't want to take place of target
            path.remove(path.size() - 1);
        }

        path.add(0, controlled.getCell());                  // add our current as first board cell to align with

    }

    private boolean makeTurnDecision() {
        if (path.isEmpty()) {

            if (hasTarget()) {                              // turn tu target if any
                double heading = controlled.getHeadingTo(getTarget());
                controlled.setOrderedHeading(round(heading / 90) * 90);
            }

            return false;
        }

        BoardCell curr = controlled.getCell();

        if (curr == path.get(0)) {

            double x = controlled.getX();
            double y = controlled.getY();
            double cx = curr.getCenterX();
            double cy = curr.getCenterY();

            if (abs(x - cx) < EPSILON && abs(y - cy) < EPSILON) {
                controlled.setPos(cx, cy);

                path.remove(0);
                return false;
            }
        }

        Point newPoint = path.get(0).getCenter();
        double newHeading = controlled.getHeadingTo(newPoint);
        controlled.setOrderedHeading(newHeading);

        return true;
    }

    private void followPath() {

        if (abs(controlled.getHeading() - controlled.getOrderedHeading()) > EPSILON) {
            return;
        }

        BoardCell cell = controlled.getCell();
        BoardCell prev = cell;
        Point start = new Point(controlled.getX(), controlled.getY());
        Point stop = new Point(cell.getCenterX(), cell.getCenterY());

        // find braking point, if any. If none found - braking at the end of route
        for (int n = 0; n < path.size(); n++) {
            cell = path.get(n);
            stop = new Point(cell.getCenterX(), cell.getCenterY());

            double moveDirection = controlled.getHeadingTo(stop);
            if (abs(moveDirection - controlled.getHeading()) > EPSILON) {
                stop = new Point(prev.getCenterX(), prev.getCenterY());
                break;
            }
            prev = cell;
        }

        // entity type constants shortcut
        EntityTypeInfo info = controlled.getTypeInfo();

        double distance = start.getDistanceTo(stop);        // distance from current pos to braking point
        double deceleration = info.maxDeceleration;         // maximum deceleration used for braking
        double acceleration = info.maxAcceleration;         // maximum acceleration for convenience
        double nowSpeed = controlled.getMoveSpeed();
        double newSpeed = nowSpeed + acceleration;          // acceleration needed if speed == 0
        double brakeTime = newSpeed / deceleration;         // brake time from current speed with maximum deceleration

        // finally, brake distance with current speed
        double brakeDistance = controlled.getMoveSpeed() * brakeTime;

        // use new speed if braking distance with new speed is less than distance to turning point, else start brake
        controlled.setOrderedSpeed(brakeDistance < distance ? newSpeed : 0);
    }

    private void stop() {
        controlled.setOrderedSpeed(0);
        if (path != null) {
            path.clear();
        }
    }

    private void makeFireDecision() {
        // check if we are facing one of four sides
        if (controlled.getHeading() % 90 > EPSILON) {
            return;
        }

        // check for targets in front of us. To do so we need to collect all cells in front first.
        // get all cells along line of fire
        Set<BoardCell> lineOfFire = new HashSet<>();

        double heading = controlled.getHeading();
        int distance = 1000; // TODO: this should be obtained from controlled.getCannon().fireRange() and bullet type info
        Point muzzlePoint = controlled.getCannon().muzzlePoint();

        for (int n = 0; n < distance; n = n + BoardCell.CELL_SIZE * 2) {
            Point p = muzzlePoint.at(heading, n);
            if (board.coordinatesInBounds(p) && board.getCellAt(p).isShootObstacle()) continue;

            lineOfFire.addAll(board.getCellsAround(p, 1));
        }

        // process potential targets in line of fire cells
        for (BoardCell cell : lineOfFire) {

            for (Entity entity : cell.getEntitiesUnmodifiable()) {
                if (entity == controlled) continue;
                if (entity instanceof Tank) {
                    if (isAimedAt((Tank) entity)) {
                        controlled.fireOnce();
                        return;
                    }
                }
            }
        }

    }

    private boolean isAimedAt(Tank target) {
        Tank shooter = controlled;
        // check if we aligned with target
        double targetAimAngle = abs(MoveableEntity.getHeadingDelta(shooter.getHeading(), shooter.getHeadingTo(target)));
        double targetDistance = shooter.getDistanceTo(target);
        double aimError = targetDistance * Math.sin(toRadians(targetAimAngle));
        double targetSize = (target.getBounds().getWidth() + target.getBounds().getHeight()) / 2;
        double maxAimError = targetSize / 1.5;

        return (abs(aimError) <= maxAimError);
    }

    private void makeTargetDecision() {
        targetKey = EntityBase.INVALID_UNIQUE_ID;

        final int TARGET_SEARCH_CELLS = 8; // search in 8-cell radius

        double closestDistance = Integer.MAX_VALUE;
        double targetDistance;

        Tank closestTarget = null;

        List<Entity> potentialTargetsList = board.getEntitiesAround(controlled.getPos(), TARGET_SEARCH_CELLS);
        for (Entity entity : potentialTargetsList) {
            if (!(entity instanceof Tank) || entity == controlled) continue;

            Tank target = (Tank) entity;

            targetDistance = controlled.getDistanceTo(target);
            if (targetDistance < closestDistance) {
                closestDistance = targetDistance;
                closestTarget = target;
            }
        }

        if (closestTarget == null) {
            log("Target decision: no target found");
        } else if (controlled.getDistanceTo(closestTarget) < BoardCell.CELL_SIZE * TARGET_SEARCH_CELLS) {
            targetKey = closestTarget.getKey();
            log(String.format("Target decision: selected target in %s", closestTarget.getCell()));
        } else {
            log("Target decision: target our of range");
        }

    }

    public synchronized PathList<BoardCell> getPath() {
        return path;
    }
}
