package components;

import jade.KeyListener;
import jade.Window;
import org.lwjgl.glfw.GLFW;

public class GizmoSystem extends Component {

    private SpriteSheet gizmos;
    private int usingGizmo = 0;

    public GizmoSystem(SpriteSheet gizmos) {
        this.gizmos = gizmos;
    }

    @Override
    public void start() {
        super.start();

        gameObject.addComponent(new TranslateGizmo(gizmos.getSprite(1),
                Window.getInstance().getImGuiLayer().getPropertiesWindow()));
        gameObject.addComponent(new ScaleGizmo(gizmos.getSprite(2),
                Window.getInstance().getImGuiLayer().getPropertiesWindow()));
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if (usingGizmo == 0) {
            // translate
            gameObject.getComponent(TranslateGizmo.class).setUsing();
            gameObject.getComponent(ScaleGizmo.class).cancelUsing();
        } else if (usingGizmo == 1) {
            // scale
            gameObject.getComponent(TranslateGizmo.class).cancelUsing();
            gameObject.getComponent(ScaleGizmo.class).setUsing();
        }

        if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_E)) {
            usingGizmo = 0;
        } else if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_R)) {
            usingGizmo = 1;
        }
    }
}
