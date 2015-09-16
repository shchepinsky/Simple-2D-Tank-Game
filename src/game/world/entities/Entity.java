package game.world.entities;

import game.Resources;
import game.world.Board;

import java.nio.ByteBuffer;
import java.util.UUID;

// an invisible global board entity, can update it's internal state
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
