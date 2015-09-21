package game.server;


import game.server.messages.ServerMessageBase;
import game.world.entities.Entity;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

/**
 * Contains state of entities. States are grouped into buffers with STATE_BUFFER_SIZE_THRESHOLD to avoid
 * exceeding MTU if multiple Entities are to be sent.
 */
public class EntityStateFragment {
    public final short entityCount;

    private final ByteBuffer stateBuffer;

    public ByteBuffer getEntityStateBuffer() {
        ByteBuffer result = ByteBuffer.allocate(stateBuffer.limit());
        result.put(stateBuffer);                            // copy data to result buffer
        result.flip();                                      // prepare result buffer for reading
        stateBuffer.rewind();                               // rewind back stateBuffer, keep limit for subsequent calls

        return result;
    }

    public ByteBuffer toBuffer() {
        int original = stateBuffer.position();

        ByteBuffer buf = ByteBuffer.allocate(stateBuffer.capacity() + Short.BYTES);

        buf.putShort(entityCount);
        buf.put(stateBuffer);
        buf.flip();

        stateBuffer.position(original);

        return buf;
    }

    public EntityStateFragment(ByteBuffer srcBuf) {
        entityCount = srcBuf.getShort();
        stateBuffer = ByteBuffer.allocate(srcBuf.remaining());
        stateBuffer.put(srcBuf);
        stateBuffer.flip();
    }

    public EntityStateFragment(short entityCount, ByteBuffer statesBuffer) {
        int original = statesBuffer.position();

        this.entityCount = entityCount;
        stateBuffer = ByteBuffer.allocate(statesBuffer.limit());
        stateBuffer.put(statesBuffer);

        statesBuffer.position(original);
        stateBuffer.flip();
    }

    public static byte getEntityTypeIndex(ByteBuffer srcBuffer) {
        return srcBuffer.get(srcBuffer.position());
    }

    public static short getEntityKey(ByteBuffer srcBuffer) {
        return srcBuffer.getShort(srcBuffer.position() + 1);
    }

    public static void appendStateFragments(Collection<Entity> entities, List<EntityStateFragment> states) {
        if (entities.size() == 0) return;

        // If there is too much entities on board and UDP packet becomes big, it can be split by IP protocol and
        // reassembled later. If part of split packet is lost, entire datagram is lost. So i will split big board
        // states into smaller ones if threshold is crossed.

        // IMPORTANT: create buffer that are smaller than server message size
        final int BUFFER_SIZE_THRESHOLD = ServerMessageBase.SEND_BUFFER_MAX_SIZE - 64;

        ByteBuffer buf = ByteBuffer.allocate(ServerMessageBase.SEND_BUFFER_MAX_SIZE);

        short entitiesInBuffer = 0;
        short entitiesProcessed = 0;

        for (Entity entity : entities) {
            entitiesProcessed++;                            // count iterations so we know when last entity is processed

            entity.put(buf);                                // save state of current entity to current buffer
            entitiesInBuffer++;                             // count entities in current buffer

            // attach current buffer to lsit if buffer size limit reached or if there is no more entities
            if (buf.position() > BUFFER_SIZE_THRESHOLD || entitiesProcessed == entities.size()) {
                buf.flip();                                 // set stateBuffer for reading

                EntityStateFragment fragment = new EntityStateFragment(entitiesInBuffer, buf);
                states.add(fragment);                       // add current partial state to result list
                entitiesInBuffer = 0;                         // reset quantity counter
                buf.clear();                                // reset stateBuffer for new fragment
            }
        }
    }


}
