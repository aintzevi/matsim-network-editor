package org.matsim.networkEditor.elements;

import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;
import org.matsim.networkEditor.visualElements.NetworkInfo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

/**
 * Includes a MATSim network and the elements needed for the visuals for nodes, links, validation items and network information, both for the
 * side panels and the map markers and lines
 */
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
    private TableView<ValidationTableEntry> validationTable = null;
    private NetworkInfo networkInfo = null;
    private String coordinateSystem = null;
    private ArrayList<ValidationTableEntry> validationWarnings = null;

    /**
     * Creates a Network and initializes tableviews and data structures
     */
    public ExtendedNetwork() {
        this.network = NetworkUtils.createNetwork();
        this.nodeTable = new TableView<>();
        this.linkTable = new TableView<>();
        this.validationTable = new TableView<>();
        this.nodeMarkers = new HashMap<>();
        this.linkLines = new HashMap<>();
        this.validationWarnings = new ArrayList<>();
        this.coordinateSystem = "WGS84";
    }

    /**
     * Creates a new empty Network and initializes the visual elements of the editor
     * @param name The name of the network
     * @param effectiveLaneWidth The width of the lane represented by the links
     * @param effectiveCellSize The length of a vehicle in MATSim
     * @param capPeriod The capacity period of the network
     * @param vBoxNetWork The visual box element containing the network information
     * @param vBoxNodes The visual box element containing the nodes information
     * @param vBoxLinks The visual box element containing the links information
     * @param vBoxValidation The visual box element containing the validation elements information
     * @param mapView The visual map component
     * @param coordinateSystem The coordinate system of the network
     */
    public ExtendedNetwork(String name, Double effectiveLaneWidth, Double effectiveCellSize, Double capPeriod, VBox vBoxNetWork,
                           VBox vBoxNodes, VBox vBoxLinks, VBox vBoxValidation, MapView mapView, String coordinateSystem) {
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
            this.coordinateSystem = coordinateSystem;
        }
        initializeMapElementLists(vBoxNetWork, vBoxNodes, vBoxLinks, vBoxValidation, mapView);
        initializeTableViews();
        paintToMap();
    }

    /**
     * Creates a new network from a file and initializes the visual elements of the editor
     * @param networkPath The path to the network file
     * @param vBoxNetWork The visual box element containing the network information
     * @param vBoxNodes The visual box element containing the nodes information
     * @param vBoxLinks The visual box element containing the links information
     * @param vBoxValidation The visual box element containing the validation elements information
     * @param mapView The visual map component
     * @param coordinateSystem The coordinate system of the network
     */
    public ExtendedNetwork(String networkPath, VBox vBoxNetWork, VBox vBoxNodes, VBox vBoxLinks, VBox vBoxValidation, MapView mapView, String coordinateSystem) {
        initializeMapElementLists(vBoxNetWork, vBoxNodes, vBoxLinks, vBoxValidation, mapView);
        this.network = NetworkUtils.createNetwork();
        this.networkPath = networkPath;
        this.coordinateSystem = coordinateSystem;

        System.out.println("---------------------------------------READER---------------------------------------");

        long startTime = System.nanoTime();
        // Target is the default for our map, therefore WGS84
        new MatsimNetworkReader(coordinateSystem, "EPSG: 4326", this.network).readFile(networkPath);

        if (this.network.getName() == null) {
            // Get the name of the imported file and set it as the network name
            Path p = Paths.get(networkPath);
            String[] parts = p.getFileName().toString().split("\\.");
            this.network.setName(parts[0]);
        }

        initializeTableViews();
        paintToMap();

        long endTime = System.nanoTime();
        //divided by 1000000 to get milliseconds.
        long duration = (endTime - startTime)/1000000;
        System.out.println("Time elapsed to load network: " + duration + "ms");
    }

    public ExtendedNetwork(String networkPath, String coordinateSystem, VBox vBoxNetWork, VBox vBoxNodes, VBox vBoxLinks, VBox vBoxValidation, MapView mapView) {
        initializeMapElementLists(vBoxNetWork, vBoxNodes, vBoxLinks, vBoxValidation, mapView);
        this.network = NetworkUtils.createNetwork();
        this.networkPath = networkPath;
        this.coordinateSystem = coordinateSystem;
        System.out.println("-------------------------------------OSM READER-------------------------------------");

        long startTime = System.nanoTime();
        new OsmNetworkReader(this.network, TransformationFactory.getCoordinateTransformation(this.getCoordinateSystem(), TransformationFactory.WGS84))
                .parse(this.networkPath);

        if (this.network.getName() == null) {
            // Get the name of the imported file and set it as the network name
            Path p = Paths.get(networkPath);
            String[] parts = p.getFileName().toString().split("\\.");
            this.network.setName(parts[0]);
        }
        initializeTableViews();
        paintToMap();

        long endTime = System.nanoTime();
        //divided by 1000000 to get milliseconds.
        long duration = (endTime - startTime)/1000000;
        System.out.println("Time elapsed to load network: " + duration + "ms");
    }

    /**
     * Updates the side panels of the editor and the markers and lines on the map
     */
    public void paintToMap() {
        populateNodesTable();
        populateLinksTable();
        populateValidationTable();
        this.networkInfo.update(this.network);
    }

    /**
     * Initializes the visual boxes of the side panel and the data structures containing nodes, links and validation warnings
     * @param vBoxNetwork The visual box element containing the network information
     * @param vBoxNodes The visual box element containing the nodes information
     * @param vBoxLinks The visual box element containing the links information
     * @param vBoxValidation The visual box element containing the validation elements information
     * @param mapView The visual map component
     */
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
        this.validationWarnings = new ArrayList<>();
    }

    /**
     * Initializes the table views of the side panel, setting up the column information
     */
    public void initializeTableViews() {
        this.networkInfo = new NetworkInfo(this.network);
        ArrayList<Pair<javafx.scene.Node, javafx.scene.Node>> networkInfoNodes = this.networkInfo.getAll();

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

        // Clear nodes box in case it contains previous data
        if (vBoxLinks.getChildren().size() > 1){
            this.vBoxLinks.getChildren().remove(1);
        }
        this.vBoxLinks.getChildren().add(this.linkTable);
        this.linkTable.getColumns().addAll(idColumnLink, fromNodeColumn, toNodeColumn, lengthColumn, capacityColumn,
                freeSpeedColumn, nofLanesColumn, allowedModes);


        this.validationTable = new TableView<>();
        this.validationTable.setEditable(false);
        this.validationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn idColumnValidation = new TableColumn<>("ID");
        idColumnValidation.setMinWidth(5);
        idColumnValidation
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValidationTableEntry, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ValidationTableEntry, String> p) {
                        return new SimpleStringProperty(p.getValue().getElementId());
                    }
                });
        TableColumn messageColumnValidation = new TableColumn<>("Message");
        messageColumnValidation.setMinWidth(5);
        messageColumnValidation
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValidationTableEntry, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ValidationTableEntry, String> p) {
                        return new SimpleStringProperty(p.getValue().getMessage());
                    }
                });

        // Clear nodes box in case it contains previous data
        if (vBoxValidation.getChildren().size() > 1){
            this.vBoxValidation.getChildren().remove(1);
        }

        this.vBoxValidation.getChildren().add(this.validationTable);
        this.validationTable.getColumns().addAll(idColumnValidation, messageColumnValidation);
    }

    /**
     * Adds a node to the underlying network and refreshes the map and table views
     * @param id The id of the node to be added in the network
     * @param coordinate The coordinate of the node in the format supported by the map
     */
    public void addNode(String id, Coordinate coordinate) {
        // Swapped Lat and Long to match MATSim and create MATSim format of coordinate
        Coord coord = new Coord(coordinate.getLongitude(), coordinate.getLatitude(), 0.0);

        if (!network.getNodes().containsKey(Id.create(id, Node.class))) {
            NetworkUtils.createAndAddNode(this.network, Id.create(id, Node.class), coord);
            // Set original node id to use in editing a node, since the other node id does not support this
            NetworkUtils.setOrigId(this.network.getNodes().get(Id.create(id, Node.class)), id);
        }
        paintToMap();
    }

    /**
     * Adds a node in the network based on its coordinate and creates a new id for it
     * @param coordinate The coordinate of the node
     */
    public void addNode(Coordinate coordinate) {
        String newId = createNodeId();
        addNode(newId, coordinate);
    }

    /**
     * Creates a unique node id based on time
     * @return The node id
     */
    public String createNodeId() {
        return "node_" + System.currentTimeMillis() / 1000L;
    }

    /**
     * Edits the id and/or the coordinate of a node
     * @param oldId The old id of the node
     * @param newId The new id of the node
     * @param newCoord The new coordinate position of the edited node
     */
    public void editNode(String oldId, String newId, Coord newCoord) {
        Node node = this.network.getNodes().get(Id.create(oldId, Node.class));
        Coord currentCoord = node.getCoord();

        // If the old and new ids are the same, check for changes in the existing/new coordinates of the node
        if (!newId.equals(oldId)) {
            // Check that no node with this id already exists in the network
            for (Node currentNode : this.network.getNodes().values()) {
                if (NetworkUtils.getOrigId(currentNode).equals(newId)) {
                    break;
                } else {
                    NetworkUtils.setOrigId(node, newId);
                }
            }
        }
        else {
            // Set the original id into the already existing id
            NetworkUtils.setOrigId(node, oldId);
        }

        // If the coordinate was changed, set it in the node, remove the marker and attached lines from map and data structures,
        // then refresh the map elements to repaint the node
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

    /**
     * Adds a link to the network with an id, from/to node coordinates and attributes, and paints it on the map
     * @param id Id of the new link
     * @param nodeA The coordinate of the 'from' node of the link
     * @param nodeB The coordinate of the 'to' node of the link
     * @param length The length of the link, should be matching the distance between the two node coordinates
     * @param freespeed Maximum speed that vehicles are allowed to travel along the link, typically in meters per second.
     * @param capacity The number of vehicles that traverse the link, typically in vehicles per hour
     * @param numLanes The number of lanes (permlanes) available in the direction specified by the ’from’ and ’to’ nodes.
     */
    public void addLink(String id, Coordinate nodeA, Coordinate nodeB, double length, double freespeed,
                        double capacity, double numLanes) {
        // TODO Check if link id already exists and other checks
        Node fromNode = findNodeByCoordinate(nodeA);
        Node toNode = findNodeByCoordinate(nodeB);
        if (fromNode != null && toNode != null) {
            NetworkUtils.createAndAddLink(network, Id.create(id, Link.class), fromNode, toNode, length, freespeed,
                    capacity, numLanes);
            paintToMap();
        }
    }

    /**
     * Adds a link to the network creating a new id for it, and paints it on the map
     * @param nodeA The coordinate of the 'from' node of the link
     * @param nodeB The coordinate of the 'to' node of the link
     * @param length The length of the link, should be matching the distance between the two node coordinates
     * @param freespeed Maximum speed that vehicles are allowed to travel along the link, typically in meters per second.
     * @param capacity The number of vehicles that traverse the link, typically in vehicles per hour
     * @param numLanes The number of lanes (permlanes) available in the direction specified by the ’from’ and ’to’ nodes.
     */
    public void addLink(Coordinate nodeA, Coordinate nodeB, double length, double freespeed, double capacity, double numLanes){
        String newId = createLinkId();
        addLink(newId, nodeA, nodeB, length, freespeed, capacity, numLanes);
    }

    /**
     * Creates a unique link id based on time
     * @return The link id
     */
    public String createLinkId() {
        return "link_" + System.currentTimeMillis() / 1000L;
    }

    /**
     * Adds link to the network and paints it on the map
     * @param id The id of the new link
     * @param nodeAId Id of the 'from' node of the link
     * @param nodeBId Id of the 'to' node of the link
     * @param length The length of the link, should be matching the distance between the two node coordinates
     * @param freespeed Maximum speed that vehicles are allowed to travel along the link, typically in meters per second.
     * @param capacity The number of vehicles that traverse the link, typically in vehicles per hour
     * @param numLanes The number of lanes (permlanes) available in the direction specified by the ’from’ and ’to’ nodes.
     * @return true if link is successfully added to the network and painted on the map, false if otherwise
     */
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

    /**
     * Gives the user the option to edit a link's id or attributes. Capacity is not included‚‚
     * @param oldId The current id of the link
     * @param newId The new link id set by the user/dialog
     * @param length The length of the link, should be matching the distance between the two node coordinates
     * @param freespeed Maximum speed that vehicles are allowed to travel along the link, typically in meters per second.
     * @param capacity The number of vehicles that traverse the link, typically in vehicles per hour
     * @param numLanes The number of lanes (permlanes) available in the direction specified by the ’from’ and ’to’ nodes.
     * @return True if the link is successfully changed, otherwise false
     */
    public boolean editLink(String oldId, String newId, double length, double freespeed, double capacity, double numLanes) {
        Link link = this.network.getLinks().get(Id.create(oldId, Link.class));

        // If the new id is different than the previous one, create new link with these attributes and remove the old one from the network
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

    /**
     * Removes a node from the network, and the map, along with the in- and outlinks attached to it
     * @param id The id of the node to be removed
     * @return True if the node is successfully removed, otherwise false
     */
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
        // Remove node from network and map, along with attached links
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

    /**
     * Remove node by coordinate
     * @param coordinate The coordinates of the node in the map format
     * @return True if the node is successfully removed from the network and the map, otherwise false
     */
    public boolean removeNode(Coordinate coordinate) {
        Node node = findNodeByCoordinate(coordinate);
        return removeNode(node.getId().toString());
    }

    /**
     * Removes a link from the network and the map based on its id
     * @param id The id of the link to be removed
     * @return True if the link is successfully removed from the network and the map, otherwise false
     */
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

    /**
     *
     * @param nodeAid
     * @param nodeBid
     * @return
     */
    public boolean removeLink(String nodeAid, String nodeBid) {
        // TODO Implement - used in editlink for Bidirectional
        return true;
    }

    /**
     * Finds and returns a Node in the network using its coordinates
     * @param coordinate The coordinates of the Node searched for in the network
     * @return The Node object if it is found in the network, otherwise null
     */
    private Node findNodeByCoordinate(Coordinate coordinate) {
        // Swap Lat and Long to match MATSim notation
        Coord coord = new Coord(coordinate.getLongitude(), coordinate.getLatitude(), 0.0);
        // Pass through the nodes of the network, check for their coordinates and if they match the parameter
        // coordinates, return the respective Node object
        for (Entry<Id<Node>, ? extends Node> entry : this.network.getNodes().entrySet()) {
            Coord entryCoord = entry.getValue().getCoord();
            if (entryCoord.getX() == coord.getX() && entryCoord.getY() == coord.getY()) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Creates a node description with id and coordinates to display a label on the editor
     * @param coordinate The coordinate of the node in the map format
     * @return The formulated description in the form node_id -> x: coordinate_x y: coordinate_y
     * if the node exists in the network, otherwise false
     */
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

    /**
     * Fills the node tableview with the nodes of the network and creates the node markers out of them
     */
    private void populateNodesTable() {
        ObservableList<Node> nodeData = FXCollections.observableArrayList(this.network.getNodes().values());
        this.nodeTable.getItems().clear();
        this.nodeTable.setItems(nodeData);
        this.nodeTable.refresh();
        createNodesMarkers(nodeData);
    }

    /**
     * Creates markers for nodes of the network that do not have a marker already, stores them by node id
     * in the respective data structure and paints them on the map
     * @param data The list containing all the node ids of the network
     */
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

    /**
     * Fills the link tableview with the links of the network and creates the link lines out of them
     */
    private void populateLinksTable() {
        ObservableList<Link> linkData = FXCollections.observableArrayList(this.network.getLinks().values());
        this.linkTable.getItems().clear();
        this.linkTable.setItems(linkData);
        this.linkTable.refresh();
        createlinkLines(linkData);
    }

    /**
     * Creates lines for the links of the network that do not have one already, stores them by link id
     * in the respective data structure and paints them on the map
     * @param data The list containing all the links of the network
     */
    private void createlinkLines(ObservableList<Link> data) {
        for (Link link : data) {
            if (!this.linkLines.containsKey(link.getId())) {
                // Create coordinate objects by swapping x, y to match MATSim coordinates
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

    /**
     * Fills the validation tableview with the validation warnings of the network
     */
    public void populateValidationTable() {
        ObservableList<ValidationTableEntry> validationData = FXCollections.observableArrayList(this.validationWarnings);
        this.validationTable.getItems().clear();
        this.validationTable.setItems(validationData);
        this.validationTable.refresh();
    }

    /**
     * @return The hashmap containing the mappings of node ids to map markers
     */
    public HashMap<Id<Node>, Marker> getNodeMarkers() {
        return this.nodeMarkers;
    }

    /**
     * @return The hashmap containing the mappings of link ids to map lines
     */
    public HashMap<Id<Link>, CoordinateLine> getLinkLines() {
        return this.linkLines;
    }

    /**
     * @return The object containing all the MATSim network information including
     * nodes, links and attributes
     */
    public Network getNetwork() {
        return this.network;
    }

    /**
     * @return A table view containing nodes, used to visualize the node id and coordinates
     * on the side panel of the editor
     */
    public TableView<Node> getNodeTable(){
        return this.nodeTable;
    }

    /**
     * @return A table view containing links, used to visualize the link id, from and to nodes
     * and the link attributes on the side panel of the editor
     */
    public TableView<Link> getLinkTable(){
        return this.linkTable;
    }

    /**
     * @return A table view containing entries for the validation warnings, used to visualize
     * the node or link that might have an issue, and the respective warning message on the
     * side panel of the editor
     */
    public TableView<ValidationTableEntry> getValidationTable() {
        return this.validationTable;
    }

    /**
     * @return A string containing the coordinate system the user wants to store the network in
     */
    public String getCoordinateSystem() {
        return this.coordinateSystem;
    }

    /**
     * @return A list of validation entries, containing the nodes/links that might be problematic
     * and the respective warning messages
     */
    public ArrayList<ValidationTableEntry> getValidationWarnings() {
        return this.validationWarnings;
    }

    /**
     * The coordinate system is a string containing the format of the coordinates
     * that the user wants to use to save the network. The map itself uses the WGS84
     * coordinate system
     *
     * @param coordinateSystem must be a valid coordinate system number
     */
    public void setCoordinateSystem(String coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }

    /**
     * Removes the node, link and validation entries from the side panel tables
     * and removes node markers and link lines from the map
     */
    public void clear() {
        this.nodeTable = new TableView<>();
        this.linkTable = new TableView<>();
        this.validationTable = new TableView<>();

        for (Entry<Id<Node>, Marker> entry : this.nodeMarkers.entrySet()) {
            mapView.removeMarker(entry.getValue());
        }
        this.nodeMarkers.clear();

        for (Entry<Id<Link>, CoordinateLine> entry : this.linkLines.entrySet()) {
            mapView.removeCoordinateLine(entry.getValue());
        }
        this.linkLines.clear();
        this.validationWarnings.clear();

        this.nodeMarkers = new HashMap<>();
        this.linkLines = new HashMap<>();
        this.validationWarnings = new ArrayList<>();
    }

    /**
     * Checks if there is a link in the network, connecting two specific nodes, using the coordinates of these nodes
     * @param coordinateFrom The coordinate of the starting node
     * @param coordinateTo The coordinate of the ending node
     * @return True if a link exists between those two node in the specific direction, otherwise false
     */
    public boolean containsLink(Coordinate coordinateFrom, Coordinate coordinateTo) {
        for (Link link : this.network.getLinks().values()) {
            // Swap X and Y to match MATSim notation
            if ((link.getFromNode().getCoord().getY() == coordinateFrom.getLatitude() &&
                    link.getFromNode().getCoord().getX() == coordinateFrom.getLongitude()) &&
                    (link.getToNode().getCoord().getY() == coordinateTo.getLatitude() &&
                            link.getToNode().getCoord().getX() == coordinateTo.getLongitude())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is a link in the network, connecting two specific nodes, using the ids of these nodes
     * @param nodeFrom The id of the possible starting node
     * @param nodeTo The id of the possible ending node
     * @return True if a link exists between those two node in the specific direction, otherwise false
     */
    public boolean containsLink(Id<Node> nodeFrom, Id<Node> nodeTo) {
        for (Link link : this.network.getLinks().values()) {
            if (link.getFromNode().getId() == nodeFrom && link.getToNode().getId() == nodeTo) {
                return true;
            }
        }
        return false;
    }
}