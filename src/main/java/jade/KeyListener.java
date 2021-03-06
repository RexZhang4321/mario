package jade;

import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

public class KeyListener {

    private static KeyListener instance;
    private boolean[] keyPressed = new boolean[350];
    private boolean[] keyBeginPress = new boolean[350];

    private KeyListener() {

    }

    public static void endFrame() {
        Arrays.fill(getInstance().keyBeginPress, false);
    }

    public static KeyListener getInstance() {
        if (KeyListener.instance == null) {
            KeyListener.instance = new KeyListener();
        }
        return KeyListener.instance;
    }

    public static void keyCallback(long window, int key, int scanCode, int action, int mods) {
        if (action == GLFW.GLFW_PRESS) {
            getInstance().keyPressed[key] = true;
            getInstance().keyBeginPress[key] = true;
        } else if (action == GLFW.GLFW_RELEASE) {
            getInstance().keyPressed[key] = false;
            getInstance().keyBeginPress[key] = false;
        }
    }

    public static boolean isKeyPressed(int keyCode) {
        if (keyCode >= getInstance().keyPressed.length) {
            return false;
        }
        return getInstance().keyPressed[keyCode];
    }

    public static boolean keyBeginPress(int keyCode) {
        return getInstance().keyBeginPress[keyCode];
    }
}
