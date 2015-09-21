package game.graphics;

import game.world.BoardCell;

/**
 * Manages virtual view port and it's position inside bounds.
 */
public enum ViewPort {
    INSTANCE;

    private volatile double viewPosX;
    private volatile double viewPosY;

    private volatile double viewWidth;
    private volatile double viewHeight;
    private final double scrollSpeed = BoardCell.CELL_SIZE;

    private volatile double minViewPosX;
    private volatile double maxViewPosX;

    private volatile double minViewPosY;
    private volatile double maxViewPosY;

    public synchronized void initViewPort(double viewWidth, double viewHeight, double x1, double y1, double x2, double y2) {
        setBoundingBox(x1,y1,x2,y2);

        setViewWidth(viewWidth);
        setViewHeight(viewHeight);
    }

    public synchronized void setBoundingBox(double x1, double y1, double x2, double y2) {
        minViewPosX = x1;
        minViewPosY = y1;

        maxViewPosX = x2 - getViewWidth();
        maxViewPosY = y2 - getViewHeight();
    }

    private synchronized void locateTo(double x, double y) {
        setViewPosX(x);
        setViewPosY(y);
        keepInBounds();
    }

    private void keepInBounds() {
        if (getViewPosX() > maxViewPosX) setViewPosX(maxViewPosX);
        if (getViewPosX() < minViewPosX) setViewPosX(minViewPosX);
        if (getViewPosY() > maxViewPosY) setViewPosY(maxViewPosY);
        if (getViewPosY() < minViewPosY) setViewPosY(minViewPosY);
    }

    public synchronized void scrollRight() {
        setViewPosX(getViewPosX() + scrollSpeed);
        keepInBounds();
    }

    public synchronized void scrollLeft() {
        setViewPosX(getViewPosX() - scrollSpeed);
        keepInBounds();
    }

    public synchronized void scrollUp() {
        setViewPosY(getViewPosY() - scrollSpeed);
        keepInBounds();
    }

    public synchronized void scrollDown() {
        setViewPosY(getViewPosY() + scrollSpeed);
        keepInBounds();
    }

    public synchronized double  getViewPosX() {
        return viewPosX;
    }

    private synchronized void setViewPosX(double viewPosX) {
        this.viewPosX = viewPosX;
    }

    public synchronized double getViewPosY() {
        return viewPosY;
    }

    private synchronized void setViewPosY(double yPos) {
        this.viewPosY = yPos;
    }

    public synchronized double getViewWidth() {
        return viewWidth;
    }

    private synchronized void setViewWidth(double viewWidth) {
        maxViewPosX = maxViewPosX + getViewWidth() - viewWidth;     // if view viewWidth changes - update max x limit
        this.viewWidth = viewWidth;
    }

    public synchronized double getViewHeight() {
        return viewHeight;
    }

    private synchronized void setViewHeight(double viewHeight) {
        maxViewPosY = maxViewPosY + getViewHeight() - viewHeight;   // if view viewHeight changes - update max y limit
        this.viewHeight = viewHeight;
    }

    public synchronized void setViewSize(double width, double height) {
        setViewWidth(width);
        setViewHeight(height);
    }

    public synchronized void centerOnPos(double viewPosX, double viewPosY) {
        locateTo(viewPosX - getViewWidth() / 2, viewPosY - getViewHeight() / 2);
    }

    public synchronized double getViewCenterPosX() {
        return getViewPosX() + getViewWidth() / 2;
    }

    public synchronized double getViewCenterPosY() {
        return getViewPosY() + getViewHeight() / 2;
    }
}
