package game.server.messages;

import java.nio.ByteBuffer;

/**
 * Optional message, confirming that client is logged out from server.
 */
public class ClientExitAccept extends ServerMessageBase {
    private final String clientName;
    private final String uniqueID;

    public ClientExitAccept(String clientName, String uniqueID) {
        this.clientName = clientName;
        this.uniqueID = uniqueID;
    }

    public ClientExitAccept(ByteBuffer srcBuffer) {
        super(srcBuffer);
        clientName = getString(srcBuffer);
        uniqueID = getString(srcBuffer);
    }

    @Override
    public ServerMessageType getType() {
        return ServerMessageType.CLIENT_EXIT_ACCEPT;
    }

    @Override
    public ByteBuffer toBuffer() {
        return make(getType(), clientName, uniqueID );
    }
}
