package game.util;

public class Debug {

    private Debug() {}

    private static String getCaller() {
        return Thread.currentThread().getStackTrace()[3].toString();
    }

    public static void log() {
        String caller = getCaller();

        System.out.println(caller);
    }

    public static void log(String message) {
        String caller = getCaller();

        long time = System.nanoTime();

        System.out.println(caller + "[ " + time + " ] " + message);
    }

    public static void log(String format, Object... args) {
        String caller = getCaller();

        System.out.println(caller + ": " + String.format(format, args));
    }

}

