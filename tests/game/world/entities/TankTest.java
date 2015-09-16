package game.world.entities;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class TankTest {

    @Test
    public void testPutGet() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(512);
        Tank t1 = new Tank( null, null);
        t1.setPos(1111, 2222);
        t1.setHeading(127);
        t1.setOrderedSpeed(0.123);
        t1.takeDamage( (byte) 50);
        t1.getAnimation().setCurrentFrameIndex(31);

        t1.put(buf);

        buf.flip();

        Tank t2 = new Tank( null, null, buf);

        assertTrue( t1.getPos().sameAs(t2.getPos()) );
        assertTrue( t1.getHeading() == t2.getHeading());
        assertTrue( t1.getOrderedSpeed() == t2.getOrderedSpeed());
        assertTrue( t1.getHitPoints() == t2.getHitPoints());
        assertTrue( t1.getAnimation().getCurrentFrameIndex() == t2.getAnimation().getCurrentFrameIndex() );

    }
}