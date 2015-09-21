package game.client;

import game.client.messages.*;
import game.graphics.ViewPort;
import game.server.EntityStateFragment;
import game.util.TimeFlow;
import game.server.messages.*;
import game.util.RateCounter;
import game.util.Timeout;
import game.world.Board;
import game.world.entities.Entity;
import game.world.entities.EntityTypeInfo;
import game.world.entities.Positionable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.function.Consumer;

import static game.util.Debug.log;
import static java.lang.Math.max;

/**
 * Client-side class, board using network state updates.
 * Read-accessed by rendering thread and write-accessed by own thread.
 */
public class ClientTask implements Runnable {
    private final DatagramChannel channel;
    private final RateCounter loopRate = new RateCounter(); // used to count Run() loop rate
    // event handlers, set before thread started
    public Consumer<String> onTryToConnectSuccess;          // when connection successful
    public Consumer<String> onTryToConnectFailure;          // when connection failed
    public Consumer<Double> onTryToConnectProgress;         // waiting for connect reply
    public Consumer<Double> onFetchBoardProgress;           // when map fetch from server is in progress
    public Consumer<Void> onConnectionFinished;             // when client closes connection
    private boolean running = false;                        // client loop is running
    private boolean paused;                                 // board update is paused
    private long timeOfStart;                               // time of client start, used for uptime calculation
    private boolean localUpdateEnabled = true;              // will update entities between network updates
    private String clientName;                              // desired name of client
    private String uniqueID;                                // unique id assigned by server
    private Board board;                                    // contains all map data and entities list
    private SocketAddress serverAddress;

    private ClientState state = ClientState.READY_TO_CONNECT;
    private InputTurnOrder turnOrder = InputTurnOrder.NONE;
    private InputFireOrder fireOrder = InputFireOrder.NONE;
    private InputMoveOrder moveOrder = InputMoveOrder.STOP;
    private short clientKey;                                // two bytes - client identifier in network operations
    private final TimeFlow timeFlow = new TimeFlow();       // TimeFlow is synced to server time and running in between

    /**
     * Constructs client with given parameters.
     *
     * @param serverAddress server internet address we intent to connect in future.
     * @param port          port number, see <code>Server.DEFAULT_UDP_PORT</code> for default value.
     * @param clientName    client name we want to use.
     * @throws IOException if network error occurs.
     */
    public ClientTask(InetAddress serverAddress, int port, String clientName) throws IOException {
        this(new InetSocketAddress(serverAddress, port), clientName);
    }

    /**
     * Constructs client with given parameters.
     *
     * @param serverAddress server socket address we intent to connect in future.
     * @param clientName    client name we want to use.
     * @throws IOException if network error occurs.
     */
    public ClientTask(SocketAddress serverAddress, String clientName) throws IOException {
        assert serverAddress != null : "Server address can't be null";

        setServerAddress(serverAddress);
        setClientName(clientName);

        try {
            channel = DatagramChannel.open();
            channel.connect(serverAddress);
            channel.configureBlocking(false);

            setRunning(true);
        } catch (Exception e) {
            cleanup();  // close resources
            throw e;    // rethrow to caller
        }
    }

    /**
     * Gets prediction status.
     *
     * @return returns prediction status.
     */
    public boolean isLocalUpdateEnabled() {
        return localUpdateEnabled;
    }

    public void setLocalUpdateEnabled(boolean localUpdateEnabled) {
        this.localUpdateEnabled = localUpdateEnabled;
    }

    public Board getBoard() {
        return board;
    }

    public long getLoopRate() {
        return loopRate.getRate();
    }

    public InputTurnOrder getTurnOrder() {
        return turnOrder;
    }

    public void setTurnOrder(InputTurnOrder turnOrder) {
        this.turnOrder = turnOrder;
    }

    private synchronized String getClientName() {
        return clientName;
    }

    private synchronized void setClientName(String clientName) {
        // validate state
        if (getState() != ClientState.READY_TO_CONNECT) {
            throw new IllegalStateException(String.format("Can't set client name in %s state!", getState()));
        }

        // validate clientName value
        clientName = clientName.trim();
        if (clientName.isEmpty()) {
            throw new IllegalArgumentException("Can't set clientName to empty string or spaces only!");
        }

        this.clientName = clientName;
    }

    /**
     * Implements Runnable.run() method. Contains client logic and state management.
     */
    @Override
    public void run() {
        try {
            log("client %s - started", channel.getLocalAddress());

            timeOfStart = TimeFlow.systemTime();            // remember startup time ofr uptime calculation

            while (isRunning()) {

                switch (getState()) {
                    case READY_TO_CONNECT: {
                        runReadyToConnect();
                        break;
                    }
                    case TRYING_TO_CONNECT: {
                        runTryingToConnect();
                        break;
                    }
                    case TRYING_TO_CONNECT_FETCH_BOARD: {
                        runTryingToConnectFetchBoard();
                        break;
                    }
                    case TRYING_TO_CONNECT_CONFIRM_READY: {
                        runTryingToConnectConfirmReady();
                        break;
                    }
                    case CONNECTED: {
                        runConnected();
                        break;
                    }
                    case TRYING_TO_DISCONNECT: {
                        runTryingToDisconnect();
                        break;
                    }
                    case DISCONNECTED: {
                        runDisconnected();
                        break;
                    }

                    default: {
                        throw new Error("Unknown client state: " + getState());
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            log("client loop finished");
            cleanup();
        }
    }

    private void runTryingToConnectConfirmReady() {
        // 1. setup timeout
        // 2. send confirm ready
        // 3. wait for network
        // 4. read response
        Timeout confirmReadyTimeout = new Timeout(30);
        while (isRunning()) try {

            if (confirmReadyTimeout.occurred()) {
                String message = String.format("Timeout in %s state", getState());
                log(message);

                if (onTryToConnectFailure != null) {
                    onTryToConnectFailure.accept(message);
                }

                setState(ClientState.TRYING_TO_DISCONNECT);
                return;
            }

            writeBufferToChannel(new ClientConfirmReady(getUniqueID()).toBuffer());

            ByteBuffer buffer = processIncomingPackets();
            Timeout replyTimeout = new Timeout(1000);
            while (buffer == null && !replyTimeout.occurred()) {
                buffer = processIncomingPackets();
            }

            if (buffer == null) {
                continue;
            }

            switch (ServerMessageBase.getTypeFromBuffer(buffer)) {
                case SHUTDOWN_NOTIFY: {
                    setState(ClientState.DISCONNECTED);
                    return;
                }
                case BOARD_STATE_UPDATE: {
                    setState(ClientState.CONNECTED);
                    return;
                }

                default: {
                    log("Unexpected packet %s from %s", ServerMessageBase.getTypeFromBuffer(buffer), getServerAddress());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <ol>
     * <li>request line at index N</li>
     * <li>wait for response until line timeout occurs</li>
     * <li>add to list if response index matches requested line's index</li>
     * <li>start over until list size == totalCount</li>
     * </ol>
     */
    private void runTryingToConnectFetchBoard() {
        log("fetching initial board data from server...");

        // buffer to store received map string
        ArrayList<String> lines = new ArrayList<>();
        int linesTotal = 0;

        // setup stage timeout
        Timeout fetchMapTimeout = new Timeout(5000);

        while (isRunning()) try {
            if (fetchMapTimeout.occurred()) {
                setState(ClientState.TRYING_TO_DISCONNECT);
                log("Timeout fetching board from server");
                return;
            }

            //1. send request for next line
            writeBufferToChannel(new ClientBoardLineRequest(lines.size()).toBuffer());

            //2. wait for next line reply from server
            Timeout timeoutForLine = new Timeout(500);
            ByteBuffer buffer = null;

            while (!timeoutForLine.occurred() && buffer == null) try {
                // wait some time for network to process amd try to read buffer
                Thread.sleep(10);
                buffer = processIncomingPackets();
            } catch (InterruptedException ignored) {
            }   // sleep interruption is ignored

            if (buffer != null) try {
                switch (ServerMessageBase.getTypeFromBuffer(buffer)) {
                    case SHUTDOWN_NOTIFY: {
                        // oops, server is shutting down - set state and exit immediately
                        setState(ClientState.DISCONNECTED);
                        return;
                    }
                    case CLIENT_FETCH_BOARD_REPLY: {
                        ClientBoardLineReply boardLineReply = new ClientBoardLineReply(buffer);

                        // check if index matches our request
                        if (lines.size() == boardLineReply.lineIndex) {
                            linesTotal = boardLineReply.totalCount;
                            lines.add(boardLineReply.line);

                            // call progress callback, if set
                            if (onFetchBoardProgress != null) {
                                onFetchBoardProgress.accept((double) boardLineReply.lineIndex / boardLineReply.totalCount);
                            }
                        }
                        break;
                    }
                    default: {
                        log("Unexpected packet %s from %s", ServerMessageBase.getTypeFromBuffer(buffer), getServerAddress());
                    }
                }
            } catch (Exception e) {
                log("Exception while parsing packet from %s", getServerAddress());
                e.printStackTrace();
            }

            if (lines.size() == linesTotal && linesTotal > 0) {

                // try to create board from fetched lines
                board = Board.fromList(lines);

                // if here, then no exception and we set state as connected
                setState(ClientState.TRYING_TO_CONNECT_CONFIRM_READY);

                // notify callback about success
                if (onTryToConnectSuccess != null) {
                    onTryToConnectSuccess.accept("board fetched successfully");
                }

                return;
            }

        } catch (Exception e) {
            log("Exception while fetching board from server - disconnecting");
            e.printStackTrace();
        }

        // if we've got here then isRunning == false
        setState(ClientState.TRYING_TO_DISCONNECT);
    }

    /**
     * Fills buffer from client's channel and rewinds it for subsequent
     * processing by caller.
     *
     * @return buffer read or null if there is no data in channel.
     */
    private ByteBuffer processIncomingPackets() {
        // process incoming message
        // for performance reasons it may be better to keep buffer allocated and just rewinding it before net read
        // and flipping after read is complete. For simplicity it is left out as is.

        // should be enough space for reading any server message
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        SocketAddress source = null;
        try {
            source = channel.receive(buffer);
            buffer.rewind();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (source != null ? buffer : null);
    }

    private void writeBufferToChannel(ByteBuffer buffer) throws IOException {
        buffer.rewind();
        channel.write(buffer);
    }

    /**
     * Attempt connection to server. This method only sets state to
     * TRYING_TO_CONNECT and returns immediately.
     */
    public synchronized void connect() {
        if (getState() == ClientState.READY_TO_CONNECT) {
            setState(ClientState.TRYING_TO_CONNECT);
        } else
            throw new IllegalStateException(String.format("Can't connect from %s state", getState()));
    }

    private void cleanup() {
        try {
            log("releasing resources");
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        setRunning(false);
    }

    private ClientState getState() {
        return state;
    }

    private void setState(ClientState state) {
        this.state = state;
    }

    public boolean isRunning() {
        return running;
    }

    private void setRunning(boolean running) {
        this.running = running;
    }

    private String getUniqueID() {
        return uniqueID;
    }

    private void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    private synchronized SocketAddress getServerAddress() {
        return serverAddress;
    }

    private void setServerAddress(SocketAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    private void runReadyToConnect() throws InterruptedException {
        log("Entering READY_TO_CONNECT state");             // log this state method name
        Thread.sleep(20);                                   // this state has nothing to do
    }

    private void runTryingToDisconnect() {
        log("Entering TRYING_TO_DISCONNECT state");         // log this state method name

        try {
            ClientExitRequest m = new ClientExitRequest(getClientName(), getUniqueID());
            ByteBuffer buffer = m.toBuffer();
            writeBufferToChannel(buffer);                   // send exit message once

            Timeout disconnectTimeout = new Timeout(3000);  // setup timeout and loop for response
            while (isRunning() && !disconnectTimeout.occurred()) {

                buffer = processIncomingPackets();        // get next message from server
                if (buffer == null) try {
                    Thread.sleep(20);                       // if no messages arrived yet - sleep for some time
                    continue;                               // start over again
                } catch (InterruptedException ignored) {
                }   // sleep interruption is ignored

                switch (ServerMessageBase.getTypeFromBuffer(buffer)) {
                    case CLIENT_EXIT_ACCEPT: {
                        log("server confirmed our exit");
                        break;
                    }
                    default: {
                        log("Unexpected packet %s from %s",
                                ServerMessageBase.getTypeFromBuffer(buffer),
                                getServerAddress()
                        );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            setState(ClientState.DISCONNECTED);             // transit to disconnected state
            if (onConnectionFinished != null) {             // fire finished callback handler, if set
                onConnectionFinished.accept(null);
            }
        }
    }

    private void runConnected() {
        log("Entered CONNECTED state");                     // log this state method name

        setRunning(true);                                   // while loop flag
        timeOfStart = TimeFlow.systemTime();                // remember time of start, for uptime calculation

        try {
            log("client %s - started", channel.getLocalAddress());

            // client internal time initialized to 0 instead of system time
            while (isRunning()) try {
                // get time of frame processing start
                long frame_t0 = TimeFlow.systemTime();

                // update rate counter with current iteration
                loopRate.update();

                // read network packets first
                for (ByteBuffer buf = processIncomingPackets(); buf != null; buf = processIncomingPackets()) {
                    processIncomingPackets(buf);
                }

                processLogic();

                long frame_t1 = TimeFlow.systemTime();      // get time of processing end
                long frame_elapsed = frame_t1 - frame_t0;   // calculate total time spent in this iteration

                final int SERVER_LOOP_RATE = 200;
                final int QUANTUM = 1000 / SERVER_LOOP_RATE;// limit maximum loop frequency

                if (frame_elapsed < QUANTUM) try {          // if time spent is less that desired delay between loop -
                    Thread.sleep(QUANTUM - frame_elapsed);  // sleep to maintain SERVER_LOOP_RATE
                } catch (InterruptedException ignored) {
                }   // sleep interruption ignored

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            log("client loop finished");
            cleanup();
        }
    }

    private void processLogic() {
        // remember last network packet from server and run for small period autonomously so we predict
        // movement of entities between updates and make it look smoother

        if (!isLocalUpdateEnabled()) {
            // if local updates are disable simply remove expired
            board.removeExpired((int) timeFlow.time());
            return;
        }

        long elapsed = loopRate.getTimeBetweenUpdates();
        int predictTime = (int) max(timeFlow.getSpeed() * elapsed - 1, 0);

        while (predictTime > 0) {
            board.update();
            predictTime--;
        }

        // move viewport accordingly to client's entity position
        Entity entity = getBoard().getEntity(clientKey);
        if (entity != null && entity instanceof Positionable) {
            Positionable positionable = (Positionable) entity;
            ViewPort.INSTANCE.centerOnPos(positionable.getX(), positionable.getY());
        }

    }

    private void processIncomingPackets(ByteBuffer buffer) {
        switch (ServerMessageBase.getTypeFromBuffer(buffer)) {
            case SHUTDOWN_NOTIFY: {                     // server is shutting down immediately
                setState(ClientState.DISCONNECTED);     // simply set state to disconnected and bail out
                break;
            }
            case BOARD_STATE_UPDATE: {
                handleServerBoardStateUpdate(buffer);   // process board state from server
                break;
            }
            default: {
                log("Unexpected packet %s from %s", ServerMessageBase.getTypeFromBuffer(buffer), getServerAddress());
            }
        }
    }

    private void handleServerBoardStateUpdate(ByteBuffer buffer) {
        // 1. check if message time is equal of higher than in client's state
        // 2. extract message buffers and parse entities
        // 3. set current sequence number to received one

        ServerBoardStateUpdate updateMessage = new ServerBoardStateUpdate(buffer);

        timeFlow.sync(updateMessage.time, updateMessage.speed);

        // network can change order of datagram, so we discard packets
        // with time marker less than current time
        double elapsed = updateMessage.time - timeFlow.time();
        if (elapsed < 0) {
            return;                                         // obsolete state frame received - discard
        }

        setPaused(updateMessage.paused);                    // set paused state from network state
        clientKey = updateMessage.clientKey;                // update client key - each spawned entity has new key

        // this buffer contains all state bytes
        try {
            ByteBuffer buf = updateMessage.state.getEntityStateBuffer();

            // iterate stored number of entities by typeIndex and creating corresponding instance
            // instance reads it's state from buffer, double checks for entityType and advances
            // buffer position accordingly to it's type, so next iteration can begin
            for (int i = 0; i < updateMessage.state.entityCount; i++) {
                short entityKey = EntityStateFragment.getEntityKey(buf);

                // try to find existing using stored key. Key is unique through class instances.
                Entity entity = getBoard().getEntity(entityKey);

                // key does not exist - we create entity with provided key, otherwise update existing
                if (entity == null) {
                    entity = EntityTypeInfo.createFromBuffer(board, buf);
                    getBoard().registerEntity(entity);
                } else {
                    entity.get(buf);
                }

                // update entity last state logic time for expiration purposes
                entity.setNetworkUpdateTime(updateMessage.time);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // get controlled entity position and update viewport
        Entity entity = getBoard().getEntity(updateMessage.clientKey);
        if (entity != null && entity instanceof Positionable) {
            Positionable positionable = (Positionable) entity;
            ViewPort.INSTANCE.centerOnPos(positionable.getX(), positionable.getY());
        }

    }

    private void runTryingToConnect() {
        log("Entered TRYING_TO_CONNECT state");             // log this state method name
        try {                                               // send connection request to server
            ClientJoinRequest m = new ClientJoinRequest(getClientName(), (uniqueID != null ? uniqueID : "none"));
            ByteBuffer buffer = m.toBuffer();
            writeBufferToChannel(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            setState(ClientState.DISCONNECTED);
            return;
        }

        Timeout connectTimeout = new Timeout(3000);         // set connection timeout
        while (isRunning()) try {                           // using try to keep loop from breaking on exception

            if (connectTimeout.occurred()) {                // handle timeout if occurred
                String message = "Timeout connecting to " + getServerAddress();
                setState(ClientState.DISCONNECTED);         // set state disconnected as no response from server
                if (onTryToConnectFailure != null) {        // call failure callback, if set and
                    onTryToConnectFailure.accept(message);  // pass meaningful failure message
                }
                log(message);                               // log & return
                return;
            }

            ByteBuffer buffer = processIncomingPackets(); // read network messages, if any
            if (buffer == null) try {
                Thread.sleep(20);                           // sleep if no response from server
                continue;                                   // start over
            } catch (InterruptedException ignored) {
            }       // ignoring sleep interruption


            switch (ServerMessageBase.getTypeFromBuffer(buffer)) {
                case CLIENT_JOIN_ACCEPT: {
                    handleServerClientJoinAccept(buffer);
                    return;
                }
                case CLIENT_JOIN_REFUSE: {
                    handleServerClientJoinRefuse(buffer);
                    return;
                }
                default: {
                    log("Unexpected packet %s in state %s", ServerMessageBase.getTypeFromBuffer(buffer), getState());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleServerClientJoinRefuse(ByteBuffer buffer) {
        ClientJoinRefuse m = new ClientJoinRefuse(buffer);
        log("%s %s", m.getType(), m.refuseMessage);
        setState(ClientState.DISCONNECTED);

        if (onTryToConnectFailure != null) {
            onTryToConnectFailure.accept(m.refuseMessage);
        }
    }

    private void handleServerClientJoinAccept(ByteBuffer buffer) {
        ClientJoinAccept m = new ClientJoinAccept(buffer);
        setUniqueID(m.uniqueID);

        log("%s from %s", m.getType(), m.uniqueID);
        setState(ClientState.TRYING_TO_CONNECT_FETCH_BOARD);
    }

    private void runDisconnected() throws InterruptedException {
        Thread.sleep(20);
    }

    public void togglePause() {
        try {
            writeBufferToChannel(new ClientPauseToggleRequest(!isPaused()).toBuffer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isPaused() {
        return paused;
    }

    private void setPaused(boolean paused) {
        if (paused == this.paused) return;

        System.out.println(paused ? "Paused" : "Unpaused");

        this.paused = paused;
    }

    public synchronized InputMoveOrder getMoveOrder() {
        return moveOrder;
    }

    public synchronized void setMoveOrder(InputMoveOrder moveOrder) {
        this.moveOrder = moveOrder;
    }

    public synchronized InputFireOrder getFireOrder() {
        return fireOrder;
    }

    public synchronized void setFireOrder(InputFireOrder fireOrder) {
        this.fireOrder = fireOrder;
    }

    public void sendInputStateToServer() {
        try {
            writeBufferToChannel(new ClientInputMessage(getTurnOrder(), getMoveOrder(), getFireOrder()).toBuffer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendSelfDestructMessage() {
        try {
            writeBufferToChannel(new ClientSelfDestructMessage(getUniqueID()).toBuffer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long uptime() {
        return TimeFlow.systemTime() - timeOfStart;
    }
}
