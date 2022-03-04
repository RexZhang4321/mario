package components;

import jade.GameObject;
import jade.MouseListener;
import jade.Window;
import org.lwjgl.glfw.GLFW;
import util.Settings;

public class MouseControls extends Component {

    GameObject holdingObject = null;

    public void pickUpObject(GameObject gameObject) {
        holdingObject = gameObject;
        Window.getScene().addGameObjectToScene(holdingObject);
    }

    public void place() {
        holdingObject = null;
    }

    @Override
    public void editorUpdate(float dt) {
        if (holdingObject != null) {
            holdingObject.transform.position.x = MouseListener.getOrthoX();
            holdingObject.transform.position.y = MouseListener.getOrthoY();
            holdingObject.transform.position.x = (int) (holdingObject.transform.position.x / Settings.GRID_WIDTH) * Settings.GRID_WIDTH;
            holdingObject.transform.position.y = (int) (holdingObject.transform.position.y / Settings.GRID_HEIGHT) * Settings.GRID_HEIGHT;

            if (MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                place();
            }
        }
    }
}
