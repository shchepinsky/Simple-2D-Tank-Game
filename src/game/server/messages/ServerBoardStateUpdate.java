package game.server.messages;

import game.server.EntityStateFragment;

import java.nio.ByteBuffer;

/**
 * Used when updating entity state of board.
 */
public class ServerBoardStateUpdate extends ServerMessageBase {
    private final double TIME_RESOLUTION  = 0.01;           //

    public final boolean paused;                            // running/paused state of board
    public final int time;                                  // this state time
    public final double speed;                              // speed state time speed
    public final short clientKey;                           // key of client so he knows his own entity
    public final EntityStateFragment state;                 // state of board entities

    public ServerBoardStateUpdate(int time, double speed, boolean paused, short clientKey, EntityStateFragment state) {
        this.time       = time;
        this.speed      = speed;
        this.paused     = paused;
        this.clientKey  = clientKey;
        this.state      = state;
    }

    public ServerBoardStateUpdate(ByteBuffer srcBuffer) {
        super(srcBuffer);

        time        = srcBuffer.getInt();
        speed       = 1.0 + srcBuffer.get() * TIME_RESOLUTION;
        paused      = srcBuffer.get() == 1;
        clientKey   = srcBuffer.getShort();

        state = new EntityStateFragment(srcBuffer);
    }

    @Override
    public ServerMessageType getType() {
        return ServerMessageType.BOARD_STATE_UPDATE;
    }

    @Override
    public ByteBuffer toBuffer() {
        byte speed_byte = (byte) ((speed - 1.0) / TIME_RESOLUTION);

        final ByteBuffer headerBuffer = make(getType(), time, speed_byte, paused, clientKey);
        final ByteBuffer stateBuffer = state.toBuffer();

        ByteBuffer resultBuffer = ByteBuffer.allocate(headerBuffer.limit() + stateBuffer.limit());

        resultBuffer.put(headerBuffer);
        resultBuffer.put(stateBuffer);
        resultBuffer.flip();
        return resultBuffer;
    }
}
