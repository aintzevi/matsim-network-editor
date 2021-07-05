package org.matsim.networkEditor.visualElements;

import java.util.ArrayList;

import javafx.util.Pair;
import org.matsim.api.core.v01.network.Network;

import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * Object containing information about the network, in order to display them on the side panel of the editor
 */
public class NetworkInfo {
    private Label name = new Label("Name:");
    Label numOfNodes = new Label("#Nodes:");
    Label numOfLinks = new Label("#Links:");
    Label capacity = new Label("Capacity Period:");
    Label effectiveLaneWidth = new Label("Effective Lane Width:");
    Label effectiveCellSize = new Label("Effective Cell Size:");
    Label nameValue = new Label();
    Label numOfNodesValue = new Label();
    Label numOfLinksValue = new Label();
    Label capacityValue = new Label();
    Label effectiveLaneWidthValue = new Label();
    Label effectiveCellSizeValue = new Label();
    ArrayList<Pair<Node, Node>> nodes = new ArrayList<>();

    /**
     * Creates an object for displaying the network information on the side panel of the network editor
     * @param network The MATSim network containing Nodes, Links and other network attributes used for MATSim
     */
    public NetworkInfo(Network network) {
        toDefault();
        nameValue.setText(network.getName());
        numOfNodesValue.setText(String.valueOf(network.getNodes().size()));
        numOfLinksValue.setText(String.valueOf(network.getLinks().size()));
        capacityValue.setText(String.valueOf(network.getCapacityPeriod()));
        effectiveLaneWidthValue.setText(String.valueOf(network.getEffectiveLaneWidth()));
        effectiveCellSizeValue.setText(String.valueOf(network.getEffectiveCellSize()));

        nodes.add(new Pair<>(name, nameValue));
        nodes.add(new Pair<>(numOfNodes, numOfNodesValue));
        nodes.add(new Pair<>(numOfLinks, numOfLinksValue));
        nodes.add(new Pair<>(capacity, capacityValue));
        nodes.add(new Pair<>(effectiveLaneWidth, effectiveLaneWidthValue));
        nodes.add(new Pair<>(effectiveCellSize, effectiveCellSizeValue));
    }

    /**
     * Sets the labels for the network information
     */
    public void toDefault() {
        name.setText("Name: ");
        numOfNodes.setText("#Nodes: ");
        numOfLinks.setText("#Links: ");
        capacity.setText("Capacity Period: ");
        effectiveLaneWidth.setText("Effective Lane Width: ");
        effectiveCellSize.setText("Effective Cell Size: ");
    }

    /**
     * Updates the values of the information of the network, like the name, number of nodes and links etc,
     * everytime things are changed in the network
     * @param network The MATSim network containing the nodes and links
     */
    public void update(Network network) {
        toDefault();
        nameValue.setText(network.getName());
        numOfNodesValue.setText(String.valueOf(network.getNodes().size()));
        numOfLinksValue.setText(String.valueOf(network.getLinks().size()));
        capacityValue.setText(String.valueOf(network.getCapacityPeriod()));
        effectiveLaneWidthValue.setText(String.valueOf(network.getEffectiveLaneWidth()));
        effectiveCellSizeValue.setText(String.valueOf(network.getEffectiveCellSize()));
    }

    /**
     * @return A list of the pairs of the visual nodes containing the labels of the network information
     * and the respective value
     */
    public ArrayList<Pair<Node, Node>> getAll() {
        return nodes;
    }
}