package game.util;

/**
 * Helper class for logging to console.
 */
public class Debug {

    private Debug() {}

    private static String caller() {
        return Thread.currentThread().getStackTrace()[3].toString();
    }

    private static String time() {
        return String.format("[%d]: ", System.currentTimeMillis());
    }

    public static void log() {
        System.out.println(caller() + time());
    }

    public static void log(String message) {
        System.out.println(caller() + time() + message);
    }

    public static void log(String format, Object... args) {
        System.out.println(caller() + time() + String.format(format, args));
    }

}

