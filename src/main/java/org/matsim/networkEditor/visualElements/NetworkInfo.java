package org.matsim.networkEditor.visualElements;

import java.util.ArrayList;

import javafx.util.Pair;
import org.matsim.api.core.v01.network.Network;

import javafx.scene.Node;
import javafx.scene.control.Label;

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

    public void toDefault() {
        name.setText("Name: ");
        numOfNodes.setText("#Nodes: ");
        numOfLinks.setText("#Links: ");
        capacity.setText("Capacity Period: ");
        effectiveLaneWidth.setText("Effective Lane Width: ");
        effectiveCellSize.setText("Effective Cell Size: ");
    }

    public void update(Network network) {
        toDefault();
        nameValue.setText(network.getName());
        numOfNodesValue.setText(String.valueOf(network.getNodes().size()));
        numOfLinksValue.setText(String.valueOf(network.getLinks().size()));
        capacityValue.setText(String.valueOf(network.getCapacityPeriod()));
        effectiveLaneWidthValue.setText(String.valueOf(network.getEffectiveLaneWidth()));
        effectiveCellSizeValue.setText(String.valueOf(network.getEffectiveCellSize()));
    }

    public ArrayList<Pair<Node, Node>> getAll() {
        return nodes;
    }


}