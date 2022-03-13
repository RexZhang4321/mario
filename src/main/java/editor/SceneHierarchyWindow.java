package editor;

import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import jade.GameObject;
import jade.Window;

import java.util.List;

public class SceneHierarchyWindow {

    private static final String PAYLOAD_DRAG_DROP_TYPE = "SceneHierarchy";

    public void imGui() {
        ImGui.begin("Scene Hierarchy");

        List<GameObject> gameObjectList = Window.getScene().getGameObjects();
        int index = 0;
        for (GameObject gameObject : gameObjectList) {
            if (!gameObject.shouldSerialize()) {
                continue;
            }
            boolean treeNodeOpen = doTreeNode(gameObject, index);
            if (treeNodeOpen) {
                ImGui.treePop();
            }
            index++;
        }
        ImGui.end();
    }

    private boolean doTreeNode(GameObject gameObject, int index) {
        ImGui.pushID(index);
        boolean isTreeNodeOpen = ImGui.treeNodeEx(
                gameObject.getName(),
                ImGuiTreeNodeFlags.DefaultOpen
                        | ImGuiTreeNodeFlags.FramePadding
                        | ImGuiTreeNodeFlags.OpenOnArrow
                        | ImGuiTreeNodeFlags.SpanAvailWidth,
                gameObject.getName()
        );
        ImGui.popID();

        if (ImGui.beginDragDropSource()) {
            ImGui.setDragDropPayload(PAYLOAD_DRAG_DROP_TYPE, gameObject);
            ImGui.text(gameObject.name);
            ImGui.endDragDropSource();
        }

        if (ImGui.beginDragDropTarget()) {
            Object payloadObject = ImGui.acceptDragDropPayload(PAYLOAD_DRAG_DROP_TYPE);
            if (payloadObject != null) {
                if (payloadObject.getClass().isAssignableFrom(GameObject.class)) {
                    GameObject payloadGameObject = ((GameObject) payloadObject);
                    System.out.println("Payload accepted");
                }
            }
            ImGui.endDragDropTarget();
        }

        return isTreeNodeOpen;
    }

}
