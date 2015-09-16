package game;

import game.graphics.ImageFrameInfo;
import game.world.TileGround;
import game.world.TileOverlay;
import game.world.entities.EntityTypeInfo;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;

import static game.util.Debug.log;

public class Resources {

    private static final HashMap<String, Image> images = new HashMap<>();
    private static final HashMap<String, ImageFrameInfo> frameInfo = new HashMap<>();
    private static final HashMap<String, TileGround> groundTypes = new HashMap<>();
    private static final HashMap<String, TileOverlay> overlayTypes = new HashMap<>();
    private static final ArrayList<String> typeArray = new ArrayList<>();
    private static final LinkedHashMap<String, EntityTypeInfo> typeMap = new LinkedHashMap<>();

    /**
     * Static resource initializer:
     * 1. manually specify menu background images.
     * 2. load ImageFrameInfo from file along with mentioned there images.
     * 3. load tile data
     * 4. overlays
     * 5. entities
     */
    static {
        try {
            Resources.loadImage("menu_bg", "/menu_bg.jpg");
            Resources.loadImage("menu_ua", "/menu_ua.jpg");

            try (Scanner scanner = new Scanner(getResourceStream("/image-data.txt"))) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.isEmpty() || line.startsWith(";") || line.startsWith("//")) continue;

                    ImageFrameInfo imageInfo = ImageFrameInfo.fromLine(line);

                    Image image = Resources.loadImage(imageInfo.getImageID(), "/" + imageInfo.getFileName());

                    // validate size
                    if (image.getWidth() < imageInfo.getFrameWidth() || image.getHeight() < imageInfo.getFrameHeight()) {
                        throw new IllegalArgumentException(
                                String.format("Image %s is smaller than specified frame size", imageInfo.getFileName()));
                    }

                    frameInfo.put(imageInfo.getImageID(), imageInfo);
                }
            }
        } catch (Exception e) {
            throw new Error("Unable to load image resources: " + e.getMessage().toLowerCase());
        }

        log("Loaded Images: %d", images.size());
        log("Loaded %ss: %d", ImageFrameInfo.class.getSimpleName(), images.size());

        try (Scanner scanner = new Scanner(getResourceStream("/tile-id.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith(";") || line.startsWith("//")) continue;

                TileGround tileGround = TileGround.fromLine(line);

                groundTypes.put(tileGround.ID, tileGround);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(String.format(
                    "Unable to load %s data",
                    TileGround.class.getSimpleName()
            ));
        }

        try (Scanner scanner = new Scanner(getResourceStream("/overlay-id.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith(";") || line.startsWith("//")) continue;

                TileOverlay tileOverlay = TileOverlay.fromLine(line);

                overlayTypes.put(tileOverlay.ID, tileOverlay);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(String.format(
                    "Unable to load %s data",
                    TileOverlay.class.getSimpleName()
            ));
        }

        try (Scanner scanner = new Scanner(getResourceStream("/entity-id.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith(";") || line.startsWith("//")) continue;

                EntityTypeInfo entityTypeInfo = new EntityTypeInfo(line);

                registerEntityType(entityTypeInfo.className, entityTypeInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(String.format(
                    "Unable to load %s data",
                    EntityTypeInfo.class.getSimpleName()
            ));

        }

    }

    public static Image getImage(String id) {
        Image image = images.get(id);

        if (image == null) {
            throw new IllegalArgumentException("Resource image not found: " + id);
        }

        return image;
    }

    public static ImageFrameInfo getFrameInfo(String id) {
        ImageFrameInfo resultFrameInfo = frameInfo.get(id);

        if (resultFrameInfo == null) {
            throw new Error("Resource FrameInfo not found: " + id);
        }

        return resultFrameInfo;
    }

    public static TileGround getGroundType(String id) {
        TileGround tileGround = groundTypes.get(id);

        if (tileGround == null) {
            throw new RuntimeException("Resource Tile not found: " + id);
        }

        return tileGround;
    }

    public static TileOverlay getOverlayType(String id) {
        TileOverlay tileOverlay = overlayTypes.get(id);

        if (tileOverlay == null) {
            throw new RuntimeException("Resource Overlay not found: " + id);
        }

        return tileOverlay;
    }

    public static InputStream getResourceStream(String resource) throws RuntimeException {
        InputStream stream = Resources.class.getResourceAsStream(resource);
        if (stream == null) throw new RuntimeException("can't get resource stream for " + resource);
        return stream;
    }

    private static Image loadImage(String imageID, String resourceName) {
        Image image = new Image(getResourceStream(resourceName));
        images.put(imageID, image);
        return image;
    }

    private static void registerEntityType(String className, EntityTypeInfo entityTypeInfo) {
        typeArray.add(className);
        typeMap.put(className, entityTypeInfo);
    }

    public static EntityTypeInfo ofClassName(String className) {
        return typeMap.get(className);
    }

    public static EntityTypeInfo ofClass(Class clazz) {
        return ofClassName(clazz.getSimpleName());
    }

    public static int indexOfClassName(String className) {
        return typeArray.indexOf(className);
    }

    public static int indexOfClass(Class clazz) {
        return indexOfClassName(clazz.getSimpleName());
    }

    public static EntityTypeInfo ofIndex(byte entityTypeIndex) {
        return typeMap.get(typeArray.get(entityTypeIndex));
    }

}
