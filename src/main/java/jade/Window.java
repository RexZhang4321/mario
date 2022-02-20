package jade;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL41;
import org.lwjgl.system.MemoryUtil;


public class Window {
    private final int width;
    private final int height;
    private final String title;

    private long glfwWindow;

    private final String glslVersion = "#version 410";

    public float r, g, b, a;

    private static Window instance;

    private static Scene currentScene;

    private static ImGuiLayer imGuiLayer;

    private Window() {
        width = 1920;
        height = 1080;
        title = "Mario";
        r = 1;
        g = 1;
        b = 1;
        a = 1;
    }

    public static void changeScene(int newScene) {
        switch (newScene) {
            case 0:
                currentScene = new LevelEditorScene();
                break;
            case 1:
                currentScene = new LevelScene();
                break;
            default:
                assert false : "Unknown scene: " + newScene;
                break;
        }
        currentScene.load();
        currentScene.init();
        currentScene.start();
    }

    public static Window getInstance() {
        if (Window.instance == null) {
            Window.instance = new Window();
        }
        return Window.instance;
    }

    public static Scene getScene() {
        return currentScene;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");
        init();
        loop();

        // terminate GLFW and release the resources
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    private void init() {
        // setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // init GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW.");
        }

        // configure GLFW
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_TRUE);

        // create the window
        glfwWindow = GLFW.glfwCreateWindow(this.width, this.height, this.title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (glfwWindow == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to create the GLFW window.");
        }

        GLFW.glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        GLFW.glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        GLFW.glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        GLFW.glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

        // make the OpenGL context current
        GLFW.glfwMakeContextCurrent(glfwWindow);
        // enable v-sync
        GLFW.glfwSwapInterval(1);

        // make the window visible
        GLFW.glfwShowWindow(glfwWindow);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        GL41.glEnable(GL41.GL_BLEND);
        GL41.glBlendFunc(GL41.GL_ONE, GL41.GL_ONE_MINUS_SRC_ALPHA);

        imGuiLayer = new ImGuiLayer(glfwWindow, glslVersion);
        imGuiLayer.init();

        Window.changeScene(0);
    }

    private void loop() {
        float beginTime = (float) GLFW.glfwGetTime();
        float dt = -1.0f;

        while (!GLFW.glfwWindowShouldClose(glfwWindow)) {
            // poll events
            GLFW.glfwPollEvents();

            GL41.glClearColor(r, g, b, a);
            GL41.glClear(GL41.GL_COLOR_BUFFER_BIT);

            if (dt >= 0) {
                currentScene.update(dt);
            }

            imGuiLayer.update(currentScene);

            GLFW.glfwSwapBuffers(glfwWindow);

            float endTime = (float) GLFW.glfwGetTime();
            dt = endTime - beginTime;
            beginTime = endTime;
        }

        currentScene.saveExit();
    }

}
