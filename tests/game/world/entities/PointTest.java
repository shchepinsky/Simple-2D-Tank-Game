package game.world.entities;

import org.junit.Test;

import static java.lang.Math.abs;
import static org.junit.Assert.*;

public class PointTest {

    boolean equals(double d1, float f2) {
        final double EPSILON = 0.00000001;

        return abs(d1) - abs(f2) < EPSILON;
    }

    @Test
    public void testAt() throws Exception {

        Point pos1, pos2;

        pos1 = new Point(0,0);
        pos2 = pos1.at(0, 10);
        assertTrue( equals(pos2.x, 0) && equals(pos2.y, -10) );

        pos1 = new Point(0,0);
        pos2 = pos1.at(180, 10);
        assertTrue( equals(pos2.x, 0) && equals(pos2.y, +10) );

        assertTrue(pos1.sameAs(new Point(0, +0)));
        assertTrue(pos2.sameAs(new Point(0, +10)));
    }


}