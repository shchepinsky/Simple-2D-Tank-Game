package game.server;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;

final class ClientInfoManager {

    private final HashMap<String, ClientInfo> nameToInfoMap;              // name to ClientInfo map
    private final HashMap<UUID, ClientInfo> uniqueIDToInfoMap;            // UUID to ClientInfo map
    private final HashMap<SocketAddress, ClientInfo> addressToInfoMap;    // address to ClientInfo map

    public ClientInfoManager() {
        nameToInfoMap = new HashMap<>();
        uniqueIDToInfoMap = new HashMap<>();
        addressToInfoMap = new HashMap<>();
    }

    public void forEach(BiConsumer<? super UUID, ? super ClientInfo> action) {
        uniqueIDToInfoMap.forEach(action);
    }

    public boolean isRegistered(String name) {
        return nameToInfoMap.get(name) != null;
    }

    public boolean isRegistered(String name, UUID uniqueID) {
        return isRegistered(name) && isRegistered(uniqueID);
    }

    public boolean isRegistered(String name, UUID uniqueID, SocketAddress address) {
        return isRegistered(name) && isRegistered(uniqueID) && isRegistered(address);
    }

    public  boolean isRegistered(SocketAddress address) {
        return addressToInfoMap.get(address) != null;
    }

    public  boolean isRegistered(UUID uniqueID) {
        return uniqueIDToInfoMap.get(uniqueID) != null;
    }

    public ClientInfo setReady(UUID uniqueID, boolean ready) {
        if (isRegistered(uniqueID)) {
            ClientInfo clientInfo = get(uniqueID);
            clientInfo.setReady(ready);
            return clientInfo;
        } else {
            throw new IllegalArgumentException(String.format("No client registered with id %s", uniqueID));
        }
    }

    public ClientInfo register(String clientName, SocketAddress clientAddress) {
        // initial client key is set to 0 in constructor
        ClientInfo clientInfo = new ClientInfo(clientName, UUID.randomUUID(), clientAddress);
        return register(clientInfo);
    }

    private ClientInfo register(ClientInfo clientInfo) {
        assert clientInfo != null : "Can't register with null join info";

        if (isRegistered(clientInfo.name) || isRegistered(clientInfo.uniqueID)) {
            throw new IllegalArgumentException(
                    String.format("%s is already registered with UUID %s", clientInfo.name, clientInfo.uniqueID));
        }

        // only add if no name and no uniqueID found in maps
        nameToInfoMap.put(clientInfo.name, clientInfo);
        uniqueIDToInfoMap.put(clientInfo.uniqueID, clientInfo);
        addressToInfoMap.put(clientInfo.address, clientInfo);

        return clientInfo;
    }

    /**
     * Removes client by it's unique ID.
     * @param uniqueID unique className of client to remove.
     */
    public void remove(UUID uniqueID) {
        ClientInfo clientInfo = get(uniqueID);

        if (clientInfo == null ) {
            throw new IllegalArgumentException("You must provide existing uniqueID");
        }

        addressToInfoMap.remove(clientInfo.address);
        nameToInfoMap.remove(clientInfo.name);
        uniqueIDToInfoMap.remove(clientInfo.uniqueID);
    }
    
    public ClientInfo get(UUID uniqueID) {
        return uniqueIDToInfoMap.get(uniqueID);
    }

    public ClientInfo get(SocketAddress clientAddress) {
        return addressToInfoMap.get(clientAddress);
    }

    public boolean isValidID(String uniqueID) {
        return uniqueID.matches("[0-9a-f]{8}-[0-9a-f]{4}-[34][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
    }
}
