package game.world.entities;

import game.world.Board;
import game.world.BoardCell;
import game.world.Bounds;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

import static game.util.Debug.log;

abstract class CollidableMovableEntity extends MoveableEntity implements Collidable {

    CollidableMovableEntity(UUID ownerUniqueID, Board board) {
        super(ownerUniqueID, board);
    }

    CollidableMovableEntity(UUID ownerUniqueID, Board board, ByteBuffer buf) {
        super(ownerUniqueID, board, buf);
    }

    @Override
    public void update() {
        // super.update();
        // overriding super.update() free movement algorithms with collision-aware one
        setHeading(getNextHeading());

        double newSpeed = getNextSpeed();
//        if (newSpeed == 0.0) return;
        setMoveSpeed(newSpeed);



        // get attempted new position
        Point newPos = getNextPosition();

        // create bounds at attempted position
        Bounds thisNewBounds = getBounds().centered(newPos.x, newPos.y);

        boolean collisionDetected = false;

        // get cells around attempted position
        List<BoardCell> cellsAround = getBoard().getCellsAround(newPos, 1);

        // check if any cells block attempted position
        for (BoardCell cell : cellsAround) {

            if (cellHasObstacle(cell)) {                    // subclass decides if cell is obstacle or not

                Bounds obstacleBounds = cell.getObstacleBounds();

                if (obstacleBounds != null && thisNewBounds.collidesWith(obstacleBounds)) {
                    collisionDetected = true;               // set collision flag

                    collideWith(null);
                    //break;                                  // break out from search
                }
            }
        }

        // check collision against other entities in the search area
        List<Collidable> obstacles = getPotentialObstaclesFromCells(cellsAround);

        for (Collidable obstacle : obstacles) {

            if (!canCollideWith(obstacle)) {
                continue;
            }

            if (thisNewBounds.overlap(obstacle.getBounds())) {
                collideWith(obstacle);                      // process collision for both this
                obstacle.collideWith(this);                 // and other entity
                collisionDetected = true;
                setMoveSpeed(0);
            }
        }

        if (boundsOutsideBoardAt(newPos.x, newPos.y) && !canCrossBoardBounds()) {
            collisionDetected = true;                       // can not move out of board bounds
        }


        if (collisionDetected) {
            setMoveSpeed(0);
        }else {


            setPos(newPos);
        }
    }

    boolean boundsOutsideBoardAt(double x, double y) {
        Bounds newBounds = getBounds().centered(x,y);

        double x1 = Math.min(newBounds.getX(), newBounds.getX() + newBounds.getWidth());
        double x2 = Math.max(newBounds.getX(), newBounds.getX() + newBounds.getWidth());

        double y1 = Math.min(newBounds.getY(), newBounds.getY() + newBounds.getHeight());
        double y2 = Math.max(newBounds.getY(), newBounds.getY() + newBounds.getHeight());

        return !(getBoard().coordinatesInBounds(x1,y1) && getBoard().coordinatesInBounds(x2,y2));
    }

    public Bounds getBounds() {
        return getTypeInfo().bounds.centered(getX(), getY());
    }

    @Override
    public boolean canCollideWith(Entity other) {
        if (!isMaster()) return false;                      // collisions are calculated only on server-side

        // by default only non-self and Collidable can be obstacles
        if (other == this) return false;                    // do not collide with self
        if (getParent() == other) return false;             // do not collide with parent
        if (other.getParent() == this) return false;        // do not collide with child

        // collide with all other objects
        return true;
    }
}
