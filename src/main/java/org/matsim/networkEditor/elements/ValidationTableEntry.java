package org.matsim.networkEditor.elements;

import org.matsim.api.core.v01.Id;

public class ValidationTableEntry {
    private Id elementId = null;
    private String message = null;

    public ValidationTableEntry() {

    }

    public ValidationTableEntry(Id elementId, String message) {
        this.elementId = elementId;
        this.message = message;
    }

    public Id getElement() {
        return elementId;
    }

    public void setElement(Id element) {
        this.elementId = element;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
