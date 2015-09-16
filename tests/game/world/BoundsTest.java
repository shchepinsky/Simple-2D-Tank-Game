package game.world;

import org.junit.Test;

import static org.junit.Assert.*;

public class BoundsTest {

    @Test
    public void testIntersects() throws Exception {
        Bounds box1 = Bounds.fromCornerPoints(0, 0, 10, 10);

        assertFalse(box1.collidesWith(Bounds.fromCornerPoints(10, 10, 20, 20)));

        assertFalse(box1.collidesWith(Bounds.fromCornerPoints(0, -10, 10, 0)));

        assertFalse(box1.collidesWith(Bounds.fromCornerPoints(-10, 0, 0, 10)));

        assertTrue(box1.collidesWith(Bounds.fromCornerPoints(-5, -5, +5, +5)));

        assertTrue(box1.collidesWith(box1));


    }
}