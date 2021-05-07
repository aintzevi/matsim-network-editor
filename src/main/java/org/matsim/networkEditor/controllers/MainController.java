/*
 Copyright 2015-2020 Peter-Josef Meisch (pj.meisch@sothawo.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.matsim.networkEditor.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.sothawo.mapjfx.Configuration;
import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.Extent;
import com.sothawo.mapjfx.MapLabel;
import com.sothawo.mapjfx.MapType;
import com.sothawo.mapjfx.MapView;
import com.sothawo.mapjfx.Marker;
import com.sothawo.mapjfx.Projection;
import com.sothawo.mapjfx.WMSParam;
import com.sothawo.mapjfx.XYZParam;
import com.sothawo.mapjfx.event.MapLabelEvent;
import com.sothawo.mapjfx.event.MapViewEvent;
import com.sothawo.mapjfx.event.MarkerEvent;
import com.sothawo.mapjfx.offline.OfflineCache;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.networkEditor.elements.ExtendedNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController {

    /**
     * logger for the class.
     */
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private ExtendedNetwork extendedNetwork = null;
    /** Keeping track of the selected node and link for editing/deleting purposes */
    private Node selectedNode = null;
    private Link selectedLink = null;

    private static final Coordinate coordGermanyNorth = new Coordinate(55.05863889, 8.417527778);
    private static final Coordinate coordGermanySouth = new Coordinate(47.27166667, 10.17405556);
    private static final Coordinate coordGermanyWest = new Coordinate(51.0525, 5.866944444);
    private static final Coordinate coordGermanyEast = new Coordinate(51.27277778, 15.04361111);
    private static final Extent extentGermany = Extent.forCoordinates(coordGermanyNorth, coordGermanySouth,
            coordGermanyWest, coordGermanyEast);

    // Default settings values (zoom and center coordinates set to Munich).
    private int zoomDefault = 14;
    private Coordinate coordCenter = new Coordinate(48.1351, 11.5820);

    /** To keep track of the nodes for link addition */
    private Marker firstNodeMarker = null;
    private Marker secondNodeMarker = null;

    /** button to import network.xml */
    @FXML
    private Button buttonImport;

    /** button to create new network */
    @FXML
    private Button buttonCreate;

    /** button to save file */
    @FXML
    private Button buttonSave;

    /** button to undo action */
    @FXML
    private Button buttonUndo;

    /** button to redo action */
    @FXML
    private Button buttonRedo;

    /** button to set the map's zoom. */
    @FXML
    private Button buttonZoom;

    /** button to open the general settings. */
    @FXML
    private Button buttonSettings;

    /** Right panel with info and options for the map */
    @FXML
    private TitledPane optionsNetwork;

    /** Validation pane in rightPanel */
    @FXML
    private TitledPane validation;

    /** content box of the network pane */
    @FXML
    private VBox vboxNetwork;

    /** content box of node pane */
    @FXML
    private VBox vboxNodes;

    /** contents box of link pane */
    @FXML
    private VBox vboxLinks;

    /** button to edit selected node */
    @FXML
    private Button nodeEditButton;

    /** button to delete selected node */
    @FXML
    private Button nodeDeleteButton;

    /** button to edit selected link */
    @FXML
    private Button linkEditButton;

    /** button to delete selected link */
    @FXML
    private Button linkDeleteButton;

    /** the MapView containing the map */
    @FXML
    private MapView mapView;

    /** Glasspane sitting over the map when opening the application */
    @FXML
    private StackPane glassPane;

    /** the box containing the top controls, must be enabled when mapView is initialized */
    @FXML
    private HBox topControls;

    /** Slider to change the zoom value */
    @FXML
    private Slider sliderZoom;

    /** Accordion for all the different network information and controls */
    @FXML
    private Accordion rightControls;

    /** label to display cursor's coordinates at the bottom of the application */
    @FXML
    private Label labelCursor;

    /** label to display selected node information at the bottom of the application*/
    @FXML
    private Label labelNode;

    /** label to display the last event at the bottom of the application */
    @FXML
    private Label labelEvent;

    // TODO Check if still relevant for the application
    /** params for the WMS server. */
    private WMSParam wmsParam = new WMSParam().setUrl("http://ows.terrestris.de/osm/service?").addParam("layers",
            "OSM-WMS");

    private XYZParam xyzParams = new XYZParam()
            .withUrl("https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x})")
            .withAttributions(
                    "'Tiles &copy; <a href=\"https://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer\">ArcGIS</a>'");

    public MainController() {
        // TODO Maybe init stuff here?
        // TODO Clear the handling of creation and import dialogs. Using event handlers for button clicks?
    }

    /**
     * Called after the fxml is loaded and all objects are created. This is not
     * called initialize any more, because we need to pass in the projection before
     * initializing.
     *
     * @param projection the projection to use in the map.
     */
    public void initMapAndControls(Projection projection) {
        logger.debug("begin initialize");

        // init MapView-Cache
        final OfflineCache offlineCache = mapView.getOfflineCache();
        final String cacheDir = System.getProperty("java.io.tmpdir") + "/mapjfx-cache";

        // set the custom css file for the MapView
        mapView.setCustomMapviewCssURL(getClass().getResource("/custom_mapview.css"));

        // Connect right controls panel with network options panel
        rightControls.setExpandedPane(optionsNetwork);

        // default values for centre of the map and default zoom value, can be changed by the user
        try {
            readFromSettingsFile("./src/main/resources/.settings");
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // button images
        Image imageUndo = new Image(getClass().getResourceAsStream("/icons/undo.png"), 18, 18, true, true);
        buttonUndo.setGraphic(new ImageView(imageUndo));
        Image imageRedo = new Image(getClass().getResourceAsStream("/icons/redo.png"), 18, 18, true, true);
        buttonRedo.setGraphic(new ImageView(imageRedo));
        Image imageSettings = new Image(getClass().getResourceAsStream("/icons/settings.png"), 18, 18, true, true);
        buttonSettings.setGraphic(new ImageView(imageSettings));
        Image imageDelete = new Image(getClass().getResourceAsStream("/icons/delete.png"), 18, 18, true, true);
        nodeDeleteButton.setGraphic(new ImageView(imageDelete));
        linkDeleteButton.setGraphic(new ImageView(imageDelete));
        Image imageEdit = new Image(getClass().getResourceAsStream("/icons/edit.png"), 18, 18, true, true);
        nodeEditButton.setGraphic(new ImageView(imageEdit));
        linkEditButton.setGraphic(new ImageView(imageEdit));

        // file chooser
        buttonImport.setOnAction(event -> importNetworkDialog());
        buttonCreate.setOnAction(event -> createNetworkDialog());
        buttonUndo.setOnAction(event -> actionUndo());
        buttonRedo.setOnAction(event -> actionRedo());
        buttonSave.setOnAction(event -> saveFile());
        // node and link operations
        nodeDeleteButton.setOnAction(event -> deleteSelectedNode());
        nodeEditButton.setOnAction(event -> editSelectedNode());
        linkDeleteButton.setOnAction(event -> deleteSelectedLink());
        linkEditButton.setOnAction(event -> editSelectedLink());

        // Undo and Redo initially disabled
        buttonUndo.setDisable(true);
        buttonRedo.setDisable(true);

        // buttons initially disabled
        nodeDeleteButton.setDisable(true);
        nodeEditButton.setDisable(true);
        linkDeleteButton.setDisable(true);
        linkEditButton.setDisable(true);
        // disable Save button before a network is created
        buttonSave.setDisable(true);

        buttonSettings.setOnAction(event -> openSettings());
        // set the controls to disabled, this will be changed when the MapView is initialized
        setControlsDisable(true);

        // TODO check if button changes position when new settings are given by the user
        // wire the zoom button and connect the slider to the map's zoom
        buttonZoom.setOnAction(event -> mapView.setZoom(zoomDefault));
        sliderZoom.valueProperty().bindBidirectional(mapView.zoomProperty());

        // watch the MapView's initialized property to finish initialization
        mapView.initializedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                afterMapIsInitialized();
            }
        });

        mapView.setMapType(MapType.OSM);

        setupEventHandlers();


        // finally initialize the map view
        logger.trace("start map initialization");
        mapView.initialize(Configuration.builder().projection(projection).showZoomControls(false).build());
        logger.debug("initialization finished");
        initTransparentWelcome();

    }

    protected void initTransparentWelcome() {
        final Label label = new Label("Create or Import Network to continue...");
        label.setStyle(
                "-fx-text-fill: white; -fx-font-size: 26; -fx-font-family: Open Sans; -fx-padding: 0 0 20 0; -fx-text-alignment: center");
        StackPane.setAlignment(label, Pos.CENTER);
        glassPane.getChildren().addAll(label);
        glassPane.setStyle("-fx-background-color: rgba(38,50,56,0.7)");
        glassPane.setMaxWidth(mapView.getMaxWidth());
        glassPane.setMaxHeight(mapView.getMaxHeight());
    }

    @FXML
    protected Object importNetworkDialog() {
        if (extendedNetwork != null) {
            if (!showSaveAlert("Import network", "Are you sure you want to continue without saving?")) {
                return null;
            }
        }
        showImportOptionsDialog();
        return null;
    }

    private boolean showImportOptionsDialog() {
        // Pop up dialog to add network information
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Set coordinate system of the importing network");
        dialog.setHeaderText("Pick a system or give EPSG code:");

        // Set the button types
        ButtonType buttonTypeImport = new ButtonType("Import", ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeImport, buttonTypeCancel);

        // Create the attributes labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 30));

        ComboBox<String> coordinateOptions = new ComboBox<>();

        // Coordinate dropdown options
        coordinateOptions.getItems().addAll(TransformationFactory.DHDN_GK4, TransformationFactory.GK4,
                TransformationFactory.WGS84, "Custom");
        coordinateOptions.setValue(TransformationFactory.DHDN_GK4);

        // Set the WGS84 as default, let it editable, since the Custom option is chosen in the dropdown
        TextField epsgCode = new TextField();
        epsgCode.setDisable(true);
        epsgCode.setPromptText("4326");

        grid.add(new Label("Coordinate system:"), 0, 0);
        grid.add(new Label("EPSG code:"), 0, 1);
        Label message = new Label("Please fill in one of the above fields.");
        message.setTextFill(Color.GRAY);
        grid.add(message, 0, 2, 2, 1);

        grid.add(coordinateOptions, 1, 0);
        grid.add(epsgCode, 1, 1);

        // Enable/Disable button
        javafx.scene.Node importButton = dialog.getDialogPane().lookupButton(buttonTypeImport);
        importButton.setDisable(false);

        // Pattern for non-negative integers
        final Pattern numPattern = Pattern.compile("\\d+");

        final ChangeListener createButtonListenerEPSG = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Boolean disable = newValue.trim().isEmpty();
                if (!disable) {
                    if (!numPattern.matcher(newValue).matches()) {
                        message.setText("One or more values are not accepted numbers!");
                        message.setTextFill(Color.RED);
                        disable = true;
                    } else {
                        message.setText("Please fill in one of the above fields.");
                        message.setTextFill(Color.GRAY);
                    }
                }
                importButton.setDisable(disable);
            }
        };

        final ChangeListener createButtonListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Boolean disable = newValue.trim().isEmpty();
                if (!disable) {
                    message.setText("Please fill in one of the above fields.");
                    message.setTextFill(Color.GRAY);
                }
                // Disable the EPSG textfield unless the drop-down option is set to "Custom"
                if (!"Custom".equals(newValue)) {
                    epsgCode.setDisable(true);
                } else {
                    epsgCode.setDisable(false);
                    disable = true;
                }
                importButton.setDisable(disable);
            }
        };

        // Do some validation (using the Java 8 lambda syntax).
        coordinateOptions.valueProperty().addListener(createButtonListener);
        epsgCode.textProperty().addListener(createButtonListenerEPSG);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to list when the create button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeImport) {
                return new ArrayList<String>(
                        Arrays.asList(coordinateOptions.getValue(), epsgCode.getText()));
            }
            return null;
        });

        importButton.addEventFilter(ActionEvent.ACTION, (event) -> {
            event.consume();
            StringBuilder coordSysOption = new StringBuilder();
            if ("Custom".equals(coordinateOptions.getValue())) {
                coordSysOption.append("EPSG: ");
                coordSysOption.append(epsgCode.getText());
            } else {
                coordSysOption.append(coordinateOptions.getValue());
            }

            if (this.locateFile(coordSysOption.toString()) == false) {
                if (showImportOptionsDialog()) {
                    locateFile(coordSysOption.toString());
                } else {
                    dialog.close();
                }
            }
            else {
                dialog.close();
            }
        });

        javafx.scene.Node cancelButton = dialog.getDialogPane().lookupButton(buttonTypeCancel);

        cancelButton.addEventFilter(ActionEvent.ACTION, (event) -> {
            event.consume();
            dialog.close();
        });

        Optional<List<String>> result = dialog.showAndWait();
        result.ifPresent(list ->
        {
            String coordinateValue = list.get(0);
            String epsgCodeValue = list.get(1);
            System.out.println("Coordinate System = " + coordinateValue + ", EPSG Code = " + epsgCodeValue);

            if (!"Custom".equals(coordinateValue)) {
                // If not numbers, show the dialog again
                if (numPattern.matcher(epsgCodeValue).matches() == false) {
                    importNetworkDialog();
                }
            }
            else {
                dialog.close();
            }

            if (this.extendedNetwork != null) {
                this.extendedNetwork.clear();
                this.selectedNode = null;
                this.selectedLink = null;
            }

        });
        return false;
    }

    protected Object createNetworkDialog () {
        if (extendedNetwork != null) {
            if (!showSaveAlert("Create new network", "Are you sure you want to continue without saving?")) {
                return null;
            }
        }
        showCreateNetworkDialog();
        return null;
    }

    private boolean showCreateNetworkDialog() {
        // Pop up dialog to add network information
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Create new network");
        dialog.setHeaderText("Enter the new network attributes: ");

        // Set the button types
        ButtonType buttonTypeCreate = new ButtonType("Create", ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeCreate, buttonTypeCancel);

        // Create the attributes labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 30));

        TextField networkName = new TextField("New Network");
        ComboBox<String> coordinateOptions = new ComboBox<>();

        // Coordinate dropdown options
        coordinateOptions.getItems().addAll(TransformationFactory.DHDN_GK4, TransformationFactory.GK4,
                TransformationFactory.WGS84, "Custom");
        coordinateOptions.setValue(TransformationFactory.DHDN_GK4);

        // Set the WGS84 code as prompt, disable since default is WGS84 label
        TextField epsgCode = new TextField();
        epsgCode.setDisable(true);
        epsgCode.setPromptText("4326");

        grid.add(new Label("Network name:"), 0, 0);
        grid.add(new Label("Coordinate system:"), 0, 1);
        grid.add(new Label("EPSG code:"), 0, 2);
        Label message = new Label("Please fill in one of the above fields.");
        message.setTextFill(Color.GRAY);
        grid.add(message, 0, 3, 2, 1);

        grid.add(networkName, 1, 0);
        grid.add(coordinateOptions, 1, 1);
        grid.add(epsgCode, 1, 2);

        // Enable/Disable button bind on effectiveCellSize
        javafx.scene.Node createButton = dialog.getDialogPane().lookupButton(buttonTypeCreate);
        createButton.setDisable(false);

        // Pattern for non-negative integers
        final Pattern numPattern = Pattern.compile("\\d+");

        final ChangeListener createButtonListenerEPSG = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Boolean disable = newValue.trim().isEmpty();
                if (!disable) {
                    if (!numPattern.matcher(newValue).matches()) {
                        message.setText("One or more values are not accepted numbers!");
                        message.setTextFill(Color.RED);
                        disable = true;
                    }
                        else {
                        message.setText("Please fill in one of the above fields.");
                        message.setTextFill(Color.GRAY);
                    }
                }
                createButton.setDisable(disable);
            }
        };

        final ChangeListener createButtonListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Boolean disable = newValue.trim().isEmpty();
                if (!disable) {
                    message.setText("Please fill in one of the above fields.");
                    message.setTextFill(Color.GRAY);
                }
                // Disable the EPSG textfield unless the drop-down option is set to "Custom"
                if (!"Custom".equals(newValue)) {
                    epsgCode.setDisable(true);
                } else {
                    epsgCode.setDisable(false);
                    disable = true;
                }
                createButton.setDisable(disable);
            }
        };

        // Do some validation (using the Java 8 lambda syntax).
        networkName.textProperty().addListener(createButtonListener);
        coordinateOptions.valueProperty().addListener(createButtonListener);
        epsgCode.textProperty().addListener(createButtonListenerEPSG);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the network name field by default.
        /*
         *   TODO this doesn't work as it is, it has smth to do with initialization
         *    see here: https://stackoverflow.com/questions/12744542/requestfocus-in-textfield-doesnt-work
         */
        networkName.requestFocus();

        // Convert the result to list when the create button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeCreate) {
                return new ArrayList<String>(
                        Arrays.asList(networkName.getText(), coordinateOptions.getValue(), epsgCode.getText()));
            }
            return null;
        });

        javafx.scene.Node cancelButton = dialog.getDialogPane().lookupButton(buttonTypeCancel);

        cancelButton.addEventFilter(ActionEvent.ACTION, (event) -> {
            event.consume();
            dialog.close();
        });

        Optional<List<String>> result = dialog.showAndWait();
        result.ifPresent(list -> {
            String nameValue = list.get(0);
            String coordinateValue = list.get(1);
            String epsgCodeValue = list.get(2);

            System.out.println("Network name = " + nameValue + ", Coordinate System = " + coordinateValue
                    + ", EPSG Code = " + epsgCodeValue);

            if ("Custom".equals(coordinateValue)) {
                // If EPSG code is not a number, show the creation dialog again
                if (numPattern.matcher(epsgCodeValue).matches() == false || epsgCodeValue.trim().isEmpty()) {
                    showCreateNetworkDialog();
                }
            }
            else {
                dialog.close();
            }

            StringBuilder coordSysOption = new StringBuilder();
            if ("Custom".equals(coordinateValue)) {
                coordSysOption.append("EPSG: ");
                coordSysOption.append(epsgCodeValue);
            } else {
                coordSysOption.append(coordinateValue);
            }

            if (this.extendedNetwork != null) {
                this.extendedNetwork.clear();

                this.selectedNode = null;
                this.selectedLink = null;
            }

            this.extendedNetwork = new ExtendedNetwork(nameValue, null, null, null, vboxNetwork,
                    vboxNodes, vboxLinks, mapView);
            this.extendedNetwork.setCoordinateSystem(coordSysOption.toString());
            initializeTableListeners();
            // Enable save button and make glasspane invisible
            buttonSave.setDisable(false);
            glassPane.setVisible(false);
        });
        return false;
    }

    public void initializeTableListeners() {
        // Add listeners to tableviews
        this.extendedNetwork.getNodeTable().getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        this.selectedNode = this.extendedNetwork.getNodeTable().getSelectionModel().getSelectedItem();
                        nodeDeleteButton.setDisable(false);
                        nodeEditButton.setDisable(false);
                    } else {
                        this.selectedNode = null;
                        nodeDeleteButton.setDisable(true);
                        nodeEditButton.setDisable(true);
                    }
                });

        this.extendedNetwork.getLinkTable().getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        this.selectedLink = this.extendedNetwork.getLinkTable().getSelectionModel().getSelectedItem();
                        linkDeleteButton.setDisable(false);
                        linkEditButton.setDisable(false);
                    } else {
                        this.selectedLink = null;
                        linkDeleteButton.setDisable(true);
                        linkEditButton.setDisable(true);
                    }
                });
    }

    private void addLinkDialog(Coordinate nodeCoordinateA, Coordinate nodeCoordinateB) {
        // Pop up dialog to add link information

        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Add new link");
        dialog.setHeaderText("Enter the link's attributes: ");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Add Link", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the attributes labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 30));

        Coord coordA = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, this.extendedNetwork.getCoordinateSystem())
                .transform(CoordUtils.createCoord(nodeCoordinateA.getLongitude(), nodeCoordinateA.getLatitude()));
        Coord coordB = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, this.extendedNetwork.getCoordinateSystem())
                .transform(CoordUtils.createCoord(nodeCoordinateB.getLongitude(), nodeCoordinateB.getLatitude()));

        // Calculate distance between two coordinates to show as default
        Double nodesDistance = CoordUtils.calcEuclideanDistance(coordA, coordB);

        // Default value for faster creation (and debugging)
        TextField linkId = new TextField(this.extendedNetwork.createLinkId());
        TextField length = new TextField(nodesDistance.toString());
        TextField freeSpeed = new TextField("13.88");
        TextField capacity = new TextField("36000");
        TextField numOfLanes = new TextField("1.0");
        CheckBox bidirectionalCheckBox = new CheckBox();
        bidirectionalCheckBox.setSelected(true);

        grid.add(new Label("Link ID:"), 0, 0);
        grid.add(linkId, 1, 0);
        grid.add(new Label("From Node:"), 0, 1);
        grid.add(new Label(this.extendedNetwork.getNodeDescr(nodeCoordinateA)), 1, 1);
        grid.add(new Label("To Node:"), 0, 2);
        grid.add(new Label(this.extendedNetwork.getNodeDescr(nodeCoordinateB)), 1, 2);
        grid.add(new Label("Length:"), 0, 3);
        grid.add(length, 1, 3);
        grid.add(new Label("Free Speed:"), 0, 4);
        grid.add(freeSpeed, 1, 4);
        grid.add(new Label("Capacity:"), 0, 5);
        grid.add(capacity, 1, 5);
        grid.add(new Label("# Lanes:"), 0, 6);
        grid.add(numOfLanes, 1, 6);
        grid.add(new Label("Bidirectional"), 0, 7);
        grid.add(bidirectionalCheckBox, 1, 7);

        Label message = new Label("Please fill in all the above fields or use the defaults.");
        message.setTextFill(Color.GRAY);
        grid.add(message, 0, 8, 2, 1);

        // Enable/Disable button
        javafx.scene.Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(false);

        Pattern numPattern = Pattern.compile("^([0-9]\\.\\d+)|([1-9]\\d*\\.?\\d*)$");

        final ChangeListener createButtonListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Boolean disable = newValue.trim().isEmpty();
                if (!disable) {
                    if (!numPattern.matcher(newValue).matches()) {
                        message.setText("One or more values are not accepted numbers!");
                        message.setTextFill(Color.RED);
                        disable = true;
                    } else {
                        message.setText("Please fill in all the above fields or use the defaults.");
                        message.setTextFill(Color.GRAY);
                    }
                }
                createButton.setDisable(disable);
            }
        };

        final ChangeListener createButtonListenerLink = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Boolean disable = newValue.trim().isEmpty();
                if (disable) {
                    message.setText("Please fill in all the above fields.");
                    message.setTextFill(Color.GRAY);
                }
                createButton.setDisable(disable);
            }
        };

        linkId.textProperty().addListener(createButtonListenerLink);
        length.textProperty().addListener(createButtonListener);
        freeSpeed.textProperty().addListener(createButtonListener);
        capacity.textProperty().addListener(createButtonListener);
        numOfLanes.textProperty().addListener(createButtonListener);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to list when the create button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new ArrayList<String>(
                        Arrays.asList(linkId.getText(), length.getText(), freeSpeed.getText(), capacity.getText(), numOfLanes.getText(),
                                String.valueOf(bidirectionalCheckBox.isSelected())));
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();
        result.ifPresent(list -> {
            String dlinkId = list.get(0);
            double dLength = Double.parseDouble(list.get(1));
            double dFreeSpeed = Double.parseDouble(list.get(2));
            double dCapacity = Double.parseDouble(list.get(3));
            double dLanes = Double.parseDouble(list.get(4));
            boolean isBidirectional = Boolean.parseBoolean(list.get(5));

            // Node descriptions here have the form "Id -> x: y: " e.g. "2 -> x: 11.586334449768067 y: 48.135529608558556" (x, y in MATSim notation)
            System.out.println("Created link-> LinkId:" + dlinkId + ", From Node:" + this.extendedNetwork.getNodeDescr(nodeCoordinateA) + ", To Node:"
                    + this.extendedNetwork.getNodeDescr(nodeCoordinateA) + ", Length:" + dLength + ", Free Speed:" + dFreeSpeed + ", Capacity:"
                    + dCapacity + ", #Lanes:" + dLanes + ", Bidirectional:" + isBidirectional);

            if (!this.extendedNetwork.containsLink(firstNodeMarker.getPosition(), secondNodeMarker.getPosition())) {
                this.extendedNetwork.addLink(dlinkId, firstNodeMarker.getPosition(), secondNodeMarker.getPosition(), dLength,
                        dFreeSpeed, dCapacity, dLanes);
                if (isBidirectional) {
                    if (NetworkUtils.findLinkInOppositeDirection(this.extendedNetwork.getNetwork().getLinks().get(Id.create(dlinkId, Link.class))) == null) {
                    //if (!this.extendedNetwork.containsLink(secondNodeMarker.getPosition(), firstNodeMarker.getPosition())) {
                        this.extendedNetwork.addLink(secondNodeMarker.getPosition(), firstNodeMarker.getPosition(), dLength, dFreeSpeed, dCapacity, dLanes);
                    }
                    else {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Cannot add link");
                        alert.setHeaderText(null);
                        alert.setContentText("Reverse link already exists!");

                        alert.showAndWait();
                    }
                }
            } else {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Cannot add link");
                alert.setHeaderText(null);
                alert.setContentText("Link already exists!");

                alert.showAndWait();
            }

            dialog.close();
        });
    }

    @FXML
    protected boolean saveFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Saving file...");
        // TODO When importing, set the name of the file as the name of the network?
        chooser.setInitialFileName(this.extendedNetwork.getNetwork().getName());
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("GZ Files", "*.gz"));

        try {
            File file = chooser.showSaveDialog(new Stage());
            chooser.setInitialDirectory(file.getParentFile());

            if (file != null) {
                NetworkUtils.writeNetwork(this.extendedNetwork.getNetwork(), file.getPath());
            }
        } catch (Exception exception) {
            logger.debug("Saving file fail");
            // If the user has pressed on cancel
            return false;
        }
        return true;
    }

    @FXML
    private Object actionRedo() {
        return null;
    }

    @FXML
    private Object actionUndo() {
        return null;
    }

    @FXML
    private Object openSettings() {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Settings");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the attributes labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 30));

        TextField defaultZoom = new TextField();
        defaultZoom.setPromptText("14");
        TextField centerCoordinates = new TextField();
        centerCoordinates.setPromptText("48.1351, 11.5820");

        grid.add(new Label("Default zoom value:"), 0, 0);
        grid.add(new Label("Map center coordinates:"), 0, 1);
        grid.add(defaultZoom, 1, 0);
        grid.add(centerCoordinates, 1, 1);

        dialog.getDialogPane().setContent(grid);
        // Request focus on the default zoom field by default.
        defaultZoom.requestFocus();

        // Convert the result to list when the create button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new ArrayList<String>(Arrays.asList(defaultZoom.getText(), centerCoordinates.getText()));
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();

        result.ifPresent(list -> {
            System.out.println("Default zoom = " + list.get(0) + "\n Center coordinates = " + list.get(1));

            Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

            String coordinatesString = list.get(1);
            List<String> coordinateList = Arrays.asList(coordinatesString.split(",[ ]*"));

            // TODO check this pattern matching because dialog doesn't close because of it if only one of the two values is set
            if (pattern.matcher(list.get(0)).matches() == false
                    || pattern.matcher(coordinateList.get(0)).matches() == false
                    || pattern.matcher(coordinateList.get(1)).matches() == false)
                openSettings();

            setDefaultSettingValues(list);
            try {
                writeToSettingsFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // TODO check if needed here
            mapView.setCenter(coordCenter);
            mapView.setZoom(zoomDefault);
            dialog.close();
        });
        return result;
    }

    /**
     * initializes the event handlers.
     */
    private void setupEventHandlers() {
        // Handler to add node by left clicking on the map
        mapView.addEventHandler(MapViewEvent.MAP_CLICKED, event -> {
            event.consume();
            final Coordinate coordinate = event.getCoordinate().normalize();
            labelEvent.setText("New Node created at: " + coordinate);
            this.extendedNetwork.addNode(coordinate);
        });

        // Handler to create link between two nodes - double click
        mapView.addEventHandler(MarkerEvent.MARKER_DOUBLECLICKED, event -> {
            event.consume();
            Marker marker = event.getMarker();
            mapView.removeMarker(marker);
            labelEvent.setText("Event: marker removed at: " + marker.getPosition());
            this.extendedNetwork.removeNode(marker.getPosition());
        });

        // TODO for this to work, create a node with an invisible label first, then make it visible here
        // Handler to show node info on click
        mapView.addEventHandler(MarkerEvent.MARKER_ENTERED, event -> {
            event.consume();
            // Add label to currently clicked node
            event.getMarker().attachLabel(
                    new MapLabel(event.getMarker().getId(), 10, -10).setVisible(true).setCssClass("green-label"));
        });

        // Handler to remove node marker on right click
        mapView.addEventHandler(MarkerEvent.MARKER_RIGHTCLICKED, event -> {
            event.consume();
            if (firstNodeMarker == null) {
                firstNodeMarker = event.getMarker();

                labelEvent.setText("Event: first node picked: " + firstNodeMarker.getPosition());
            } else {
                secondNodeMarker = event.getMarker();
                // Coordinate secondNodeCoordinate = secondNodeMarker.getPosition().normalize();
                labelEvent.setText("Event: second node picked: " + secondNodeMarker.getPosition());
                addLinkDialog(firstNodeMarker.getPosition(), secondNodeMarker.getPosition());
                // Clear markers and coords for next pair
                firstNodeMarker = null;
                secondNodeMarker = null;
            }
        });

        // add an event handler for MapViewEvent#MAP_EXTENT and set the extent in the map
        mapView.addEventHandler(MapViewEvent.MAP_EXTENT, event -> {
            event.consume();
            mapView.setExtent(event.getExtent());
        });

        // add an event handler for extent changes and display them in the status label
        mapView.addEventHandler(MapViewEvent.MAP_BOUNDING_EXTENT, event -> {
            event.consume();
        });

        mapView.addEventHandler(MapViewEvent.MAP_RIGHTCLICKED, event -> {
            event.consume();
            labelEvent.setText("Event: map right clicked at: " + event.getCoordinate());
        });
        mapView.addEventHandler(MarkerEvent.MARKER_CLICKED, event -> {
            event.consume();
            labelEvent.setText("Event: marker clicked: " + event.getMarker().getId());
            labelNode.setText("Selected node: " + this.extendedNetwork.getNodeDescr(event.getMarker().getPosition()));
        });
        mapView.addEventHandler(MarkerEvent.MARKER_RIGHTCLICKED, event -> {
            event.consume();
            labelEvent.setText("Event: marker right clicked: " + event.getMarker().getId());
        });
        mapView.addEventHandler(MapLabelEvent.MAPLABEL_CLICKED, event -> {
            event.consume();
            labelEvent.setText("Event: label clicked: " + event.getMapLabel().getText());
        });
        mapView.addEventHandler(MapLabelEvent.MAPLABEL_RIGHTCLICKED, event -> {
            event.consume();
            labelEvent.setText("Event: label right clicked: " + event.getMapLabel().getText());
        });

        mapView.addEventHandler(MapViewEvent.MAP_POINTER_MOVED, event -> {
            // logger.debug("pointer moved to " + event.getCoordinate());
            labelCursor.setText("Cursor at: " + event.getCoordinate().toString());
        });

        logger.debug("map handlers initialized");
    }

    /**
     * enables / disables the different controls
     *
     * @param flag if true the controls are disabled
     */
    private void setControlsDisable(boolean flag) {
        topControls.setDisable(flag);
        rightControls.setDisable(flag);
    }

    /**
     * finishes setup after the map is initialzed
     */
    private void afterMapIsInitialized() {
        logger.debug("map intialized");
        // start at the harbour with default zoom
        mapView.setZoom(zoomDefault);
        mapView.setCenter(coordCenter);
        // now enable the controls
        setControlsDisable(false);
    }

    private void readFromSettingsFile(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        try {
            List<String> list = new ArrayList<>();
            String line = br.readLine();

            while (line != null) {
                list.add(line);
                line = br.readLine();
            }
            setDefaultSettingValues(list);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
        }
    }

    private void writeToSettingsFile() throws IOException {
        String str = zoomDefault + "\n" + coordCenter.getLatitude() + ", " + coordCenter.getLongitude();
        BufferedWriter writer = new BufferedWriter(new FileWriter("./src/main/resources/.settings"));

        writer.write(str);
        writer.close();
    }

    private void setDefaultSettingValues(List<String> settingsList) {
        zoomDefault = Integer.valueOf(settingsList.get(0));
        String coordinatesString = settingsList.get(1);
        List<String> coordinateList = Arrays.asList(coordinatesString.split(",[ ]*"));

        coordCenter = new Coordinate(Double.valueOf(coordinateList.get(0)), Double.valueOf(coordinateList.get(1)));
    }

    private boolean showSaveAlert(String title, String headerText) {
        // Option to save before importing dialog
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);

        ButtonType buttonTypeDontSave = new ButtonType("Don't Save");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        ButtonType buttonTypeSave = new ButtonType("Save");

        alert.getButtonTypes().setAll(buttonTypeDontSave, buttonTypeCancel, buttonTypeSave);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == buttonTypeSave) {
            // Handle cancel at the saving stage, show the save promt again
            if (this.saveFile() == false) {
                if (showSaveAlert(title, headerText) == false)
                    return false;
            }
        } else if (result.get() == buttonTypeCancel) {
            return false;
        }
        return true;
    }

    private boolean importFile(String coordinateOptions, String epsgCode) {
        return false;
    }

    protected boolean locateFile(String coordinateSystem) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose the .xml file with your network");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("GZ Files", "*.gz"));

        File selectedFile = chooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            if (this.extendedNetwork != null) {
                this.extendedNetwork.clear();
                this.selectedNode = null;
                this.selectedLink = null;
                this.extendedNetwork.setCoordinateSystem(coordinateSystem);
            }
            this.extendedNetwork = new ExtendedNetwork(selectedFile.getPath(), this.vboxNetwork, this.vboxNodes,
                    this.vboxLinks, this.mapView);

            initializeTableListeners();
            // Enable save button and make glasspane invisible
            buttonSave.setDisable(false);
            glassPane.setVisible(false);
            return true;
        }
        return false;
    }

    private void deleteSelectedNode() {
        if (this.selectedNode != null) {
            this.extendedNetwork.removeNode(this.selectedNode.getId().toString());
            this.selectedNode = null;
            nodeDeleteButton.setDisable(true);
            nodeEditButton.setDisable(true);

        }
    }

    private void deleteSelectedLink() {
        if (this.selectedLink != null) {
            this.extendedNetwork.removeLink(this.selectedLink.getId().toString());
            this.selectedLink = null;
            linkDeleteButton.setDisable(true);
            linkEditButton.setDisable(true);
        }
    }

    private void editSelectedNode() {
        if (this.selectedNode != null) {
            // Pop up dialog to edit node information

            Dialog<List<String>> dialog = new Dialog<>();
            dialog.setTitle("Edit node");
            dialog.setHeaderText("Edit the node's attributes");

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the attributes labels and fields.
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 30));
            Coord coord = this.selectedNode.getCoord();

            // Default value for faster creation (and debugging)
            TextField newNodeIdText = new TextField(this.selectedNode.getId().toString());
            // Swap X and Y to match MATSim notation
            TextField coordinateX = new TextField(Double.toString(coord.getY()));
            TextField coordinateY = new TextField(Double.toString(coord.getX()));

            grid.add(new Label("Node ID:"), 0, 0);
            grid.add(newNodeIdText, 1, 0);
            grid.add(new Label("Coordinate X:"), 0, 1);
            grid.add(coordinateX, 1, 1);
            grid.add(new Label("Coordinate Y:"), 0, 2);
            grid.add(coordinateY, 1, 2);

            Label message = new Label("Edit the above fields and click save.");
            message.setTextFill(Color.GRAY);
            grid.add(message, 0, 7, 2, 1);

            // Enable/Disable button
            javafx.scene.Node createButton = dialog.getDialogPane().lookupButton(saveButtonType);
            createButton.setDisable(false);

            Pattern numPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

            final ChangeListener createButtonListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    Boolean disable = newValue.trim().isEmpty();
                    if (!disable) {
                        if (!numPattern.matcher(newValue).matches()) {
                            message.setText("One or more values are not accepted numbers!");
                            message.setTextFill(Color.RED);
                            disable = true;
                        } else {
                            message.setText("Please fill in all the above fields.");
                            message.setTextFill(Color.GRAY);
                        }
                    }
                    createButton.setDisable(disable);
                }
            };

            final ChangeListener createButtonListenerNode = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    Boolean disable = newValue.trim().isEmpty();
                    if (disable) {
                        message.setText("Please fill in all the above fields.");
                        message.setTextFill(Color.GRAY);
                    }
                    createButton.setDisable(disable);
                }
            };

            newNodeIdText.textProperty().addListener(createButtonListenerNode);
            coordinateX.textProperty().addListener(createButtonListener);
            coordinateY.textProperty().addListener(createButtonListener);

            dialog.getDialogPane().setContent(grid);

            // Convert the result to list when the create button is clicked.
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    return new ArrayList<String>(Arrays.asList(newNodeIdText.getText(), coordinateX.getText(), coordinateY.getText()));
                }
                return null;
            });

            Optional<List<String>> result = dialog.showAndWait();
            result.ifPresent(list -> {

                String newNodeId = list.get(0);
                Double coordX = Double.parseDouble(list.get(1));
                Double coordY = Double.parseDouble(list.get(2));

                System.out.println("Edited node-> OldNodeID: " + this.selectedNode.getId().toString() + ", NewNodeId:" + newNodeId + ", New X:"
                        + coordX + ", New Y:" + coordY);

                // Swap X and Y to match MATSim notation
                Coord newCoord = new Coord(coordY, coordX);
                this.extendedNetwork.editNode(this.selectedNode.getId().toString(), newNodeId, newCoord);
                this.selectedNode = null;
                nodeDeleteButton.setDisable(true);
                nodeEditButton.setDisable(true);
                dialog.close();
            });
            this.selectedNode = null;
            nodeDeleteButton.setDisable(true);
            nodeEditButton.setDisable(true);
        }
    }

    private void editSelectedLink() {
        if (this.selectedLink != null) {
            // Pop up dialog to edit link information

            Dialog<List<String>> dialog = new Dialog<>();
            dialog.setTitle("Edit link");
            dialog.setHeaderText("Edit the link's attributes");

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the attributes labels and fields.
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 30));

            // Default value for faster creation (and debugging)
            TextField newLinkIdField = new TextField(this.selectedLink.getId().toString());
            TextField length = new TextField(Double.toString(this.selectedLink.getLength()));
            TextField freeSpeed = new TextField(Double.toString(this.selectedLink.getFreespeed()));
            TextField capacity = new TextField(Double.toString(this.selectedLink.getCapacity()));
            TextField numOfLanes = new TextField(Double.toString(this.selectedLink.getNumberOfLanes()));
            CheckBox bidirectionalCheckBox = new CheckBox();

            grid.add(new Label("Link ID:"), 0, 0);
            grid.add(newLinkIdField, 1, 0);
            grid.add(new Label("From Node:"), 0, 1);
            grid.add(new Label(this.selectedLink.getFromNode().getId().toString()), 1, 1);
            grid.add(new Label("To Node:"), 0, 2);
            grid.add(new Label(this.selectedLink.getToNode().getId().toString()), 1, 2);
            grid.add(new Label("Length:"), 0, 3);
            grid.add(length, 1, 3);
            grid.add(new Label("FreeSpeed"), 0, 4);
            grid.add(freeSpeed, 1, 4);
            grid.add(new Label("Capacity:"), 0, 5);
            grid.add(capacity, 1, 5);
            grid.add(new Label("#Lanes"), 0, 6);
            grid.add(numOfLanes, 1, 6);
            grid.add(new Label("Bidirectional"), 0, 7);
            grid.add(bidirectionalCheckBox, 1, 7);

            Label message = new Label("Edit the above fields and click save.");
            message.setTextFill(Color.GRAY);
            grid.add(message, 0, 8, 2, 1);

            // Enable/Disable button
            javafx.scene.Node createButton = dialog.getDialogPane().lookupButton(saveButtonType);
            createButton.setDisable(false);

            Pattern numPattern = Pattern.compile("^([0-9]\\.\\d+)|([1-9]\\d*\\.?\\d*)$");

            final ChangeListener createButtonListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    Boolean disable = newValue.trim().isEmpty();
                    if (!disable) {
                        if (!numPattern.matcher(newValue).matches()) {
                            message.setText("One or more values are not valid numbers!");
                            message.setTextFill(Color.RED);
                            disable = true;
                        } else {
                            message.setText("Please fill in all the above fields.");
                            message.setTextFill(Color.GRAY);
                        }
                    }
                    createButton.setDisable(disable);
                }
            };

            final ChangeListener createButtonListenerLink = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    Boolean disable = newValue.trim().isEmpty();
                    if (disable) {
                        message.setText("Please fill in all the above fields.");
                        message.setTextFill(Color.GRAY);
                    }
                    createButton.setDisable(disable);
                }
            };

            newLinkIdField.textProperty().addListener(createButtonListenerLink);
            length.textProperty().addListener(createButtonListener);
            freeSpeed.textProperty().addListener(createButtonListener);
            capacity.textProperty().addListener(createButtonListener);
            numOfLanes.textProperty().addListener(createButtonListener);

            dialog.getDialogPane().setContent(grid);

            // Convert the result to list when the create button is clicked.
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    // Passing old link too because for some reason selectedLink equals to null after the isBidirectional check
                    return new ArrayList<String>(Arrays.asList(selectedLink.getId().toString(), newLinkIdField.getText(), length.getText(), freeSpeed.getText(), capacity.getText(),
                            numOfLanes.getText(), String.valueOf(bidirectionalCheckBox.isSelected())));
                }
                return null;
            });

            Optional<List<String>> result = dialog.showAndWait();
            result.ifPresent(list -> {
                String oldLinkId = list.get(0);
                String newLinkId = list.get(1);
                double newLength = Double.parseDouble(list.get(2));
                double newFreeSpeed = Double.parseDouble(list.get(3));
                double newCapacity = Double.parseDouble(list.get(4));
                double newLanes = Double.parseDouble(list.get(5));
                boolean isBidirectional = Boolean.parseBoolean(list.get(6));

                System.out.println("Edited link-> Old LinkId: " + oldLinkId + ", New LinkId: " + newLinkId + ", FromNode: "
                        + this.selectedLink.getFromNode().getId().toString() + ", ToNode: " + this.selectedLink.getToNode().getId().toString() +
                        ", Length: " + newLength + ", FreeSpeed: " + newFreeSpeed + ", Capacity: " + newCapacity + ", #Lanes: " + newLanes +
                        ", Bidirectional: " + isBidirectional);
                // TODO Check correctness
                if (!isBidirectional) {
                    if (NetworkUtils.findLinkInOppositeDirection(this.selectedLink) != null) {
                        this.extendedNetwork.removeLink(this.selectedLink.getToNode().getId().toString(), this.selectedLink.getFromNode().getId().toString());
                    }
                } else {
                    if (NetworkUtils.findLinkInOppositeDirection(this.selectedLink) == null) {
                        this.extendedNetwork.addLink(this.extendedNetwork.createLinkId(), this.selectedLink.getToNode().getId().toString(),
                                this.selectedLink.getFromNode().getId().toString(), newLength, newFreeSpeed, newCapacity, newLanes);
                    }
                    else {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Cannot add link");
                        alert.setHeaderText(null);
                        alert.setContentText("Reverse link already exists!");

                        alert.showAndWait();
                    }
                }

                // TODO if false, show alert to user about link ID existing?
                this.extendedNetwork.editLink(oldLinkId, newLinkId, newLength, newFreeSpeed, newCapacity, newLanes);

                // TODO This needs to be rechecked
                this.extendedNetwork.getLinkTable().sort();
                this.extendedNetwork.getLinkTable().refresh();
                this.selectedLink = null;

                linkDeleteButton.setDisable(true);
                linkEditButton.setDisable(true);
                dialog.close();
            });

            this.selectedLink = null;
            linkDeleteButton.setDisable(true);
            linkEditButton.setDisable(true);
        }
    }
}
