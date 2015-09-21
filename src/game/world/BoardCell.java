package game.world;

import game.Resources;
import game.world.entities.Entity;
import game.world.entities.Point;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A single board cell.
 */
public class BoardCell {
    public static final int CELL_SIZE = 32;

    public final TileGround ground;
    public final TileOverlay overlay;
    public final int col;
    public final int row;

    private final LinkedHashSet<Entity> entities = new LinkedHashSet<>();

    public BoardCell(String groundID, String overlayID, int row, int col) {
        this.row = row;
        this.col = col;

        this.ground = Resources.getGroundType(groundID);
        this.overlay = Resources.getOverlayType(overlayID);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ ((col * 33) * row);
    }

    @Override
    public String toString() {
        return String.format("{row=%s, col=%s} entities: %s", row, col, entities.size());
    }

    /**
     * Returns cell obstacle bounds with absolute coordinates depending on
     * ground layer, overlay and bounds specified.
     * @return obstacle bounds or null if cell has no obstacle.
     */
    public Bounds getObstacleBounds() {
        if ( ground.isMoveObstacle() ) {
            // return full cell as obstacle
            return Bounds.fromBoardCell(row, col);
        }

        if ( overlay.isMoveObstacle() || overlay.isShootObstacle() ) {
            // otherwise return overlay obstacle parsed
            String[] parts = overlay.bounds.split("\\s*,\\s*");

            int x1 = col * CELL_SIZE + Integer.parseInt(parts[0]);
            int y1 = row * CELL_SIZE + Integer.parseInt(parts[1]);
            int x2 = col * CELL_SIZE + Integer.parseInt(parts[2]);
            int y2 = row * CELL_SIZE + Integer.parseInt(parts[3]);

            return Bounds.fromCornerPoints(x1, y1, x2, y2);
        }

        return null;
    }

    public static int xToCol(double x) {
        return x<0 ? (int) (x-CELL_SIZE) / CELL_SIZE : (int) x / CELL_SIZE;
    }

    public static int yToRow(double y) {
        return y<0 ? (int) (y-CELL_SIZE) / CELL_SIZE : (int) y / CELL_SIZE;
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }

    public void insertEntity(Entity entity) {
        entities.add(entity);
    }

    public Set<Entity> getEntitiesUnmodifiable() {
        return Collections.unmodifiableSet(entities);
    }

    private static int colToX(int col) {
        return col * CELL_SIZE;
    }

    private static int rowToY(int row) {
        return row * CELL_SIZE;
    }

    private int colToX() {
        return BoardCell.colToX(col);
    }

    private int rowToY() {
        return BoardCell.rowToY(row);
    }

    public int getCenterX() {
        return colToX() + CELL_SIZE / 2;
    }

    public int getCenterY() {
        return rowToY() + CELL_SIZE / 2;
    }

    public Point getCenter() {
        return new Point( getCenterX(), getCenterY());
    }

    public boolean isShootObstacle() {
        return ground.isShootObstacle() || overlay.isShootObstacle();
    }
}

