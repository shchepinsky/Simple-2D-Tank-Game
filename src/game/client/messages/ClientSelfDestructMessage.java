package game.client.messages;

import java.nio.ByteBuffer;

/**
 * Optional message to request client entity self-destruct. Was used for some debugging and left over for fun.
 */
public class ClientSelfDestructMessage extends ClientMessageBase {
    public final String uniqueID;

    public ClientSelfDestructMessage(ByteBuffer srcBuffer) {
        super(srcBuffer);
        uniqueID = getString(srcBuffer);
    }

    public ClientSelfDestructMessage(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    @Override
    public ClientMessageType getType() {
        return ClientMessageType.CLIENT_SELF_DESTRUCT;
    }

    @Override
    public ByteBuffer toBuffer() {
        return make(getType(), uniqueID);
    }
}
