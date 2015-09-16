package game.client.messages;

import java.nio.ByteBuffer;

/**
 * Used to request client exit from server.
 */
public class ClientExitRequest extends ClientMessageBase {
    public final String clientName;
    public final String uniqueID;

    public ClientExitRequest(String clientName, String uniqueID) {
        this.clientName = clientName;
        this.uniqueID = uniqueID;
    }

    public ClientExitRequest(ByteBuffer srcBuffer) {
        super(srcBuffer);
        clientName = getString(srcBuffer);
        uniqueID = getString(srcBuffer);
    }

    @Override
    public ClientMessageType getType() {
        return ClientMessageType.CLIENT_EXIT_REQUEST;
    }

    public ByteBuffer toBuffer() {
        return make(getType(), clientName, uniqueID);
    }
}