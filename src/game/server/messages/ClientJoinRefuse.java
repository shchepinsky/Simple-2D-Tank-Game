package game.server.messages;

import java.nio.ByteBuffer;

/**
 * {@code ServerJoinReply} is a class for server message that is sends to
 * client as reply to {@code ClientJoinRequest}.
 *
 */
public class ClientJoinRefuse extends ServerMessageBase {
    public final String refuseMessage;

    public ClientJoinRefuse(String refuseMessage) {
        this.refuseMessage = refuseMessage;
    }

    public ClientJoinRefuse(ByteBuffer srcBuffer) {
        refuseMessage = getString(srcBuffer);
    }

    @Override
    public ServerMessageType getType() {
        return ServerMessageType.CLIENT_JOIN_REFUSE;
    }

    @Override
    public ByteBuffer toBuffer() {
        return make(getType(),refuseMessage);
    }
}