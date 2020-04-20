package controllers;

import com.gluonhq.maps.MapLayer;
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
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private BorderPane mainPane;
    @FXML
    private MenuBar menuBar;

    @FXML
    private MapView mapView;

    @FXML
    private ScrollPane nodesScrollPane;

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
        mapView.setZoom(12.0);
        mapView.setCenter(48.1351, 11.5820);
        MapLayer mapLayer = new MapLayer();
        mapLayer.autosize();

/*        MapPoint point = new MapPoint(37.396256,-121.953847);
        Node icon = new Circle(5, Color.BLUE);
        Point2D mapPoint = baseMap.getMapPoint(point.getLatitude(), point.getLongitude());
        icon.setTranslateX(mapPoint.getX());
        icon.setTranslateY(mapPoint.getY());*/
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
        if (importedFile != null) {
            System.out.println(importedFile.getName());
            Network network = NetworkUtils.createNetwork();

            new MatsimNetworkReader(network).readFile(importedFile.getPath());

            // Create tables for nodes and links
            createAttributeTables();

            // TODO: Correct attribute values acquisition, do I need to create an Observable Node object?
            for (Node node : network.getNodes().values()) {
                // Add to drop menu
                nodesTable.getItems().add(network.getNodes().values().toString());
            }

            for (Link link : network.getLinks().values()) {
                // Add to drop menu
                linksTable.getItems().add(network.getLinks().values().toString());
            }

            System.out.println(network.getNodes().values().toArray()[0]);
        }
        else
            System.out.println("Import cancelled");
    }

    private void createAttributeTables() {
        // Clear TableViews
        nodesTable.getItems().clear();
        linksTable.getItems().clear();

        // Create Table columns
        TableColumn<String, Node> nodeIdColumn = new TableColumn<>("id");
        TableColumn<String, Node> xCoordColumn = new TableColumn<>("x");
        TableColumn<String, Node> yCoordColumn = new TableColumn<>("y");
        TableColumn<String, Node> typeColumn = new TableColumn<>("type");
        TableColumn<String, Node> nOfInLinksColumn = new TableColumn<>("in-links");
        TableColumn<String, Node> nOfOutLinksColumn = new TableColumn<>("out-links");

        // Create Table columns
        TableColumn<String, Node> linkIdColumn = new TableColumn<>("id");
        TableColumn<String, Node> fromColumn = new TableColumn<>("from");
        TableColumn<String, Node> toColumn = new TableColumn<>("to");
        TableColumn<String, Node> lengthColumn = new TableColumn<>("lenght");
        TableColumn<String, Node> capacityColumn = new TableColumn<>("capacity");
        TableColumn<String, Node> freespeedColumn = new TableColumn<>("freespeed");
        TableColumn<String, Node> permlanesColumn = new TableColumn<>("perm-lanes");

        //TODO Create Node and Link class containing Property -> PropertyValueFactory works with reflection, this doesn't work
        // Property-matching value for each cell
        nodeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        xCoordColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
        yCoordColumn.setCellValueFactory(new PropertyValueFactory<>("y"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        nOfInLinksColumn.setCellValueFactory(new PropertyValueFactory<>("in-links"));
        nOfOutLinksColumn.setCellValueFactory(new PropertyValueFactory<>("out-links"));

        linkIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        toColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        freespeedColumn.setCellValueFactory(new PropertyValueFactory<>("freespeed"));
        permlanesColumn.setCellValueFactory(new PropertyValueFactory<>("perm-lanes"));

        nodesTable.getColumns().addAll(nodeIdColumn, xCoordColumn, yCoordColumn, nOfInLinksColumn, nOfOutLinksColumn);
        nodesTable.setPlaceholder(new Label("No rows to display"));

        linksTable.getColumns().addAll(linkIdColumn, toColumn, fromColumn, lengthColumn, capacityColumn, freespeedColumn, permlanesColumn);
        linksTable.setPlaceholder(new Label("No rows to display"));
    }
}