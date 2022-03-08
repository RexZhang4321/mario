package editor;

import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import jade.GameObject;
import jade.Window;

import java.util.List;

public class SceneHierarchyWindow {

    public void imGui() {
        ImGui.begin("Scene Hierarchy");

        List<GameObject> gameObjectList = Window.getScene().getGameObjects();
        int index = 0;
        for (GameObject gameObject : gameObjectList) {
            if (!gameObject.shouldSerialize()) {
                continue;
            }
            ImGui.pushID(index);
            boolean treeNodeOpen = ImGui.treeNodeEx(
                    gameObject.getName(),
                    ImGuiTreeNodeFlags.DefaultOpen
                            | ImGuiTreeNodeFlags.FramePadding
                            | ImGuiTreeNodeFlags.OpenOnArrow
                            | ImGuiTreeNodeFlags.SpanAvailWidth,
                    gameObject.getName()
            );
            ImGui.popID();
            if (treeNodeOpen) {
                ImGui.treePop();
            }
            index++;
        }
        ImGui.end();
    }

}
