package game.world.entities;

import game.world.BoardCell;

// can be moved across board
public interface Moveable extends Positionable {
    void setMoveSpeed(double moveSpeed);
    void setOrderedSpeed(double orderedSpeed);

    void setOrderedHeading(double orderedHeading);

    double getMoveSpeed();
    double getOrderedSpeed();
    double getOrderedHeading();
    boolean cellHasObstacle(BoardCell cell);
    }
