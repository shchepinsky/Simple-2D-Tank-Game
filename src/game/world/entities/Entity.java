package game.world.entities;

import game.world.Board;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * An invisible global board entity that update it's internal state and read/write it's state to buffer.
 */
public interface Entity {
    Board getBoard();

    UUID getOwnerUniqueID();

    short getKey();

    void update();

    Entity getParent();
    void setParentKey(short parentKey);

    boolean isMaster();

    boolean isReadyForRemoval();

    EntityTypeInfo getTypeInfo();

    byte indexOfClass();

    void put(ByteBuffer dst);
    void get(ByteBuffer src);

    void setNetworkUpdateTime(int time);
    boolean hasExpired(int now);
}
