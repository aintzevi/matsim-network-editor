package org.matsim.networkEditor.elements;

/**
 * Creates an object containg an element, its id and a warning message, to
 * display warnings regarding the network
 */
public class ValidationTableEntry {
    private Object element = null;
    private String elementId = null;
    private String message = null;

    public ValidationTableEntry() {

    }

    /**
     * Creates an object for the validation table of the side panel of the editor,
     * with a node or link, its id and a message about the warning regarding this entry
     * @param element The Node or Link thatt these is a warning about
     * @param elementId The id of the Node or Link
     * @param message The warning message for the possibly problematic Node or Link
     */
    public ValidationTableEntry(Object element, String elementId, String message) {
        this.element = element;
        this.elementId = elementId;
        this.message = message;
    }

    /**
     * @return The id of the Node or Link that the warning is about, in String form
     */
    public String getElementId() {
        return elementId;
    }

    /**
     * @param elementId The element id is the identifier of the Node or Link that the validation
     * considers might have an issue
     */
    public void setElementId(String elementId) { this.elementId = elementId;
    }

    /**
     * @return A warning message explaining why the Node/Link needs to be checked by the user
     * It could be that a link has attribute values that might not be correct, bidirectional links
     * having different attributes, or nodes not being connected by any links
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message The warning message for the possible validation issue. These messages are manually
     * created and reflect the specific issues a network may have that prevent it from safe use with MATSim
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return The Node or Link object that could potentially be problematic for MATSim. The type of the object
     * is checked when trying to edit the object from the validation table
     */
    public Object getElement() {
        return element;
    }

    /**
     * @param element The Node or Link element that might be problematic for MATSim and the user should know about
     */
    public void setElement(Object element) {
        this.element = element;
    }
}
