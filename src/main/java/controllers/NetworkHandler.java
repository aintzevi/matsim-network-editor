package controllers;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;

public class NetworkHandler extends DefaultHandler {
    //As we read any XML element we will push that in this stack
    private Stack elementStack = new Stack();
    //As we complete one user block in XML, we will push the User instance in userList
    private Stack objectStack = new Stack();

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        //Push it in element stack
        this.elementStack.push(qName);

        switch (qName) {
            // create object
            case "node":
//                Node newNode = new Node();
                System.out.println("Node id: " + attributes.getValue("id"));
                System.out.println("x-coord: " + attributes.getValue("x"));
                System.out.println("y-coord: " + attributes.getValue("y"));
                break;
            case "link":
//                Link newLink = new Link();
                System.out.println("Link id: " + attributes.getValue("id"));
                System.out.println("From: " + attributes.getValue("from"));
                System.out.println("To: " + attributes.getValue("to"));
                System.out.println("Length: " + attributes.getValue("length"));
                System.out.println("Capacity: " + attributes.getValue("capacity"));
                System.out.println("Freespeed: " + attributes.getValue("freespeed"));
                System.out.println("Parmlanes: " + attributes.getValue("parmlanes"));
                break;
            default:
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        // TODO Add item in list
        switch (qName) {
            case "node":
            case "link":
                break;
        }
    }

    @Override
    public void endDocument() {
        System.out.println("End");
    }

    @Override
    public void warning(SAXParseException e) {
        System.out.println("Warning   : " + e.getMessage());
    }

    @Override
    public void error(SAXParseException e) {
        System.out.println("Wrror     : " + e.getMessage());
    }

    @Override
    public void fatalError(SAXParseException e) {
        System.out.println("FatalError: " + e.getMessage());
    }
}
