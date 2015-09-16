package game.world.entities;

import game.world.Board;

import java.nio.ByteBuffer;
import java.util.UUID;

public class MediumExplosion extends Explosion {
    public MediumExplosion(UUID ownerUniqueID, Board board) {
        super(ownerUniqueID, board);
    }

    public MediumExplosion(UUID ownerUniqueID, Board board, ByteBuffer buf) {
        super(ownerUniqueID, board, buf);
    }
}
