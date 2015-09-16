package game.client.messages;

import java.nio.ByteBuffer;

public class ClientConfirmReady extends ClientMessageBase {
    final public String uniqueID;

    public ClientConfirmReady(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public ClientConfirmReady(ByteBuffer buffer) {
        super(buffer);
        uniqueID = getString(buffer);
    }

    @Override
    public ClientMessageType getType() {
        return ClientMessageType.CLIENT_CONFIRM_READY;
    }

    @Override
    public ByteBuffer toBuffer() {
        return make(getType(), uniqueID);
    }
}
