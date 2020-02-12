package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for all the actions in the main menu
 */
public class MenuController {
    @FXML
    private MenuBar menuBar;

    public void handleMenuExit() {
        //TODO Save action here
        Platform.exit();
    }

    public void handleMenuImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import from file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xlm file", "*.xml"));
        File importedFile = fileChooser.showOpenDialog(this.menuBar.getScene().getWindow());

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            NetworkHandler handler = new NetworkHandler();
            saxParser.parse(importedFile, handler);
            /*
            Network network = null;

            List<Node> nodeList = new ArrayList<>();
            List<Link> linkList = null;
            */
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}
