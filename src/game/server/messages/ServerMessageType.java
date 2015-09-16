package game.server.messages;

public enum ServerMessageType {
    CLIENT_JOIN_ACCEPT,
    CLIENT_JOIN_REFUSE,
    CLIENT_EXIT_ACCEPT,
    CLIENT_FETCH_BOARD_REPLY,
    BOARD_STATE_UPDATE,
    SHUTDOWN_NOTIFY
}
