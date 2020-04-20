import com.gluonhq.charm.down.ServiceFactory;
import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.StorageService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import static com.sun.javafx.util.Utils.*;

public class Main extends Application {

    public static void main(String[] args) {

        if (isWindows() || isMac() || isUnix()) {
            System.setProperty("javafx.platform", "Desktop");
        }

        // define service for desktop - to cache the map
        StorageService storageService = new StorageService() {
            @Override
            public Optional<File> getPrivateStorage() {
                // user home app config location (linux: /home/[yourname]/.gluonmaps)
                return Optional.of(new File(System.getProperty("user.home")));
            }

            @Override
            public Optional<File> getPublicStorage(String subdirectory) {
                // this should work on desktop systems because home path is public
                return getPrivateStorage();
            }

            @Override
            public boolean isExternalStorageWritable() {
                return getPrivateStorage().get().canWrite();
            }

            @Override
            public boolean isExternalStorageReadable() {
                return getPrivateStorage().get().canRead();
            }
        };

        ServiceFactory<StorageService> storageServiceFactory = new ServiceFactory<StorageService>() {

            @Override
            public Class<StorageService> getServiceType() {
                return StorageService.class;
            }

            @Override
            public Optional<StorageService> getInstance() {
                return Optional.of(storageService);
            }

        };

        // register service
        Services.registerServiceFactory(storageServiceFactory);

        launch(args);
    }

    @Override
    public void start(Stage mainStage) throws IOException {
        // Changed from Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("menu.fxml"));
        // This fixes no Location found exception
        URL url = new File("src/main/resources/fxml/main.fxml").toURI().toURL();
        Parent root = FXMLLoader.load(url);

        GridPane nodesGridPane = new GridPane();
        nodesGridPane.setId("nodesGridPane");

        mainStage.setTitle("MATSim network editor");
        mainStage.setScene(new Scene(root, 800, 650));
        mainStage.show();
    }

    //TODO init() and/or stop()?
}