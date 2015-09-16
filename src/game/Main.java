package game;

import game.gui.MenuManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    private final static int VIEW_WIDTH = 640;
    private final static int VIEW_HEIGHT = 480;

    public static void main(String[] args) {
        launch(args);
        Engine.exit();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        new MenuManager(primaryStage, VIEW_WIDTH, VIEW_HEIGHT);
    }


}
