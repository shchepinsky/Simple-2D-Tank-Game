package game.graphics;

import game.Resources;
import javafx.scene.image.Image;

public class ImageFrameInfo {
    private static final int TILE_INFO_COLUMNS = 6;

    private String imageID;
    public String getImageID() { return imageID; }
    private void setImageID(String imageID) { this.imageID = imageID; }

    private String fileName;
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private int frameWidth;
    public int getFrameWidth() { return frameWidth; }
    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    private int frameHeight;
    public int getFrameHeight() { return frameHeight; }
    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

    private ImageType imageType;
    public ImageType getImageType() { return imageType; }
    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    private int rotationFrameCount;
    public int getRotationFrameCount() { return rotationFrameCount; }
    public void setRotationFrameCount(int rotationFrameCount) {
        this.rotationFrameCount = rotationFrameCount;
    }

    private int animationFrameCount;
    public int getAnimationFrameCount() { return animationFrameCount; }
    public void setAnimationFrameCount(int animationFrameCount) {
        this.animationFrameCount = animationFrameCount;
    }

    private int animationFrameDelay;
    public int getAnimationFrameDelay() { return animationFrameDelay; }
    public void setAnimationFrameDelay(int animationFrameDelay) {
        this.animationFrameDelay = animationFrameDelay;
    }

    private boolean animationLoop;
    public boolean isAnimationLooped() {
        return animationLoop;
    }
    public void setAnimationLoop(boolean animationLoop) {
        this.animationLoop = animationLoop;
    }

    private ImageFrameInfo() {

    }

    public int getFramesPerRow() {
        Image image = Resources.getImage(imageID);
        return ImageFrameInfo.getFramesPerRow(image, frameWidth);
    }

    public int getFrameY(int index) {
        return (index / getFramesPerRow()) * frameHeight;
    }

    public int getFrameX(int index) {
        return (index % getFramesPerRow()) * frameWidth;
    }

    public static ImageFrameInfo fromLine(String line) {
        if (line.isEmpty()) {
            throw new IllegalArgumentException("line is empty");
        }

        String[] parts = line.split("\\s*:\\s*");

        if (parts.length != TILE_INFO_COLUMNS ) {
            throw new IllegalArgumentException(String.format("malformed line: %s", line));
        }

        ImageFrameInfo result = new ImageFrameInfo();

        result.setImageID(parts[0]);
        result.setFileName(parts[1]);
        result.setFrameWidth(Integer.parseInt(parts[2]));
        result.setFrameHeight(Integer.parseInt(parts[3]));
        String typeString = parts[4].toUpperCase();

        String[] animData = parts[5].split("\\s*,\\s*");

        switch (typeString) {
            case "TILE": {
                if (animData.length != 1) {
                    throw new IllegalArgumentException("Wrong number of parameters in frames column: " + animData.length);
                }

                result.setImageType(ImageType.TILE);
                result.setRotationFrameCount(Integer.parseInt(animData[0]));
                break;
            }
            case "EFFECT": {
                if (animData.length != 3) {
                    throw new IllegalArgumentException("Wrong number of parameters in frames column: " + animData.length);
                }

                result.setImageType(ImageType.EFFECT);
                result.setAnimationFrameCount(Integer.parseInt(animData[0]));
                result.setAnimationFrameDelay(1000 / Integer.parseInt(animData[1]));
                result.setAnimationLoop(Boolean.valueOf(animData[2]));
                break;
            }
            case "ACTOR": {
                if (animData.length != 4) {
                    throw new IllegalArgumentException("Wrong number of parameters in frames column: " + animData.length);
                }

                result.setImageType(ImageType.ACTOR);
                result.setRotationFrameCount(Integer.parseInt(animData[0]));
                result.setAnimationFrameCount(Integer.parseInt(animData[1]));
                result.setAnimationFrameDelay(1000 / Integer.parseInt(animData[2]));
                result.setAnimationLoop(Boolean.valueOf(animData[3]));
                break;
            }
            default: {
                throw new RuntimeException("Unknown image imageType: " + typeString);
            }
        }


        return result;
    }

    public static int getFramesPerRow(Image image, int frameW) {
        return (int) image.getWidth() / frameW;
    }

    public static double getFrameX(Image image, int frameW, int frameH, int index) {
        assert( image != null);
        assert( frameW > 0 );
        assert( frameH > 0 );
        assert( index >= 0 );

        return ((index % getFramesPerRow(image, frameW)) * frameW);
    }

    public static double getFrameY(Image image, int frameW, int frameH, int index) {
        assert( image != null);
        assert( frameW > 0 );
        assert( frameH > 0 );
        assert( index >= 0 );

        return (index / getFramesPerRow(image, frameW)) * frameH;
    }

}
