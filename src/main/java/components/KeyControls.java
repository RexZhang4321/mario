package components;

import editor.PropertiesWindow;
import jade.GameObject;
import jade.KeyListener;
import jade.Window;
import org.lwjgl.glfw.GLFW;
import util.Settings;

import java.util.ArrayList;
import java.util.List;

public class KeyControls extends Component {

    private float debounceTime = 0.2f;
    private float debounce = 0f;

    @Override
    public void editorUpdate(float dt) {
        debounce -= dt;
        float multiplier = KeyListener.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) ? 0.1f : 1f;

        PropertiesWindow propertiesWindow = Window.getInstance().getImGuiLayer().getPropertiesWindow();
        GameObject activeGameObject = propertiesWindow.getActiveGameObject();
        List<GameObject> activeGameObjects = propertiesWindow.getActiveGameObjects();

        if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL)
                && KeyListener.keyBeginPress(GLFW.GLFW_KEY_D)
                && activeGameObject != null
        ) {
            GameObject newObj = activeGameObject.copy();
            Window.getScene().addGameObjectToScene(newObj);
            newObj.transform.position.add(Settings.GRID_WIDTH, 0f);
            propertiesWindow.setActiveGameObject(newObj);
            if (newObj.getComponent(StateMachine.class) != null) {
                newObj.getComponent(StateMachine.class).refreshTextures();
            }
        } else if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL)
                && KeyListener.keyBeginPress(GLFW.GLFW_KEY_D)
                && !activeGameObjects.isEmpty()) {
            List<GameObject> gameObjects = new ArrayList<>(activeGameObjects);
            propertiesWindow.clearSelected();
            for (GameObject gameObject : gameObjects) {
                GameObject copy = gameObject.copy();
                Window.getScene().addGameObjectToScene(copy);
                propertiesWindow.addActiveGameObject(copy);
                if (copy.getComponent(StateMachine.class) != null) {
                    copy.getComponent(StateMachine.class).refreshTextures();
                }
            }
        } else if (KeyListener.keyBeginPress(GLFW.GLFW_KEY_DELETE)) {
            for (GameObject gameObject : activeGameObjects) {
                gameObject.destroy();
            }
            propertiesWindow.clearSelected();
        } else if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_PAGE_DOWN) && debounce < 0) {
            debounce = debounceTime;
            for (GameObject gameObject1 : activeGameObjects) {
                gameObject1.transform.zIndex--;
            }
        } else if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_PAGE_UP) && debounce < 0) {
            debounce = debounceTime;
            for (GameObject gameObject1 : activeGameObjects) {
                gameObject1.transform.zIndex++;
            }
        } else if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_UP) && debounce < 0) {
            debounce = debounceTime;
            for (GameObject gameObject1 : activeGameObjects) {
                gameObject1.transform.position.y += Settings.GRID_HEIGHT * multiplier;
            }
        } else if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_LEFT) && debounce < 0) {
            debounce = debounceTime;
            for (GameObject gameObject1 : activeGameObjects) {
                gameObject1.transform.position.x -= Settings.GRID_WIDTH * multiplier;
            }
        } else if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_RIGHT) && debounce < 0) {
            debounce = debounceTime;
            for (GameObject gameObject1 : activeGameObjects) {
                gameObject1.transform.position.x += Settings.GRID_WIDTH * multiplier;
            }
        } else if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_DOWN) && debounce < 0) {
            debounce = debounceTime;
            for (GameObject gameObject1 : activeGameObjects) {
                gameObject1.transform.position.y -= Settings.GRID_HEIGHT * multiplier;
            }
        }
    }
}
