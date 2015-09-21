package game.gui;

import game.Engine;
import game.Resources;
import game.server.ServerTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static game.util.Debug.log;

/**
 * This menu appears after application started
 */
public class MenuMain extends MenuBase {

    private static final String DEFAULT_PLAYER_NAME          = "player";
    private static final String DEFAULT_MAP_RESOURCE_NAME    = "/map.txt";

    /**
     * Constructs overlay with given background. Background image is stretched
     * to size of parent pane.
     *
     * @param menuManager     menu manager to use
     * @param backgroundImage image to use as background, can be set to
     *                        null if no image required
     */
    public MenuMain(MenuManager menuManager, Image backgroundImage) {
        super(menuManager, backgroundImage);
    }

    @Override
    public void constructMenu() {
        Button startGameButton = new Button("SINGLE FREE FOR ALL");
        startGameButton.setOnAction(this::startGameButtonAction);

        Button exitButton = new Button("EXIT");
        exitButton.setOnAction(event -> getMenuManager().getStage().close());

        Label titleLabel = new Label("Yet Another Tank Game");
        titleLabel.setTranslateY(-100);
        titleLabel.setId("title");

        VBox buttonVBox = new VBox();
        buttonVBox.setTranslateY(titleLabel.getLayoutY() + titleLabel.getHeight());
        buttonVBox.setAlignment(Pos.CENTER);
        buttonVBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
        buttonVBox.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
        buttonVBox.getChildren().addAll(startGameButton, exitButton);
        buttonVBox.setId("shadowed");

        getRoot().getChildren().addAll(titleLabel, buttonVBox);
    }

    private void startGameButtonAction(ActionEvent event) {
        try {
            SocketAddress serverAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), ServerTask.DEFAULT_UDP_PORT);

            MenuConnecting menuConnecting = new MenuConnecting(getMenuManager(), Resources.getImage("menu_ua"));
            menuConnecting.setServerLabelText(serverAddress.toString());
            getMenuManager().switchTo(menuConnecting);

            Engine.startLoopback(DEFAULT_PLAYER_NAME, DEFAULT_MAP_RESOURCE_NAME);
            Engine.getServer().setMaxBoxCount(4);

            Engine.getClient().onTryToConnectProgress = progress -> Platform.runLater( () -> {
                menuConnecting.setProgress(progress);
                menuConnecting.setTitleLabelText("connecting to server: ");
                menuConnecting.setServerLabelText(serverAddress.toString());
            });

            Engine.getClient().onFetchBoardProgress = progress -> Platform.runLater( () -> {
                menuConnecting.setProgress(progress);
                menuConnecting.setTitleLabelText("fetching map from server: ");
                menuConnecting.setServerLabelText(serverAddress.toString());
            });

            Engine.getClient().onTryToConnectSuccess = message -> Platform.runLater(() -> {
                log(message);
                getMenuManager().switchTo(getMenuManager().menuGameScreen);
            });

            Engine.getClient().onTryToConnectFailure = message -> Platform.runLater(() -> {
                Engine.exit();
                getMenuManager().switchTo((getMenuManager().menuMain));
            });

            Engine.getClient().onConnectionFinished = nothing -> Platform.runLater(() -> {
                Engine.exit();
                getMenuManager().switchTo(getMenuManager().menuMain);
            });

        } catch (Exception e) {
            getMenuManager().switchTo(getMenuManager().menuMain);
            e.printStackTrace();
        }
    }

    @Override
    public void onKeyPressed(KeyEvent event) {

    }

    @Override
    public void onKeyReleased(KeyEvent event) {

    }
}
