package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

/**
 * Controller for all the actions in the main menu
 */
public class MenuController {
    @FXML
    public MenuItem menuExit;

    // TODO Decide if I need to split this over multiple controllers
    public void handleMenuExit() {
        // TODO Save things here
//        menuExit.setText("hm");
        System.out.println("blah");

//        mainWindow.close();
    }
}
