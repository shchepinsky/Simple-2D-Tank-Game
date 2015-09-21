package game.util;

/**
 * This class provides facility to count rate of update() calls
 * Regardless of calculation interval it is always measured in calls per second
 */
public class RateCounter {
    private static final int DEFAULT_REFRESH_INTERVAL = 1000;
    private long counter;
    private long rate;
    private long lastCalcTime;
    private long lastFrameTime;
    private long deltaBetweenUpdateCalls;
    private long refreshInterval;

    public long getRefreshInterval() {
        return refreshInterval;
    }

    private void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    /**
     * Constructs with default refresh interval of 1000 ms.
     */
    public RateCounter() {
        this(DEFAULT_REFRESH_INTERVAL);
    }

    /**
     * Constructor with arbitrary refresh interval.
     * @param refreshInterval rate refresh interval in milliseconds.
     */
    private RateCounter(long refreshInterval) {
        setRefreshInterval(refreshInterval);
    }

    /**
     * Returns time elapsed since last update() call.
     * @return time in milliseconds.
     */
    public long getTimeBetweenUpdates() {
        return deltaBetweenUpdateCalls;
    }

    /**
     * This method increments rate counter by one resulting in calculating <code>update()</code> call rate.
     * <code>refreshInterval</code> period of time.
     * Call this method after iteration you want to count is completed.
     */
    public void update() {
        update(1);
    }

    /**
     * This method increments counter by arbitrary amount and calculates average.
     * <code>refreshInterval</code> period of time.
     * Call this method with each amount you need to calculate average from.
     * @param amount amount to sum in calculation.
     */
    public void update(int amount) {
        long currentTime = System.nanoTime() / (1000 * 1000);

        if (lastFrameTime == 0) {
            lastFrameTime = currentTime;
        }

        long deltaBetweenRateUpdate = currentTime - lastCalcTime;

        deltaBetweenUpdateCalls = currentTime - lastFrameTime;
        lastFrameTime = currentTime;

        // calculate at least once per second
        if (deltaBetweenRateUpdate >= refreshInterval) {
            rate = (counter * refreshInterval) / deltaBetweenRateUpdate;

            counter = 0;                        // and reset counter counter

            lastCalcTime = currentTime;         // remember time of calculation
        }

        counter += amount;
    }

    /**
     * Getter for rate property.
     * @return amount of update() calls per second.
     */
    public long getRate() {
        return rate;
    }

}
