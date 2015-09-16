package game.client.messages;

import java.nio.ByteBuffer;

/**
 * Common ancestor for all client messages.
 * <p>
 * This is abstract class that provides static utility methods to simplify
 * buffer writing and reading for subclasses.
 * <p>
 * Static make() method is used by subclasses to write their data.
 * <p>
 * Subclasses should carefully check that the amount and order of fields
 * read are equal to written.
 */
public abstract class ClientMessageBase {
    public static final int SEND_BUFFER_MAX_SIZE = 500;

    /**
     * Constructs message from ByteBuffer.
     *
     * @param srcBuffer source ByteBuffer
     */
    public ClientMessageBase(ByteBuffer srcBuffer) {
        assert srcBuffer != null : "Source buffer can't be null!";

        // read from start, in case caller forgot to rewind
        srcBuffer.rewind();

        // check if subclass messageType matches messageType in buffer
        ClientMessageType type = ClientMessageType.values()[srcBuffer.getInt()];
        if (getType() != type) {
            throw new IllegalArgumentException("Wrong message type in source buffer!");
        }

    }

    ClientMessageBase() {
    }

    /**
     * Static method to get messageType from ByteBuffer without moving
     * buffer's position pointer.
     *
     * @param buffer buffer to get messageType from
     * @return messageType
     */
    public static ClientMessageType getTypeFromBuffer(ByteBuffer buffer) {
        assert buffer != null;
        return ClientMessageType.values()[buffer.getInt(0)];
    }

    /**
     * This is static helper method to fill ByteBuffer with parameters provided.
     * Parameter messageType is determined from class name constants.
     *
     * @param messageType mandatory messageType field
     * @param args        variable amount of arguments
     * @return ByteBuffer object with position = 0
     */
    protected static ByteBuffer make(ClientMessageType messageType, Object... args) {

        ByteBuffer temp = ByteBuffer.allocate(SEND_BUFFER_MAX_SIZE);
        ;

        temp.putInt(messageType.ordinal());

        for (Object o : args) {

            String className = o.getClass().getSimpleName();
            switch (className) {
                case "Long": {
                    temp.putLong((long) o);
                    break;
                }

                case "Integer": {
                    temp.putInt((int) o);
                    break;
                }

                case "Double": {
                    temp.putDouble((double) o);
                    break;
                }

                case "Float": {
                    temp.putFloat((float) o);
                    break;
                }

                case "Byte": {
                    temp.put((byte) o);
                    break;
                }

                case "Char": {
                    temp.putChar((char) o);
                    break;
                }

                case "String": {
                    putString(temp, (String) o);
                    break;
                }

                default: {
                    throw new IllegalArgumentException(String.format("Writing %s is not supported", o.getClass()));
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
     *
     * @param dst destination buffer
     * @param s   string to save
     */
    private static void putString(ByteBuffer dst, String s) {
        byte[] bytes = s.getBytes();

        dst.putInt(bytes.length);
        dst.put(bytes, 0, bytes.length);
    }

    /**
     * Gets actual messageType. Should be implemented by subclass.
     *
     * @return messageType.
     */
    public abstract ClientMessageType getType();

    /**
     * Subclass should implement making of ByteBuffer by using static <code>make()</code>.
     *
     * @return ByteBuffer constructed and rewind.
     */
    public abstract ByteBuffer toBuffer();

    /**
     * Helper method to read string written with length byte by putString()
     *
     * @param src source buffer to read from
     * @return string read
     */
    protected String getString(ByteBuffer src) {
        int size = src.getInt();
        byte[] bytes = new byte[size];

        src.get(bytes, 0, bytes.length);
        return new String(bytes);
    }
}
