package game.world;

import game.Resources;
import game.util.Debug;
import game.world.entities.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static game.util.Debug.log;

/**
 * Game board class, manages entities and map accordingly to external time source.
 * Read-accessed by rendering thread and write-accessed by client thread.
 * Review synchronization required when ClientTask is interacting with this.
 */
public class Board {

    private static final String DEFAULT_GROUND_TILE_ID = "0";
    private static final String DEFAULT_OVERLAY_TILE_ID = "0";

    private final Random random = new Random();
    private final ConcurrentHashMap<Short, Entity> activeEntities = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Short, Entity> inactiveEntities = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Short, Entity> newEntities = new ConcurrentHashMap<>();

    private final List<String> rawLines = new ArrayList<>();
    private final List<SpawnPoint> spawnPoints = new ArrayList<>();
    private final BoardCell[][] cells;
    private PathFinderHashSet pathFinder;

    /**
     * Constructor is hidden in favour of static factories
     */
    private Board(List<String> lines) {
        int total_cols = 0;
        int total_rows = 0;

        // read map parameters first
        int lineNumber = 0;

        for (; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber).trim();
            if (line.isEmpty() || line.startsWith(";") || line.startsWith("//") || line.startsWith("#")) continue;

            // check for map size
            if (line.toLowerCase().startsWith("size")) {
                final String[] size = line.split("\\s*:\\s*");
                total_rows = Integer.parseInt(size[1]);
                total_cols = Integer.parseInt(size[2]);
                continue;
            }

            // check if spawn points exist
            if (line.toLowerCase().startsWith("spawn")) {
                final String[] spawnStrings = line.split("\\s*:\\s*");

                for (int i = 1; i < spawnStrings.length; i++) {
                    String[] parts = spawnStrings[i].split("\\s*,\\s*");
                    final int spawn_row = Integer.parseInt(parts[0]);
                    final int spawn_col = Integer.parseInt(parts[1]);

                    if (spawn_col < 0 || spawn_col >= total_cols || spawn_row < 0 || spawn_row >= total_rows) {
                        log(String.format("Spawn point %d,%d is out of map range!", spawn_col, spawn_row));
                    } else {
                        spawnPoints.add(new SpawnPoint(spawn_row, spawn_col));
                    }
                }

                log("Number of spawn points: " + spawnPoints.size());
                continue;
            }

            if (line.toLowerCase().startsWith("map")) {
                lineNumber++;
                break;
            }
        }

        if (spawnPoints.size() == 0) {
            throw new RuntimeException("No spawn points found in map!");
        }

        cells = createOfSize(total_rows, total_cols);

        int map_row = 0;
        // continue parsing map token
        for (; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber).trim();

            String[] rowCells = line.split("\\s+");

            // Load map cells. First ruler row starts with XY is skipped
            if (line.startsWith("XY") && (rowCells.length == getColCount() + 1)) {
                continue;
            }

            if (rowCells.length != getColCount() + 1) {
                throw new Error("Insufficient columns in row: " + line);
            }

            // parse line, skipping first column
            for (int col = 0; col < getColCount(); col++) {
                String overlayID = rowCells[col + 1].substring(0, 1);
                String tileID = rowCells[col + 1].substring(1, 2);

                // set cell properties read
                setCell(map_row, col, new BoardCell(tileID, overlayID, map_row, col));
            }

            map_row++;
        }

        if (map_row != getRowCount()) {
            throw new RuntimeException("Insufficient rows in map data list");
        }

        rawLines.addAll(lines);
    }

    /**
     * Utility method to initialize 2D array of specified size.
     *
     * @param cols number of columns in array.
     * @param rows number of rows in array.
     * @return array created.
     */
    private static BoardCell[][] createOfSize(int rows, int cols) {
        BoardCell[][] cells = new BoardCell[rows][cols];

        log("Setting up board with %d rows, %d cols", rows, cols);

        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                cells[row][col] = new BoardCell(DEFAULT_GROUND_TILE_ID, DEFAULT_OVERLAY_TILE_ID, row, col);
            }
        }

        return cells;
    }
//    private int time;

    /**
     * Static factory method to create Board instance from list of rawLines.
     *
     * @param lines source rawLines to create board from.
     * @return new instance of Board class.
     */
    public static Board fromList(List<String> lines) {
        return new Board(lines);
    }

    /**
     * Static factory method to create Board from resource name. Uses fromList() internally.
     *
     * @param resourceName name of resource as defined by java.
     * @return new instance of Board class.
     */
    public static Board fromResource(String resourceName) {
        return fromStream(Resources.getResourceStream(resourceName));
    }

    /**
     * Static factory method to create Board from InputStream. After converting stream to list of strings it
     * internally uses fromList() method.
     *
     * @param in stream to load list of strings from.
     * @return new instance if Board class.
     */
    private static Board fromStream(InputStream in) {
        List<String> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        // add all rawLines, except empty and comments
        reader.lines()
                .filter(l -> !l.isEmpty() && !l.matches("^\\s*;.*"))
                .forEachOrdered(list::add);

        return Board.fromList(list);
    }

    public PathFinderHashSet getPathFinder() {
        // lazily initializing single instance of A* pathfinder
        if (pathFinder == null) {
            pathFinder = new PathFinderHashSet(this);
        }

        return pathFinder;
    }

    public List<SpawnPoint> getSpawnPointsUnmodifiable() {
        return Collections.unmodifiableList(spawnPoints);
    }

    /**
     * Utility method to get list of cells nearby location x,y
     *
     * @param cellsAway number of cells to widen search area.
     * @return list of cells in board limits.
     */
    public List<BoardCell> getCellsAround(Point pos, final int cellsAway) {
        final List<BoardCell> cells = new ArrayList<>();

        final int thisRow = BoardCell.yToRow(pos.y);
        final int thisCol = BoardCell.xToCol(pos.x);

        final int rowStart = Math.max(0, thisRow - cellsAway);
        final int rowEnd = Math.min(getRowCount() - 1, thisRow + cellsAway);

        final int colStart = Math.max(0, thisCol - cellsAway);
        final int colEnd = Math.min(getColCount() - 1, thisCol + cellsAway);

        for (int col = colStart; col <= colEnd; col++) {
            for (int row = rowStart; row <= rowEnd; row++) {
                cells.add(getCell(row, col));
            }
        }

        return cells;
    }

    public BoardCell getCell(int row, int col) {
        return cells[row][col];
    }

    private void setCell(int row, int col, BoardCell cell) {
        cells[row][col] = cell;
    }

    public List<String> getRowsUnmodifiable() {
        return Collections.unmodifiableList(rawLines);
    }

    public int getRowCount() {
        return cells.length;
    }

    public int getColCount() {
        return (cells == null) ? 0 : cells[0].length;
    }

    public int getWidthInPixels() {
        return getColCount() * BoardCell.CELL_SIZE;
    }

    public int getHeightInPixels() {
        return getRowCount() * BoardCell.CELL_SIZE;
    }

    /**
     * Adds player object with given unique ID.
     *
     * @param uniqueID universally unique identifier.
     * @return clientKey assigned to player object.
     */
    public Entity spawnPlayer(UUID uniqueID) {
        Point pos = getRandomFreeSpawnPos();

        if (pos == null) return null;

        Tank player = new Tank(uniqueID, this);

        player.setPos(pos);

        // make player orientation towards center
        Point center = new Point(getWidthInPixels() / 2, getHeightInPixels() / 2);
        player.setHeading((int) (player.getHeadingTo(center) / 90) * 90);
        player.setOrderedHeading(player.getHeading());

        registerEntity(player);

        return player;
    }

    public void update() {

        for (Entity e : activeEntities.values()) {

            e.update();

            if (e.isReadyForRemoval()) {
                // remove entity in both client and server modes
                removeActiveEntity(e);

                // if server mode - add to inactive, so it's last state is sent to client
                if (e.isMaster()) {
                    registerInactiveEntity(e);
                }
            }
        }

    }

    public void removeActiveEntity(short key) {
        Entity e = getEntity(key);
        if (e == null) return;
        removeActiveEntity(e);
    }

    public void removeActiveEntity(Entity e) {
        if (e instanceof Positionable) {
            Positionable p = (Positionable) e;

            if (coordinatesInBounds(p.getX(), p.getY())) {  // remove from cell if in range
                int row = BoardCell.yToRow(p.getY());
                int col = BoardCell.xToCol(p.getX());
                getCell(row, col).removeEntity(p);
            }
        }

        activeEntities.remove(e.getKey());                  // remove from global list
    }

    public void registerInactiveEntity(Entity e) {
        inactiveEntities.put(e.getKey(), e);
    }

    public void flushInactiveEntityList() {
        inactiveEntities.forEach(activeEntities::remove);
        inactiveEntities.clear();
    }

    public void flushNewEntityList() {
        newEntities.clear();
    }

    public short registerEntity(Entity e) {
        activeEntities.put(e.getKey(), e);

        if (e.isMaster()) {
            newEntities.put(e.getKey(), e);
        }

        return e.getKey();
    }

    public Enemy spawnEnemy(UUID uniqueID) {
        Point pos = getRandomFreeSpawnPos();

        if (pos == null) return null;                       // no spawn points available

        Enemy bot = new Enemy(uniqueID, this);
        bot.setPos(pos);

        // make enemy orientation towards center
        Point center = new Point(getWidthInPixels() / 2, getHeightInPixels() / 2);
        bot.setHeading((int) (bot.getHeadingTo(center) / 90) * 90);
        bot.setOrderedHeading(bot.getHeading());

        registerEntity(bot);
        return bot;
    }

    private Point getRandomFreeSpawnPos() {
        // number of attempts equals to number of spawn points
        for (int i = 0; i < spawnPoints.size(); i++) {
            // copy to temp list as we will delete inappropriate & occupied spawn points
            List<SpawnPoint> spawnPointsAvailable = new ArrayList<>(spawnPoints);

            // get random from list of available spawn points
            SpawnPoint randomSpawnPoint = spawnPointsAvailable.get(random.nextInt(spawnPointsAvailable.size()));

            // check if selected Spawn Point is not occupied:
            // get cell bounds - i will use it ti check if other entities around block entire cell or it's part
            Bounds spawnPointBounds = Bounds.fromBoardCell(randomSpawnPoint.row, randomSpawnPoint.col);

            // get list entities in cells around
            // get list of collidable in cells around
            // and test if any of them collides with spawnPointCellBounds
            List<Entity> entitiesAround = getEntitiesAround(randomSpawnPoint.getPos(), 1);
            List<Bounds> obstaclesAround = new ArrayList<>();

            for (Entity entity : entitiesAround) {
                if (entity instanceof Collidable) obstaclesAround.add(((Collidable) entity).getBounds());
            }

            if (spawnPointBounds.overlap(obstaclesAround)) {
                spawnPointsAvailable.remove(randomSpawnPoint);
                continue;
            }

            return new Point(randomSpawnPoint.getPosX(), randomSpawnPoint.getPosY());
        }

        return null;
    }

    public List<Entity> getEntitiesAround(Point pos, int cellsAway) {
        List<Entity> entitiesAround = new ArrayList<>();

        List<BoardCell> cellsAround = getCellsAround(pos, cellsAway);

        for (BoardCell cell : cellsAround) {
            for (Entity entity : cell.getEntitiesUnmodifiable()) {
                entitiesAround.add(entity);
            }
        }

        return entitiesAround;
    }

    private List<Entity> getEntitiesAround(Point pos, int cellsAway, Predicate<Entity> condition) {
        List<Entity> entitiesAround = new ArrayList<>();

        List<BoardCell> cellsAround = getCellsAround(pos, cellsAway);

        for (BoardCell cell : cellsAround) {
            for (Entity entity : cell.getEntitiesUnmodifiable()) {
                if (condition.test(entity)) {
                    entitiesAround.add(entity);
                }
            }
        }


        return entitiesAround;
    }

    public Collection<Entity> getActiveEntitiesUnmodifiable() {
        return Collections.unmodifiableCollection(activeEntities.values());
    }

    public Collection<Entity> getInactiveEntitiesUnmodifiable() {
        return Collections.unmodifiableCollection(inactiveEntities.values());
    }

    public BoardCell getCellAt(Point p) {
        return getCell(BoardCell.yToRow(p.y), BoardCell.xToCol(p.x));
    }

    public boolean coordinatesInBounds(Point p) {
        return coordinatesInBounds(p.x, p.y);
    }

    public void removeExpired(int currentTime) {
        for (Entity entity : activeEntities.values()) {
            if (entity.hasExpired(currentTime)) {
                removeActiveEntity(entity);
            }
        }
    }

    public boolean coordinatesInBounds(double x, double y) {
        int mapWidth = getWidthInPixels();
        int mapHeight = getHeightInPixels();
        return (x >= 0 && y >= 0 && x < mapWidth && y < mapHeight);
    }

    public Entity getEntity(short key) {
        return activeEntities.get(key);
    }

    public Collection<Entity> getNewEntitiesUnmodifiable() {
        return Collections.unmodifiableCollection(newEntities.values());
    }

    public static class SpawnPoint {
        public final int row;
        public final int col;

        private SpawnPoint(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public Point getPos() {
            return new Point(getPosX(), getPosY());
        }

        private int getPosX() {
            return col * BoardCell.CELL_SIZE + BoardCell.CELL_SIZE / 2;
        }

        private int getPosY() {
            return row * BoardCell.CELL_SIZE + BoardCell.CELL_SIZE / 2;
        }
    }
}
