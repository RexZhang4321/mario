package components;

import jade.GameObject;
import jade.KeyListener;
import jade.MouseListener;
import jade.Window;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import util.Settings;

public class MouseControls extends Component {

    GameObject holdingObject = null;
    private float debounceTime = 0.1f;
    private float debounce = debounceTime;

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
        newObj.getComponent(SpriteRenderer.class).setColor(new Vector4f(1, 1, 1, 1));
        newObj.removeComponent(NonPickable.class);
        Window.getScene().addGameObjectToScene(newObj);
    }

    @Override
    public void editorUpdate(float dt) {
        debounce -= dt;
        if (holdingObject != null && debounce <= 0) {
            holdingObject.transform.position.x = MouseListener.getWorldX();
            holdingObject.transform.position.y = MouseListener.getWorldY();
            holdingObject.transform.position.x = ((int) Math.floor(holdingObject.transform.position.x / Settings.GRID_WIDTH) * Settings.GRID_WIDTH) + Settings.GRID_WIDTH / 2.0f;
            holdingObject.transform.position.y = ((int) Math.floor(holdingObject.transform.position.y / Settings.GRID_HEIGHT) * Settings.GRID_HEIGHT) + Settings.GRID_HEIGHT / 2.0f;

            if (MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                place();
                debounce = debounceTime;
            }

            if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                holdingObject.destroy();
                holdingObject = null;
            }
        }
    }
}
