package org.matsim.networkEditor.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
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
    private MapView mapView = null;
    private HashMap<Id<Node>, Marker> nodeMarkers = null;
    private HashMap<Id<Link>, CoordinateLine> linkLines = null;
    private TableView<Node> nodeTable = null;
    private TableView<Link> linkTable = null;
    private NetworkInfo networkInfo = null;
    private String coordinateSystem = null;

    public ExtendedNetwork() {
        this.network = NetworkUtils.createNetwork();
        this.nodeTable = new TableView<>();
        this.linkTable = new TableView<>();
        this.nodeMarkers = new HashMap<>();
        this.linkLines = new HashMap<>();
    }

    // TODO Add coordinate system here - pick from textfield or dropdown?
    public ExtendedNetwork(String name, Double effectiveLaneWidth, Double effectiveCellSize, Double capPeriod, VBox vBoxNetWork,
            VBox vBoxNodes, VBox vBoxLinks, MapView mapView) {
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
        initializeMapElementLists(vBoxNetWork, vBoxNodes, vBoxLinks, mapView);
        initializeTableViews();
        paintToMap();
    }

    public ExtendedNetwork(String networkPath,VBox vBoxNetWork, VBox vBoxNodes, VBox vBoxLinks, MapView mapView) {
        initializeMapElementLists(vBoxNetWork, vBoxNodes, vBoxLinks, mapView);
        this.network = NetworkUtils.createNetwork();
        this.networkPath = networkPath;
        System.out.println("--------------------------READER---------------------------------------");
        new MatsimNetworkReader("EPSG: 32633", "EPSG: 3857", this.network).readFile(networkPath);
        initializeTableViews();
        paintToMap();
    }

    public void paintToMap() {
        populateNodesTable();
        populateLinksTable();
        this.networkInfo.update(this.network);
    }

    private void initializeMapElementLists(VBox vBoxNetwork, VBox vBoxNodes, VBox vBoxLinks, MapView mapView) {
        this.vBoxNetWork = vBoxNetwork;
        this.vBoxLinks = vBoxLinks;
        this.vBoxNodes = vBoxNodes;
        this.mapView = mapView;
        this.nodeTable = new TableView<>();
        this.linkTable = new TableView<>();
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
                return new SimpleStringProperty(p.getValue().getId().toString());
            }
        });

        TableColumn coordx = new TableColumn("x");
        coordx.setMinWidth(50);
        coordx.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Node, Coord>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Node, Coord> p) {

                return new SimpleStringProperty(Double.toString(p.getValue().getCoord().getX()));
            }
        });

        TableColumn coordy = new TableColumn("y");
        coordy.setMinWidth(50);
        coordy.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Node, Coord>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Node, Coord> p) {
                return new SimpleStringProperty(Double.toString(p.getValue().getCoord().getY()));
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

    }

    public void addNode(String id, Coordinate coordinate) {
        // TODO Check if node id already exists
        Coord coord = new Coord(coordinate.getLatitude(), coordinate.getLongitude(), 0.0);
        NetworkUtils.createAndAddNode(this.network, Id.create(id, Node.class), coord);
        paintToMap();
    }

    public void addNode(Coordinate coordinate) {
        int max = findMaxNodeId();
        addNode(String.valueOf(max + 1), coordinate);
    }
    public int findMaxNodeId(){
        int max = 0;
        for (Id<Node> id : this.network.getNodes().keySet()) {
            int intID = Integer.parseInt(id.toString());
            if (intID > max) {
                max = intID;
            }
        }
        return max;
    }

    public void editNode(String id, Coord newCoord){
        Node node = this.network.getNodes().get(Id.create(id, Node.class));
        // Coord newCoord = new Coord(newCoordinate.getLatitude(),newCoordinate.getLongitude(), 0.0);
        Coord currentCoord = node.getCoord();
        if (newCoord.getX() != currentCoord.getX() || newCoord.getY() != currentCoord.getY()){
            node.setCoord(newCoord);
            mapView.removeMarker(this.nodeMarkers.get(node.getId()));
            this.nodeMarkers.remove(node.getId());
            Set<Id<Link>> inlinks = node.getInLinks().keySet();
            Set<Id<Link>> outlinks = node.getOutLinks().keySet();
            HashSet<Id<Link>> merged = new HashSet<Id<Link>>() {
                {
                    addAll(inlinks);
                    addAll(outlinks);
                }
            };
            for (Id<Link> idlink : merged) {
                mapView.removeCoordinateLine(this.linkLines.get(idlink));
                this.linkLines.remove(idlink);
            }
            paintToMap();
        }
        
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
        int max = findMaxLinkId();
        return addLink(String.valueOf(max + 1), nodeA, nodeB, length, freespeed, capacity, numLanes);
    }

    public int findMaxLinkId(){
        int max = 0;
        for (Id<Link> id : this.network.getLinks().keySet()) {
            int intID = Integer.parseInt(id.toString());
            if (intID > max) {
                max = intID;
            }
        }
        return max;
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

    public Boolean editLink(String id, String newFromNode, String newToNode, double length, double freespeed, double capacity, double numLanes) {
        Link link = this.network.getLinks().get(Id.create(id, Link.class));
        if (!link.getFromNode().getId().toString().equals(newFromNode) || !link.getToNode().getId().toString().equals(newToNode)) {
            Id<Node> newFromNodeId = Id.create(newFromNode, Node.class);
            Id<Node> newToNodeId = Id.create(newToNode, Node.class);

            if (this.network.getNodes().containsKey(newFromNodeId) && this.network.getNodes().containsKey(newToNodeId)) {
                if (this.containsLink(newFromNodeId, newToNodeId)) {
                    removeLink(id);
                    addLink(id, newFromNode, newToNode, length, freespeed, capacity, numLanes);
                }
                return true;
            } else {
                return false;
            }
        } else if (link.getLength() != length || link.getCapacity() != capacity || link.getNumberOfLanes() != numLanes || link.getFreespeed() != freespeed) {
            link.setLength(length);
            link.setCapacity(capacity);
            link.setFreespeed(freespeed);
            link.setNumberOfLanes(numLanes);
            paintToMap();
            return true;
        }
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
        Coord coord = new Coord(coordinate.getLatitude(), coordinate.getLongitude(), 0.0);
        for (Entry<Id<Node>, ? extends Node> entry : this.network.getNodes().entrySet()) {
            Coord entryCoord = entry.getValue().getCoord();
            if (entryCoord.getX() == coord.getX() && entryCoord.getY() == coord.getY()) {
                return entry.getValue();
            }
            // if (entryCoord.equals(coord)) { // This can be used, when network initialized
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
                Coordinate coordinate = new Coordinate(node.getCoord().getX(), node.getCoord().getY());
                Marker marker = new Marker(getClass().getResource("/icons/node-icon.png"), -3, -8)
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
                Coordinate coordinateFrom = new Coordinate(link.getFromNode().getCoord().getX(),
                        link.getFromNode().getCoord().getY());
                Coordinate coordinateTo = new Coordinate(link.getToNode().getCoord().getX(),
                        link.getToNode().getCoord().getY());
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

    public boolean containsLink(Coordinate coordFrom, Coordinate coordTo) {
        for (Link link : this.network.getLinks().values())
            if ((link.getFromNode().getCoord().getX() == coordFrom.getLatitude() &&
                    link.getFromNode().getCoord().getY() == coordFrom.getLongitude()) &&
                    (link.getToNode().getCoord().getX() == coordTo.getLatitude() &&
                    link.getToNode().getCoord().getY() == coordTo.getLongitude())) {
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