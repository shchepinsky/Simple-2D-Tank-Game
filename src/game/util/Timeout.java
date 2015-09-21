package game.util;

/**
 * Utility class used to check for timeout happened.
 */
public class Timeout {
    private long timeoutTime;
    private long startTime;
    private long runningTime;

    /**
     * Constructs timeout class
     * @param timeoutTime amount of time in milliseconds before timeout
     */
    public Timeout(final long timeoutTime) {
        if (timeoutTime <= 0.0) {
            throw new IllegalArgumentException("Timeout must be greater than zero!");
        }

        this.timeoutTime = timeoutTime;
        this.startTime = System.currentTimeMillis();
    }

    public void reset() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Checks for timeout occurrence by using system time source.
     * @return true if amount of time passed is greater than timeout or false otherwise.
     */
    public boolean occurred() {
        return occurred(System.currentTimeMillis());
    }

    /**
     * Checks for timeout occurrence by using external time source.
     * @param nowTimeMillis current time in milliseconds.
     * @return true if amount of time passed is greater than timeout or false otherwise.
     */
    private boolean occurred(long nowTimeMillis) {
        runningTime = nowTimeMillis - startTime;
        return runningTime > timeoutTime;
    }

    public Double getProgress() {
        return Math.min(runningTime / timeoutTime, 1.0);
    }

    public double getTimeoutTime() {
        return timeoutTime;
    }
}
