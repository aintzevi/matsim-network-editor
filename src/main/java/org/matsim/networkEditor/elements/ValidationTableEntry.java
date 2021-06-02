package org.matsim.networkEditor.elements;

public class ValidationTableEntry {
    private Object element = null;
    private String elementId = null;
    private String message = null;

    public ValidationTableEntry() {

    }

    public ValidationTableEntry(Object element, String elementId, String message) {
        this.element = element;
        this.elementId = elementId;
        this.message = message;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) { this.elementId = elementId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getElement() {
        return element;
    }

    public void setElement(Object element) {
        this.element = element;
    }
}
