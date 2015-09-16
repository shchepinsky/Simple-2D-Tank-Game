package game.world.entities;

import game.world.BoardCell;

// can be placed on board at specific position
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
