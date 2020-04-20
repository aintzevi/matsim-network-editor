package controllers;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapView;
import function.LinkModel;
import function.NodeModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.network.Link;
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
    private ScrollPane nodesScrollPane;

    @FXML
    TableView<NodeModel> nodesTable = new TableView<>();
    @FXML
    TableView<LinkModel> linksTable = new TableView<>();

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

            // TODO: Correct attribute values acquisition, from MATSim nodes to the NodeModel
            for (Node node : network.getNodes().values()) {
                // Add to drop menu
//                nodesTable.getItems().add(network.getNodes().values().toString());
            }

            for (Link link : network.getLinks().values()) {
                // Add to drop menu
//                linksTable.getItems().add(network.getLinks().values().toString());
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
        TableColumn<NodeModel, String> nodeIdColumn = new TableColumn<>("id");
        TableColumn<NodeModel, String> xCoordColumn = new TableColumn<>("x");
        TableColumn<NodeModel, String> yCoordColumn = new TableColumn<>("y");
        TableColumn<NodeModel, String> typeColumn = new TableColumn<>("type");
        TableColumn<NodeModel, String> nOfInLinksColumn = new TableColumn<>("in-links");
        TableColumn<NodeModel, String> nOfOutLinksColumn = new TableColumn<>("out-links");

        // Create Table columns
        TableColumn<LinkModel, String> linkIdColumn = new TableColumn<>("id");
        TableColumn<LinkModel, String> fromColumn = new TableColumn<>("from");
        TableColumn<LinkModel, String> toColumn = new TableColumn<>("to");
        TableColumn<LinkModel, String> lengthColumn = new TableColumn<>("length");
        TableColumn<LinkModel, String> capacityColumn = new TableColumn<>("capacity");
        TableColumn<LinkModel, String> freespeedColumn = new TableColumn<>("freespeed");
        TableColumn<LinkModel, String> permlanesColumn = new TableColumn<>("perm-lanes");

        //TODO Get data and populate properties in Node and Link
        // PropertyValueFactory works with reflection, cannot get Node and Link data on the fly

        // Property-matching value for each cell
        ObservableList<NodeModel> nodeList = FXCollections.observableArrayList();
        NodeModel node = new NodeModel();

        nodeIdColumn.setCellValueFactory(new PropertyValueFactory<NodeModel, String>("id"));
        xCoordColumn.setCellValueFactory(new PropertyValueFactory<NodeModel, String>("x"));
        yCoordColumn.setCellValueFactory(new PropertyValueFactory<NodeModel, String>("y"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<NodeModel, String>("type"));
        nOfInLinksColumn.setCellValueFactory(new PropertyValueFactory<NodeModel, String>("in-links"));
        nOfOutLinksColumn.setCellValueFactory(new PropertyValueFactory<NodeModel, String>("out-links"));

        linkIdColumn.setCellValueFactory(new PropertyValueFactory<LinkModel, String>("id"));
        toColumn.setCellValueFactory(new PropertyValueFactory<LinkModel, String>("from"));
        fromColumn.setCellValueFactory(new PropertyValueFactory<LinkModel, String>("to"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<LinkModel, String>("length"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<LinkModel, String>("capacity"));
        freespeedColumn.setCellValueFactory(new PropertyValueFactory<LinkModel, String>("freespeed"));
        permlanesColumn.setCellValueFactory(new PropertyValueFactory<LinkModel, String>("perm-lanes"));


        nodesTable.getColumns().addAll(nodeIdColumn, xCoordColumn, yCoordColumn, nOfInLinksColumn, nOfOutLinksColumn);
        nodesTable.setPlaceholder(new Label("No rows to display"));

        linksTable.getColumns().addAll(linkIdColumn, toColumn, fromColumn, lengthColumn, capacityColumn, freespeedColumn, permlanesColumn);
        linksTable.setPlaceholder(new Label("No rows to display"));
    }
}