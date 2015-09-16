package game.world;

import game.graphics.ImageFrameInfo;
import game.Resources;

/**
 * This class holds ground tile information
 */
public class TileGround {
    public final String ID;
    public final ImageFrameInfo imageFrame;
    public final int frameIndex;
    public final double moveObstacle;
    public final double shootObstacle;

    protected TileGround(String id, String imageFrameID, int frameIndex, double moveObstacle, double shootObstacle) {
        ID = id;
        this.imageFrame = Resources.getFrameInfo(imageFrameID);
        this.frameIndex = frameIndex;
        this.moveObstacle = moveObstacle;
        this.shootObstacle = shootObstacle;
    }

    public static TileGround fromLine(String line) {
        String[] parts = line.split("\\s*:\\s*");
        if (parts.length != 5) {
            throw new IllegalArgumentException(String.format("Wrong number of parts in line: %s", line));
        }

        String id           = parts[0];
        String imageFrameID = parts[1];
        int frameIndex      = Integer.parseInt(parts[2]);
        double moveObstacle = Double.parseDouble(parts[3]);
        double shootObstacle= Double.parseDouble(parts[4]);

        return new TileGround(id, imageFrameID, frameIndex, moveObstacle, shootObstacle);
    }

    public boolean isShootObstacle() {
        return shootObstacle == 1.0d;
    }
    public boolean isMoveObstacle() {
        return moveObstacle == 1.0d;
    }
}