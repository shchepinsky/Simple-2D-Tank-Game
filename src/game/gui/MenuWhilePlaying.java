package game.gui;

import game.Engine;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

/**
 * This menu appears while user invokes menu during game
 */
public class MenuWhilePlaying extends MenuBase {

    /**
     * Constructs overlay with given background. Background image is stretched
     * to size of parent pane.
     *
     * @param menuManager     menu manager to use
     * @param backgroundImage image to use as background, can be set to
     */
    public MenuWhilePlaying(MenuManager menuManager, Image backgroundImage) {
        super(menuManager, backgroundImage);
    }

    @Override
    public void constructMenu() {
        Label titleLabel = new Label("menu");
        titleLabel.setTranslateY(-100);
        titleLabel.setId("title");

        Button resumeGameButton = new Button("RESUME PLAYING");
        resumeGameButton.setOnAction(event -> {
            // switch back to game gui
            getMenuManager().switchTo(getMenuManager().menuGameScreen);
        });

        Button backToMenuButton = new Button("QUIT TO MAIN MENU");
        backToMenuButton.setOnAction( event -> {
            Engine.exit();
            getMenuManager().switchTo(getMenuManager().menuMain);
        });

        Button exitGameButton = new Button("EXIT GAME");
        exitGameButton.setOnAction(event -> {
            Engine.exit();
            getMenuManager().getStage().close();
        });

        VBox buttonVBox = new VBox();
        buttonVBox.setTranslateY(titleLabel.getLayoutY() + titleLabel.getHeight());
        buttonVBox.setAlignment(Pos.CENTER);
        buttonVBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
        buttonVBox.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
        buttonVBox.getChildren().addAll(resumeGameButton, backToMenuButton, exitGameButton);
        buttonVBox.setId("shadowed");

        getRoot().getChildren().addAll(titleLabel, buttonVBox);
    }

    @Override
    public void onKeyPressed(KeyEvent event) {

    }

    @Override
    public void onKeyReleased(KeyEvent event) {

    }
}
