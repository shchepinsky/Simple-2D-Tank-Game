package game.server;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClientInfoManagerTest {
    static String name1 = "test name 1";
    static String name2 = "test name 2";
    static String wrong_uuid = "00000000-0000-0000-0000-000000000000";

    @Test
    public void testIsRegistered() throws Exception {
        ClientInfoManager manager = new ClientInfoManager();

        manager.register(name1, InetSocketAddress.createUnresolved("127.0.0.1", 20000));

        assertTrue(manager.isRegistered(name1));

        assertFalse(manager.isRegistered(name2));
    }

    @Test
    public void testIsRegisteredNegative() throws Exception {
        ClientInfoManager manager = new ClientInfoManager();

        ClientInfo clientInfo = manager.register(name1, InetSocketAddress.createUnresolved("127.0.0.1", 20000));

        assertTrue(manager.isRegistered(name1));
        assertTrue(manager.isRegistered(clientInfo.uniqueID));

        assertFalse(manager.isRegistered(name2));
        assertFalse(manager.isRegistered(wrong_uuid));

    }

    @Test
    public void testRegister() throws Exception {
        ClientInfoManager manager = new ClientInfoManager();

        manager.register(name1, InetSocketAddress.createUnresolved("127.0.0.1", 20000));
        manager.register(name2, InetSocketAddress.createUnresolved("127.0.0.1", 20000));
    }

    @Test
    public void testRegisterNegative() throws Exception {
        ClientInfoManager manager = new ClientInfoManager();

        manager.register(name1, InetSocketAddress.createUnresolved("127.0.0.1", 20000));

        // try register with existing name - should throw exception
        try {
            manager.register(name1, InetSocketAddress.createUnresolved("127.0.0.1", 20000));
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        // try register with existing UUID - should throw exception
        try {
            manager.register(name2, InetSocketAddress.createUnresolved("127.0.0.1", 20000));
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testRemove() throws Exception {
        ClientInfoManager manager = new ClientInfoManager();

        ClientInfo clientInfo1 = manager.register(name1, InetSocketAddress.createUnresolved("127.0.0.1", 20000));
        ClientInfo clientInfo2 = manager.register(name2, InetSocketAddress.createUnresolved("127.0.0.1", 20000));

        manager.remove(clientInfo1.uniqueID);

        assertFalse(manager.isRegistered(name1));
        assertFalse(manager.isRegistered(clientInfo1.uniqueID.toString()));
    }

    @Test
    public void testRemoveNegative() throws Exception {
        ClientInfoManager manager = new ClientInfoManager();

        manager.register(name1, InetSocketAddress.createUnresolved("127.0.0.1", 20000));

        try {
            manager.remove(UUID.fromString(wrong_uuid));
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testGet() throws Exception {
        ClientInfoManager manager = new ClientInfoManager();

        ClientInfo clientInfo1 = manager.register(name1, InetSocketAddress.createUnresolved("127.0.0.1", 20000));
        ClientInfo clientInfo2 = manager.register(name2, InetSocketAddress.createUnresolved("127.0.0.1", 20000));

        assertTrue(clientInfo1.name.contentEquals(name1));
        assertTrue(clientInfo2.name.contentEquals(name2));
    }

    @Test
    public void testGetNegative() throws Exception {
        ClientInfoManager manager = new ClientInfoManager();

        manager.register(name1, InetSocketAddress.createUnresolved("127.0.0.1", 20000));

        ClientInfo clientInfo2 = manager.get(UUID.fromString(wrong_uuid));

        assertTrue(clientInfo2 == null);
    }
}