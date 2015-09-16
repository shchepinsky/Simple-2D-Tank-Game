package game.server;

import game.client.messages.*;
import game.server.messages.*;
import game.util.RateCounter;
import game.util.TimeFlow;
import game.util.Timeout;
import game.world.Board;
import game.world.entities.Destructible;
import game.world.entities.Enemy;
import game.world.entities.Entity;
import game.world.entities.Tank;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static game.util.Debug.log;

/**
 * This class implements server network logic.
 */
public class ServerTask implements Runnable {
    public static final int DEFAULT_UDP_PORT = 20000;       //
    public static final int WORLD_STATE_SEND_RATE = 10;     // target rate to send state to clients
    public static final int SERVER_LOOP_RATE = 200;         // target rate to cycle run()
    private final Board board;                                                  // board object holds all entities
    private final TimeFlow timeFlow = new TimeFlow();                           // converts real time into board time
    private final RateCounter loopRate = new RateCounter();                     // used to count Run() loop rate
    private final RateCounter networkStateSendRate = new RateCounter();         // used to count network state send rate
    private final ClientInfoManager clientInfoManager = new ClientInfoManager();// used to manage client state and info
    private final BotInfoManager botInfoManager = new BotInfoManager();         // used to manage AI players state
    private final DatagramChannel channel;
    private final RateCounter bandwidthCounter = new RateCounter();
    private volatile int botCount = 0;                      // number if computer players
    private volatile boolean running;
    private long timeOfStart;                               // time of server start-up, used for calculating uptime
    private double logicTime;                               // board virtual time
    public ServerTask(SocketAddress address, String mapResourceName) throws IOException {
        assert (address != null) : "Socket address can not be null";

        channel = DatagramChannel.open();                   // open datagram channel
        channel.configureBlocking(false);                   // init channel for non-blocking io
        channel.bind(address);                              // bind to listening address

        board = Board.fromResource(mapResourceName);        // loading map specified
    }

    public int getBotCount() {
        return botCount;
    }

    public ServerTask setBotCount(int botCount) {
        this.botCount = botCount;
        return this;
    }

    public double getBytesSentPerSecond() {
        return bandwidthCounter.getRate();
    }

    public long getNetworkStateSendRate() {
        return networkStateSendRate.getRate();
    }

    public long getLoopRate() {
        return loopRate.getRate();
    }

    /**
     * Sets internal flag which terminates loop in run() method.
     */
    private void cleanup() {
        try {
            log("releasing resources");
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Request server to exit and exit.
     */
    public void exit() {
        setRunning(false);
    }

    @Override
    public void run() {
        setRunning(true);                                   // while loop flag
        timeOfStart = TimeFlow.systemTime();                // remember time of start, for uptime calculation

        try {
            log("server %s - started", channel.getLocalAddress());

            Timeout worldStateRefreshTimeout = new Timeout(1000 / WORLD_STATE_SEND_RATE);

            // server internal time initialized to 0 instead of system time
            double lastTime = 0;

            while (isRunning()) try {
                // get time of frame processing start
                long frame_t0 = TimeFlow.systemTime();

                // update rate counter with current iteration
                loopRate.update();

                // read network packets
                processIncomingPackets();

                // update time flow with it's speed relative to real time
                getTimeFlow().update();

                double elapsed = (lastTime == 0) ? 0 : (getTimeFlow().time() - lastTime);
                lastTime = timeFlow.time();
                processLogic(elapsed);
                processNewEntities();                       // new entities, if any, are sent immediately after creation
                processInactiveEntities();

                logicTime = logicTime + elapsed;            // this is internal logic time counter, for debug info

                if (worldStateRefreshTimeout.occurred()) {  // check if it is time to send updates to clients
                    processActiveEntities();              // calculate and send state update
                    worldStateRefreshTimeout.reset();       // reset timer after state is sent
                }

                long frame_t1 = TimeFlow.systemTime();      // get time of processing end
                long frame_elapsed = frame_t1 - frame_t0;   // calculate total time spent in this iteration

                final int QUANTUM = 1000 / SERVER_LOOP_RATE;// limit maximum loop frequency

                if (frame_elapsed < QUANTUM) try {          // if time spent is less that desired delay between loop -
                    Thread.sleep(QUANTUM - frame_elapsed);  // sleep to maintain SERVER_LOOP_RATE
                } catch (InterruptedException ignored) {    // sleep interruption ignored
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sendServerShutdown();
            log("server loop finished");
            cleanup();
        }
    }

    private void processInactiveEntities() {
        if (channel == null || !channel.isOpen()) {         // check if network channel is up
            return;
        }

        Collection<Entity> inactive = board.getInactiveEntitiesUnmodifiable();
        List<EntityStateFragment> stateFragments = new ArrayList<>();

        EntityStateFragment.appendStateFragments(inactive, stateFragments);
        sendStateFragmentsToClients(stateFragments);        // send initial states of new entities to all clients

        // remove flush inactive entity list after sending
        // this should be done after making state buffer
        getBoard().flushInactiveEntityList();
    }

    private void processNewEntities() {
        if (channel == null || !channel.isOpen()) {         // check if network channel is up
            return;
        }

        Collection<Entity> newEntities = board.getNewEntitiesUnmodifiable();
        List<EntityStateFragment> stateFragments = new ArrayList<>();

        EntityStateFragment.appendStateFragments(newEntities, stateFragments);
        sendStateFragmentsToClients(stateFragments);        // send initial states of new entities to all clients

        // remove flush inactive entity list after sending
        // this should be done after making state buffer
        getBoard().flushNewEntityList();                    // flush after sending once
    }

    private void sendStateFragmentsToClients(List<EntityStateFragment> stateFragments) {
        clientInfoManager.forEach((uniqueID, client) -> {
            if (client.isReady()) try {
                // send state updates only to clients that are ready

                for (EntityStateFragment stateFragment : stateFragments) {
                    sendClientStateFragment(client, stateFragment);
                }

            } catch (Exception e) {
                log(e.getMessage());                    // log exception, if happens, without stack trace
            }
        });
    }

    private void processLogic(double elapsed) {

        if (!timeFlow.isPaused()) {
            checkClientsSpawned();
            checkEnemiesSpawned();

            while (elapsed > 0) {
                board.update();
                elapsed--;
            }
        }
    }

    /**
     * Ensures AI bots are spawned accordingly.
     */
    private void checkEnemiesSpawned() {
        while (botInfoManager.getBotCount() < botCount) {
            botInfoManager.register(new BotInfo("Bot %d" + botInfoManager.getBotCount()));
        }

        botInfoManager.forEach(botInfo -> {

            Entity entity = getBoard().getEntity(botInfo.getClientKey());

            if (entity == null) {                           // not spawned
                if (botInfo.getSpawnDelay() > 0) {          // not ready to spawn yet - decrease delay
                    botInfo.decrementSpawnDelay();
                } else {                                    // no delay left - ready to spawn
                    Enemy bot = board.spawnEnemy(botInfo.uniqueID);

                    if (bot == null) {
                        return;                             // oops, can't spawn. Maybe all spawn points are occupied.
                    }

                    bot.setComputerControlled(true);        // enable AI

                    botInfo.setClientKey(bot.getKey());     // set key to new spawned entity
                    botInfo.resetSpawnDelay();              // reset counter for next spawn
                }
            }

        });

    }

    /**
     * Ensures client respawn accordingly with delay.
     */
    private void checkClientsSpawned() {

        clientInfoManager.forEach((uuid, client) -> {

            if (!client.isReady()) return;

            Entity player = getBoard().getEntity(client.getKey());

            if (player == null) {                       // not spawned

                if (client.getSpawnDelay() > 0) {       // not ready to spawn yet - decrease delay
                    client.decrementSpawnDelay();
                } else {                                // no delay left - ready to spawn
                    player = getBoard().spawnPlayer(client.uniqueID);

                    if (player == null) {               // oops, can't spawn. Maybe all spawn points are occupied.
                        return;
                    }

                    client.setKey(player.getKey());// set key to new spawned entity
                    client.resetSpawnDelay();           // reset counter for next spawn
                }
            }

        });

    }

    /**
     * Sends board state buffers to clients.
     */
    private void processActiveEntities() {
        if (channel == null || !channel.isOpen()) {         // check if network channel is up
            return;
        }

        Collection<Entity> active = board.getActiveEntitiesUnmodifiable();

        List<EntityStateFragment> stateFragments = new ArrayList<>();

        EntityStateFragment.appendStateFragments(active, stateFragments);

        // SPECIAL CASE: if no entities exist server will send empty "heartbeat for client to run time"
        if (stateFragments.size() == 0) {
            EntityStateFragment fragment = new EntityStateFragment((short) 0, ByteBuffer.allocate(0));
            stateFragments.add(fragment);
        }

        // send everything to everybody ;-)
        sendStateFragmentsToClients(stateFragments);

        networkStateSendRate.update();
    }

    private void sendClientStateFragment(ClientInfo client, EntityStateFragment stateFragment) throws IOException {
        int time = (int) logicTime;                         // send as int - fractional part is irrelevant
        double speed = getTimeFlow().getSpeed();            // speed is converted to byte inside board state update
        boolean paused = getTimeFlow().isPaused();
        short key = client.getKey();

        ServerBoardStateUpdate boardStateUpdate = new ServerBoardStateUpdate(
                time,           // send board time, so we know if it is still actual first
                speed,          // send time speed for client prediction feature
                paused,         // server paused state
                key,            // tell client his key so he knows which units he owns
                stateFragment   // actual state of objects
        );

        ByteBuffer buf = boardStateUpdate.toBuffer();
        int sent = channel.send(buf, client.address);

        if (sent < boardStateUpdate.toBuffer().limit()) {
            log("Send buffer overflow with state size of %d bytes", buf.limit());
        }

        bandwidthCounter.update(sent);
    }

    private void processIncomingPackets() {
        SocketAddress clientAddress;
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // read incoming packets
        while (true) try {
            buffer.clear();
            clientAddress = channel.receive(buffer);        // next buffer, address will be null if no data present

            if (clientAddress == null) break;               // no more datagram left
            buffer.flip();                                  // prepare buffer to be read

            // process by MessageType
            switch (ClientMessageBase.getTypeFromBuffer(buffer)) {
                case CLIENT_JOIN_REQUEST: {
                    handleClientJoin(buffer, clientAddress);
                    break;
                }
                case CLIENT_EXIT_REQUEST: {
                    handleClientExit(buffer, clientAddress);
                    break;
                }
                case CLIENT_WORLD_LINE_REQUEST: {
                    handleClientWorldLineRequest(buffer, clientAddress);
                    break;
                }
                case CLIENT_CONFIRM_READY: {
                    handleClientConfirmReady(buffer, clientAddress);
                    break;
                }
                case CLIENT_PAUSE_TOGGLE_REQUEST: {
                    handleClientPauseToggleRequest(buffer, clientAddress);
                    break;
                }
                case CLIENT_SELF_DESTRUCT: {
                    handleClientSelfDestructMessage(buffer, clientAddress);
                    break;
                }
                case CLIENT_INPUT: {
                    handleClientInputMessage(buffer, clientAddress);
                    break;
                }
                default: {
                    log("Unknown packet %s from %s", ClientMessageBase.getTypeFromBuffer(buffer), clientAddress);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClientSelfDestructMessage(ByteBuffer buffer, SocketAddress clientAddress) {
        // check client unique ID to match stored address
        ClientInfo clientInfo = clientInfoManager.get(clientAddress);

        ClientSelfDestructMessage message = new ClientSelfDestructMessage(buffer);
        if (!UUID.fromString(message.uniqueID).equals(clientInfo.uniqueID)) {
            log("Client uniqueID does not match in self-destruct message");
            return;
        }

        Entity player = getBoard().getEntity(clientInfo.getKey());
        if (player == null) {
            return;                                         // no player spawned with such key
        }

        if (player instanceof Destructible) {               // if destructible we do full damage
            Destructible d = (Destructible) player;
            d.takeDamage(d.getHitPoints());
        } else {                                            // otherwise simply remove entity
            getBoard().removeActiveEntity(player.getKey());
        }

    }

    private void handleClientInputMessage(ByteBuffer buffer, SocketAddress clientAddress) {
        ClientInputMessage clientInputMessage = new ClientInputMessage(buffer);

        // use clientAddress to find client UUID and see if it matches to key
        short clientKey = clientInfoManager.get(clientAddress).getKey();

        //for (Entity entity : getBoard().getEntity.values()) {
        {
            Entity entity = getBoard().getEntity(clientKey);
            // check if this is unit under client control by matching stored key with entities key
            // also check if it is an instance of Tank because only it can be fully controlled
            //if (entity.getKey() != clientKey ||
            if (!(entity instanceof Tank)) return;//continue;

            Tank client = (Tank) entity;

            switch (clientInputMessage.turnOrder) {
                case NORTH: {
                    client.setOrderedHeading(0);
                    break;
                }
                case EAST: {
                    client.setOrderedHeading(90);
                    break;
                }
                case SOUTH: {
                    client.setOrderedHeading(180);
                    break;
                }
                case WEST: {
                    client.setOrderedHeading(270);
                    break;
                }
            }

            switch (clientInputMessage.moveOrder) {
                case FORWARD: {
                    client.forward();
                    break;
                }
                case REVERSE: {
                    client.reverse();
                    break;
                }
                case STOP: {
                    client.stop();
                    break;
                }
            }

            switch (clientInputMessage.fireOrder) {
                case FIRE: {
                    client.openFire();
                    break;
                }
                case NONE: {
                    client.ceaseFire();
                    break;
                }
            }
        }
    }

    private void handleClientConfirmReady(ByteBuffer buffer, SocketAddress clientAddress) {
        ClientConfirmReady message = new ClientConfirmReady(buffer);

        ClientInfo clientInfo = clientInfoManager.get(UUID.fromString(message.uniqueID));

        clientInfo.setReady(true);
    }

    private void handleClientWorldLineRequest(ByteBuffer buffer, SocketAddress clientAddress) throws IOException {
        ClientBoardLineRequest request = new ClientBoardLineRequest(buffer);

        List<String> lines = getBoard().getRowsUnmodifiable();
        String line = lines.get(request.lineIndex);
        int index = request.lineIndex;
        int count = lines.size();

        channel.send(new ClientBoardLineReply(line, index, count).toBuffer(), clientAddress);
    }

    private void handleClientPauseToggleRequest(ByteBuffer buffer, SocketAddress clientAddress) {
        ClientPauseToggleRequest request = new ClientPauseToggleRequest(buffer);

        log(String.format("received %s with pause %s from %s.", request.getType(), request.pause, clientAddress));
        getTimeFlow().setPaused(request.pause);
        log(String.format("TimeFlow is %s", (getTimeFlow().isPaused() ? "paused" : "running")));
    }

    /**
     * Check if client request is valid and disconnect client.
     *
     * @param buffer        ByteBuffer constructed with network data.
     * @param clientAddress address of client.
     * @throws IOException if network error occurs.
     */
    private void handleClientExit(ByteBuffer buffer, SocketAddress clientAddress) throws IOException {
        ClientExitRequest request = new ClientExitRequest(buffer);
        // make sure exit message valid by calling isRegistered
        if (clientInfoManager.isRegistered(request.clientName, UUID.fromString(request.uniqueID), clientAddress)) {

            clientInfoManager.remove(UUID.fromString(request.uniqueID));
            channel.send(new ClientExitAccept(request.clientName, request.uniqueID).toBuffer(), clientAddress);
            log(String.format("received %s from %s - removing client %s", request.getType(), request.clientName, clientAddress));
        } else {
            log(String.format("received %s from %s but data does not match - ignoring.", request.getType(), clientAddress));
        }
    }

    /**
     * This method handles client join request packets. The algorithm is simple:
     * <p>If valid UUID provided, we reconnect if name provided matches stored one.</p>
     * <p>if provided UUID is invalid, then we create new connection if name provided is
     * not already registered.</p>
     *
     * @param buffer        buffer with network message.
     * @param clientAddress remote client address.
     * @throws IOException if network error occurs.
     */
    private void handleClientJoin(ByteBuffer buffer, SocketAddress clientAddress) throws IOException {
        ClientJoinRequest request = new ClientJoinRequest(buffer);

        String replyText;
        ServerMessageBase replyMessage;

        if (clientInfoManager.isValidID(request.uniqueID)) {
            // valid ID provided - try to reconnect
            ClientInfo storedClientInfo = clientInfoManager.get(UUID.fromString(request.uniqueID));

            if (storedClientInfo != null && clientInfoManager.isRegistered(request.clientName)) {
                replyText = String.format("re-connecting %s with ID %s from %s", request.clientName, request.uniqueID, clientAddress);
                replyMessage = new ClientJoinAccept(storedClientInfo.uniqueID.toString(), replyText);
            } else {
                replyText = String.format("refuse to re-connect %s with ID %s from %s", request.clientName, request.uniqueID, clientAddress);
                replyMessage = new ClientJoinRefuse(replyText);
            }
        } else {
            // invalid ID provided - check name
            if (clientInfoManager.isRegistered(request.clientName)) {
                replyText = String.format("client with name %s already exist", request.clientName);
                replyMessage = new ClientJoinRefuse(replyText);
            } else {
                replyText = String.format("accepting new connection of %s from %s", request.clientName, clientAddress);

                ClientInfo newClientInfo = clientInfoManager.register(request.clientName, clientAddress);
                newClientInfo.setReady(false); // waiting for map fetch and client readiness confirmation
                replyMessage = new ClientJoinAccept(newClientInfo.uniqueID.toString(), replyText);
            }
        }

        channel.send(replyMessage.toBuffer(), clientAddress);
        log(replyText);
    }

    private void sendServerShutdown() {
        if (channel != null && channel.isOpen()) {      // check if network channel is up and
            clientInfoManager.forEach((s, client) -> {  // send client a shutdown notification
                try {
                    channel.send(new ServerShutdownMessage("Server is exiting").toBuffer(), client.address);
                } catch (Exception e) {
                    log(e.getMessage());              // log exception, if happens, without stack trace
                }
            });
        }
    }

    public TimeFlow getTimeFlow() {
        return timeFlow;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Board getBoard() {
        return board;
    }

    public long uptime() {
        return TimeFlow.systemTime() - timeOfStart;
    }

    public double getLogicTime() {
        return logicTime;
    }
}
