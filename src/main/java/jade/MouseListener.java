package jade;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

public class MouseListener {

    private static MouseListener instance;
    private double scrollX, scrollY;
    private double xPos, yPos, lastX, lastY;
    private boolean[] mouseButtonPressed = new boolean[9];
    private boolean isDragging;

    private int numMouseButtonDown = 0;

    private Vector2f gameViewportPos = new Vector2f();
    private Vector2f gameViewportSize = new Vector2f();

    private MouseListener() {
        this.scrollX = 0.0;
        this.scrollY = 0.0;
        this.xPos = 0.0;
        this.yPos = 0.0;
        this.lastX = 0.0;
        this.lastY = 0.0;
    }

    public static MouseListener getInstance() {
        if (MouseListener.instance == null) {
            MouseListener.instance = new MouseListener();
        }
        return MouseListener.instance;
    }

    public static void mousePosCallback(long window, double xPos, double yPos) {
        if (!Window.getInstance().getImGuiLayer().getGameViewWindow().shouldCaptureMouse()) {
            clear();
        }
        getInstance().lastX = getInstance().xPos;
        getInstance().lastY = getInstance().yPos;
        getInstance().xPos = xPos;
        getInstance().yPos = yPos;

        if (getInstance().numMouseButtonDown > 0) {
            getInstance().isDragging = true;
        }
    }

    public static Vector2f screenToWorld(Vector2f screenCoords) {
        Vector2f normalizedScreenCoords = new Vector2f(
                screenCoords.x / Window.getInstance().getWidth(),
                screenCoords.y / Window.getInstance().getHeight()
        );
        normalizedScreenCoords.mul(2.0f).sub(new Vector2f(1.0f, 1.0f));
        Camera camera = Window.getScene().camera();
        Vector4f tmp = new Vector4f(normalizedScreenCoords.x, normalizedScreenCoords.y, 0, 1);
        Matrix4f inverseView = new Matrix4f(camera.getInverseView());
        Matrix4f inverseProjection = new Matrix4f(camera.getInverseProjection());
        tmp.mul(inverseView.mul(inverseProjection));
        return new Vector2f(tmp.x, tmp.y);
    }

    public static Vector2f worldToScreen(Vector2f worldCoords) {
        Camera camera = Window.getScene().camera();
        Vector4f normalizedScreenCoordsSpacePosition = new Vector4f(worldCoords.x, worldCoords.y, 0, 1);
        Matrix4f view = new Matrix4f(camera.getViewMatrix());
        Matrix4f projection = new Matrix4f(camera.getProjectionMatrix());
        normalizedScreenCoordsSpacePosition.mul(projection.mul(view));
        Vector2f windowSpace = new Vector2f(normalizedScreenCoordsSpacePosition.x, normalizedScreenCoordsSpacePosition.y)
                .mul(1.0f / normalizedScreenCoordsSpacePosition.w);
        windowSpace.add(new Vector2f(1.0f, 1.0f)).mul(0.5f);
        windowSpace.mul(new Vector2f(Window.getInstance().getWidth(), Window.getInstance().getHeight()));
        return windowSpace;
    }

    public static void mouseButtonCallback(long window, int button, int action, int mods) {
        if (button >= getInstance().mouseButtonPressed.length) {
            return;
        }
        if (action == GLFW.GLFW_PRESS) {
            getInstance().numMouseButtonDown++;
            getInstance().mouseButtonPressed[button] = true;
        } else if (action == GLFW.GLFW_RELEASE) {
            getInstance().numMouseButtonDown--;
            getInstance().mouseButtonPressed[button] = false;
            getInstance().isDragging = false;
        }
    }

    public static void mouseScrollCallback(long window, double xOffset, double yOffset) {
        getInstance().scrollX = xOffset;
        getInstance().scrollY = yOffset;
    }

    public static void endFrame() {
        getInstance().scrollX = 0.0;
        getInstance().scrollY = 0.0;
        //getInstance().lastX = getInstance().xPos;
        //getInstance().lastY = getInstance().yPos;
    }

    public static void clear() {
        getInstance().scrollX = 0.0;
        getInstance().scrollY = 0.0;
        getInstance().lastX = 0;
        getInstance().lastY = 0;
        getInstance().xPos = 0;
        getInstance().yPos = 0;
        getInstance().numMouseButtonDown = 0;
        getInstance().isDragging = false;
        Arrays.fill(getInstance().mouseButtonPressed, false);
    }

    public static float getX() {
        return (float) getInstance().xPos;
    }

    public static float getY() {
        return (float) getInstance().yPos;
    }

    public static float getScreenX() {
        return getScreen().x;
    }

    public static float getScreenY() {
        return getScreen().y;
    }

    public void setGameViewportPos(Vector2f gameViewportPos) {
        this.gameViewportPos.set(gameViewportPos);
    }

    public void setGameViewportSize(Vector2f gameViewportSize) {
        this.gameViewportSize.set(gameViewportSize);
    }

    public static float getWorldX() {
        return getWorld().x;
    }

    public static float getWorldY() {
        return getWorld().y;
    }

    public static Vector2f getWorld() {
        float currentX = getX() - getInstance().gameViewportPos.x;
        currentX = (currentX / getInstance().gameViewportSize.x) * 2.0f - 1.0f;

        float currentY = getInstance().gameViewportSize.y - getY() + getInstance().gameViewportPos.y;
        currentY = (currentY / getInstance().gameViewportSize.y) * 2.0f - 1.0f;

        Vector4f tmp = new Vector4f(currentX, currentY, 0, 1);

        Camera camera = Window.getScene().camera();
        Matrix4f inverseView = new Matrix4f(camera.getInverseView());
        Matrix4f inverseProjection = new Matrix4f(camera.getInverseProjection());
        tmp.mul(inverseView.mul(inverseProjection));

        return new Vector2f(tmp.x, tmp.y);
    }

    public static Vector2f getScreen() {
        float currentX = getX() - getInstance().gameViewportPos.x;
        currentX = (currentX / getInstance().gameViewportSize.x) * 1920.0f;

        float currentY = getInstance().gameViewportSize.y - getY() + getInstance().gameViewportPos.y;
        currentY = (currentY / getInstance().gameViewportSize.y) * 1080.0f;

        return new Vector2f(currentX, currentY);
    }

    public static float getDx() {
        return (float) (getInstance().lastX - getInstance().xPos);
    }

    public static float getDy() {
        return (float) (getInstance().lastY - getInstance().yPos);
    }

    public static float getScrollX() {
        return (float) getInstance().scrollX;
    }

    public static float getScrollY() {
        return (float) getInstance().scrollY;
    }

    public static boolean isDragging() {
        return getInstance().isDragging;
    }

    public static boolean mouseButtonDown(int button) {
        if (button >= getInstance().mouseButtonPressed.length) {
            return false;
        }
        return getInstance().mouseButtonPressed[button];
    }
}
