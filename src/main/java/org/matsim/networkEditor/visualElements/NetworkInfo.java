package org.matsim.networkEditor.visualElements;

import java.util.ArrayList;

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
    ArrayList<Node> nodes = new ArrayList<>();


    public NetworkInfo(Network network){
        toDefault();
        name.setText(name.getText() + network.getName());
        numOfNodes.setText(numOfNodes.getText() + network.getNodes().size());
        numOfLinks.setText(numOfLinks.getText() + network.getLinks().size());
        capacity.setText(capacity.getText() + network.getCapacityPeriod());
        effectiveLaneWidth.setText(effectiveLaneWidth.getText() + network.getEffectiveLaneWidth());
        effectiveCellSize.setText(effectiveCellSize.getText() + network.getEffectiveCellSize());
        nodes.add(name);
        nodes.add(numOfNodes);
        nodes.add(numOfLinks);
        nodes.add(capacity);
        nodes.add(effectiveLaneWidth);
        nodes.add(effectiveCellSize);
    }

    public void toDefault(){
        name.setText("Name: ");
        numOfNodes.setText("#Nodes: ");
        numOfLinks.setText("#Links: ");
        capacity.setText("Capacity Period: ");
        effectiveLaneWidth.setText("Effective Lane Width: ");
        effectiveCellSize.setText("Effective Cell Size: ");
    }

    public void update(Network network){
        toDefault();
        name.setText(name.getText() + network.getName());
        numOfNodes.setText(numOfNodes.getText() + network.getNodes().size());
        numOfLinks.setText(numOfLinks.getText() + network.getLinks().size());
        capacity.setText(capacity.getText() + network.getCapacityPeriod());
        effectiveLaneWidth.setText(effectiveLaneWidth.getText() + network.getEffectiveLaneWidth());
        effectiveCellSize.setText(effectiveCellSize.getText() + network.getEffectiveCellSize());
    }

    public ArrayList<Node> getAll(){
        return nodes;
    }

    
}