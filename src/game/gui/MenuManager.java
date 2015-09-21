package game.gui;

import game.Resources;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.HashMap;

import static game.util.Debug.log;

/**
 * This central class that manages GUI overlays, their injection to
 * application's main stage and switching between overlays.
 */
public class MenuManager {
    private Stage stage;
    private Pane rootPane;
    private GraphicsContext gc;
    private Canvas canvas;
    private HashMap<KeyCode, Boolean> keyState;
    private MenuBase menuActive;

    public MenuBase menuMain;
    private MenuBase menuStartServer;
    public MenuGameScreen menuGameScreen;
    public MenuWhilePlaying menuWhilePlaying;

    /**
     * Create MenuManager for usage with application.
     * @param primaryStage stage to use when switching overlays.
     * @param width window client area width.
     * @param height window client area height.
     */
    public MenuManager(Stage primaryStage, int width, int height) {
        assert primaryStage != null : "primaryStage can not be null!";

        rootPane = new StackPane();
        canvas = new Canvas();
        gc = canvas.getGraphicsContext2D();
        keyState = new HashMap<>();

        menuMain = new MenuMain(this, Resources.getImage("menu_bg"));
        menuStartServer = new MenuStartServer(this, Resources.getImage("menu_bg"));
        menuGameScreen = new MenuGameScreen(this, null);
        menuWhilePlaying = new MenuWhilePlaying(this, Resources.getImage("menu_bg"));

        canvas.widthProperty().bind(rootPane.widthProperty());
        canvas.heightProperty().bind(rootPane.heightProperty());

        setStage(primaryStage);
        getStage().setScene(new Scene(getRoot(), width, height));

        getStage().getScene().getStylesheets().add("/style.css");
        getStage().setResizable(false); // set stage as non-resizable, sizeToScene should be called
        getStage().sizeToScene();       // to avoid weird padding that appears (framework bug?)
        getStage().setTitle("Yet Another Tank Game");
        getStage().show();

        log("scene size: %s x %s", getRoot().getScene().getWidth(), getRoot().getScene().getHeight());

        // register global keyboard listeners
        getStage().getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        getStage().getScene().addEventFilter(KeyEvent.KEY_RELEASED, this::handleKeyReleased);

        switchTo(menuMain);
    }

    private void handleKeyPressed(KeyEvent keyEvent) {
        if (!keyState.containsKey(keyEvent.getCode())) {
            keyState.put(keyEvent.getCode(), false);
        }

        // check if key was in unpressed state and openFire event only if state changes
        if (!keyState.get(keyEvent.getCode())) {
            keyState.put(keyEvent.getCode(), true);

            if (menuActive != null) {
                menuActive.onKeyPressed(keyEvent);
            }
        }
    }

    private void handleKeyReleased(KeyEvent keyEvent) {
        // check if key was in pressed state and openFire event only if state changes
        if (!keyState.containsKey(keyEvent.getCode()) || keyState.get(keyEvent.getCode())) {
            keyState.put(keyEvent.getCode(), false);
        }

        if (menuActive != null) {
            menuActive.onKeyReleased(keyEvent);
        }
    }

    public Stage getStage() {
        return stage;
    }

    private void setStage(Stage stage) {
        assert stage != null : "Stage can not be null";
        this.stage = stage;
    }

    public Pane getRoot() { return rootPane; }

    public GraphicsContext getGC() { return gc; }


    /**
     * Switches MenuManager to another overlay.
     *
     * @param overlay GUI controls hierarchy to activate.
     */
    public void switchTo(MenuBase overlay) {
        rootPane.getChildren().clear();
        rootPane.getChildren().add(canvas);

        if (menuActive != null) menuActive.setActive(false);

        // set new active overlay
        menuActive = overlay;

        if (menuActive != null) {
            rootPane.getChildren().add(menuActive.getRoot());
            menuActive.setActive(true);
        }
    }

}
