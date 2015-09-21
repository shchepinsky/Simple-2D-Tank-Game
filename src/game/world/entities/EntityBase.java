package game.world.entities;

import game.Resources;
import game.world.Board;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * An skeleton class of most basic entity that can be attached to board globally.
 */
abstract class EntityBase implements Entity {
    public static final short INVALID_UNIQUE_ID = 0;        // this is invalid ID, unique key will skip this value
    private static short uniqueKey = INVALID_UNIQUE_ID + 1; // global key counter, gives each instance it own number

    private final boolean master;                           // if master - unit is on server side, if slave - on client
    private final Board board;
    private final UUID ownerUniqueID;
    private final short key;                                // unique key of instance

    private short parentKey = 0;                            // unique key of parent entity to exclude self-hitting
    private int networkUpdateTime;                          // time of last state, used for removing orphan & outdated

    EntityBase(UUID ownerUniqueID, Board board) {
        uniqueKey++;                                        // increment unique id
        if (uniqueKey == INVALID_UNIQUE_ID) uniqueKey++;    // if incremented unique id is invalid one - skip it

        this.ownerUniqueID = ownerUniqueID;
        this.board = board;
        this.key = uniqueKey;                               // use as unique key
        this.master = true;                                 // this is created on server, so it is master
    }

    EntityBase(UUID ownerUniqueID, Board board, ByteBuffer src) {
        this.ownerUniqueID = ownerUniqueID;
        this.board = board;

        // i do not use get() because this will violate unique key field final and immutability
        readClassIndex(src);
        this.key = src.getShort();

        this.master = false;                                // because this is created from buffer on client side
    }

    @Override
    public boolean hasExpired(int now) {
        final int ENTITY_EXPIRATION_TIME = 200;
        return now - networkUpdateTime > ENTITY_EXPIRATION_TIME && !isMaster() ;
    }

    @Override
    public void setNetworkUpdateTime(int time) {
        this.networkUpdateTime = time;
    }

    public boolean isMaster() {
        return master;
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public short getKey() {
        return key;
    }

    @Override
    public UUID getOwnerUniqueID() {
        return ownerUniqueID;
    }

    void readClassIndex(ByteBuffer src) {
        // check if this buffer has proper typeIndex
        byte type1 = src.get();
        byte type2 = indexOfClass();

        if (type1 != type2) {
            String message = String.format("Wrong buffer! EntityTypeInfo byte does not match: %d != %d.", type1, type2);
            throw new IllegalArgumentException(message);
        }
    }

    void writeClassIndex(ByteBuffer dst) {
        dst.put(indexOfClass());
    }

    void writeKey(ByteBuffer dst) {
        dst.putShort( getKey() );
    }

    void readKey(ByteBuffer src) {

        short key1 = getKey();
        short key2 = src.getShort();
        if (key1 != key2) {
            String message = String.format("Wrong buffer! EntityKey does not match: %d != %d.", key1, key2);
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public Entity getParent() {
        return getBoard().getEntity(parentKey);
    }

    public short getParentKey() {
        return parentKey;
    }

    public void setParentKey(short parentKey) {
        this.parentKey = parentKey;
    }

    public EntityTypeInfo getTypeInfo() {
        return Resources.ofClass(this.getClass());
    }

    public byte indexOfClass() {
        return (byte) Resources.indexOfClass(this.getClass());
    }

    @Override
    public String toString() {
        return "EntityBase{" +
                "key=" + key +
                '}';
    }
}
