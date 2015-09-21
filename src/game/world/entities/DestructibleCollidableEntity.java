package game.world.entities;

import game.world.Board;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * An skeleton class of moving entity that can be destroyed.
 */
public abstract class DestructibleCollidableEntity extends CollidableMovableEntity implements Destructible {

    private byte hitPoints;

    DestructibleCollidableEntity(UUID ownerUniqueID, Board board) {
        super(ownerUniqueID, board);
        hitPoints = getTypeInfo().maxHitPoints;
    }

    DestructibleCollidableEntity(UUID ownerUniqueID, Board board, ByteBuffer buf) {
        super(ownerUniqueID, board, buf);
    }

    @Override
    public boolean isDead() {
        return hitPoints == 0;
    }

    @Override
    public byte getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(byte hitPoints) {
        this.hitPoints = hitPoints;
    }

    @Override
    public void takeDamage(byte amount) {
        hitPoints = amount > hitPoints ? 0 : (byte)(hitPoints - amount);
    }

    @Override
    public boolean isReadyForRemoval() {
        return isDead();
    }

    void writeHitPoints(ByteBuffer dst) {
        dst.put( getHitPoints() );
    }

    void readHitPoints(ByteBuffer src) {
        this.hitPoints = src.get();
    }
}
