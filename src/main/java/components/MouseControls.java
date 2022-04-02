package components;

import jade.GameObject;
import jade.KeyListener;
import jade.MouseListener;
import jade.Window;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import renderer.DebugDraw;
import renderer.PickingTexture;
import scenes.Scene;
import util.Settings;

import java.util.HashSet;
import java.util.Set;

public class MouseControls extends Component {

    GameObject holdingObject = null;
    private float debounceTime = 0.1f;
    private float debounce = debounceTime;
    private boolean boxSelectSet = false;
    private Vector2f boxSelectStart = new Vector2f();
    private Vector2f boxSelectEnd = new Vector2f();

    public void pickUpObject(GameObject gameObject) {
        if (holdingObject != null) {
            holdingObject.destroy();
        }
        holdingObject = gameObject;
        holdingObject.getComponent(SpriteRenderer.class).setColor(new Vector4f(0.8f, 0.8f, 0.8f, 0.5f));
        holdingObject.addComponent(new NonPickable());
        Window.getScene().addGameObjectToScene(holdingObject);
    }

    public void place() {
        GameObject newObj = holdingObject.copy();
        if (newObj.getComponent(StateMachine.class) != null) {
            newObj.getComponent(StateMachine.class).refreshTextures();
        }
        newObj.getComponent(SpriteRenderer.class).setColor(new Vector4f(1, 1, 1, 1));
        newObj.removeComponent(NonPickable.class);
        Window.getScene().addGameObjectToScene(newObj);
    }

    @Override
    public void editorUpdate(float dt) {
        debounce -= dt;
        PickingTexture pickingTexture = Window.getInstance().getImGuiLayer().getPropertiesWindow().getPickingTexture();
        Scene currentScene = Window.getScene();

        if (holdingObject != null && debounce <= 0) {
            holdingObject.transform.position.x = MouseListener.getWorldX();
            holdingObject.transform.position.y = MouseListener.getWorldY();
            holdingObject.transform.position.x = ((int) Math.floor(holdingObject.transform.position.x / Settings.GRID_WIDTH) * Settings.GRID_WIDTH) + Settings.GRID_WIDTH / 2.0f;
            holdingObject.transform.position.y = ((int) Math.floor(holdingObject.transform.position.y / Settings.GRID_HEIGHT) * Settings.GRID_HEIGHT) + Settings.GRID_HEIGHT / 2.0f;

            if (MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                place();
                debounce = debounceTime;
            }

            if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)
                    || MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
                holdingObject.destroy();
                holdingObject = null;
            }
        } else if (!MouseListener.isDragging() && MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT) && debounce < 0) {
            int x = (int) MouseListener.getScreenX();
            int y = (int) MouseListener.getScreenY();
            GameObject pickedObject = currentScene.getGameObject(pickingTexture.readPixel(x, y));
            if (pickedObject != null && pickedObject.getComponent(NonPickable.class) == null) {
                Window.getInstance().getImGuiLayer().getPropertiesWindow().setActiveGameObject(pickedObject);
            } else if (pickedObject == null && !MouseListener.isDragging()) {
                Window.getInstance().getImGuiLayer().getPropertiesWindow().clearSelected();
            }
            debounce = 0.2f;
        } else if (MouseListener.isDragging() && MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            if (!boxSelectSet) {
                Window.getInstance().getImGuiLayer().getPropertiesWindow().clearSelected();
                boxSelectStart = MouseListener.getScreen();
                boxSelectSet = true;
            }
            boxSelectEnd = MouseListener.getScreen();
            Vector2f boxSelectStartWorld = MouseListener.screenToWorld(boxSelectStart);
            Vector2f boxSelectEndWorld = MouseListener.screenToWorld(boxSelectEnd);
            Vector2f halfSize = (new Vector2f(boxSelectEndWorld).sub(boxSelectStartWorld)).mul(0.5f);
            DebugDraw.addBox2D(new Vector2f(boxSelectStartWorld).add(halfSize),
                    new Vector2f(halfSize).mul(2.0f), 0.0f);
        } else if (boxSelectSet) {
            boxSelectSet = false;
            int screenStartX = ((int) Math.min(boxSelectStart.x, boxSelectEnd.x));
            int screenStartY = ((int) Math.min(boxSelectStart.y, boxSelectEnd.y));
            int screenEndX = ((int) Math.max(boxSelectStart.x, boxSelectEnd.x));
            int screenEndY = ((int) Math.max(boxSelectEnd.x, boxSelectEnd.y));

            float[] gameObjectIds = pickingTexture.readPixels(
                    new Vector2i(screenStartX, screenStartY),
                    new Vector2i(screenEndX, screenEndY)
            );
            Set<Integer> gameObjectIdSet = new HashSet<>();
            for (float objId : gameObjectIds) {
                gameObjectIdSet.add(((int) objId));
            }

            for (int gameObjectId : gameObjectIdSet) {
                GameObject pickedObj = Window.getScene().getGameObject(gameObjectId);
                if (pickedObj != null && pickedObj.getComponent(NonPickable.class) == null) {
                    Window.getInstance().getImGuiLayer().getPropertiesWindow().addActiveGameObject(pickedObj);
                }
            }
        }
    }
}
