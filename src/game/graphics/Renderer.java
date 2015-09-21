package game.graphics;

import com.sun.javafx.tk.FontLoader;
import game.Engine;
import game.Resources;
import game.util.RateCounter;
import game.world.BoardCell;
import game.world.Bounds;
import game.world.entities.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Renders visuals like board map, grid, entities and console info.
 */
public enum Renderer {
    INSTANCE;

    private final RateCounter renderRateCounter = new RateCounter();
    private boolean gridLinesVisible = true;
    private boolean axisLinesVisible = false;
    private boolean consoleTextVisible = false;
    private boolean waypointsVisible = true;                // waypoint are not transferred to client currently
    private boolean boundsVisible = false;
    private boolean renderEntityInfoVisible = false;

    public static void render(GraphicsContext gc) {
        // Different layers should be implemented to make some kind of Z-buffer:
        // 1. map
        // 2. path points
        // 3. entities
        // 4. explosions
        // 5. entity info & console
        //
        // ...but for simplicity it left as is :)

        ViewPort.INSTANCE.setBoundingBox(0, 0,
                Engine.getClient().getBoard().getWidthInPixels(),
                Engine.getClient().getBoard().getHeightInPixels()
        );

        ViewPort.INSTANCE.setViewSize(gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        renderMap(gc);

        if (INSTANCE.isGridLinesVisible()) renderGrid(gc);
        if (INSTANCE.isAxisLinesVisible()) renderAxisLines(gc);

        renderEntities(gc);

        if (INSTANCE.isConsoleTextVisible()) renderConsoleText(gc);

        INSTANCE.renderRateCounter.update();
    }

    private static void renderEntities(GraphicsContext gc) {
        // while debugging it is possible to render entities from local server - just change getClient() to getServer()
        Collection<Entity> entities = Engine.getClient().getBoard().getActiveEntitiesUnmodifiable();

        for (Entity entity : entities) {

            if (entity instanceof Positionable) {
                renderEntity(gc, (Positionable) entity);

                if (INSTANCE.isRenderEntityInfoVisible()) {
                    renderEntityInfo(gc, (Positionable) entity);
                }
            }

            if (INSTANCE.isBoundsVisible() && entity instanceof Collidable) {
                renderBounds(gc, ((Collidable) (entity)).getBounds());
            }

            if (entity instanceof Enemy && INSTANCE.isWaypointsVisible()) {
                Enemy enemy = (Enemy) entity;

                PathList<BoardCell> path = enemy.getPath();
                if (path != null && !path.isEmpty()) {
                    for (int i = 0; i < path.size(); i++) {

                        BoardCell cell = path.get(i);
                        double x = cell.getCenterX() - ViewPort.INSTANCE.getViewPosX();
                        double y = cell.getCenterY() - ViewPort.INSTANCE.getViewPosY();

                        ImageFrameInfo imageInfo = Resources.getFrameInfo("waypoint");

                        renderImageFrameCenteredRotated(gc, imageInfo, 0, x, y, 1.0, 0);
                    }
                }
            }

        }
    }

    private static void renderBounds(GraphicsContext gc, Bounds bounds) {
        if (bounds == null) return;                         // no bounds - nothing to render
        gc.save();

        double x = bounds.getX() - ViewPort.INSTANCE.getViewPosX();
        double y = bounds.getY() - ViewPort.INSTANCE.getViewPosY();
        double w = bounds.getWidth();
        double h = bounds.getHeight();

        gc.setStroke(Color.YELLOW);
        gc.setFill(Color.YELLOW.deriveColor(1.0, 1.0, 1.0, 0.5));

        gc.strokeRect(x, y, w, h);
        gc.fillRect(x, y, w, h);

        gc.restore();
    }

    private static void renderEntityInfo(GraphicsContext gc, Positionable entity) {

        if (!(entity instanceof Tank)) {
            return;
        }

        List<String> lines = new ArrayList<>();
        Tank m = (Tank) entity;

        BoardCell cell = m.getCell();
        String cs = cell == null ? "out of bounds" : String.format("row=%d, col=%d", cell.row, cell.col);

        lines.add(String.format("Key: %d, Hit Points: %d", m.getKey(), m.getHitPoints()));
        lines.add(String.format("Position: %s, %s", m.getPos(), cs));
        lines.add(String.format("Heading: %.1f Ordered: %.1f", m.getHeading(), m.getOrderedHeading()));
        lines.add(String.format("Speed: %.1f px/sec Ordered: %.1f px/sec", m.getMoveSpeed() * 1000, m.getOrderedSpeed() * 1000));

        gc.setFont(Font.font("Calibri", FontWeight.BOLD, 10));

        double tx = entity.getX() - ViewPort.INSTANCE.getViewPosX();
        double ty = entity.getY() - ViewPort.INSTANCE.getViewPosY();
        double tw = 2;
        double th = 0;

        for (String line : lines) {
            double lw = getLineWidth(gc, line);
            tw = lw > tw ? lw : tw;
            th += getLineHeight(gc);
        }

        gc.setFill(Color.BLACK.deriveColor(1, 1, 1, 0.5));
        gc.fillRect(tx, ty, tw, th);

        gc.setStroke(Color.BLACK.deriveColor(1, 1, 1, 0.5));
        gc.strokeRect(tx, ty, tw, th);


        gc.setFill(Color.YELLOW);

        for (String line : lines) {
            gc.fillText(line, tx + 1, getLineHeight(gc) * 0.75 + ty);
            ty = ty + getLineHeight(gc);
        }

    }

    private static void renderEntity(GraphicsContext gc, Positionable entity) {
        if (entity instanceof Visible) {
            Animation animation = ((Visible) entity).getAnimation();

            double x = entity.getX() - ViewPort.INSTANCE.getViewPosX();
            double y = entity.getY() - ViewPort.INSTANCE.getViewPosY();

            int frame = animation.getCurrentFrameIndex();

            if (entity instanceof Moveable) {
                Moveable moveable = (Moveable) entity;
                renderImageFrameCenteredRotated(gc, animation.getImageInfo(), frame, x, y, 1.0, moveable.getHeading());
            } else {
                renderImageFrameCenteredRotated(gc, animation.getImageInfo(), frame, x, y, 1.0, 0);
            }
        }
    }

    private static void renderGrid(GraphicsContext gc) {
        gc.save();

        final int cellSize = BoardCell.CELL_SIZE;

        final int viewPosX = (int) ViewPort.INSTANCE.getViewPosX();
        final int viewPosY = (int) ViewPort.INSTANCE.getViewPosY();

        final int viewWidth = (int) ViewPort.INSTANCE.getViewWidth();
        final int viewHeight = (int) ViewPort.INSTANCE.getViewHeight();

        final int colStart = viewPosX / cellSize;
        final int rowStart = viewPosY / cellSize;

        gc.setStroke(Color.MAROON.deriveColor(1.0, 1.0, 1.0, 1.0));
        gc.setLineDashes(4);

        final double lineOffset = 0.0;
        final int xOffset = -viewPosX % BoardCell.CELL_SIZE;
        final int yOffset = -viewPosY % BoardCell.CELL_SIZE;

        // horizontal lines, 0.5 shifts are needed to make line 1px wide, otherwise it it blurred due to the way
        // javafx coordinate system. It treats integer coordinates to be between pixels.
        for (int y = (rowStart * cellSize) - viewPosY; y < viewHeight; y += cellSize) {
            gc.strokeLine(xOffset, y + lineOffset, gc.getCanvas().getWidth(), y + lineOffset);
        }

        // vertical lines
        for (int x = (colStart * cellSize) - viewPosX; x < viewWidth; x += cellSize) {
            gc.strokeLine(x + lineOffset, yOffset, x + lineOffset, gc.getCanvas().getHeight());
        }

        gc.restore();
    }

    private static void renderAxisLines(GraphicsContext gc) {
        gc.save();

        gc.setStroke(Color.YELLOW);

        double vx = ViewPort.INSTANCE.getViewCenterPosX();
        double vy = ViewPort.INSTANCE.getViewCenterPosY();
        String axisLabel = String.format("ViewPort: %.2fx%.2f", vx, vy);

        double cx = 0.5 + gc.getCanvas().getWidth() / 2;
        double cy = 0.5 + gc.getCanvas().getHeight() / 2;
        gc.strokeLine(0, cy, cx * 2, cy);
        gc.strokeLine(cx, 0, cx, cy * 2);


        gc.setFill(Color.YELLOW);
        gc.fillText(axisLabel, cx, cy + getLineHeight(gc));

        gc.restore();
    }

    private static void renderConsoleText(GraphicsContext gc) {

        List<String> console = new ArrayList<>();
        console.add(String.format("Graphics Render rate: %d fps", INSTANCE.renderRateCounter.getRate()));

        if (Engine.getServer() != null && Engine.getServer().getTimeFlow() != null) {
            console.add("");

            console.add(String.format(
                    "SERVER: loop rate = %d/s, network state send rate = %d/s",
                    Engine.getServer().getLoopRate(),
                    Engine.getServer().getNetworkStateSendRate()
            ));

            console.add("SERVER: uptime: " + formatUptimeString(Engine.getServer().uptime()));

            console.add(String.format(
                    "SERVER: logic is %s, update() rate = %d/s, time speed = %.2f, logic time %.2f",
                    Engine.getServer().getTimeFlow().isPaused() ? "Paused" : "Running",
                    Engine.getServer().getTimeFlow().getUpdateRate(),
                    Engine.getServer().getTimeFlow().getSpeed(),
                    Engine.getServer().getLogicTime()
            ));

            double bytes = Engine.getServer().getBytesSentPerSecond();
            String s = String.format("%.0f bytes/s", bytes);
            if (bytes > 1_000_000) s = String.format("%.2f MB/s", bytes / 1_000_000.0);
            if (bytes > 1_000) s = String.format("%.2f KB/s", bytes / 1_000.0);
            console.add(String.format("SERVER: network output rate = %s", s));

            int active = Engine.getServer().getBoard().getActiveEntitiesUnmodifiable().size();
            int inactive = Engine.getServer().getBoard().getInactiveEntitiesUnmodifiable().size();
            int created = Engine.getServer().getBoard().getNewEntitiesUnmodifiable().size();
            console.add(String.format("SERVER: total entities: %d", active + inactive));
            console.add(String.format("SERVER: active = %d, inactive = %d, created = %d", active, inactive, created));
            console.add(String.format("SERVER: AI bot players = %d", Engine.getServer().getMaxBoxCount()));
        }

        if (Engine.getClient() != null) {
            console.add("");

            console.add(String.format(
                    "CLIENT: loop rate = %d/s, update prediction is : %s",
                    Engine.getClient().getLoopRate(),
                    Engine.getClient().isLocalUpdateEnabled() ? "enabled" : "disabled"
            ));

            console.add("CLIENT: uptime: " + formatUptimeString(Engine.getClient().uptime()));

            console.add(String.format("CLIENT: turn order = %s move order = %s action order = %s",
                    Engine.getClient().getTurnOrder(),
                    Engine.getClient().getMoveOrder(),
                    Engine.getClient().getFireOrder()
            ));

            int active = Engine.getClient().getBoard().getActiveEntitiesUnmodifiable().size();
            int inactive = Engine.getClient().getBoard().getInactiveEntitiesUnmodifiable().size();
            console.add(String.format("CLIENT: total entities: %d", active + inactive));
            console.add(String.format("CLIENT: active = %d inactive = %d", active, inactive));
        }

        gc.save();

        gc.setFont(Font.font("Calibri", FontWeight.BOLD, 10));
        gc.setFill(Color.YELLOW);

        double lineHeight = getLineHeight(gc);
        double x1 = gc.getCanvas().getWidth();
        double y1 = lineHeight * console.size();
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(0, 0, x1, y1);

        gc.setStroke(Color.BLACK);
        gc.strokeLine(0, y1, x1, y1);

        int y = 0;
        gc.setFill(Color.YELLOW);
        for (String s : console) {
            y += lineHeight;
            gc.fillText(s, 2, y);
        }

        gc.restore();
    }

    private static String formatUptimeString(long ms) {
        int day = (int) (ms / (24 * 60 * 60 * 1000.0));
        int hr = (int) (ms / (60 * 60 * 1000.0) - day * 24);
        int min = (int) (ms / (60 * 1000.0) - day * 24 - hr * 60);
        float sec = (float) (ms - day * 24 * 60 * 1000 - hr * 60 * 60 * 1000 - min * 60 * 1000) / 1000;
        return String.format("%02d days, %02d hours, %02d minutes, %02.2f seconds", day, hr, min, sec);
    }

    private static float getLineWidth(GraphicsContext gc, String text) {
        FontLoader fl = com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader();
        return fl.getFontMetrics(gc.getFont()).computeStringWidth(text);
    }

    private static float getLineHeight(GraphicsContext gc) {
        FontLoader fl = com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader();
        return fl.getFontMetrics(gc.getFont()).getLineHeight();
    }

    private static void renderMap(GraphicsContext gc) {
        if (Engine.getClient().getBoard() == null) {
            return;
        }

        int cellSize = BoardCell.CELL_SIZE;

        int viewPosX = (int) ViewPort.INSTANCE.getViewPosX();
        int viewPosY = (int) ViewPort.INSTANCE.getViewPosY();

        int viewWidth = (int) gc.getCanvas().getWidth();
        int viewHeight = (int) gc.getCanvas().getHeight();

        int row = viewPosY / cellSize;

        for (int y = (row * cellSize) - viewPosY; y < viewHeight; y += cellSize) {

            int col = viewPosX / cellSize;
            for (int x = (col * cellSize) - viewPosX; x < viewWidth; x += cellSize) {

                BoardCell cell = Engine.getClient().getBoard().getCell(row, col);

                int imageFrameIndex = cell.ground.frameIndex;

                renderImageFrame(gc, cell.ground.imageFrame, imageFrameIndex, x, y, 1.0);

                if (cell.overlay != null && cell.overlay.isVisible()) {
                    imageFrameIndex = cell.overlay.frameIndex;
                    renderImageFrame(gc, cell.overlay.imageFrame, imageFrameIndex, x, y, 1.0);
                }

                if (INSTANCE.isBoundsVisible()) renderBounds(gc, cell.getObstacleBounds());

                col++;
            }

            row++;
        }
    }

    private static void renderImageFrameCenteredRotated(GraphicsContext gc, ImageFrameInfo frameInfo, int index, double dx, double dy, double scale, double angle) {
        gc.save();
        gc.translate(dx, dy);
        gc.rotate(angle);
        renderImageFrame(gc,
                frameInfo,
                index,
                -(frameInfo.getFrameWidth() * scale) / 2,
                -(frameInfo.getFrameHeight() * scale) / 2,
                scale);

        gc.restore();
    }

    private static void renderAnimation(GraphicsContext gc, Animation animation, double dx, double dy, double scale) {
        renderImageFrame(gc, animation.getImageInfo(), animation.getCurrentFrameIndex(), dx, dy, scale);
    }

    private static void renderImageFrame(GraphicsContext gc, ImageFrameInfo frameInfo, int index, double dx, double dy, double scale) {
        Image image = Resources.getImage(frameInfo.getImageID());

        int sx = frameInfo.getFrameX(index);
        int sy = frameInfo.getFrameY(index);
        int sw = frameInfo.getFrameWidth();
        int sh = frameInfo.getFrameHeight();

        double dw = sw * scale;
        double dh = sh * scale;

        gc.drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);
    }

    private boolean isWaypointsVisible() {
        return waypointsVisible;
    }

    public boolean isConsoleTextVisible() {
        return consoleTextVisible;
    }

    public void setConsoleTextVisible(boolean consoleTextVisible) {
        this.consoleTextVisible = consoleTextVisible;
    }

    public boolean isGridLinesVisible() {
        return gridLinesVisible;
    }

    public void setGridLinesVisible(boolean gridLinesVisible) {
        this.gridLinesVisible = gridLinesVisible;
    }

    public boolean isAxisLinesVisible() {
        return axisLinesVisible;
    }

    public void setAxisLinesVisible(boolean axisLinesVisible) {
        this.axisLinesVisible = axisLinesVisible;
    }

    public boolean isRenderEntityInfoVisible() {
        return renderEntityInfoVisible;
    }

    public void setRenderEntityInfoVisible(boolean renderEntityInfoVisible) {
        this.renderEntityInfoVisible = renderEntityInfoVisible;
    }

    public boolean isBoundsVisible() {
        return boundsVisible;
    }

    public void setBoundsVisible(boolean boundsVisible) {
        this.boundsVisible = boundsVisible;
    }
}
