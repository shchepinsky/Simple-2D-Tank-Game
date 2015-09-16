package game.world;

import java.util.ArrayList;
import java.util.List;

public class Bounds {
    private static class OverlapResult {
        final boolean xAxisCollision;
        final boolean yAxisCollision;

        public OverlapResult(boolean xAxisCollision, boolean yAxisCollision) {
            this.xAxisCollision = xAxisCollision;
            this.yAxisCollision = yAxisCollision;
        }

        public boolean collisionOccurred() { return xAxisCollision && yAxisCollision; }
    }

    public final double x1;
    public final double y1;
    public final double x2;
    public final double y2;

    private Bounds(double x1, double y1, double x2, double y2) {
        this.x1 = Math.min(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.x2 = Math.max(x1, x2);
        this.y2 = Math.max(y1, y2);
    }

    /**
     * This creates a list of Bounds from given rectangle corners.
     *
     * @param x1 first coordinate x
     * @param y1 first coordinate y
     * @param x2 second coordinate x
     * @param y2 second coordinate y
     * @return list of Bounds surrounding provided rectangle.
     */
    public static List<Bounds> fromOutsideOfRectangle(double x1, double y1, double x2, double y2) {
        List<Bounds> listOfBounds = new ArrayList<>(4);

        final int THICKNESS = BoardCell.CELL_SIZE;

        // make surrounding rectangles: left, top, right, bottom
        listOfBounds.add(Bounds.fromCornerPoints(x1 - THICKNESS, y1, x1, y2));
        listOfBounds.add(Bounds.fromCornerPoints(x1, y1 - THICKNESS, x2, y1));
        listOfBounds.add(Bounds.fromCornerPoints(x2, y1, x2 + THICKNESS, y2));
        listOfBounds.add(Bounds.fromCornerPoints(x1, y2, x2, y2 + THICKNESS));

        return listOfBounds;
    }

    /**
     * Utility method to create rectangle bounds from two points.
     *
     * @param x1 x coordinate of first point.
     * @param y1 y coordinate of first point.
     * @param x2 x coordinate of second point.
     * @param y2 y coordinate of second point
     * @return new instance of bounds.
     */
    public static Bounds fromCornerPoints(double x1, double y1, double x2, double y2) {
        return new Bounds(x1, y1, x2, y2);
    }

    /**
     * Utility method to create rectangle bounds from center point, width and height.
     *
     * @param cx     center point x coordinate.
     * @param cy     center point y coordinate.
     * @param width  width of bounds.
     * @param height height of bounds.
     * @return new instance of bounds.
     */
    public static Bounds fromCenterAndSize(double cx, double cy, double width, double height) {
        return new Bounds(cx - width / 2, cy - height / 2, cx + width / 2, cy + height / 2);
    }

    /**
     * Utility method to create bounds of particular BoardCell.
     *
     * @param row row of cell.
     * @param col column of cell.
     * @return new instance of bounds.
     */
    public static Bounds fromBoardCell(int row, int col) {
        return new Bounds(
                col * BoardCell.CELL_SIZE,
                row * BoardCell.CELL_SIZE,
                col * BoardCell.CELL_SIZE + BoardCell.CELL_SIZE,
                row * BoardCell.CELL_SIZE + BoardCell.CELL_SIZE
        );
    }

    public static Bounds fromText(String boundsLine) {
        String[] parts = boundsLine.split("\\s*,\\s*");
        int x1 = Integer.parseInt(parts[0]);
        int y1 = Integer.parseInt(parts[1]);
        int x2 = Integer.parseInt(parts[2]);
        int y2 = Integer.parseInt(parts[3]);

        return new Bounds(x1, y1, x2, y2);
    }

    public static Bounds fromBoundsCentered(Bounds bounds, double center_x, double center_y) {
        return fromCenterAndSize(center_x, center_y, bounds.getWidth(), bounds.getHeight());
    }

    /**
     * Check collision against list of other instances
     *
     * @param obstacles list of other instances to check collision against.
     * @return true if collision is detected or false if all instance in list do not collide.
     */
    public Bounds collidesWith(List<Bounds> obstacles) {

        for (Bounds obstacle : obstacles) {
            if (collidesWith(obstacle)) {
                return obstacle;
            }
        }

        return null;
    }

    /**
     * Checks collision against other instance.
     *
     * @param other instance to check collision against.
     * @return true if collision detected or false otherwise.
     */
    public boolean collidesWith(Bounds other) {
        if ((x1 == other.x1 && y1 == other.y1) || (x1 == other.x1 && y1 == other.y1)) return true;

        boolean xIntersect = (x1 < other.x1 && x2 > other.x1) || (x1 > other.x1 && other.x2 > x1);
        boolean yIntersect = (y1 < other.y1 && y2 > other.y1) || (y1 > other.y1 && other.y2 > y1);

        return xIntersect && yIntersect;
    }

    public double getHeight() {
        return Math.abs(y2 - y1);
    }

    public double getWidth() {
        return Math.abs(x2 - x1);
    }

    public Bounds centered(double center_x, double center_y) {
        return fromBoundsCentered(this, center_x, center_y);
    }

    public double getX() {
        return Math.min(x1, x2);
    }

    public double getY() {
        return Math.min(y1, y2);
    }

    public boolean overlap(Bounds other) {
        return overlap(this, other);
    }

    public boolean overlap(List<Bounds> others) {
        return overlap(this, others);
    }

    public static boolean overlap(Bounds bounds, Bounds other) {

        double x1 = bounds.getX();
        double y1 = bounds.getY();
        double x2 = x1 + bounds.getWidth();
        double y2 = y1 + bounds.getHeight();

        double ox1 = other.getX();
        double oy1 = other.getY();
        double ox2 = ox1 + other.getWidth();
        double oy2 = oy1 + other.getHeight();

        // check if our bounds intersect with each other
        boolean xIntersect = (x1 < ox1 & x2 >= ox1) || (ox1 < x1 & ox2 > x1);
        boolean yIntersect = (y1 < oy1 & y2 >= oy1) || (oy1 < y1 & oy2 > y1);

        return xIntersect && yIntersect;
    }

    public static boolean overlap(Bounds bounds, List<Bounds> others) {

        for (Bounds other : others) {
            if (bounds.overlap(other)) return true;
        }

        return false;
    }
}
