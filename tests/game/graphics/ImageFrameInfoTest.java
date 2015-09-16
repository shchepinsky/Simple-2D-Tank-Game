package game.graphics;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.junit.Test;

import static org.junit.Assert.*;

public class ImageFrameInfoTest {

    @Test
    public void testGetFrameXY() throws Exception {
        Image image = new WritableImage(128, 64);

        double x = ImageFrameInfo.getFrameX(image, 32, 32, 1);
        double y = ImageFrameInfo.getFrameY(image, 32, 32, 1);
        assertTrue(x == 32);
        assertTrue(y == 0);

        x = ImageFrameInfo.getFrameX(image, 32, 32, 4);
        y = ImageFrameInfo.getFrameY(image, 32, 32, 4);

        assertTrue(x == 0);
        assertTrue(y == 32);

        x = ImageFrameInfo.getFrameX(image, 32, 32, 7);
        y = ImageFrameInfo.getFrameY(image, 32, 32, 7);
        assertTrue(x == 96);
        assertTrue(y == 32);

    }
}