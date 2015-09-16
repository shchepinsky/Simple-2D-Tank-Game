package game.graphics;

public class Animation {
    private final ImageFrameInfo imageInfo;
    private double delay;
    private int currentFrameIndex;
    private boolean paused;
    private boolean completed;

    public Animation(ImageFrameInfo imageFrameInfo) {
        this.imageInfo = imageFrameInfo;
    }

    public ImageFrameInfo getImageInfo() {
        return imageInfo;
    }

    public boolean isCompleted() {
        return completed;
    }

    public synchronized int getCurrentFrameIndex() {
        return currentFrameIndex;
    }

    public synchronized void setCurrentFrameIndex(int currentFrameIndex) {
        this.currentFrameIndex = currentFrameIndex;
    }

    public synchronized void update() {

        if (isPaused()) return;

        delay--;

        if (delay < 0) {
            setCurrentFrameIndex(getCurrentFrameIndex() + 1);
            delay = delay + imageInfo.getAnimationFrameDelay();
        }

        if (currentFrameIndex >= imageInfo.getAnimationFrameCount()) {
            if (imageInfo.isAnimationLooped()) {
                currentFrameIndex = 0;
            } else {
                completed = true;
            }
        }

    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
