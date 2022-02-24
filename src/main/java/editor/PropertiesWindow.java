package editor;

import imgui.ImGui;
import jade.GameObject;
import jade.MouseListener;
import org.lwjgl.glfw.GLFW;
import renderer.PickingTexture;
import scenes.Scene;

public class PropertiesWindow {

    private GameObject activeGameObject = null;
    private PickingTexture pickingTexture;

    public PropertiesWindow(PickingTexture pickingTexture) {
        this.pickingTexture = pickingTexture;
    }

    public void update(float dt, Scene currentScene) {
        if (MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            int x = (int) MouseListener.getScreenX();
            int y = (int) MouseListener.getScreenY();
            activeGameObject = currentScene.getGameObject(pickingTexture.readPixel(x, y));
        }
    }

    public void imGui() {
        if (activeGameObject != null) {
            ImGui.begin("Properties");
            activeGameObject.imGui();
            ImGui.end();
        }
    }
}
