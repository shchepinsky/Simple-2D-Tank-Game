package game.world.entities;

import game.world.Board;
import game.world.BoardCell;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Enemy extends Tank {
    private boolean computerControlled;

    private EnemyBotAI botAI;

    public Enemy(UUID ownerUniqueID, Board board) {
        super(ownerUniqueID, board);

        botAI = new EnemyBotAI(board, this);
    }

    public Enemy(UUID ownerUniqueID, Board board, ByteBuffer buf) {
        super(ownerUniqueID, board, buf);

        botAI = new EnemyBotAI(board, this);
    }

    public PathList<BoardCell> getPath() {
        return botAI.getPath();
    }

    @Override
    public void update() {
        if (isComputerControlled()) {
            botAI.update(); // set calculate AI orders
        }

        super.update(); // move to nearby cell
    }

    public boolean isComputerControlled() {
        return computerControlled;
    }

    public void setComputerControlled(boolean computerControlled) {
        this.computerControlled = computerControlled;
    }
}
