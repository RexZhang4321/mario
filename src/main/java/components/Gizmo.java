package components;

import editor.PropertiesWindow;
import jade.*;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

public class Gizmo extends Component {

    private static final float RESIZE_FACTOR = 80.0f;

    private Vector4f xAxisColor = new Vector4f(1, 0.3f, 0.3f, 1);
    private Vector4f xAxisColorHover = new Vector4f(1, 0, 0, 1);
    private Vector4f yAxisColor = new Vector4f(0.3f, 1, 0.3f, 1);
    private Vector4f yAxisColorHover = new Vector4f(0, 1, 0, 1);

    private GameObject xAxisObject;
    private GameObject yAxisObject;
    private SpriteRenderer xAxisSprite;
    private SpriteRenderer yAxisSprite;
    protected GameObject activeGameObject = null;

    private Vector2f xAxisOffset = new Vector2f(24.0f / RESIZE_FACTOR, -6.0f / RESIZE_FACTOR);
    private Vector2f yAxisOffset = new Vector2f(-7.0f / RESIZE_FACTOR, 21.0f / RESIZE_FACTOR);

    protected boolean xAxisActive = false;
    protected boolean yAxisActive = false;

    private boolean using = false;

    private float gizmoWidth = 16 / RESIZE_FACTOR;
    private float gizmoHeight = 48 / RESIZE_FACTOR;

    private PropertiesWindow propertiesWindow;

    public Gizmo(Sprite arrowSprite, PropertiesWindow propertiesWindow) {
        this.xAxisObject = Prefabs.generateSpriteObject(arrowSprite, gizmoWidth, gizmoHeight);
        this.yAxisObject = Prefabs.generateSpriteObject(arrowSprite, gizmoWidth, gizmoHeight);
        this.xAxisSprite = xAxisObject.getComponent(SpriteRenderer.class);
        this.yAxisSprite = yAxisObject.getComponent(SpriteRenderer.class);
        this.propertiesWindow = propertiesWindow;

        // this prevents gizmos from being selected
        this.xAxisObject.addComponent(new NonPickable());
        this.yAxisObject.addComponent(new NonPickable());

        Window.getScene().addGameObjectToScene(xAxisObject);
        Window.getScene().addGameObjectToScene(yAxisObject);
    }

    @Override
    public void start() {
        super.start();
        this.xAxisObject.transform.rotation = 90;
        this.yAxisObject.transform.rotation = 180;
        this.xAxisObject.transform.zIndex = 100;
        this.yAxisObject.transform.zIndex = 100;
        this.xAxisObject.setNoSerialize();
        this.yAxisObject.setNoSerialize();
    }

    @Override
    public void update(float dt) {
        if (using) {
            this.setInactive();
        }
    }

    @Override
    public void editorUpdate(float dt) {
        if (!using) {
            return;
        }

        this.activeGameObject = this.propertiesWindow.getActiveGameObject();
        if (this.activeGameObject != null) {
            this.setActive();

            // TODO: move this into it's own keyEditorBinding component class
            if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL)
                    && KeyListener.keyBeginPress(GLFW.GLFW_KEY_D)) {
                GameObject newGameObject = this.activeGameObject.copy();
                Window.getScene().addGameObjectToScene(newGameObject);
                newGameObject.transform.position.add(0.1f, 0.1f);
                this.propertiesWindow.setActiveGameObject(newGameObject);
                return;
            } else if (KeyListener.keyBeginPress(GLFW.GLFW_KEY_DELETE)) {
                activeGameObject.destroy();
                this.setInactive();
                this.propertiesWindow.setActiveGameObject(null);
                return;
            }
        } else {
            this.setInactive();
            return;
        }

        boolean xAxisHot = checkXHoverState();
        boolean yAxisHot = checkYHoverState();

        if ((xAxisHot || xAxisActive) && MouseListener.isDragging() && MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            xAxisActive = true;
            yAxisActive = false;
        } else if ((yAxisHot || yAxisActive) && MouseListener.isDragging() && MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            yAxisActive = true;
            xAxisActive = false;
        } else {
            xAxisActive = false;
            yAxisActive = false;
        }

        if (this.activeGameObject != null) {
            this.xAxisObject.transform.position.set(this.activeGameObject.transform.position);
            this.yAxisObject.transform.position.set(this.activeGameObject.transform.position);
            this.xAxisObject.transform.position.add(xAxisOffset);
            this.yAxisObject.transform.position.add(yAxisOffset);
        }
    }

    public void setUsing() {
        this.using = true;
    }

    public void cancelUsing() {
        this.using = false;
        this.setInactive();
    }

    private void setActive() {
        this.xAxisSprite.setColor(xAxisColor);
        this.yAxisSprite.setColor(yAxisColor);
    }

    private void setInactive() {
        this.xAxisSprite.setColor(new Vector4f(0, 0, 0, 0));
        this.yAxisSprite.setColor(new Vector4f(0, 0, 0, 0));
    }

    private boolean checkXHoverState() {
        float xOffset = gizmoHeight / 2.0f;
        float yOffset = gizmoWidth / 2.0f;
        Vector2f mousePos = MouseListener.getWorld();
        if (mousePos.x >= xAxisObject.transform.position.x + xOffset - gizmoHeight
                && mousePos.x <= xAxisObject.transform.position.x + xOffset
                && mousePos.y <= xAxisObject.transform.position.y - yOffset + gizmoWidth
                && mousePos.y >= xAxisObject.transform.position.y - yOffset) {
            xAxisSprite.setColor(xAxisColorHover);
            return true;
        } else {
            xAxisSprite.setColor(xAxisColor);
            return false;
        }
    }

    private boolean checkYHoverState() {
        float xOffset = gizmoWidth / 2.0f;
        float yOffset = gizmoHeight / 2.0f;
        Vector2f mousePos = MouseListener.getWorld();
        if (mousePos.x >= yAxisObject.transform.position.x + xOffset - gizmoWidth
                && mousePos.x <= yAxisObject.transform.position.x + xOffset
                && mousePos.y <= yAxisObject.transform.position.y + yOffset
                && mousePos.y >= yAxisObject.transform.position.y + yOffset - gizmoHeight) {
            yAxisSprite.setColor(yAxisColorHover);
            return true;
        } else {
            yAxisSprite.setColor(yAxisColor);
            return false;
        }
    }
}
