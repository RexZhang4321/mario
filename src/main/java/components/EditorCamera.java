package components;

import jade.Camera;
import jade.KeyListener;
import jade.MouseListener;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class EditorCamera extends Component {

    // 1 / 60 = 0.016 second per second
    // we make two frames
    private float dragDebounce = 0.032f;

    private Camera levelEditorCamera;
    private Vector2f clickOrigin;
    private boolean reset = false;
    private float lerpTime = 0.0f;
    private float dragSensitivity = 20.0f;
    private float scrollSensitivity = 0.2f;

    public EditorCamera(Camera levelEditorCamera) {
        this.levelEditorCamera = levelEditorCamera;
        this.clickOrigin = new Vector2f();
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_MIDDLE) && dragDebounce > 0) {
            this.clickOrigin = new Vector2f(MouseListener.getOrthoX(), MouseListener.getOrthoY());
            dragDebounce -= dt;
            return;
        } else if (MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)) {
            Vector2f mousePos = new Vector2f(MouseListener.getOrthoX(), MouseListener.getOrthoY());
            Vector2f delta = new Vector2f(mousePos).sub(this.clickOrigin);
            levelEditorCamera.position.sub(delta.mul(dt).mul(dragSensitivity));

            // this will smooth out the movement
            this.clickOrigin.lerp(mousePos, dt);
        }

        if (dragDebounce <= 0.0f && !MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)) {
            dragDebounce = 0.032f;
        }

        if (MouseListener.getScrollY() != 0.0f) {
            float addVal = (float) Math.pow(Math.abs(MouseListener.getScrollY() * scrollSensitivity),
                    1 / levelEditorCamera.getZoom());
            addVal *= -Math.signum(MouseListener.getScrollY());
            levelEditorCamera.addZoom(addVal);
        }

        if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_PERIOD)) {
            reset = true;
        }

        if (reset) {
            levelEditorCamera.position.lerp(new Vector2f(), lerpTime);
            levelEditorCamera.setZoom(levelEditorCamera.getZoom() + (1.0f - levelEditorCamera.getZoom()) * lerpTime);
            lerpTime += 0.1f * dt;

            if (Math.abs(levelEditorCamera.position.x) <= 5.0f && Math.abs(levelEditorCamera.position.y) <= 5.0f) {
                levelEditorCamera.position.set(0.0f, 0.0f);
                levelEditorCamera.setZoom(1.0f);
                reset = false;
                lerpTime = 0.0f;
            }
        }
    }
}
