package jade;

import org.lwjgl.glfw.GLFW;

import java.awt.event.KeyEvent;

public class LevelEditorScene extends Scene {

    private boolean changingScene;
    private float timeToChangeScene;

    public LevelEditorScene() {
        changingScene = false;
        timeToChangeScene = 2.0f;
        System.out.println("Inside LevelEditorScene");
    }

    @Override
    public void update(float dt) {
        if (!changingScene && KeyListener.isKeyPressed(KeyEvent.VK_SPACE)) {
            changingScene = true;
        }

        if (changingScene && timeToChangeScene > 0) {
            timeToChangeScene -= dt;
            Window.getInstance().r -= dt * 0.5f;
            Window.getInstance().g -= dt * 0.5f;
            Window.getInstance().b -= dt * 0.5f;
        } else if (changingScene) {
            Window.changeScene(1);
        }
    }
}
