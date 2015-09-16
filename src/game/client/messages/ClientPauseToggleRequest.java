package game.client.messages;

import java.nio.ByteBuffer;

/**
 * Used to notify server about client toggling pause state.
 */
public class ClientPauseToggleRequest extends ClientMessageBase {
    public final boolean pause;

    public ClientPauseToggleRequest(boolean pause) {
        this.pause = pause;
    }

    public ClientPauseToggleRequest(ByteBuffer srcBuffer) {
        super(srcBuffer);
        pause = Boolean.valueOf(getString(srcBuffer));
    }

    @Override
    public ClientMessageType getType() {
        return ClientMessageType.CLIENT_PAUSE_TOGGLE_REQUEST;
    }

    @Override
    public ByteBuffer toBuffer() {
        return make(getType(), Boolean.toString(pause));
    }
}
