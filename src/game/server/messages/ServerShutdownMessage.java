package game.server.messages;

import java.nio.ByteBuffer;

/**
 * Used to notify clients about server shutting down.
 */
public class ServerShutdownMessage extends ServerMessageBase {
    private final String message;

    public ServerShutdownMessage(String message) {
        this.message = message;
    }

    public ServerShutdownMessage(ByteBuffer srcBuffer) {
        message = getString(srcBuffer);
    }

    @Override
    public ServerMessageType getType() {
        return ServerMessageType.SHUTDOWN_NOTIFY;
    }

    @Override
    public ByteBuffer toBuffer() {
        return make(getType(), message);
    }
}