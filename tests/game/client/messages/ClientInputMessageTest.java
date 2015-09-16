package game.client.messages;

import game.client.InputFireOrder;
import game.client.InputMoveOrder;
import game.client.InputTurnOrder;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Random;

import static org.junit.Assert.*;

public class ClientInputMessageTest {
    @Test
    public void test1() {
        InputTurnOrder turnOrder = InputTurnOrder.EAST;
        InputMoveOrder moveOrder = InputMoveOrder.FORWARD;
        InputFireOrder fireOrder = InputFireOrder.FIRE;

        ClientInputMessage c1 = new ClientInputMessage(turnOrder, moveOrder, fireOrder);

        ByteBuffer buf = c1.toBuffer();

        ClientInputMessage c2 = new ClientInputMessage(buf);

        assertTrue(c1.turnOrder == c2.turnOrder && c1.moveOrder == c2.moveOrder && c1.fireOrder == c2.fireOrder);
    }
}