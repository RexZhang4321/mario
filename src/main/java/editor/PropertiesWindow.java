package editor;

import components.NonPickable;
import imgui.ImGui;
import jade.GameObject;
import jade.MouseListener;
import org.lwjgl.glfw.GLFW;
import physics2d.components.Box2DCollider;
import physics2d.components.CircleCollider;
import physics2d.components.RigidBody2D;
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
            GameObject pickedObject = currentScene.getGameObject(pickingTexture.readPixel(x, y));
            if (pickedObject != null && pickedObject.getComponent(NonPickable.class) == null) {
                activeGameObject = pickedObject;
            } else if (pickedObject == null && !MouseListener.isDragging()) {
                activeGameObject = null;
            }
            debounce = 0.2f;
        }
    }

    public void imGui() {
        if (activeGameObject != null) {
            ImGui.begin("Properties");

            if (ImGui.beginPopupContextWindow("ComponentAdder")) {
                if (activeGameObject.getComponent(RigidBody2D.class) == null
                        && ImGui.menuItem("Add RigidBody")) {
                    if (activeGameObject.getComponent(RigidBody2D.class) == null) {
                        activeGameObject.addComponent(new RigidBody2D());
                    }
                }

                if (activeGameObject.getComponent(Box2DCollider.class) == null
                        && activeGameObject.getComponent(CircleCollider.class) == null
                        && ImGui.menuItem("Add Box Collider")) {
                    if (activeGameObject.getComponent(Box2DCollider.class) == null) {
                        activeGameObject.addComponent(new Box2DCollider());
                    }
                }

                if (activeGameObject.getComponent(Box2DCollider.class) == null
                        && activeGameObject.getComponent(CircleCollider.class) == null
                        && ImGui.menuItem("Add Circle Collider")) {
                    if (activeGameObject.getComponent(CircleCollider.class) == null) {
                        activeGameObject.addComponent(new CircleCollider());
                    }
                }

                ImGui.endPopup();
            }
            activeGameObject.imGui();
            ImGui.end();
        }
    }

    public GameObject getActiveGameObject() {
        return activeGameObject;
    }
}
