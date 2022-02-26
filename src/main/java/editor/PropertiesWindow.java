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

    private float debounce = 0.2f;

    public PropertiesWindow(PickingTexture pickingTexture) {
        this.pickingTexture = pickingTexture;
    }

    public void update(float dt, Scene currentScene) {
        debounce -= dt;
        if (MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT) && debounce < 0) {
            int x = (int) MouseListener.getScreenX();
            int y = (int) MouseListener.getScreenY();
            activeGameObject = currentScene.getGameObject(pickingTexture.readPixel(x, y));
            debounce = 0.2f;
        }
    }

    public void imGui() {
        if (activeGameObject != null) {
            ImGui.begin("Properties");
            activeGameObject.imGui();
            ImGui.end();
        }
    }

    public GameObject getActiveGameObject() {
        return activeGameObject;
    }
}
