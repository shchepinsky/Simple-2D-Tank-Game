//package game.world;
//
//import org.junit.Test;
//
//import static game.world.entities.PositionableEntity.getXInFront;
//import static game.world.entities.Moveable.getYInFront;
//
//import static org.junit.Assert.*;
//
//public class EntityTest {
//    int x;
//    int y;
//    int col;
//    int row;
//
//    @Test
//    public void testGetNextCell() {
//        x = getXInFront(16, 0, BoardCell.CELL_SIZE);
//        y = getYInFront(16, 0, BoardCell.CELL_SIZE);
//        col = BoardCell.xToCol(x);
//        row = BoardCell.yToRow(y);
//        assertEquals(0, col);
//        assertEquals(-1, row);
//
//        x = getXInFront(16, 90, BoardCell.CELL_SIZE);
//        y = getYInFront(16, 90, BoardCell.CELL_SIZE);
//        col = BoardCell.xToCol(x);
//        row = BoardCell.yToRow(y);
//        assertEquals(+1, col);
//        assertEquals(0, row);
//
//        x = getXInFront(16, 180, BoardCell.CELL_SIZE);
//        y = getYInFront(16, 180, BoardCell.CELL_SIZE);
//        col = BoardCell.xToCol(x);
//        row = BoardCell.yToRow(y);
//        assertEquals(0, col);
//        assertEquals(+1, row);
//
//        x = getXInFront(16, 270, BoardCell.CELL_SIZE);
//        y = getYInFront(16, 270, BoardCell.CELL_SIZE);
//        col = BoardCell.xToCol(x);
//        row = BoardCell.yToRow(y);
//        assertEquals(-1, col);
//        assertEquals(0, row);
//
//    }
//}