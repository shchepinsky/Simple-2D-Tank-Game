package game.server;

import java.net.SocketAddress;
import java.util.UUID;

public final class ClientInfo {
    public static final int MAX_SPAWN_DELAY = 1000;

    public final String name;
    public final UUID uniqueID;
    public final SocketAddress address;
    private int spawnDelay;

    private short key;
    private boolean ready;

    public int getSpawnDelay() {
        return spawnDelay;
    }

    public void setSpawnDelay(int spawnDelay) {
        this.spawnDelay = spawnDelay;
    }

    public void resetSpawnDelay() {
        spawnDelay = MAX_SPAWN_DELAY;
    }

    public short getKey() {
        return key;
    }

    public ClientInfo(String name, UUID uniqueID, SocketAddress address) {
        this.name = name;
        this.uniqueID = uniqueID;
        this.address = address;
        this.setKey((short) 0);
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void setKey(short key) {
        this.key = key;
    }


    public void decrementSpawnDelay() {
        spawnDelay--;
    }
}
