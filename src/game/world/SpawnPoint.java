package game.world;

import game.world.entities.Point;

/**
 * Class that holds information about spawn point on board.
 */
public class SpawnPoint {
    public final int row;
    public final int col;

    public SpawnPoint(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public Point getPos() {
        return new Point(getPosX(), getPosY());
    }

    public int getPosX() {
        return col * BoardCell.CELL_SIZE + BoardCell.CELL_SIZE / 2;
    }

    public int getPosY() {
        return row * BoardCell.CELL_SIZE + BoardCell.CELL_SIZE / 2;
    }
}