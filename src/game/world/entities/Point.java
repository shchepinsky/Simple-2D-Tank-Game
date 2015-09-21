package game.world.entities;

import static java.lang.Math.*;

public class Point {
    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point at(double heading, double distance) {

        double newY = y - cos(toRadians(heading)) * distance;
        double newX = x + sin(toRadians(heading)) * distance;

        return new Point(newX, newY);
    }

    public boolean sameAs(Point other) {
        if (other == this) return true;
        if (other == null) return false;

        final double EPSILON = 1E-10;

        return abs(x - other.x) < EPSILON && abs(y - other.y) < EPSILON;
    }

    public double getDistanceTo(Point other) {
        if (other == null) throw new IllegalArgumentException("Can't calculate distance to null point");

        double dx = abs(other.x - x);
        double dy = abs(other.y - y);

        return sqrt(dx * dx + dy * dy);
    }

    public static double getDistanceTo (Point from, Point to) {
        if (to == null) throw new IllegalArgumentException("Can't calculate distance to null point");
        if (from == null) throw new IllegalArgumentException("Can't calculate distance from null point");

        double dx = abs(to.x - from.x);
        double dy = abs(to.y - from.y);

        return sqrt(dx * dx + dy * dy);
    }

    public double getHeadingTo(Point other) {
        return getHeadingTo(this, other);
    }

    private static double getHeadingTo(Point fromPos, Point toPos) {
        double dx = toPos.x - fromPos.x;
        double dy = toPos.y - fromPos.y;

        double heading = 90 + Math.toDegrees(Math.atan2(dy, dx));
        return makeHeadingInRange(heading);
    }

    @Override
    public String toString() {
        return String.format("{x=%.2f y=%.2f}", x, y);
    }

    /**
     * Utility method to keep heading in range 0f 0..359
     * @param heading input heading
     * @return corrected output heading in range.
     */
    static double makeHeadingInRange(double heading) {
        if (Math.abs(heading) >= 360) heading = heading % 360;
        if (heading < 0) heading = 360 + heading;

        return heading;
    }
}


