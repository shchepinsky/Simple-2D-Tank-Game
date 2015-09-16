package game.util;

import game.util.RateCounter;

/**
 * This class manages game time flow.
 */
public class TimeFlow {

    private final RateCounter updateRate = new RateCounter();
    private boolean paused;
    private double speed = 1.0;
    private double last;
    private double time;

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public double getSpeed() {
        return speed;
    }

    private void setSpeed(double speed) {
        speed = Math.min(speed, 1.6);
        speed = Math.max(speed, 0.2);

        this.speed = speed;
    }

    public long getUpdateRate() {
        return (isPaused() ? 0 : updateRate.getRate());
    }

    /**
     * Advances virtual time accordingly to speed set.
     */
    public void update() {
        updateRate.update();                                //

        long now = systemTime();
        if (last == 0) {                                    // first-time condition: last time initialized to now
            last = now;
        }

        double elapsed = (now - last) * speed;              // calculate real time delta
        time = time + elapsed;                              // advance virtual time accordingly to speed multiplier
        last = now;                                         // remember last time of update

    }

    /**
     * Returns current virtual time.
     * @return virtual time in milliseconds.
     */
    public double time() {
        return time;
    }

    /**
     * Convenience methods to make world's relative time to run faster.
     */
    public void makeTimeFaster() {
        setSpeed(getSpeed() + 0.1);
    }

    /**
     * Convenience methods to make world's relative time to run slower.
     */
    public void makeTimeSlower() {
        setSpeed(getSpeed() - 0.1);
    }

    /**
     * Gets time source.
     * @return system time in milliseconds
     */
    public static long systemTime() {
        return System.nanoTime()/(1000*1000);
    }

    public void sync(double time, double speed) {
        this.time = time;
        this.speed = speed;
    }
}
