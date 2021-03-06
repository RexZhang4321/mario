package editor;

import components.SpriteRenderer;
import imgui.ImGui;
import jade.GameObject;
import org.joml.Vector4f;
import physics2d.components.Box2DCollider;
import physics2d.components.CircleCollider;
import physics2d.components.RigidBody2D;
import renderer.PickingTexture;

import java.util.ArrayList;
import java.util.List;

public class PropertiesWindow {

    private List<GameObject> activeGameObjects = new ArrayList<>();
    private List<Vector4f> activeGameObjectOriginalColor = new ArrayList<>();
    private GameObject activeGameObject = null;
    private PickingTexture pickingTexture;

    public PropertiesWindow(PickingTexture pickingTexture) {
        this.pickingTexture = pickingTexture;
    }

    public void imGui() {
        // only show properties window only when one game object is selected
        if (activeGameObjects.size() == 1 && activeGameObjects.get(0) != null) {
            activeGameObject = activeGameObjects.get(0);
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
        return activeGameObjects.size() == 1 ? activeGameObjects.get(0) : null;
    }

    public void setActiveGameObject(GameObject gameObject) {
        // TODO: also check if the game object is in the activeGameObjects
        if (gameObject != null) {
            clearSelected();
            activeGameObjects.add(gameObject);
        }
    }

    public List<GameObject> getActiveGameObjects() {
        return activeGameObjects;
    }

    public void addActiveGameObject(GameObject gameObject) {
        if (gameObject != null) {
            SpriteRenderer spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
            if (spriteRenderer != null) {
                activeGameObjectOriginalColor.add(new Vector4f(spriteRenderer.getColor()));
                spriteRenderer.setColor(new Vector4f(0.8f, 0.8f, 0.0f, 0.8f));
            } else {
                activeGameObjectOriginalColor.add(new Vector4f());
            }
            activeGameObjects.add(gameObject);
        }
    }

    public void clearSelected() {
        if (activeGameObjectOriginalColor.size() > 0) {
            int i = 0;
            for (GameObject gameObject : activeGameObjects) {
                SpriteRenderer spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
                if (spriteRenderer != null) {
                    spriteRenderer.setColor(activeGameObjectOriginalColor.get(i));
                }
            }
        }
        activeGameObjects.clear();
        activeGameObjectOriginalColor.clear();
        activeGameObject = null;
    }

    public PickingTexture getPickingTexture() {
        return pickingTexture;
    }
}
