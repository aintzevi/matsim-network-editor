package function;

import javafx.beans.property.StringProperty;

public class NodeModel {
    private StringProperty id;
    private StringProperty x;
    private StringProperty y;
    private StringProperty inLink;
    private StringProperty outLink;

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getX() {
        return x.get();
    }

    public StringProperty xProperty() {
        return x;
    }

    public void setX(String x) {
        this.x.set(x);
    }

    public String getY() {
        return y.get();
    }

    public StringProperty yProperty() {
        return y;
    }

    public void setY(String y) {
        this.y.set(y);
    }

    public String getInLink() {
        return inLink.get();
    }

    public StringProperty inLinkProperty() {
        return inLink;
    }

    public void setInLink(String inLink) {
        this.inLink.set(inLink);
    }

    public String getOutLink() {
        return outLink.get();
    }

    public StringProperty outLinkProperty() {
        return outLink;
    }

    public void setOutLink(String outLink) {
        this.outLink.set(outLink);
    }
}
