package game.world.entities;

import game.world.Board;
import game.world.BoardCell;
import game.world.SpawnPoint;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class PathFinderTest {

    @Test
    public void testFind() throws Exception {

        final int ITERATIONS = 5000;

        Random r = new Random();
        Board board = Board.fromResource("/map-for-path-test.txt");
        // Board board = Board.fromResource("/map.txt");

        PathFinder pathFinder = new PathFinder(board);

        List<SpawnPoint> spawnPoints = board.getSpawnPointsUnmodifiable();
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

        PathList<BoardCell> path = new PathList<>();

        long time1;
        long time2;
        double averagePerIteration;

        start = board.getCell(spawnPoints.get(0).row, spawnPoints.get(0).col);
        destination = board.getCell(spawnPoints.get(1).row, spawnPoints.get(1).col);
        tank.setPos(start.getCenterX(), start.getCenterY());
        path = pathFinder.find(tank, destination);
        System.out.println(start);

    }
}