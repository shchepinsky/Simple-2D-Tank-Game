package game.world.entities;

import game.graphics.Animation;
import game.world.Board;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Explosion extends PositionableEntity implements Visible {
    private final Animation animation = new Animation(getTypeInfo().imageInfo);;

    public Explosion(UUID ownerUniqueID, Board board) {
        super(ownerUniqueID, board);
    }

    public Explosion(UUID ownerUniqueID, Board board, ByteBuffer src) {
        super(ownerUniqueID, board, src);

        readPosition(src);
        readExplosion(src);
    }

    @Override
    public void update() {
        animation.update();
    }

    @Override
    public boolean isReadyForRemoval() {
        return animation.isCompleted();
    }

    @Override
    public void put(ByteBuffer dst) {
        writeClassIndex(dst);
        writeKey(dst);

        writePosition(dst);
        writeExplosion(dst);
    }

    private void writeExplosion(ByteBuffer dst) {
        dst.put( (byte) animation.getCurrentFrameIndex() );
    }

    @Override
    public void get(ByteBuffer src) {
        readClassIndex(src);
        readKey(src);

        readPosition(src);
        readExplosion(src);
    }

    private void readExplosion(ByteBuffer src) {
        animation.setCurrentFrameIndex(src.get());
    }

    @Override
    public Animation getAnimation() {
        return animation;
    }
}
