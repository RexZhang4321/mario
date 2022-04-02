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

    @Override
    public void editorUpdate(float dt) {
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
        } else if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL)
            && KeyListener.keyBeginPress(GLFW.GLFW_KEY_D)
            && !activeGameObjects.isEmpty()) {
            List<GameObject> gameObjects = new ArrayList<>(activeGameObjects);
            propertiesWindow.clearSelected();
            for (GameObject gameObject : gameObjects) {
                GameObject copy = gameObject.copy();
                Window.getScene().addGameObjectToScene(copy);
                propertiesWindow.addActiveGameObject(copy);
            }
        } else if (KeyListener.keyBeginPress(GLFW.GLFW_KEY_DELETE)) {
            for (GameObject gameObject : activeGameObjects) {
                gameObject.destroy();
            }
            propertiesWindow.clearSelected();
        }
    }
}
