package game.world.entities;

import game.world.Board;

import java.nio.ByteBuffer;
import java.util.UUID;

public class SpecialExplosion extends Explosion {
    public SpecialExplosion(UUID ownerUniqueID, Board board) {
        super(ownerUniqueID, board);
    }

    public SpecialExplosion(UUID ownerUniqueID, Board board, ByteBuffer src) {
        super(ownerUniqueID, board, src);
    }
}
