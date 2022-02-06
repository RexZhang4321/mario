package jade;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;


public class Window {
    private final int width;
    private final int height;
    private final String title;
    private long glfwWindow;

    private float r, g, b, a;
    private boolean fadeToBlack;

    private static Window instance;

    private Window() {
        width = 1920;
        height = 1080;
        title = "Mario";
        r = 1;
        g = 1;
        b = 1;
        a = 1;
        fadeToBlack = false;
    }

    public static Window getInstance() {
        if (Window.instance == null) {
            Window.instance = new Window();
        }
        return Window.instance;
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
        GLFW.glfwDefaultWindowHints();
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
    }

    private void loop() {
        while (!GLFW.glfwWindowShouldClose(glfwWindow)) {
            // poll events
            GLFW.glfwPollEvents();

            GL11.glClearColor(r, g, b, a);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            if (fadeToBlack) {
                r = Math.max(0, r - 0.01f);
                g = Math.max(0, g - 0.01f);
                b = Math.max(0, b - 0.01f);
            }

            if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
                fadeToBlack = true;
            }

            GLFW.glfwSwapBuffers(glfwWindow);
        }
    }

}
