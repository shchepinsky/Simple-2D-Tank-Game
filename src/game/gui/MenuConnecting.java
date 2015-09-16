package game.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class MenuConnecting extends MenuBase {
    private Label titleLabel;
    private Label serverLabel;
    private ProgressBar progressBar;
    /**
     * Constructs overlay with given background. Background image is stretched
     * to size of parent pane.
     *
     * @param menuManager     menu manager to use
     * @param backgroundImage image to use as background, can be set to
     */
    public MenuConnecting(MenuManager menuManager, Image backgroundImage) {
        super(menuManager, backgroundImage);
    }

    @Override
    public void constructMenu() {
        titleLabel = new Label();
        titleLabel.setId("title-small");

        serverLabel = new Label();
        serverLabel.setId("title-small");

        progressBar = new ProgressBar();
        progressBar.setProgress(0);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(titleLabel, serverLabel, progressBar);
        vbox.setAlignment(Pos.CENTER);

        getRoot().getChildren().add(vbox);
    }

    /**
     * Set's ProgressBar progress value.
     * @param progress progress value, should be in range between 0.0 and 1.0.
     */
    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

    public void setServerLabelText(String serverLabelText) {
        serverLabel.setText(serverLabelText);
    }

    public void setTitleLabelText(String titleLabelText) {
        titleLabel.setText(titleLabelText);
    }

    @Override
    public void onKeyPressed(KeyEvent event) {

    }

    @Override
    public void onKeyReleased(KeyEvent event) {

    }
}
