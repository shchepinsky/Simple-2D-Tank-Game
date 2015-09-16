package game.world.entities;


import game.world.Board;
import game.world.BoardCell;

import java.util.*;

import static game.util.Debug.log;

public class PathFinderHashSet {
    static final boolean    ALLOW_DIAGONAL_MOVEMENT = false;

    private static class PathCell {

        static final int        STRAIGHT_MOVE_COST = 10;
        static final int        DIAGONAL_MOVE_COST = 14;

        PathCell parent;

        private int G;
        private int H;
        private final BoardCell boardCell;

        public int getF() {
            return G+H;
        }
        public int getG() {
            return G;
        }
        public int getH() {
            return H;
        }

        private int calcG() {
            G=0;

            PathCell current = this;
            while (current.parent != null) {

                int dx = Math.abs(current.getCol() - current.parent.getCol());
                int dy = Math.abs(current.getRow() - current.parent.getRow());

                if (dx == 1 && dy == 1 ) {
                    G = G + DIAGONAL_MOVE_COST;
                } else {
                    G = G + STRAIGHT_MOVE_COST;
                }

                current = current.parent;
            }

            return G;
        }
        private int calcH(PathCell destination) {
            H = 0;

            int dx = Math.abs(getCol() - destination.getCol());
            int dy = Math.abs(getRow() - destination.getRow());

            H = (dx + dy) * STRAIGHT_MOVE_COST;
            return H;
        }

        int getCol() {
            return boardCell.col;
        }

        int getRow() {
            return boardCell.row;
        }

        BoardCell getBoardCell() {
            return boardCell;
        }

        PathCell(BoardCell cell) {
            this(cell, null);
        }

        PathCell(BoardCell cell, PathCell parent) {
            this.boardCell = cell;
            this.parent = parent;
        }

        @Override
        public int hashCode() {
            return super.hashCode() ^ boardCell.hashCode();
        }

        @Override
        public String toString() {
            return String.format("{row=%s, col=%s} F=%s, G=%s, H=%s", getRow(), getCol(), getF(), getG(), getH());
        }

        public void reset() {
            parent = null;
            G=0;
            H=0;
        }
    }

    final HashSet<PathCell> open     = new HashSet<>();
    final HashSet<PathCell> closed   = new HashSet<>();

    private final PathCell[][] cells;

    private static PathCell getPathCellMinimumF(Set<PathCell> open, PathCell destination) {
        PathCell result = null;
        int minCost = Integer.MAX_VALUE;

        for (PathCell currentPathCell : open) {

            currentPathCell.calcG();
            currentPathCell.calcH(destination);

            int cost = currentPathCell.getF();

            if (cost < minCost) {
                minCost = cost;
                result = currentPathCell;
            }
        }

        return result;
    }

    private PathCell[][] createOfSize(Board board, int rows, int cols) {
        PathCell[][] cells = new PathCell[rows][cols];

       log(String.format("Setting up %s with %d rows, %d cols", getClass().getSimpleName(), rows, cols));

        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                cells[row][col] = new PathCell(board.getCell(row, col), null);
            }
        }

        return cells;
    }

    public PathFinderHashSet(Board board) {
        cells = createOfSize(board, board.getRowCount(), board.getColCount());
    }

    private void reset() {
        open.clear();
        closed.clear();

        for (PathCell[] rows : cells) {
            for (PathCell cell : rows) {
                cell.reset();
            }
        }
    }

    /**
     * A-star algorithm to find path on board.
     * @param entity entity to find path for, used to test if cells are obstacles or not.
     * @param destinationCell destination on board.
     * @return list of board cells with path or empty list if no path was found.
     */
    public PathList<BoardCell> find(Moveable entity, BoardCell destinationCell) {
        reset();

        boolean found;

        BoardCell startCell = entity.getCell();

        PathCell start = cells[startCell.row][startCell.col];
        PathCell destination = cells[destinationCell.row][destinationCell.col];

        open.add(start);

        while (true) {
            PathCell current = getPathCellMinimumF(open, destination);

            closed.add(current);
            open.remove(current);

            PathCell[] adjacent = getAdjacentCells(current);

            for (PathCell cell : adjacent) {
                if (entity.cellHasObstacle(cell.getBoardCell())) {
                    continue;
                }

                if (closed.contains(cell)) {
                    continue;
                }

                if (!open.contains(cell)) {

                    if (!ALLOW_DIAGONAL_MOVEMENT) {
                        int dx = Math.abs(current.getCol() - cell.getCol());
                        int dy = Math.abs(current.getRow() - cell.getRow());

                        if (dx == 1 && dy == 1) {
                            continue;
                        }
                    }


                    open.add(cell);
                    cell.parent = current;
                    cell.calcG();
                    cell.calcH(destination);

                } else {

                    if (cell.getG() < current.getG()) {
                        cell.parent = current;
                        cell.calcG();
                    }
                }
            }

            found = open.contains(destination);
            if (found || open.isEmpty()) break;
        }

        PathList<BoardCell> path = new PathList<>();

        if (found) while (destination != start) {
            path.add(0, destination.boardCell);
            destination = destination.parent;
        }

        return path;
    }

    private PathCell[] getAdjacentCells(PathCell current) {
        int rowStart    = Math.max(current.getRow() - 1, 0);
        int rowEnd = Math.min(current.getRow() + 1, cells.length - 1);
        int colStart    = Math.max(current.getCol() - 1, 0);
        int colEnd      = Math.min(current.getCol() + 1, cells[0].length - 1);

        int size = ((colEnd - colStart)+1) * ((rowEnd - rowStart)+1)-1; // -1 excluding current

        PathCell[] adjacent = new PathCell[size];

        for (int col = colStart; col <= colEnd; col++) {
            for (int row = rowStart; row <= rowEnd; row++) {
                if (col == current.getCol() && row == current.getRow()) continue;

                adjacent[size-1] = cells[row][col];
                size--;
            }
        }

        return adjacent;
    }
}