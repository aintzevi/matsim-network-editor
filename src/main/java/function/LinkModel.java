package function;

import javafx.beans.property.StringProperty;

public class LinkModel {
    private StringProperty id;
    private StringProperty from;
    private StringProperty to;
    private StringProperty length;
    private StringProperty capacity;
    private StringProperty freespeed;
    private StringProperty permLanes;

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getFrom() {
        return from.get();
    }

    public StringProperty fromProperty() {
        return from;
    }

    public void setFrom(String from) {
        this.from.set(from);
    }

    public String getTo() {
        return to.get();
    }

    public StringProperty toProperty() {
        return to;
    }

    public void setTo(String to) {
        this.to.set(to);
    }

    public String getLength() {
        return length.get();
    }

    public StringProperty lengthProperty() {
        return length;
    }

    public void setLength(String length) {
        this.length.set(length);
    }

    public String getCapacity() {
        return capacity.get();
    }

    public StringProperty capacityProperty() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity.set(capacity);
    }

    public String getFreespeed() {
        return freespeed.get();
    }

    public StringProperty freespeedProperty() {
        return freespeed;
    }

    public void setFreespeed(String freespeed) {
        this.freespeed.set(freespeed);
    }

    public String getPermLanes() {
        return permLanes.get();
    }

    public StringProperty permLanesProperty() {
        return permLanes;
    }

    public void setPermLanes(String permLanes) {
        this.permLanes.set(permLanes);
    }
}
