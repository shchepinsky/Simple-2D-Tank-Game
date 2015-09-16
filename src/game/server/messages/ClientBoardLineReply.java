package game.server.messages;

import java.nio.ByteBuffer;

/**
 * Used in board fetch process.
 */
public class ClientBoardLineReply extends ServerMessageBase{
    public final String line;
    public final int lineIndex;
    public final int totalCount;

    public ClientBoardLineReply(String line, int lineIndex, int totalCount) {
        this.line = line;
        this.lineIndex = lineIndex;
        this.totalCount = totalCount;
    }

    public ClientBoardLineReply(ByteBuffer srcBuffer) {
        super(srcBuffer);
        line = getString(srcBuffer);
        lineIndex = srcBuffer.getInt();
        totalCount = srcBuffer.getInt();
    }

    @Override
    public ServerMessageType getType() {
        return ServerMessageType.CLIENT_FETCH_BOARD_REPLY;
    }

    @Override
    public ByteBuffer toBuffer() {
        return make(getType(), line, lineIndex, totalCount);
    }
}
