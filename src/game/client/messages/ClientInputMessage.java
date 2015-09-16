package game.client.messages;

import game.client.InputFireOrder;
import game.client.InputMoveOrder;
import game.client.InputTurnOrder;

import java.nio.ByteBuffer;

public class ClientInputMessage extends ClientMessageBase{
    public final InputTurnOrder turnOrder;
    public final InputMoveOrder moveOrder;
    public final InputFireOrder fireOrder;

    /**
     * This is used to transfer user input to server. Server uses client address to determine
     * how to apply this message to world state.
     * @param moveOrder moving order.
     * @param fireOrder firing order.
     */
    public ClientInputMessage(InputTurnOrder turnOrder, InputMoveOrder moveOrder, InputFireOrder fireOrder) {
        this.turnOrder = turnOrder;
        this.moveOrder = moveOrder;
        this.fireOrder = fireOrder;
    }

    public ClientInputMessage(ByteBuffer srcBuffer) {
        super(srcBuffer);
        turnOrder = InputTurnOrder.values()[srcBuffer.get()];
        moveOrder = InputMoveOrder.values()[srcBuffer.get()];
        fireOrder = InputFireOrder.values()[srcBuffer.get()];
    }

    @Override
    public ClientMessageType getType() {
        return ClientMessageType.CLIENT_INPUT;
    }

    @Override
    public ByteBuffer toBuffer() {
        return make(getType(), (byte) turnOrder.ordinal(), (byte) moveOrder.ordinal(), (byte) fireOrder.ordinal() );
    }
}
