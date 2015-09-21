package game.world.entities;

import game.graphics.Animation;
import game.world.Board;
import game.world.BoardCell;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * An entity that can damage other entities by colliding with thenm.
 */
public class Bullet extends DestructibleCollidableEntity implements Visible {
    private final Animation animation = new Animation(getTypeInfo().imageInfo);
    private int lifetime = getTypeInfo().maxLifetime;

    public Bullet(UUID ownerUniqueID, Board board) {
        super(ownerUniqueID, board);

        // bullet is spawned with maximum ordered speed
        setOrderedSpeed(getTypeInfo().maxForwardSpeed);

        // and accelerate instantly
        setMoveSpeed(getOrderedSpeed());
    }

    public Bullet(UUID ownerUniqueID, Board board, ByteBuffer src) {
        super(ownerUniqueID, board, src);
        readPosition(src);
        readOrderedHeading(src);
        readOrderedSpeed(src);

        readHitPoints(src);

        readAnimation(src);

        setHeading(getOrderedHeading());
        setMoveSpeed(getOrderedSpeed());
    }

    private void readAnimation(ByteBuffer src) {
        getAnimation().setCurrentFrameIndex(src.get());
    }

    @Override
    public void update() {
        super.update();
        if (lifetime > 0) lifetime--;

        if (lifetime == 0 || rangeExceeded()) {
            // self destruct
            lifetime = 0;
            takeDamage(getHitPoints());
        }
    }

    @Override
    public void put(ByteBuffer dst) {
        writeClassIndex(dst);
        writeKey(dst);

        writePosition(dst);
        writeOrderedHeading(dst);
        writeOrderedSpeed(dst);

        writeHitPoints(dst);

        writeAnimation(dst);
    }

    private void writeAnimation(ByteBuffer dst) {
        dst.put((byte) getAnimation().getCurrentFrameIndex());
    }

    @Override
    public void get(ByteBuffer src) {
        readClassIndex(src);
        readKey(src);

        readPosition(src);
        readOrderedHeading(src);
        readOrderedSpeed(src);

        readHitPoints(src);

        readAnimation(src);
    }

    @Override
    public void takeDamage(byte amount) {
        super.takeDamage(amount);

        if (isDead() && isMaster()) {
            Explosion explosion = new SmallExplosion(getOwnerUniqueID(), getBoard());
            explosion.setPos(getPos());
            getBoard().registerEntity(explosion);
        }

    }

    @Override
    public boolean canCollideWith(Entity other) {
        return !(other instanceof Bullet) && super.canCollideWith(other);

    }

    @Override
    public void collideWith(Collidable other) {
        // bullet hit something - remove HitPoints by damaging
        takeDamage(getHitPoints());
    }

    @Override
    public boolean cellHasObstacle(BoardCell cell) {
        return cell.ground.isShootObstacle() || cell.overlay.isShootObstacle();
    }

    @Override
    protected boolean canCrossBoardBounds() {
        return true;
    }

    @Override
    public Animation getAnimation() {
        return animation;
    }

}
