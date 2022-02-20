package components;

import jade.GameObject;
import jade.MouseListener;
import jade.Window;
import org.lwjgl.glfw.GLFW;

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
    public void update(float dt) {
        super.update(dt);
        if (holdingObject != null) {
            holdingObject.transform.position.x = MouseListener.getOrthoX() - 16;
            holdingObject.transform.position.y = MouseListener.getOrthoY() - 16;

            if (MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                place();
            }
        }
    }
}
