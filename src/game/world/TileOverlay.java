package game.world;

import game.graphics.ImageFrameInfo;
import game.Resources;

/**
 * This class holds overlay tile information
 */
public class TileOverlay {
    public final String ID;
    public final ImageFrameInfo imageFrame;
    public final int frameIndex;
    private final double moveObstacle;
    private final double shootObstacle;
    public final String bounds;

    private final String flags;

    protected TileOverlay(String id, String imageFrameID, int frameIndex, double moveObstacle, double shootObstacle, String flags, String bounds) {
        ID = id;
        this.imageFrame = (imageFrameID.isEmpty()) ? null : Resources.getFrameInfo(imageFrameID);
        this.frameIndex = frameIndex;
        this.moveObstacle = moveObstacle;
        this.shootObstacle = shootObstacle;
        this.flags = flags;
        this.bounds = bounds;
    }

    public static TileOverlay fromLine(String line) {
        String[] parts = line.split("\\s*:\\s*");
        if (parts.length < 6) {
            throw new IllegalArgumentException(String.format("Wrong number of parts in line: %s", line));
        }

        String id           = parts[0];
        String imageFrameID = parts[1];
        int frameIndex      = Integer.parseInt(parts[2]);
        double moveObstacle = Double.parseDouble(parts[3]);
        double shootObstacle= Double.parseDouble(parts[4]);
        String flags        = parts[5];
        String bounds       = parts[6];

        return new TileOverlay(id, imageFrameID, frameIndex, moveObstacle, shootObstacle, flags, bounds);
    }

    public boolean isShootObstacle() {
        return shootObstacle == 1.0d;
    }
    public boolean isMoveObstacle() {
        return moveObstacle == 1.0d;
    }

    public boolean isBridge() {
        return flags.contains("b");
    }

    public boolean isVisible() {
        return imageFrame != null;
    }
}