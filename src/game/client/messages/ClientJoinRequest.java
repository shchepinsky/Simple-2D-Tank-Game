package game.client.messages;

import java.nio.ByteBuffer;

/**
 * {@code ClientJoinRequest} is a class for client join request message that
 * is sent by client to server.
 */
public class ClientJoinRequest extends ClientMessageBase {
    public final String clientName;
    public final String uniqueID;

    public ClientJoinRequest(String clientName, String uniqueID) {
        this.clientName = clientName;
        this.uniqueID = uniqueID;
    }

    public ClientJoinRequest(ByteBuffer srcBuffer) {
        super(srcBuffer);
        clientName = getString(srcBuffer);
        uniqueID = getString(srcBuffer);
    }

    @Override
    public ClientMessageType getType() {
        return ClientMessageType.CLIENT_JOIN_REQUEST;
    }

    public ByteBuffer toBuffer() {
        return make(getType(), clientName, uniqueID);
    }
}