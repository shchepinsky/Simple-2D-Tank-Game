package game.gui;

import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

/**
 * This menu appears when user clicks START SERVER from MainMenu
 */
public class MenuStartServer extends MenuBase {

    /**
     * Constructs overlay with given background. Background image is stretched
     * to size of parent pane.
     *
     * @param menuManager     menu manager to use
     * @param backgroundImage image to use as background, can be set to
     *                        null if no image required
     */
    public MenuStartServer(MenuManager menuManager, Image backgroundImage) {
        super(menuManager, backgroundImage);
    }

    private void startServerButtonAction(ActionEvent actionEvent) {
        getMenuManager().switchTo(getMenuManager().menuGameScreen);
        // multi-player start can be done here
    }

    @Override
    public void constructMenu() {
        Label titleLabel = new Label("Start Server");
        titleLabel.setTranslateY(-100);
        titleLabel.setId("title");

        // maximum player count dropdown list
        Label maxPlayersLabel = new Label("Server capacity");
        maxPlayersLabel.getStyleClass().add("label-bold");
        ComboBox<String> serverCapacityChoiceBox = new ComboBox<>();
        serverCapacityChoiceBox.getItems().addAll("2 players", "3 players", "4 players");
        serverCapacityChoiceBox.getSelectionModel().select(0);
        //^^^ maximum player count dropdown list

        // join server group
        CheckBox joinThisServerCheckBox = new CheckBox("Join this server after creation");
        joinThisServerCheckBox.setSelected(true);
        //^^^ join server group

        Label playerNameLabel = new Label("Your player name");
        TextField playerName = new TextField();
        playerName.setPromptText("Enter your name");
        playerName.disableProperty().bind(joinThisServerCheckBox.selectedProperty().not());

        // back and start buttons
        Button backButton = new Button("BACK");
        backButton.setOnAction(event -> getMenuManager().switchTo(getMenuManager().menuMain));

        Button startServerButton = new Button("START");

        // simple validation - start button is disabled if join checked but no name entered
        startServerButton.disableProperty().bind(
                joinThisServerCheckBox.selectedProperty().and(
                        playerName.textProperty().isEmpty()
                )
        );

        startServerButton.setOnAction(this::startServerButtonAction);

        // grid pane
        GridPane gridPane = new GridPane();
        gridPane.setTranslateY(titleLabel.getLayoutY() + titleLabel.getHeight());
        gridPane.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
        gridPane.setAlignment(Pos.CENTER);

        gridPane.addRow(0, maxPlayersLabel, serverCapacityChoiceBox);
        gridPane.addRow(1, joinThisServerCheckBox);
        gridPane.addRow(2, playerNameLabel, playerName);
        gridPane.addRow(3, backButton, startServerButton);

        GridPane.setColumnSpan(joinThisServerCheckBox, GridPane.REMAINING);
        GridPane.setHalignment(serverCapacityChoiceBox, HPos.RIGHT);

        gridPane.setId("shadowed");
        //^^^ grid pane layout

        getRoot().getChildren().addAll(titleLabel, gridPane);
    }

    @Override
    public void onKeyPressed(KeyEvent event) {

    }

    @Override
    public void onKeyReleased(KeyEvent event) {

    }
}
