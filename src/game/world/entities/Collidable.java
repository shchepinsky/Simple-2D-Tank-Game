package game.world.entities;

import game.world.BoardCell;
import game.world.Bounds;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * An entity that can participate in collisions with other.
 */
public interface Collidable extends Positionable {

    Bounds getBounds();

    boolean canCollideWith(Entity other);

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

    void collideWith(Collidable other);
}

