package game.world.entities;

import game.world.BoardCell;

/**
 * An entity that can be placed on board at concrete position.
 */
public interface Positionable extends Entity {
    BoardCell getCell();

    double getX();
    double getY();
    void setPos(Point newPos);
    void setPos(double x, double y);
    Point getPos();
    void setHeading(double heading);
    double getHeading();

    double getDistanceTo(Positionable other);
    double getDistanceTo(Point pos);
    double getHeadingTo(Positionable other);
    double getHeadingTo(Point pos);
}
