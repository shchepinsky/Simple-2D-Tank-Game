package game.server.messages;

import java.nio.ByteBuffer;

/**
 * Common ancestor for all server messages.
 *
 * This is abstract class that provides static utility methods to simplify
 * buffer writing and reading for subclasses.
 *
 * Static make() method is used by subclasses to write their data.
 *
 * Subclasses should carefully check that the amount and order of fields
 * read are equal to written.
 */
public abstract class ServerMessageBase {
    public static final int SEND_BUFFER_MAX_SIZE = 500;

    /**
     * Constructs message from ByteBuffer.
     * @param srcBuffer source ByteBuffer
     */
    public ServerMessageBase(ByteBuffer srcBuffer) {
        assert srcBuffer != null:"Source buffer can't be null!";

        srcBuffer.rewind(); // read from start, in case caller forgot to rewind
        // check if subclass messageType matches messageType in buffer
        ServerMessageType type = ServerMessageType.values()[srcBuffer.getInt()];
        if (getType() != type) {
            throw new IllegalArgumentException("Wrong message type in source buffer!");
        }
    }

    protected ServerMessageBase() {

    }

    /**
     * Gets actual messageType. Should be implemented by subclass.
     * @return messageType.
     */
    public abstract ServerMessageType getType();

    /**
     * Subclass should implement making of ByteBuffer. Static make() is
     * provided to facilitate data writing.
     * @return ByteBuffer constructed and rewind.
     */
    public abstract ByteBuffer toBuffer();

    /**
     * Static method to get messageType from ByteBuffer without moving
     * buffer's position pointer.
     * @param buffer buffer to get messageType from
     * @return messageType
     */
    public static ServerMessageType getTypeFromBuffer(ByteBuffer buffer) {
        assert buffer != null;
        return ServerMessageType.values()[buffer.getInt(0)];
    }

    /**
     * This is static helper method to fill ByteBuffer with parameters provided.
     * Parameter messageType is determined from class name constants.
     * @param messageType mandatory messageType field
     * @param args        variable amount of arguments
     * @return ByteBuffer object with position = 0
     */
    protected static ByteBuffer make(ServerMessageType messageType, Object... args) {

        ByteBuffer temp = ByteBuffer.allocate(SEND_BUFFER_MAX_SIZE);

        temp.putInt(messageType.ordinal());

        for (Object arg : args) {

            String className = arg.getClass().getSimpleName();
            switch (className) {
                case "Long": {
                    temp.putLong((long) arg);
                    break;
                }

                case "Integer": {
                    temp.putInt((int) arg);
                    break;
                }

                case "Double": {
                    temp.putDouble((double) arg);
                    break;
                }

                case "Float": {
                    temp.putFloat((float) arg);
                    break;
                }

                case "Short": {
                    temp.putShort((short) arg);
                    break;
                }
                case "Byte": {
                    temp.put((byte) arg);
                    break;
                }

                case "Char": {
                    temp.putChar((char) arg);
                    break;
                }

                case "String": {
                    putString(temp, (String) arg);
                    break;
                }

                case "Boolean": {
                    byte bool = (byte) ((boolean) arg ? 1 : 0);
                    temp.put(bool);
                    break;
                }

                default:{
                    throw new IllegalArgumentException(String.format("Writing %s is not supported", arg.getClass()));
                }
            }
        }

        ByteBuffer result = ByteBuffer.allocate(temp.position());
        temp.flip();

        result.put(temp);
        result.flip();

        return result;
    }

    /**
     * Helper function to save string length before with string itself.
     * @param dst destination buffer
     * @param s string to save
     */
    private static void putString(ByteBuffer dst, String s) {
        byte[] bytes = s.getBytes();

        dst.putInt(bytes.length);
        dst.put(bytes, 0, bytes.length);
    }

    /**
     * Helper method to read string written with length byte by putString()
     * @param src source buffer to read from
     * @return string read
     */
    protected String getString(ByteBuffer src) {
        int size = src.getInt();
        byte[] bytes = new byte[size];
        try {
            src.get(bytes, 0, bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String(bytes);
    }
}
