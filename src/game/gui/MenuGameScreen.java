package game.gui;

import game.Engine;
import game.graphics.Renderer;
import game.client.InputFireOrder;
import game.client.InputMoveOrder;
import game.client.InputTurnOrder;
import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;

/**
 * This GUI overlay appears while playing game
 */
public class MenuGameScreen extends MenuBase {

    /**
     * Constructs overlay with given background. Background image is stretched
     * to size of parent pane.
     *
     * @param menuManager     menu manager to use
     * @param backgroundImage image to use as background, can be set to
     *                        null if no image required
     */
    public MenuGameScreen(MenuManager menuManager, Image backgroundImage) {
        super(menuManager, backgroundImage);
    }

    @Override
    public void constructMenu() {

        AnimationTimer renderTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isActive()) {
                    Renderer.render(getMenuManager().getGC());
                }
            }
        };

        renderTimer.start();
    }

    @Override
    public void onKeyPressed(KeyEvent event) {

        switch (event.getCode()) {
            case INSERT: {
                int botCount = Engine.getServer().getMaxBoxCount();
                Engine.getServer().setMaxBoxCount(botCount + 1);
                break;
            }
            case DELETE: {
                int botCount = Engine.getServer().getMaxBoxCount();
                Engine.getServer().setMaxBoxCount(botCount > 0 ? botCount - 1 : 0);
                break;
            }
            case BACK_QUOTE: {
                Renderer.INSTANCE.setConsoleTextVisible(!Renderer.INSTANCE.isConsoleTextVisible());
                break;
            }
            case A: {
                Renderer.INSTANCE.setAxisLinesVisible(!Renderer.INSTANCE.isAxisLinesVisible());
                break;
            }
            case B: {
                Renderer.INSTANCE.setBoundsVisible(!Renderer.INSTANCE.isBoundsVisible());
                break;
            }
            case D: {
                Engine.getClient().sendSelfDestructMessage();
                break;
            }
            case U: {
                Engine.getClient().setLocalUpdateEnabled(!Engine.getClient().isLocalUpdateEnabled());
                break;
            }
            case G: {
                Renderer.INSTANCE.setGridLinesVisible(!Renderer.INSTANCE.isGridLinesVisible());
                break;
            }
            case E: {
                Renderer.INSTANCE.setRenderEntityInfoVisible(!Renderer.INSTANCE.isRenderEntityInfoVisible());
                break;
            }
            case P:
            case PAUSE: {
                Engine.getClient().togglePause();
                break;
            }
            case ESCAPE: {
                getMenuManager().switchTo(getMenuManager().menuWhilePlaying);
                break;
            }
            case EQUALS: {
                Engine.getServer().getTimeFlow().makeTimeFaster();
                break;
            }
            case MINUS: {
                Engine.getServer().getTimeFlow().makeTimeSlower();
                break;
            }
            case SPACE: {
                Engine.getClient().setFireOrder(InputFireOrder.FIRE);
                break;
            }
            case UP: {
                if (!event.isShiftDown()) {
                    Engine.getClient().setMoveOrder(InputMoveOrder.FORWARD);
                    Engine.getClient().setTurnOrder(InputTurnOrder.NORTH);
                } else {
                    Engine.getClient().setMoveOrder(InputMoveOrder.REVERSE);
                    Engine.getClient().setTurnOrder(InputTurnOrder.SOUTH);
                }
                break;
            }
            case RIGHT: {
                if (!event.isShiftDown()) {
                    Engine.getClient().setMoveOrder(InputMoveOrder.FORWARD);
                    Engine.getClient().setTurnOrder(InputTurnOrder.EAST);
                } else {
                    Engine.getClient().setMoveOrder(InputMoveOrder.REVERSE);
                    Engine.getClient().setTurnOrder(InputTurnOrder.WEST);
                }
                break;
            }
            case DOWN: {
                if (!event.isShiftDown()) {
                    Engine.getClient().setMoveOrder(InputMoveOrder.FORWARD);
                    Engine.getClient().setTurnOrder(InputTurnOrder.SOUTH);
                } else {
                    Engine.getClient().setMoveOrder(InputMoveOrder.REVERSE);
                    Engine.getClient().setTurnOrder(InputTurnOrder.NORTH);
                }
                break;
            }
            case LEFT: {
                if (!event.isShiftDown()) {
                    Engine.getClient().setMoveOrder(InputMoveOrder.FORWARD);
                    Engine.getClient().setTurnOrder(InputTurnOrder.WEST);
                } else {
                    Engine.getClient().setMoveOrder(InputMoveOrder.REVERSE);
                    Engine.getClient().setTurnOrder(InputTurnOrder.EAST);
                }
                break;
            }
        }

        Engine.getClient().sendInputStateToServer();
    }

    @Override
    public void onKeyReleased(KeyEvent event) {
        switch (event.getCode()) {
            case SPACE: {
                if (Engine.getClient().getFireOrder() == InputFireOrder.FIRE) {
                    Engine.getClient().setFireOrder(InputFireOrder.NONE);
                }
                break;
            }
            case UP: {
                if (!event.isShiftDown()) {
                    if (Engine.getClient().getTurnOrder() == InputTurnOrder.NORTH) {
                        Engine.getClient().setMoveOrder(InputMoveOrder.STOP);
                    }
                } else {
                    if (Engine.getClient().getTurnOrder() == InputTurnOrder.SOUTH) {
                        Engine.getClient().setMoveOrder(InputMoveOrder.STOP);
                    }
                }
                break;
            }
            case DOWN: {
                if (!event.isShiftDown()) {
                    if (Engine.getClient().getTurnOrder() == InputTurnOrder.SOUTH) {
                        Engine.getClient().setMoveOrder(InputMoveOrder.STOP);
                    }
                } else {
                    if (Engine.getClient().getTurnOrder() == InputTurnOrder.NORTH) {
                        Engine.getClient().setMoveOrder(InputMoveOrder.STOP);
                    }
                }
                break;
            }
            case RIGHT: {
                if (!event.isShiftDown()) {
                    if (Engine.getClient().getTurnOrder() == InputTurnOrder.EAST) {
                        Engine.getClient().setMoveOrder(InputMoveOrder.STOP);
                    }
                } else {
                    if (Engine.getClient().getTurnOrder() == InputTurnOrder.WEST) {
                        Engine.getClient().setMoveOrder(InputMoveOrder.STOP);
                    }
                }
                break;
            }
            case LEFT: {
                if (!event.isShiftDown()) {
                    if (Engine.getClient().getTurnOrder() == InputTurnOrder.WEST) {
                        Engine.getClient().setMoveOrder(InputMoveOrder.STOP);
                    }
                } else {
                    if (Engine.getClient().getTurnOrder() == InputTurnOrder.EAST) {
                        Engine.getClient().setMoveOrder(InputMoveOrder.STOP);
                    }
                }
                break;
            }
            case SHIFT : {
                if (Engine.getClient().getMoveOrder() != InputMoveOrder.STOP)
                    Engine.getClient().setMoveOrder(InputMoveOrder.FORWARD);
            }
        }

        Engine.getClient().sendInputStateToServer();
    }
}
