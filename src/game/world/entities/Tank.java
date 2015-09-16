package game.world.entities;

import game.graphics.Animation;
import game.world.Board;
import game.world.BoardCell;

import java.nio.ByteBuffer;
import java.util.UUID;

import static game.util.Debug.log;

public class Tank extends DestructibleCollidableEntity implements Visible {
    private final Cannon cannon = new Cannon(this);
    private final Animation animation = new Animation(getTypeInfo().imageInfo);

    private boolean shooting;                                 // if entity is in shooting mode

    public Tank(UUID ownerUniqueID, Board board) {
        super(ownerUniqueID, board);
    }

    public Tank(UUID ownerUniqueID, Board board, ByteBuffer src) {
        super(ownerUniqueID, board, src);

        readPosition(src);
        readHeading(src);
        readOrderedHeading(src);
        readMoveSpeed(src);
        readOrderedSpeed(src);
        readHitPoints(src);
        readAnimation(src);
    }

    private void readAnimation(ByteBuffer src) {
        animation.setCurrentFrameIndex(src.get());
    }

    public Cannon getCannon() {
        return cannon;
    }

    @Override
    public void takeDamage(byte amount) {
        super.takeDamage(amount);

        // explosions are generated only on server side
        if (isDead() && isMaster()) {
            Explosion explosion = new MediumExplosion(getOwnerUniqueID(), getBoard());
            explosion.setPos(getPos());

            getBoard().registerEntity(explosion);
        }
    }

    @Override
    public boolean cellHasObstacle(BoardCell cell) {
        // tank can't move over water obstacle without bridge overlay
        if (cell.ground.isMoveObstacle() && !cell.overlay.isBridge()) return true;

        // if overlay is obstacle, we check bounds
        if (cell.overlay.isMoveObstacle()) return true;

        // otherwise it's all clear
        return false;
    }

    @Override
    protected boolean canCrossBoardBounds() {
        return false;
    }

    @Override
    public Animation getAnimation() {
        return animation;
    }

    public void openFire() {
        shooting = true;
    }

    public void ceaseFire() {
        shooting = false;
    }

    @Override
    public void update() {
        super.update();

        cannon.update();

        if (shooting && cannon.canShoot()) {
            cannon.shoot();
        }

        boolean turning = Math.abs(getHeadingDelta(getHeading(), getOrderedHeading())) > 1.0;
        boolean moving = Math.abs(getMoveSpeed()) > 0.1 / 1000;

        animation.setPaused(!moving && !turning);

        animation.update();
    }

    @Override
    public void put(ByteBuffer dst) {
        // use each class helper methods to write required set of properties
        writeClassIndex(dst);
        writeKey(dst);

        writePosition(dst);
        writeHeading(dst);
        writeOrderedHeading(dst);
        writeMoveSpeed(dst);
        writeOrderedSpeed(dst);
        writeHitPoints(dst);
        writeAnimation(dst);
    }

    private void writeAnimation(ByteBuffer dst) {
        dst.put((byte) animation.getCurrentFrameIndex());
    }

    @Override
    public void get(ByteBuffer src) {
        // use each class helper methods to read required set of properties
        readClassIndex(src);
        readKey(src);

        readPosition(src);
        readHeading(src);
        readOrderedHeading(src);
        readMoveSpeed(src);
        readOrderedSpeed(src);
        readHitPoints(src);
        writeAnimation(src);
    }

    @Override
    public void collideWith(Collidable other) {
        if (other instanceof Bullet) {
            takeDamage((byte) 50);
        }
    }

    public void fireOnce() {
        if (cannon.canShoot()) {
            cannon.shoot();
        }
    }

}
