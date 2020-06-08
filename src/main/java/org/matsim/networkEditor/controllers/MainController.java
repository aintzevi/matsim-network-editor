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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
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

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.networkEditor.elements.ExtendedNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
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
    private Node selectedNode = null;
    private Link selectedLink = null;

    private static final Coordinate coordGermanyNorth = new Coordinate(55.05863889, 8.417527778);
    private static final Coordinate coordGermanySouth = new Coordinate(47.27166667, 10.17405556);
    private static final Coordinate coordGermanyWest = new Coordinate(51.0525, 5.866944444);
    private static final Coordinate coordGermanyEast = new Coordinate(51.27277778, 15.04361111);
    private static final Extent extentGermany = Extent.forCoordinates(coordGermanyNorth, coordGermanySouth,
            coordGermanyWest, coordGermanyEast);
    /**
     * default settings values (zoom and center coordinates).
     */
    private int zoomDefault = 14;
    // Map center coordinates, set to Munich
    private Coordinate coordCenter = new Coordinate(48.1351, 11.5820);

    // Save nodes for link addition
    private Marker firstNodeMarker = null;
    private Marker secondNodeMarker = null;

    @FXML
    /** button to import network.xml */
    private Button buttonImport;

    @FXML
    /** button to create new network */
    private Button buttonCreate;

    @FXML
    /** button to save file */
    private Button buttonSave;
    @FXML
    /** button to undo action */
    private Button buttonUndo;

    @FXML
    /** button to redo action */
    private Button buttonRedo;

    @FXML
    /** button to set the map's zoom. */
    private Button buttonZoom;

    @FXML
    /** button to open the general settings. */
    private Button buttonSettings;

    /** map options pane in leftPanel */
    @FXML
    private TitledPane optionsMapType;

    /** map options pane in leftPanel */
    @FXML
    private TitledPane optionsNetwork;

    /** contents of network pane */
    @FXML
    private VBox vboxNetwork;

    /** contents of node pane */
    @FXML
    private VBox vboxNodes;

    /** contents of link pane */
    @FXML
    private VBox vboxLinks;

    @FXML
    private Button nodeEditButton;

    @FXML
    private Button nodeDeleteButton;

    @FXML
    private Button linkEditButton;

    @FXML
    private Button linkDeleteButton;

    /**
     * the MapView containing the map
     */
    @FXML
    private MapView mapView;

    @FXML
    private StackPane glassPane;

    /**
     * the box containing the top controls, must be enabled when mapView is
     * initialized
     */
    @FXML
    private HBox topControls;

    /**
     * Slider to change the zoom value
     */
    @FXML
    private Slider sliderZoom;

    /**
     * Accordion for all the different options
     */
    @FXML
    private Accordion rightControls;

    /**
     * for editing the animation duration
     */
    @FXML
    private TextField animationDuration;

    /**
     * the BIng Maps API Key.
     */
    @FXML
    private TextField bingMapsApiKey;

    /**
     * label to display cursor's coordinates.
     */
    @FXML
    private Label labelCursor;

    /**
     * label to display selected node information.
     */
    @FXML
    private Label labelNode;

    /**
     * label to display the last event.
     */
    @FXML
    private Label labelEvent;

    /**
     * RadioButton for MapStyle OSM
     */
    @FXML
    private RadioButton radioMsOSM;

    /**
     * RadioButton for MapStyle Stamen Watercolor
     */
    @FXML
    private RadioButton radioMsSTW;

    /**
     * RadioButton for MapStyle Bing Roads
     */
    @FXML
    private RadioButton radioMsBR;

    /**
     * RadioButton for MapStyle Bing Roads - dark
     */
    @FXML
    private RadioButton radioMsCd;

    /**
     * RadioButton for MapStyle Bing Roads - grayscale
     */
    @FXML
    private RadioButton radioMsCg;

    /**
     * RadioButton for MapStyle Bing Roads - light
     */
    @FXML
    private RadioButton radioMsCl;

    /**
     * RadioButton for MapStyle Bing Aerial
     */
    @FXML
    private RadioButton radioMsBA;

    /**
     * RadioButton for MapStyle Bing Aerial with Label
     */
    @FXML
    private RadioButton radioMsBAwL;

    /**
     * RadioButton for MapStyle WMS.
     */
    @FXML
    private RadioButton radioMsWMS;

    /**
     * RadioButton for MapStyle XYZ
     */
    @FXML
    private RadioButton radioMsXYZ;

    /**
     * ToggleGroup for the MapStyle radios
     */
    @FXML
    private ToggleGroup mapTypeGroup;

    /**
     * Check Button for constraining th extent.
     */
    @FXML
    private CheckBox checkConstrainGermany;

    /**
     * params for the WMS server.
     */
    private WMSParam wmsParam = new WMSParam().setUrl("http://ows.terrestris.de/osm/service?").addParam("layers",
            "OSM-WMS");

    private XYZParam xyzParams = new XYZParam()
            .withUrl("https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x})")
            .withAttributions(
                    "'Tiles &copy; <a href=\"https://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer\">ArcGIS</a>'");

    public MainController() {
        // TODO Maybe init stuff here?
    }

    /**
     * called after the fxml is loaded and all objects are created. This is not
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

        rightControls.setExpandedPane(optionsNetwork);

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
        buttonImport.setOnAction(event -> locateFile());
        buttonCreate.setOnAction(event -> createNetworkDialog());
        buttonUndo.setOnAction(event -> actionUndo());
        buttonRedo.setOnAction(event -> actionRedo());
        buttonSave.setOnAction(event -> saveFile());
        nodeDeleteButton.setOnAction(event -> deleteSelectedNode());
        nodeEditButton.setOnAction(event -> editSelectedNode());
        linkDeleteButton.setOnAction(event -> deleteSelectedLink());
        linkEditButton.setOnAction(event -> editSelectedLink());

        // Undo and Redo initially dissabled
        buttonUndo.setDisable(true);
        buttonRedo.setDisable(true);
        nodeDeleteButton.setDisable(true);
        nodeEditButton.setDisable(true);
        linkDeleteButton.setDisable(true);
        linkEditButton.setDisable(true);
        // Disable Save button before a network is created
        buttonSave.setDisable(true);

        buttonSettings.setOnAction(event -> openSettings());
        // set the controls to disabled, this will be changed when the MapView is
        // intialized
        setControlsDisable(true);

        // wire the zoom button and connect the slider to the map's zoom
        buttonZoom.setOnAction(event -> mapView.setZoom(zoomDefault));
        sliderZoom.valueProperty().bindBidirectional(mapView.zoomProperty());

        // add a listener to the animationDuration field and make sure we only accept
        // int values
        animationDuration.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                mapView.setAnimationDuration(0);
            } else {
                try {
                    mapView.setAnimationDuration(Integer.parseInt(newValue));
                } catch (NumberFormatException e) {
                    animationDuration.setText(oldValue);
                }
            }
        });
        animationDuration.setText("500");

        // watch the MapView's initialized property to finish initialization
        mapView.initializedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                afterMapIsInitialized();
            }
        });

        // observe the map type radiobuttons
        mapTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            logger.debug("map type toggled to {}", newValue.toString());
            MapType mapType = MapType.OSM;
            if (newValue == radioMsOSM) {
                mapType = MapType.OSM;
            } else if (newValue == radioMsBR) {
                mapType = MapType.BINGMAPS_ROAD;
            } else if (newValue == radioMsCd) {
                mapType = MapType.BINGMAPS_CANVAS_DARK;
            } else if (newValue == radioMsCg) {
                mapType = MapType.BINGMAPS_CANVAS_GRAY;
            } else if (newValue == radioMsCl) {
                mapType = MapType.BINGMAPS_CANVAS_LIGHT;
            } else if (newValue == radioMsBA) {
                mapType = MapType.BINGMAPS_AERIAL;
            } else if (newValue == radioMsBAwL) {
                mapType = MapType.BINGMAPS_AERIAL_WITH_LABELS;
            } else if (newValue == radioMsWMS) {
                mapView.setWMSParam(wmsParam);
                mapType = MapType.WMS;
            } else if (newValue == radioMsXYZ) {
                mapView.setXYZParam(xyzParams);
                mapType = MapType.XYZ;
            }
            mapView.setBingMapsApiKey(bingMapsApiKey.getText());
            mapView.setMapType(mapType);
        });
        mapTypeGroup.selectToggle(radioMsOSM);

        setupEventHandlers();

        // add the constrain listener
        checkConstrainGermany.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                mapView.constrainExtent(extentGermany);
            } else {
                mapView.clearConstrainExtent();
            }
        }));

        // finally initialize the map view
        logger.trace("start map initialization");
        mapView.initialize(Configuration.builder().projection(projection).showZoomControls(false).build());
        logger.debug("initialization finished");
        initTransparentWelcome();

    }

    protected void initTransparentWelcome() {
        final Label label = new Label("Create or Import Network to continue...");
        label.setStyle(
                "-fx-text-fill: white; -fx-font-size: 26; -fx-font-family: Open Sans -fx-padding: 0 0 20 0; -fx-text-alignment: center");
        StackPane.setAlignment(label, Pos.CENTER);
        glassPane.getChildren().addAll(label);
        glassPane.setStyle("-fx-background-color: rgba(38,50,56,0.7)");
        glassPane.setMaxWidth(mapView.getMaxWidth());
        glassPane.setMaxHeight(mapView.getMaxHeight());
    }

    @FXML
    protected void locateFile() {

        if (extendedNetwork != null) {
            if (showSaveAlert("Import new network", "Are you sure you want to import without saving?") == false) {
                return;
            }
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose your .xml file with your network");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("GZ Files", "*.gz"));

        File selectedFile = chooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            if (this.extendedNetwork != null) {
                this.extendedNetwork.clear();
                this.selectedNode = null;
                this.selectedLink = null;
            }
            this.extendedNetwork = new ExtendedNetwork(selectedFile.getPath(), this.vboxNetwork, this.vboxNodes,
                    this.vboxLinks, this.mapView);
            // Enable save button and make glasspane invisible
            buttonSave.setDisable(false);
            glassPane.setVisible(false);
        }
    }

    @FXML
    private Object createNetworkDialog() {

        if (extendedNetwork != null) {
            if (showSaveAlert("Create new network",
                    "Are you sure you want to create new network without saving?") == false) {
                return null;
            }
        }

        // Pop up dialog to add network information
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Create new network");
        dialog.setHeaderText("Enter the new network attributes: ");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the attributes labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 30));

        TextField networkName = new TextField("New Network");
        TextField capacity = new TextField("1.0");
        TextField effectiveCellSize = new TextField("7.5");

        grid.add(new Label("Network name:"), 0, 0);
        grid.add(new Label("Link capacity:"), 0, 1);
        grid.add(new Label("Effective cell size:"), 0, 2);
        grid.add(networkName, 1, 0);
        grid.add(capacity, 1, 1);
        grid.add(effectiveCellSize, 1, 2);

        // Enable/Disable button bind on effectiveCellSize
        javafx.scene.Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(false);
        final ChangeListener createButtonListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Boolean disable = newValue.trim().isEmpty();
                createButton.setDisable(disable);
            }
        };
        // Do some validation (using the Java 8 lambda syntax).
        effectiveCellSize.textProperty().addListener(createButtonListener);
        networkName.textProperty().addListener(createButtonListener);
        capacity.textProperty().addListener(createButtonListener);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the network name field by default.
        networkName.requestFocus();

        // Convert the result to list when the create button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new ArrayList<String>(
                        Arrays.asList(networkName.getText(), capacity.getText(), effectiveCellSize.getText()));
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();

        result.ifPresent(list -> {
            System.out.println("Network name = " + list.get(0) + "Capacity = " + list.get(1)
                    + ", Effective Cell Size = " + list.get(2));

            Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

            // If capacity and cell sizes are not numbers, show the creation dialog again
            if (pattern.matcher(list.get(1)).matches() == false || pattern.matcher(list.get(2)).matches() == false)
                createNetworkDialog();

            String nameValue = list.get(0);
            Double capacityValue = Double.valueOf(list.get(1));
            Double cellSizeValue = Double.valueOf(list.get(2));
            if (this.extendedNetwork != null) {
                this.extendedNetwork.clear();
                this.selectedNode = null;
                this.selectedLink = null;
            }
            this.extendedNetwork = new ExtendedNetwork(nameValue, null, cellSizeValue, capacityValue, vboxNetwork,
                    vboxNodes, vboxLinks, mapView);
            initializeTableListeners();

            // Enable save button and make glasspane invisible
            buttonSave.setDisable(false);
            glassPane.setVisible(false);

        });
        return result;
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

    private void addLinkDialog(String linkID, String nodeADescr, String nodeBDescr) {
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

        // Default value for faster creation (and debugging)
        TextField length = new TextField("10000.00");
        TextField freeSpeed = new TextField("27.78");
        TextField capacity = new TextField("36000");
        TextField numOfLanes = new TextField("1");

        grid.add(new Label("Link ID:"), 0, 0);
        grid.add(new Label(linkID), 1, 0);
        grid.add(new Label("From Node:"), 0, 1);
        grid.add(new Label(nodeADescr), 1, 1);
        grid.add(new Label("To Node:"), 0, 2);
        grid.add(new Label(nodeBDescr), 1, 2);

        grid.add(new Label("Length:"), 0, 3);
        grid.add(new Label("Free Speed:"), 0, 4);
        grid.add(new Label("Capacity:"), 0, 5);
        grid.add(new Label("# Lanes:"), 0, 6);
        Label message = new Label("Please fill in all the above fields or use the defaults.");
        message.setTextFill(Color.GRAY);
        grid.add(message, 0, 7, 2, 1);

        grid.add(length, 1, 3);
        grid.add(freeSpeed, 1, 4);
        grid.add(capacity, 1, 5);
        grid.add(numOfLanes, 1, 6);

        // Enable/Disable button
        javafx.scene.Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(false);

        Pattern numPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

        final ChangeListener createButtonListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Boolean disable = newValue.trim().isEmpty();
                if (!disable) {
                    if (!numPattern.matcher(newValue).matches()) {
                        message.setText("One or more values are not numbers!");
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

        length.textProperty().addListener(createButtonListener);
        freeSpeed.textProperty().addListener(createButtonListener);
        capacity.textProperty().addListener(createButtonListener);
        numOfLanes.textProperty().addListener(createButtonListener);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to list when the create button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new ArrayList<String>(
                        Arrays.asList(length.getText(), freeSpeed.getText(), capacity.getText(), numOfLanes.getText()));
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();
        result.ifPresent(list -> {

            System.out.println("Created link-> LinkId:" + linkID + ", From Node:" + nodeADescr + ", To Node:"
                    + nodeBDescr + ", Length:" + list.get(0) + ", Free Speed:" + list.get(1) + ", Capacity:"
                    + list.get(2) + ", #Lanes:" + list.get(3));

            Double dLength = Double.parseDouble(list.get(0));
            Double dFreeSpeed = Double.parseDouble(list.get(1));
            Double dCapacity = Double.parseDouble(list.get(2));
            Double dLanes = Double.parseDouble(list.get(3));
            this.extendedNetwork.addLink(linkID, firstNodeMarker.getPosition(), secondNodeMarker.getPosition(), dLength,
                    dFreeSpeed, dCapacity, dLanes);
            dialog.close();
        });
    }

    @FXML
    protected boolean saveFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Saving file...");
        chooser.setInitialFileName("network.xml");
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
            mapView.setCenter(coordCenter);
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

        // TODO for this to work, create a node with an invisible label first, then make
        // it visible here
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
                String linkID = String.valueOf(this.extendedNetwork.findMaxLinkId() + 1);
                addLinkDialog(linkID, this.extendedNetwork.getNodeDescr(firstNodeMarker.getPosition()),
                        this.extendedNetwork.getNodeDescr(secondNodeMarker.getPosition()));
                // Clear markers and coords for next pair
                firstNodeMarker = null;
                secondNodeMarker = null;
            }
        });

        // add an event handler for MapViewEvent#MAP_EXTENT and set the extent in the
        // map
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
            if (this.saveFile() == false) {
                if (showSaveAlert(title, headerText) == false)
                    return false;
            }
        } else if (result.get() == buttonTypeCancel) {
            return false;
        }
        return true;
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
            TextField coordinateX = new TextField(Double.toString(coord.getX()));
            TextField coordinateY = new TextField(Double.toString(coord.getY()));

            grid.add(new Label("Node ID:"), 0, 0);
            grid.add(new Label(this.selectedNode.getId().toString()), 1, 0);
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
                            message.setText("One or more values are not numbers!");
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

            coordinateX.textProperty().addListener(createButtonListener);
            coordinateY.textProperty().addListener(createButtonListener);

            dialog.getDialogPane().setContent(grid);

            // Convert the result to list when the create button is clicked.
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    return new ArrayList<String>(Arrays.asList(coordinateX.getText(), coordinateY.getText()));
                }
                return null;
            });

            Optional<List<String>> result = dialog.showAndWait();
            result.ifPresent(list -> {

                System.out.println("Edited node-> NodeId:" + this.selectedNode.getId().toString() + ", New X:"
                        + list.get(0) + ", New Y:" + list.get(1));

                Double coordX = Double.parseDouble(list.get(0));
                Double coordY = Double.parseDouble(list.get(1));
                Coord newCoord = new Coord(coordX, coordY);
                this.extendedNetwork.editNode(this.selectedNode.getId().toString(), newCoord);
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

            // Default value for faster creation (and debugging)
            TextField fromNode = new TextField(this.selectedLink.getFromNode().getId().toString());
            TextField toNode = new TextField(this.selectedLink.getToNode().getId().toString());
            TextField length = new TextField(Double.toString(this.selectedLink.getLength()));
            TextField freeSpeed = new TextField(Double.toString(this.selectedLink.getFreespeed()));
            TextField capacity = new TextField(Double.toString(this.selectedLink.getCapacity()));
            TextField numOfLanes = new TextField(Double.toString(this.selectedLink.getNumberOfLanes()));

            grid.add(new Label("Link ID:"), 0, 0);
            grid.add(new Label(this.selectedLink.getId().toString()), 1, 0);
            grid.add(new Label("From Node:"), 0, 1);
            grid.add(fromNode, 1, 1);
            grid.add(new Label("To Node:"), 0, 2);
            grid.add(toNode, 1, 2);
            grid.add(new Label("Length:"), 0, 3);
            grid.add(length, 1, 3);
            grid.add(new Label("FreeSpeed"), 0, 4);
            grid.add(freeSpeed, 1, 4);
            grid.add(new Label("Capacity:"), 0, 5);
            grid.add(capacity, 1, 5);
            grid.add(new Label("#Lanes"), 0, 6);
            grid.add(numOfLanes, 1, 6);

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
                            message.setText("One or more values are not numbers!");
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

            final ChangeListener createButtonListenerWithNodeCheck = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    Boolean disable = newValue.trim().isEmpty();
                    if (!disable) {
                        if (!numPattern.matcher(newValue).matches()) {
                            message.setText("One or more values are not numbers!");
                            message.setTextFill(Color.RED);
                            disable = true;
                        } else if (!extendedNetwork.getNetwork().getNodes()
                                .containsKey(Id.create(newValue, Node.class))) {
                            message.setText("From/To Node doesn't exist.");
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

            length.textProperty().addListener(createButtonListener);
            freeSpeed.textProperty().addListener(createButtonListener);
            capacity.textProperty().addListener(createButtonListener);
            numOfLanes.textProperty().addListener(createButtonListener);
            fromNode.textProperty().addListener(createButtonListenerWithNodeCheck);
            toNode.textProperty().addListener(createButtonListenerWithNodeCheck);

            dialog.getDialogPane().setContent(grid);

            // Convert the result to list when the create button is clicked.
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    return new ArrayList<String>(Arrays.asList(fromNode.getText(), toNode.getText(), length.getText(),
                            freeSpeed.getText(), capacity.getText(), numOfLanes.getText()));
                }
                return null;
            });

            Optional<List<String>> result = dialog.showAndWait();
            result.ifPresent(list -> {

                System.out.println("Edited link-> NodeId:" + this.selectedLink.getId().toString() + ", FromNode:"
                        + list.get(0) + ", ToNode:" + list.get(1) + ", Length:" + list.get(2) + ", FreeSpeed:"
                        + list.get(3) + ", Capacity" + list.get(4) + ", #Lanes" + list.get(5));

                this.extendedNetwork.editLink(this.selectedLink.getId().toString(), list.get(0), list.get(1),
                        Double.parseDouble(list.get(2)), Double.parseDouble(list.get(3)), Double.parseDouble(list.get(4)), Double.parseDouble(list.get(5)));
                
                this.extendedNetwork.getLinkTable().sort(); // TODO This needs to be rechecked
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
