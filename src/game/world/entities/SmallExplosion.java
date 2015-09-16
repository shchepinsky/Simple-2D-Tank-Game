package game.world.entities;

import game.world.Board;

import java.nio.ByteBuffer;
import java.util.UUID;

public class SmallExplosion extends Explosion {

    public SmallExplosion(UUID ownerUniqueID, Board board) {
        super(ownerUniqueID, board);
    }

    public SmallExplosion(UUID ownerUniqueID, Board board, ByteBuffer buf) {
        super(ownerUniqueID, board, buf);
    }
}
