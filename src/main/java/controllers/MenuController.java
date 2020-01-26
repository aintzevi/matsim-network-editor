package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

/**
 * Controller for all the actions in the main menu
 */
public class MenuController {
    @FXML
    private MenuItem fileExit;
    @FXML
    private MenuBar menuBar;

    public void handleMenuExit() {
        Stage stage = (Stage) menuBar.getScene().getWindow();
        System.out.println("Close window");
        stage.close();
    }
}
