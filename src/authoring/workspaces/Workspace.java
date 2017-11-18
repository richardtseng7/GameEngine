package authoring.workspaces;

import javafx.scene.layout.Region;

public interface Workspace {
    // Every workspace has a properties file with keys of the panels and values of where in the workspace they are.
    //This is auto-generated by the workspace and auto-updated on program close.
    //A workspace uses a PanelManager to get the regions for each of the workspace's various areas, and a list of all panels if needed.

    //Knows that the camera panel is a specific entity that must be shown at all times, and this treats it differently.
    void addCameraPanel(Region cameraPanel);

    Region getWorkspace();
}
