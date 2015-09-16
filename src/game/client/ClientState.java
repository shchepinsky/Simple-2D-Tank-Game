package game.client;

public enum ClientState {
    READY_TO_CONNECT,                           // just started
    TRYING_TO_CONNECT,                          // trying to contact server and log in
    TRYING_TO_CONNECT_FETCH_BOARD,              // server contacted, trying to fetch world map
    TRYING_TO_CONNECT_CONFIRM_READY,            // map fetched, send "ready" to server for it to begin state updates
    CONNECTED,                                  // first state update received - finally connected and running
    TRYING_TO_DISCONNECT,                       // send client exit request to gracefully disconnect
    DISCONNECTED                                // disconnected state
}
