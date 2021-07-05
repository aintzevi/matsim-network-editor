package org.matsim.networkEditor;

import com.sothawo.mapjfx.Projection;
import org.matsim.networkEditor.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo application for the mapjfx component.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class App extends Application {

    /** Logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.trace("begin main");
        launch(args);
        logger.trace("end main");
    }

    /**
     * Main function displaying the page of the application and linking the controllers to the visuals
     * @param primaryStage The main application window
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Starting Matsim Network Editor");
        String fxmlFile = "/fxml/Main.fxml";
        logger.debug("loading fxml file {}", fxmlFile);
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent rootNode = fxmlLoader.load(getClass().getResourceAsStream(fxmlFile));
        logger.trace("stage loaded");

        final MainController controller = fxmlLoader.getController();
        final Projection projection = getParameters().getUnnamed().contains("wgs84")
                ? Projection.WGS_84 : Projection.WEB_MERCATOR;
        controller.initMapAndControls(projection);

        Scene scene = new Scene(rootNode);
        logger.trace("scene created");
        scene.getStylesheets().add("https://fonts.googleapis.com/css2?family=Open+Sans");
        primaryStage.setTitle("MATSim Network Editor");
        primaryStage.setScene(scene);
        logger.trace("showing scene");
        primaryStage.show();

        logger.debug("application start method finished.");
    }
}
