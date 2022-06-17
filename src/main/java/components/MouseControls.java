package components;

import editor.PropertiesWindow;
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
    private float debounceTime = 0.2f;
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

        if (holdingObject != null) {
            holdingObject.transform.position.x = ((int) Math.floor(MouseListener.getWorldX() / Settings.GRID_WIDTH) * Settings.GRID_WIDTH) + Settings.GRID_WIDTH / 2.0f;
            holdingObject.transform.position.y = ((int) Math.floor(MouseListener.getWorldY() / Settings.GRID_HEIGHT) * Settings.GRID_HEIGHT) + Settings.GRID_HEIGHT / 2.0f;

            if (MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                float halfWidth = Settings.GRID_WIDTH / 2.0f;
                float halfHeight = Settings.GRID_HEIGHT / 2.0f;
                if (MouseListener.isDragging()
                        && !hasBlockInSquare(holdingObject.transform.position.x - halfWidth, holdingObject.transform.position.y - halfHeight)
                ) {
                    place();
                } else if(!MouseListener.isDragging() && debounce < 0) {
                    place();
                    debounce = debounceTime;
                }
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

    private boolean hasBlockInSquare(float x, float y) {
        PropertiesWindow propertiesWindow = Window.getInstance().getImGuiLayer().getPropertiesWindow();
        Vector2f start = new Vector2f(x, y);
        Vector2f end = new Vector2f(start).add(new Vector2f(Settings.GRID_WIDTH, Settings.GRID_HEIGHT));
        Vector2f startScreenFloat = MouseListener.worldToScreen(start);
        Vector2f endScreenFloat = MouseListener.worldToScreen(end);
        Vector2i startScreen = new Vector2i((int) startScreenFloat.x + 2, (int) startScreenFloat.y + 2);
        Vector2i endScreen = new Vector2i((int) endScreenFloat.x - 2, (int) endScreenFloat.y - 2);
        float[] gameObjectIds = propertiesWindow.getPickingTexture().readPixels(startScreen, endScreen);

        for (int i = 0; i < gameObjectIds.length; i++) {
            if (gameObjectIds[i] >= 0) {
                GameObject pickedObj = Window.getScene().getGameObject((int) gameObjectIds[i]);
                if (pickedObj.getComponent(NonPickable.class) == null) {
                    return true;
                }
            }
        }
        return false;
    }
}
