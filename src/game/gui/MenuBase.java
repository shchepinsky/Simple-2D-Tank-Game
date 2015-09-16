package game.gui;

import static game.util.Debug.*;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;

/**
 * A base class for application game ui. This class provides background
 * management and infrastructure for actual menu subclasses.
 */
public abstract class MenuBase {
    private boolean active = false;
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    private MenuManager menuManager;
    MenuManager getMenuManager() { return menuManager; }

    private final ImageView background = new ImageView();
    public ImageView getBackground() { return background; }

    private final Pane root = new StackPane();
    public Pane getRoot() { return root; }

    /**
     * Utility method to iterate recursively through own hierarchy and set
     * button size to predefined size to make them look consistent.
     */
    private void setChildStyles() {
        setChildStyles(getRoot());
    }

    /**
     * Utility method to iterate recursively through given hierarchy and set
     * button size to predefined size to make them look consistent.
     * @param hierarchy gui controls hierarchy
     */
    private void setChildStyles(Node hierarchy ) {
        if (hierarchy instanceof Pane) {
            Pane pane = (Pane) hierarchy;

            // create CSS style class for layouts as they do not have
            // them created by default
            pane.getStyleClass().setAll(pane.getClass().getSimpleName().toLowerCase());

            // recursively process child nodes of this pane
            pane.getChildren().forEach(this::setChildStyles);
        }
    }

    /**
     * Constructs overlay with given background. Background image is stretched
     * to size of parent pane.
     * @param menuManager     menu manager to use
     * @param backgroundImage image to use as background, can be set to
     *                        null if no image required
     */
    public MenuBase(MenuManager menuManager, Image backgroundImage) {
        assert menuManager != null : "menuManager can not be null";

        this.menuManager = menuManager;

        if (backgroundImage == null) {
            log("background image set to null in %s", getClass());
        }

        // set overlay image and fit it to parent pane size
        getBackground().setImage(backgroundImage);
        getBackground().fitWidthProperty().bind(menuManager.getRoot().widthProperty());
        getBackground().fitHeightProperty().bind(menuManager.getRoot().heightProperty());

        // set overlay root pane to size of background image
        getRoot().getChildren().add(getBackground());

        constructMenu();

        setChildStyles();
    }

    /**
     * This method must be overrode in subclass to construct custom menu.
     */
    protected abstract void constructMenu();

    public abstract void onKeyPressed(KeyEvent event);
    public abstract void onKeyReleased(KeyEvent event);

}
