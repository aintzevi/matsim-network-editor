package controllers;

import com.gluonhq.maps.MapView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private BorderPane mainPane;
    @FXML
    private MenuBar menuBar;

    @FXML
    private MapView mapView;

    @FXML
    private GridPane nodesGridPanel;

    @FXML
    TableView<String> nodesTable = new TableView<>();
    @FXML
    TableView<String> linksTable = new TableView<>();

    @FXML
    private Button toolboxButton1 = new Button();
    private ImageView toolBoxIcon = new ImageView();

    @FXML
    private ToolBar toolBar = new ToolBar();


    public void initialize(URL url, ResourceBundle resourceBundle) {
//        mapView.minHeight(mainPane.getScene().getHeight());
    }

    public void handleMenuExit() {
        //TODO Save action here
        Platform.exit();
    }

    public void handleMenuImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import from file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xlm file", "*.xml"));
        File importedFile = fileChooser.showOpenDialog(this.menuBar.getScene().getWindow());
        System.out.println(importedFile.getName());
        Network network = NetworkUtils.createNetwork();

        new MatsimNetworkReader(network).readFile(importedFile.getPath());

        // Create Table columns
        TableColumn<String, Node> nodeIdCol = new TableColumn<>("id");
        TableColumn<String, Node> xCoordCol = new TableColumn<>("x");
        TableColumn<String, Node> yCoordCol = new TableColumn<>("y");

        nodeIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        xCoordCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        yCoordCol.setCellValueFactory(new PropertyValueFactory<>("y"));

        nodesTable.getColumns().addAll(nodeIdCol, xCoordCol, yCoordCol);

        for(Node node : network.getNodes().values()) {
            // Add to drop menu
            nodesTable.getItems().add(network.getNodes().values().toString());
        }

        System.out.println(network.getNodes());
    }
}