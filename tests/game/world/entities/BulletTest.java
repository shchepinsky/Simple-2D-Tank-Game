package game.world.entities;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

import static java.lang.Math.*;
import static java.lang.StrictMath.sin;
import static org.junit.Assert.*;

public class BulletTest {

    @Test
    public void testPutGet() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(512);

        Bullet b1 = new Bullet( null, null );
        b1.getAnimation().setCurrentFrameIndex(1);
        b1.setPos(11, 22);
        b1.setOrderedHeading(84);
        b1.setOrderedSpeed(0.222);

        b1.put(buf);

        buf.flip();

        Bullet b2 = new Bullet( null, null, buf);

        assertTrue( b1.getKey() == b2.getKey() );
        assertTrue( b1.getPos().sameAs( b2.getPos() ));
        assertTrue( b1.getOrderedHeading() == b2.getOrderedHeading());
        assertTrue( b1.getOrderedSpeed() == b2.getOrderedSpeed());
        assertTrue( b1.getAnimation().getCurrentFrameIndex() == b2.getAnimation().getCurrentFrameIndex());
    }
}