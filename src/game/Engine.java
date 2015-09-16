package game;


import game.client.ClientTask;
import game.server.ServerTask;
import game.util.Timeout;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static game.util.Debug.log;

/**
 * Singleton Engine class. Starts Client and Server threads.
 */
public enum Engine {
    INSTANCE;

    private static void startServer(SocketAddress serverAddress, String mapResourceName) throws IOException {
        log("server %s - starting thread", serverAddress);

        INSTANCE.serverTask = new ServerTask(serverAddress, mapResourceName);
        Thread serverThread = new Thread(INSTANCE.serverTask);
        serverThread.setName("server");
        serverThread.start();
    }

    private static void startClient(SocketAddress serverAddress, String clientName) throws IOException {
        log("client %s - starting thread", clientName, serverAddress);

        INSTANCE.clientTask = new ClientTask(serverAddress, clientName);
        Thread clientThread = new Thread(INSTANCE.clientTask);
        clientThread.setName("client");
        clientThread.start();
    }

    public static void startLoopback(String clientName, String mapResourceName) throws Exception {
        try {
            log("engine starting in loopback mode");
            SocketAddress serverAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), ServerTask.DEFAULT_UDP_PORT);

            startServer(serverAddress, mapResourceName);
            Timeout serverTimeout = new Timeout(1000);
            while (!getServer().isRunning()) {
                if (serverTimeout.occurred()) {
                    throw new RuntimeException("Timeout starting server in loopback mode");
                }
            }

            startClient(serverAddress, clientName);
            Timeout clientTimeout = new Timeout(1000);
            while (!getClient().isRunning()) {
                if (clientTimeout.occurred()) {
                    throw new RuntimeException("Timeout starting client in loopback mode");
                }
            }

            INSTANCE.clientTask.connect();
        } catch (Exception e) {
            exit();
            throw e;
        }
    }

    public static ClientTask getClient() {
        return INSTANCE.clientTask;
    }

    public static ServerTask getServer() {
        return INSTANCE.serverTask;
    }

    public static void exit() {
        if (INSTANCE.clientTask != null) {
            INSTANCE.clientTask.exit();
        }

        if (INSTANCE.serverTask != null) {
            INSTANCE.serverTask.exit();
        }
    }

    private ClientTask clientTask;
    private ServerTask serverTask;
}
