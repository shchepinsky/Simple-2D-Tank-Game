package game.client.messages;

import java.nio.ByteBuffer;

/**
 * Used to download map from server.
 */
public class ClientBoardLineRequest extends ClientMessageBase{
    public final int lineIndex;

    public ClientBoardLineRequest(int lineIndex) {
        this.lineIndex = lineIndex;
    }

    public ClientBoardLineRequest(ByteBuffer srcBuffer) {
        super(srcBuffer);
        lineIndex = srcBuffer.getInt();
    }

    @Override
    public ClientMessageType getType() {
        return ClientMessageType.CLIENT_WORLD_LINE_REQUEST;
    }

    @Override
    public ByteBuffer toBuffer() {
        return make(getType(), lineIndex);
    }
}
