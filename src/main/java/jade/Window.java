package jade;

import observers.EventSystem;
import observers.Observer;
import observers.events.Event;
import observers.events.EventType;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.openal.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL41;
import org.lwjgl.system.MemoryUtil;
import renderer.*;
import scenes.LevelEditorSceneInitializer;
import scenes.Scene;
import scenes.SceneInitializer;
import util.AssetPool;


public class Window implements Observer {
    private int width;
    private int height;
    private final String title;

    private long glfwWindow;

    private final String glslVersion = "#version 410";

    private static Window instance;

    private static Scene currentScene;

    private static ImGuiLayer imGuiLayer;

    private Framebuffer framebuffer;

    private PickingTexture pickingTexture;

    private boolean runtimePlaying = false;

    private long audioContext;
    private long audioDevice;

    private Window() {
        width = 1920;
        height = 1080;
        title = "Mario";
        EventSystem.addObserver(this);
    }

    public static void changeScene(SceneInitializer sceneInitializer) {
        if (currentScene != null) {
            currentScene.destroy();
        }
        imGuiLayer.getPropertiesWindow().setActiveGameObject(null);
        currentScene = new Scene(sceneInitializer);
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

    public ImGuiLayer getImGuiLayer() {
        return imGuiLayer;
    }

    public static Scene getScene() {
        return currentScene;
    }

    public int getWidth() {
        return getInstance().width;
    }

    public int getHeight() {
        return getInstance().height;
    }

    public Framebuffer getFramebuffer() {
        return getInstance().framebuffer;
    }

    public static float getTargetAspectRatio() {
        return 16.0f / 9.0f;
    }

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");
        init();
        loop();

        // destroy the audio context
        ALC10.alcDestroyContext(audioContext);
        ALC10.alcCloseDevice(audioDevice);

        // terminate GLFW and release the resources
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    @Override
    public void onNotify(GameObject gameObject, Event event) {
        if (event.eventType == EventType.GameEngineStartPlay) {
            this.runtimePlaying = true;
            currentScene.save();
            Window.changeScene(new LevelEditorSceneInitializer());
        } else if (event.eventType == EventType.GameEngineStopPlay) {
            this.runtimePlaying = false;
            Window.changeScene(new LevelEditorSceneInitializer());
        } else if (event.eventType == EventType.LoadLevel) {
            Window.changeScene(new LevelEditorSceneInitializer());
        } else if (event.eventType == EventType.SaveLevel) {
            currentScene.save();
        }
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
        GLFW.glfwSetWindowSizeCallback(glfwWindow, (w, newWidth, newHeight) -> {
            Window.getInstance().width = newWidth;
            Window.getInstance().height = newHeight;
        });

        // make the OpenGL context current
        GLFW.glfwMakeContextCurrent(glfwWindow);
        // enable v-sync
        GLFW.glfwSwapInterval(1);

        // make the window visible
        GLFW.glfwShowWindow(glfwWindow);

        // init audio device
        String defaultDeviceName = ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);
        audioDevice = ALC10.alcOpenDevice(defaultDeviceName);

        int[] attributes = {0};
        audioContext = ALC10.alcCreateContext(audioDevice, attributes);
        ALC10.alcMakeContextCurrent(audioContext);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        if (!alCapabilities.OpenAL10) {
            assert false : "Audio library not support.";
        }

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        GL41.glEnable(GL41.GL_BLEND);
        GL41.glBlendFunc(GL41.GL_ONE, GL41.GL_ONE_MINUS_SRC_ALPHA);

        framebuffer = new Framebuffer(1920, 1080);
        pickingTexture = new PickingTexture(1920, 1080);
        GL41.glViewport(0, 0, 1920, 1080);

        imGuiLayer = new ImGuiLayer(glfwWindow, glslVersion, pickingTexture);
        imGuiLayer.init();

        Window.changeScene(new LevelEditorSceneInitializer());
    }

    private void loop() {
        float beginTime = (float) GLFW.glfwGetTime();
        float dt = -1.0f;

        Shader defaultShader = AssetPool.getShader("assets/shaders/default.glsl");
        Shader pickingShader = AssetPool.getShader("assets/shaders/pickingShader.glsl");

        while (!GLFW.glfwWindowShouldClose(glfwWindow)) {
            // poll events
            GLFW.glfwPollEvents();

            // render pass 1: render to picking texture
            GL41.glDisable(GL41.GL_BLEND);
            pickingTexture.enableWriting();
            GL41.glViewport(0, 0, 1920, 1080);
            GL41.glClearColor(0, 0, 0, 0);
            GL41.glClear(GL41.GL_COLOR_BUFFER_BIT | GL41.GL_DEPTH_BUFFER_BIT);

            Renderer.bindShader(pickingShader);
            currentScene.render();

            pickingTexture.disableWriting();
            GL41.glEnable(GL41.GL_BLEND);

            // render pass 2: render actual game
            DebugDraw.beginFrame();

            framebuffer.bind();
            GL41.glClearColor(1, 1, 1, 1);
            GL41.glClear(GL41.GL_COLOR_BUFFER_BIT);

            if (dt >= 0) {
                Renderer.bindShader(defaultShader);
                if (runtimePlaying) {
                    currentScene.update(dt);
                } else {
                    currentScene.editorUpdate(dt);
                }
                currentScene.render();
                DebugDraw.draw();
            }
            framebuffer.unbind();

            imGuiLayer.update(dt, currentScene);

            MouseListener.endFrame();

            GLFW.glfwSwapBuffers(glfwWindow);

            float endTime = (float) GLFW.glfwGetTime();
            dt = endTime - beginTime;
            beginTime = endTime;
        }
    }
}
