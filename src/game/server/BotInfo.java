package game.server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

public final class BotInfo {
    public static final int MAX_SPAWN_DELAY = 1000;

    public final String name;
    public final UUID uniqueID;
    private int spawnDelay;

    private short clientKey;

    public int getSpawnDelay() {
        return spawnDelay;
    }

    public void setSpawnDelay(int spawnDelay) {
        this.spawnDelay = spawnDelay;
    }

    public void resetSpawnDelay() {
        spawnDelay = MAX_SPAWN_DELAY;
    }

    public short getClientKey() {
        return clientKey;
    }

    public void setClientKey(short clientKey) {
        this.clientKey = clientKey;
    }

    public BotInfo(String name) {
        this.name = name;
        this.uniqueID = UUID.randomUUID();
    }

    public void decrementSpawnDelay() {
        spawnDelay--;
    }
}
