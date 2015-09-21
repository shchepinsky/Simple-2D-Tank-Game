package game.world.entities;

import game.server.EntityStateFragment;
import game.server.messages.ServerBoardStateUpdate;
import game.world.Board;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.*;

public class EntityTypeInfoTest {

    @Test
    public void testCreateFromBuffer() throws Exception {
        // generate data
        Board board = Board.fromResource("/map.txt");
        for (int i=0; board.getActiveEntitiesUnmodifiable().size() <100; i++) {
            board.registerEntity(new Tank(new UUID(0,0), board));
            board.registerEntity(new Enemy(new UUID(0,0), board));
            board.registerEntity(new Bullet(new UUID(0,0), board));
            board.registerEntity(new SmallExplosion(new UUID(0,0), board));
            board.registerEntity(new MediumExplosion(new UUID(0,0), board));
        }


        System.out.println("Entities Created: " + board.getActiveEntitiesUnmodifiable().size());

        // get entity states
        List<EntityStateFragment> fragments = new ArrayList<>();
        EntityStateFragment.appendStateFragments(board.getActiveEntitiesUnmodifiable(), fragments);

        // states saved, clear board
        Collection<Entity> entities= board.getActiveEntitiesUnmodifiable();

        for (Entity entity : entities) {
            board.removeActiveEntity(entity.getKey());
        }

        // write states to message
        List<ServerBoardStateUpdate> updates = new ArrayList<>();
        for (EntityStateFragment fragment : fragments) {
            ServerBoardStateUpdate state = new ServerBoardStateUpdate(0, 1.0, false, (short)0, fragment);
            updates.add(state);
        }
        System.out.println("Fragments Written: " + fragments.size());

        // get message buffers
        List<ByteBuffer> buffers = new ArrayList<>();
        for (ServerBoardStateUpdate update : updates) {
            buffers.add(update.toBuffer());
        }

        // read messages back from buffers
        updates.clear();
        for (ByteBuffer buffer : buffers) {
            ServerBoardStateUpdate update = new ServerBoardStateUpdate(buffer);

            ByteBuffer entityBuffer = update.state.getEntityStateBuffer();

            // process entities of each message
            for (int i = 0; i < update.state.entityCount; i++) {
                short entityKey = EntityStateFragment.getEntityKey(entityBuffer);

                Entity e = EntityTypeInfo.createFromBuffer(board, entityBuffer);
                board.registerEntity(e);
            }
        }

        System.out.println("Entities Read: " + board.getActiveEntitiesUnmodifiable().size());

    }
}