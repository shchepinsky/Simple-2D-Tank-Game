package game.world.entities;

import game.world.BoardCell;
import game.world.Bounds;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


// can be collided with
public interface Collidable extends Positionable {

    Bounds getBounds();

    boolean canCollideWith(Entity other);
//    default boolean canCollideWith(Entity other) {
//        // by default only non-self and Collidable can be obstacles
//        if (other == this) return false;                    // do not collide with self
//        if (getParent() == other) return false;             // do not collide with parent
//        if (other.getParent() == this) return false;        // do not collide with childs
//
//        // collide with all other objects
//        return true;
//    }

    default List<Collidable> getPotentialObstaclesFromCells(List<BoardCell> cells) {
        final List<Collidable> obstacles = new ArrayList<>();

        for (BoardCell cell : cells) {

            obstacles.addAll(cell
                    .getEntitiesUnmodifiable()
                    .stream().filter(entity -> entity instanceof Collidable && canCollideWith(entity))
                    .map(entity -> (Collidable) entity)
                    .collect(Collectors.toList())
            );
        }

        return obstacles;
    }

    default List<Collidable> getPotentialObstaclesAround(Point pos) {
        final int SEARCH_CELLS = 1;                         // offset to expand search area

        final List<BoardCell> cells = getBoard().getCellsAround(pos, SEARCH_CELLS);

        return getPotentialObstaclesFromCells(cells);
    }

    void collideWith(Collidable other);
}

