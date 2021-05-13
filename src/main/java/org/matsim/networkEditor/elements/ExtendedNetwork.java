package org.matsim.networkEditor.elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.CoordinateLine;
import com.sothawo.mapjfx.MapView;
import com.sothawo.mapjfx.Marker;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.networkEditor.visualElements.NetworkInfo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class ExtendedNetwork {
    private Network network = null;
    private String networkPath = null;
    private VBox vBoxNetWork = null;
    private VBox vBoxNodes = null;
    private VBox vBoxLinks = null;
    private VBox vBoxValidation = null;
    private MapView mapView = null;
    private HashMap<Id<Node>, Marker> nodeMarkers = null;
    private HashMap<Id<Link>, CoordinateLine> linkLines = null;
    private TableView<Node> nodeTable = null;
    private TableView<Link> linkTable = null;
    private TableView<Object> validationTable = null;
    private NetworkInfo networkInfo = null;
    private String coordinateSystem = null;

    public ExtendedNetwork() {
        this.network = NetworkUtils.createNetwork();
        this.nodeTable = new TableView<>();
        this.linkTable = new TableView<>();
        this.validationTable = new TableView<>();
        this.nodeMarkers = new HashMap<>();
        this.linkLines = new HashMap<>();
    }

    public ExtendedNetwork(String name, Double effectiveLaneWidth, Double effectiveCellSize, Double capPeriod, VBox vBoxNetWork,
            VBox vBoxNodes, VBox vBoxLinks, VBox vBoxValidation, MapView mapView) {
        this.network = NetworkUtils.createNetwork();
        if (name != null) {
            this.network.setName(name);
        }
        if (effectiveCellSize != null) {
            this.network.setEffectiveCellSize(effectiveCellSize);
        }
        if (effectiveLaneWidth != null) {
            this.network.setEffectiveLaneWidth(effectiveLaneWidth);
        }
        if (capPeriod != null) {
            this.network.setCapacityPeriod(capPeriod);
        }
        if (coordinateSystem != null) {
            StringBuilder str = new StringBuilder(coordinateSystem);
        }
        initializeMapElementLists(vBoxNetWork, vBoxNodes, vBoxLinks, vBoxValidation, mapView);
        initializeTableViews();
        paintToMap();
    }

    public ExtendedNetwork(String networkPath,VBox vBoxNetWork, VBox vBoxNodes, VBox vBoxLinks, VBox vBoxValidation, MapView mapView) {
        initializeMapElementLists(vBoxNetWork, vBoxNodes, vBoxLinks, vBoxValidation, mapView);
        this.network = NetworkUtils.createNetwork();
        this.networkPath = networkPath;
        System.out.println("--------------------------READER---------------------------------------");
        // Target is the default for our map, therefore WGS84
        new MatsimNetworkReader(coordinateSystem, "EPSG: 4326", this.network).readFile(networkPath);
        initializeTableViews();
        paintToMap();
    }

    public void paintToMap() {
        populateNodesTable();
        populateLinksTable();
        this.networkInfo.update(this.network);
    }

    private void initializeMapElementLists(VBox vBoxNetwork, VBox vBoxNodes, VBox vBoxLinks, VBox vBoxValidation, MapView mapView) {
        this.vBoxNetWork = vBoxNetwork;
        this.vBoxLinks = vBoxLinks;
        this.vBoxNodes = vBoxNodes;
        this.vBoxValidation = vBoxValidation;
        this.mapView = mapView;
        this.nodeTable = new TableView<>();
        this.linkTable = new TableView<>();
        this.validationTable = new TableView<>();
        this.nodeMarkers = new HashMap<>();
        this.linkLines = new HashMap<>();
    }

    private void initializeTableViews() {
        this.networkInfo = new NetworkInfo(this.network);
        ArrayList<Pair<javafx.scene.Node, javafx.scene.Node>> networkInfoNodes=  this.networkInfo.getAll();

        this.vBoxNetWork.getChildren().clear();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        for (int i = 0; i < networkInfoNodes.size() ; i++) {
            grid.add(networkInfoNodes.get(i).getKey(), 0, i);
            grid.add(networkInfoNodes.get(i).getValue(), 2, i);
        }
        this.vBoxNetWork.getChildren().add(grid);

        this.nodeTable = new TableView<>();
        this.nodeTable.setEditable(false);
        this.nodeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn idColumn = new TableColumn("ID");
        idColumn.setMinWidth(5);
        idColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Node, Id>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Node, Id> p) {
                // Use origID to show
                return new SimpleStringProperty(NetworkUtils.getOrigId(p.getValue()));
            }
        });

        TableColumn coordx = new TableColumn("x");
        coordx.setMinWidth(50);
        coordx.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Node, Coord>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Node, Coord> p) {
                // Swap X with Y to match MATSim notation
                return new SimpleStringProperty(Double.toString(p.getValue().getCoord().getY()));
            }
        });

        TableColumn coordy = new TableColumn("y");
        coordy.setMinWidth(50);
        coordy.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Node, Coord>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Node, Coord> p) {
                // Swap X with Y to match MATSim notation
                return new SimpleStringProperty(Double.toString(p.getValue().getCoord().getX()));
            }
        });

        TableColumn inLinks = new TableColumn<>("InLinks");
        inLinks.setMinWidth(50);
        inLinks.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Node, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Node, String> p) {
                StringBuilder inLinksString =  new StringBuilder();
                for (Id<Link> id:p.getValue().getInLinks().keySet()){
                    if (inLinksString.length() !=0){
                        inLinksString.append(",");
                    }
                    inLinksString.append(id.toString());
                }
                return new SimpleStringProperty(inLinksString.toString());
            }
        });

        TableColumn outLinks = new TableColumn<>("OutLinks");
        outLinks.setMinWidth(50);
        outLinks.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Node, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Node, String> p) {
                StringBuilder outLinkString =  new StringBuilder();
                for (Id<Link> id:p.getValue().getOutLinks().keySet()){
                    if (outLinkString.length() !=0){
                        outLinkString.append(",");
                    }
                    outLinkString.append(id.toString());
                }
                return new SimpleStringProperty(outLinkString.toString());
            }
        });

        // Clear nodes box in case it contains previous data
        if (vBoxNodes.getChildren().size() > 1){
            this.vBoxNodes.getChildren().remove(1);
        }
        this.vBoxNodes.getChildren().add(this.nodeTable);
        this.nodeTable.getColumns().addAll(idColumn, coordx, coordy,inLinks,outLinks);

        this.linkTable = new TableView<>();
        this.linkTable.setEditable(false);
        this.linkTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn idColumnLink = new TableColumn<>("ID");
        idColumnLink.setMinWidth(5);
        idColumnLink
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Link, Id>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Link, Id> p) {
                        return new SimpleStringProperty(p.getValue().getId().toString());
                    }
                });

        TableColumn fromNodeColumn = new TableColumn("From");
        fromNodeColumn.setMinWidth(5);
        fromNodeColumn
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Link, Node>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Link, Node> p) {
                        return new SimpleStringProperty(p.getValue().getFromNode().getId().toString());
                    }
                });

        TableColumn toNodeColumn = new TableColumn("To");
        toNodeColumn.setMinWidth(5);
        toNodeColumn
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Link, Node>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Link, Node> p) {
                        return new SimpleStringProperty(p.getValue().getToNode().getId().toString());
                    }
                });

        TableColumn lengthColumn = new TableColumn("Length");
        lengthColumn.setMinWidth(10);
        lengthColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Link, Double>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Link, Double> p) {
                        return new SimpleStringProperty(Double.toString(p.getValue().getLength()));
                    }
                });

        TableColumn capacityColumn = new TableColumn("Capacity");
        capacityColumn.setMinWidth(10);
        capacityColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Link, Double>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Link, Double> p) {
                        return new SimpleStringProperty(Double.toString(p.getValue().getCapacity()));
                    }
                });

        TableColumn freeSpeedColumn = new TableColumn("Free Speed");
        freeSpeedColumn.setMinWidth(10);
        freeSpeedColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Link, Double>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Link, Double> p) {
                        return new SimpleStringProperty(Double.toString(p.getValue().getFreespeed()));
                    }
                });

        TableColumn nofLanesColumn = new TableColumn("#Lanes");
        nofLanesColumn.setMinWidth(10);
        nofLanesColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Link, Double>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Link, Double> p) {
                        return new SimpleStringProperty(Double.toString(p.getValue().getNumberOfLanes()));
                    }
                });

        TableColumn allowedModes = new TableColumn("AllowedModes");
        allowedModes.setMinWidth(10);
        allowedModes.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Link, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Link, String> p) {
                        
                        return new SimpleStringProperty(String.join(",", p.getValue().getAllowedModes()));
                    }
                });
        
        TableColumn flowCapacity = new TableColumn("FlowCapacityPerSec");
        flowCapacity.setMinWidth(10);
        flowCapacity.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Link, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Link, String> p) {
                        
                        return new SimpleStringProperty(Double.toString(p.getValue().getFlowCapacityPerSec()));
                    }
                });


        // Clear nodes box in case it contains previous data
        if (vBoxLinks.getChildren().size() > 1){
            this.vBoxLinks.getChildren().remove(1);
        }
        this.vBoxLinks.getChildren().add(this.linkTable);
        this.linkTable.getColumns().addAll(idColumnLink, fromNodeColumn, toNodeColumn, lengthColumn, capacityColumn,
                freeSpeedColumn, nofLanesColumn,allowedModes,flowCapacity);


        // Validation Table stuff
        this.validationTable = new TableView<>();
        this.validationTable.setEditable(false);
        this.validationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn idColumnValidation = new TableColumn<>("ID");
        idColumnValidation.setMinWidth(5);
        idColumnValidation
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Link, Id>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Link, Id> p) {
                        return new SimpleStringProperty(p.getValue().getId().toString());
                    }
                });
        TableColumn messageColumnValidation = new TableColumn<>("Message");
        idColumnValidation.setMinWidth(5);
        idColumnValidation
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Link, Id>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Link, Id> p) {
                        return new SimpleStringProperty("Issue description");
                    }
                });

        // Clear nodes box in case it contains previous data
        if (vBoxValidation.getChildren().size() > 1){
            this.vBoxValidation.getChildren().remove(1);
        }

        this.vBoxValidation.getChildren().add(this.validationTable);
        this.validationTable.getColumns().addAll(idColumnValidation, messageColumnValidation);
    }

    public void addNode(String id, Coordinate coordinate) {
        // TODO Check if node id already exists
        // Swapped Lat and Long to match MATSim
        Coord coord = new Coord(coordinate.getLongitude(), coordinate.getLatitude(), 0.0);
        NetworkUtils.createAndAddNode(this.network, Id.create(id, Node.class), coord);
        NetworkUtils.setOrigId(this.network.getNodes().get(Id.create(id, Node.class)), id);
        paintToMap();
    }

    public void addNode(Coordinate coordinate) {
        String newId = createNodeId();
        addNode(newId, coordinate);
    }

    public String createNodeId() {
        return "node_" + System.currentTimeMillis() / 1000L;
    }

    public void editNode(String oldId, String newId, Coord newCoord) {
        Node node = this.network.getNodes().get(Id.create(oldId, Node.class));
        Coord currentCoord = node.getCoord();
        if (!newId.equals(oldId)) {
            // Check that no node with this id already exists in the network
            for (Node currentNode : this.network.getNodes().values()) {
                if (NetworkUtils.getOrigId(currentNode).equals(newId))
                    break;
                else
                    NetworkUtils.setOrigId(node, newId);
            }
        }
        else
            NetworkUtils.setOrigId(node, oldId);

        if (newCoord.getX() != currentCoord.getX() || newCoord.getY() != currentCoord.getY()) {
            node.setCoord(newCoord);
            mapView.removeMarker(this.nodeMarkers.get(node.getId()));
            this.nodeMarkers.remove(node.getId());
            Set<Id<Link>> inLinks = node.getInLinks().keySet();
            Set<Id<Link>> outLinks = node.getOutLinks().keySet();
            HashSet<Id<Link>> merged = new HashSet<>() {
                {
                    addAll(inLinks);
                    addAll(outLinks);
                }
            };
            for (Id<Link> idLink : merged) {
                mapView.removeCoordinateLine(this.linkLines.get(idLink));
                this.linkLines.remove(idLink);
            }
        }
        paintToMap();
    }

    public boolean addLink(String id, Coordinate nodeA, Coordinate nodeB, double length, double freespeed,
            double capacity, double numLanes) {
        // TODO Check if link id already exists and other checks
        Node fromNode = findNodeByCoordinate(nodeA);
        Node toNode = findNodeByCoordinate(nodeB);
        if (fromNode != null && toNode != null) {
            NetworkUtils.createAndAddLink(network, Id.create(id, Link.class), fromNode, toNode, length, freespeed,
                    capacity, numLanes);
            paintToMap();
            return true;
        }
        return false;
    }
        
    public boolean addLink(Coordinate nodeA, Coordinate nodeB, double length, double freespeed, double capacity, double numLanes){
        String newId = createLinkId();
        return addLink(newId, nodeA, nodeB, length, freespeed, capacity, numLanes);
    }

    public String createLinkId() {
        return "link_" + System.currentTimeMillis() / 1000L;
    }

    public boolean addLink(String id, String nodeAId, String nodeBId, double length, double freespeed, double capacity,
            double numLanes) {
        // TODO Check if link id already exists and other checks
        Node fromNode = this.network.getNodes().get(Id.create(nodeAId, Node.class));
        Node toNode = this.network.getNodes().get(Id.create(nodeBId, Node.class));
        if (fromNode != null && toNode != null) {
            NetworkUtils.createAndAddLink(network, Id.create(id, Link.class), fromNode, toNode, length, freespeed,
                    capacity, numLanes);
            paintToMap();
            return true;
        }
        return false;
    }

    public Boolean editLink(String oldId, String newId, double length, double freespeed, double capacity, double numLanes) {
        Link link = this.network.getLinks().get(Id.create(oldId, Link.class));

        if (!newId.equals(oldId)) {
            if (!this.network.getLinks().containsKey(Id.create(newId, Link.class))) {
                NetworkUtils.createAndAddLink(this.network, Id.create(newId, Link.class), link.getFromNode(), link.getToNode(),
                        length, freespeed, capacity, numLanes);
                network.removeLink(Id.create(oldId, Link.class));
            }
            else {
                return false;
            }
        } else {
            if (link.getLength() != length || link.getCapacity() != capacity || link.getNumberOfLanes() != numLanes || link.getFreespeed() != freespeed) {
                link.setLength(length);
                link.setCapacity(capacity);
                link.setFreespeed(freespeed);
                link.setNumberOfLanes(numLanes);
            }
        }
        paintToMap();
        return true;
    }

    public boolean removeNode(String id) {
        // TODO Checks
        Id<Node> nodeId = Id.create(id, Node.class);
        Node node = this.network.getNodes().get(nodeId);
        Set<Id<Link>> inlinks = node.getInLinks().keySet();
        Set<Id<Link>> outlinks = node.getOutLinks().keySet();
        HashSet<Id<Link>> merged = new HashSet<Id<Link>>() {
            {
                addAll(inlinks);
                addAll(outlinks);
            }
        };
        if (this.network.removeNode(nodeId) != null) {
            mapView.removeMarker(this.nodeMarkers.get(nodeId));
            this.nodeMarkers.remove(nodeId);
            for (Id<Link> idlink : merged) {
                mapView.removeCoordinateLine(this.linkLines.get(idlink));
                this.linkLines.remove(idlink);
            }
            paintToMap();
            return true;
        }
        return false;
    }

    public boolean removeNode(Coordinate coordinate) {
        Node node = findNodeByCoordinate(coordinate);
        return removeNode(node.getId().toString());
    }

    public boolean removeLink(String id) {
        // TODO Checks
        Id<Link> idlink = Id.create(id, Link.class);
        if (this.network.removeLink(idlink) != null) {
            mapView.removeCoordinateLine(this.linkLines.get(idlink));
            this.linkLines.remove(idlink);
            paintToMap();
            return true;
        }
        return false;
    }

    public boolean removeLink(String nodeAid, String nodeBid) {
        // TODO Checks
        return true;
    }

    private Node findNodeByCoordinate(Coordinate coordinate) {
        // Swap Lat and Long to match MATSim notation
        Coord coord = new Coord(coordinate.getLongitude(), coordinate.getLatitude(), 0.0);
        for (Entry<Id<Node>, ? extends Node> entry : this.network.getNodes().entrySet()) {
            Coord entryCoord = entry.getValue().getCoord();
            if (entryCoord.getX() == coord.getX() && entryCoord.getY() == coord.getY()) {
                return entry.getValue();
            }
            // if (entryCoord.equals(coord)) {
            // This can be used, when network initialized
            // the Z value is to -Infinity but when a new node was add it goes to 0
            // return entry.getValue();
            // }
        }
        return null;
    }

    public String getNodeDescr(Coordinate coordinate){
        Node node = findNodeByCoordinate(coordinate);
        StringBuilder stringBuilder = new StringBuilder();
        if (node!=null){
            stringBuilder.append(node.getId().toString());
            stringBuilder.append(" -> x: " + node.getCoord().getX() +  " y: " + node.getCoord().getY());
            return stringBuilder.toString();
        }
        return null;
    }

    private void populateNodesTable() {
        ObservableList<Node> nodeData = FXCollections.observableArrayList(this.network.getNodes().values());
        this.nodeTable.getItems().clear();
        this.nodeTable.setItems(nodeData);
        this.nodeTable.refresh();
        createNodesMarkers(nodeData);
    }

    private void createNodesMarkers(ObservableList<Node> data) {
        for (Node node : data) {
            if (!this.nodeMarkers.containsKey(node.getId())) {
                // Swapped X and Y to match MATSim notation
                Coordinate coordinate = new Coordinate(node.getCoord().getY(), node.getCoord().getX());
                Marker marker = new Marker(getClass().getResource("/icons/node.png"), -3, -8)
                        .setPosition(coordinate).setVisible(true);
                nodeMarkers.put(node.getId(), marker);
                mapView.addMarker(marker);
            }
        }
    }

    private void populateLinksTable() {
        ObservableList<Link> linkData = FXCollections.observableArrayList(this.network.getLinks().values());
        this.linkTable.getItems().clear();
        this.linkTable.setItems(linkData);
        this.linkTable.refresh();
        createlinkLines(linkData);
    }

    private void createlinkLines(ObservableList<Link> data) {

        for (Link link : data) {
            if (!this.linkLines.containsKey(link.getId())) {
                // Swap
                Coordinate coordinateFrom = new Coordinate(link.getFromNode().getCoord().getY(),
                        link.getFromNode().getCoord().getX());
                Coordinate coordinateTo = new Coordinate(link.getToNode().getCoord().getY(),
                        link.getToNode().getCoord().getX());
                List<Coordinate> coordinates = new ArrayList<Coordinate>() {
                    {
                        add(coordinateFrom);
                        add(coordinateTo);
                    }
                };
                CoordinateLine coordinateLine = new CoordinateLine(coordinates).setColor(Color.DARKBLUE).setWidth(1)
                        .setVisible(true);
                this.linkLines.put(link.getId(), coordinateLine);
                mapView.addCoordinateLine(coordinateLine);
            }
        }
    }

    public HashMap<Id<Node>, Marker> getNodeMarkers() {
        return this.nodeMarkers;
    }

    public HashMap<Id<Link>, CoordinateLine> getLinkLines() {
        return this.linkLines;
    }

    public Network getNetwork() {
        return this.network;
    }

    public TableView<Node> getNodeTable(){
        return this.nodeTable;
    }

    public TableView<Link> getLinkTable(){
        return this.linkTable;
    }

    public TableView<Object> getValidationTable() {
        return this.validationTable;
    }

    public String getCoordinateSystem() {
        return this.coordinateSystem;
    }

    public void setCoordinateSystem(String coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }

    public void clear() {
        this.nodeTable = new TableView<>();
        this.linkTable = new TableView<>();
        for (Entry<Id<Node>, Marker> entry : this.nodeMarkers.entrySet()) {

            mapView.removeMarker(entry.getValue());
        }
        this.nodeMarkers.clear();
        for (Entry<Id<Link>, CoordinateLine> entry : this.linkLines.entrySet()) {

            mapView.removeCoordinateLine(entry.getValue());
        }
        this.linkLines.clear();

        this.nodeMarkers = new HashMap<>();
        this.linkLines = new HashMap<>();
    }

    public boolean containsLink(Coordinate coordinateFrom, Coordinate coordinateTo) {
        for (Link link : this.network.getLinks().values())
            // Swap X and Y to match MATSim notation
            if ((link.getFromNode().getCoord().getY() == coordinateFrom.getLatitude() &&
                    link.getFromNode().getCoord().getX() == coordinateFrom.getLongitude()) &&
                    (link.getToNode().getCoord().getY() == coordinateTo.getLatitude() &&
                    link.getToNode().getCoord().getX() == coordinateTo.getLongitude())) {
                return true;
            }
        return false;
    }

    public boolean containsLink(Id<Node> nodeFrom, Id<Node> nodeTo) {
        for (Link link : this.network.getLinks().values())
            if (link.getFromNode().getId() == nodeFrom && link.getToNode().getId() == nodeTo) {
                return true;
            }
        return false;
    }

}