package game.server.messages;

import java.nio.ByteBuffer;

/**
 * {@code ServerJoinReply} is a server message that it sends to
 * client as reply to {@code ClientJoinRequest}.
 */
public class ClientJoinAccept extends ServerMessageBase {

    public final String uniqueID;
    private final String acceptMessage;

    public ClientJoinAccept(String uniqueID, String acceptMessage) {
        this.uniqueID = uniqueID;
        this.acceptMessage = acceptMessage;
    }

    public ClientJoinAccept(ByteBuffer srcBuffer) {
        super(srcBuffer);
        uniqueID = getString(srcBuffer);
        acceptMessage = getString(srcBuffer);
    }

    @Override
    public ServerMessageType getType() {
        return ServerMessageType.CLIENT_JOIN_ACCEPT;
    }

    @Override
    public ByteBuffer toBuffer() {
        return make(getType(), uniqueID, acceptMessage);
    }
}