import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;

public class Main extends Application {
    Stage mainWindow;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage mainStage) throws Exception {
        // Changed from Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        // This fixes no Location found exception
        mainWindow = mainStage;

        URL url = new File("src/main/resources/fxml/menu.fxml").toURI().toURL();
        Parent root = FXMLLoader.load(url);
        mainWindow.setTitle("MATSim network editor");
        mainWindow.setScene(new Scene(root, 800, 650));
        mainWindow.show();
     }

     //TODO init() and/or stop()?
}
