package game.world.entities;

import game.world.Board;
import game.world.BoardCell;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class PathFinderTest {

    @Test
    public void testFind() throws Exception {

        final int ITERATIONS = 5000;

        Random r = new Random();
        Board board = Board.fromResource("/map-for-path-test.txt");
//        Board board = Board.fromResource("/map.txt");

        PathFinderHashSet       pathFinderHashSet       = new PathFinderHashSet(board);

        List<Board.SpawnPoint> spawnPoints = board.getSpawnPointsUnmodifiable();
        int[] startPointIndex = new int[ITERATIONS];
        int[] endPointIndex = new int[ITERATIONS];

        for (int i = 0; i < ITERATIONS; i++) {
            startPointIndex[i] = r.nextInt(spawnPoints.size());
            endPointIndex[i] = r.nextInt(spawnPoints.size());

            while (endPointIndex[i] == startPointIndex[i]) {
                endPointIndex[i] = r.nextInt(spawnPoints.size());
            }
        }


        BoardCell start;
        BoardCell destination;

        Tank tank = new Tank(UUID.randomUUID(), board);

        PathList<BoardCell> path = new PathList<>();// Collections.emptyList();

        long time1;
        long time2;
        double averagePerIteration;


//        time1 = System.nanoTime();
//        for (int i=0; i<ITERATIONS; i++) {
//            start = board.getCell(spawnPoints.get(startPointIndex[i]).row,spawnPoints.get(startPointIndex[i]).col);
//            destination = board.getCell(spawnPoints.get(startPointIndex[i]).row,spawnPoints.get(startPointIndex[i]).col);
//            tank.setPos(start.getCenterX(), start.getCenterY());
//            path = pathFinderLinkedList.find(tank, destination);
//        }
//        time2 = System.nanoTime();
//        System.out.println(String.format("List: %f", (time2-time1) / 1_000_000.0));


//        while (true) {
//            time1 = System.nanoTime();
//            for (int i = 0; i < ITERATIONS; i++) {
//                start = board.getCell(spawnPoints.get(startPointIndex[i]).row, spawnPoints.get(startPointIndex[i]).col);
//                destination = board.getCell(spawnPoints.get(endPointIndex[i]).row, spawnPoints.get(endPointIndex[i]).col);
//                tank.setPos(start.getCenterX(), start.getCenterY());
//                path = pathFinderArrayList.find(tank, destination);
//            }
//            time2 = System.nanoTime();
//            averagePerIteration = ((time2 - time1) / 1_000_000.0) / ITERATIONS;
//            System.out.println(String.format("ArrayList PathFinder ms per iteration: %.4f", averagePerIteration));
//        }

//        while (true) {
//            time1 = System.nanoTime();
//            for (int i = 0; i < ITERATIONS; i++) {
//                start = board.getCell(spawnPoints.get(startPointIndex[i]).row, spawnPoints.get(startPointIndex[i]).col);
//                destination = board.getCell(spawnPoints.get(endPointIndex[i]).row, spawnPoints.get(endPointIndex[i]).col);
//                tank.setPos(start.getCenterX(), start.getCenterY());
//                path = pathFinderHashSet.find(tank, destination);
//            }
//            time2 = System.nanoTime();
//            averagePerIteration = ((time2 - time1) / 1_000_000.0) / ITERATIONS;
//            System.out.println(String.format("HashSet PathFinder ms per iteration: %.4f", averagePerIteration));
//
//        }

        start = board.getCell(spawnPoints.get(0).row, spawnPoints.get(0).col);
        destination = board.getCell(spawnPoints.get(1).row, spawnPoints.get(1).col);
        tank.setPos(start.getCenterX(), start.getCenterY());
        path = pathFinderHashSet.find(tank, destination);
        System.out.println(start);
//        for (BoardCell step : path) {
//            System.out.println(step);
//        }

    }
}