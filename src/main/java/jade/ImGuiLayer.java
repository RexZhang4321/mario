package jade;


import editor.GameViewWindow;
import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import org.lwjgl.glfw.GLFW;
import scenes.Scene;

public class ImGuiLayer {

    private long glfwWindow;
    private String glslVersion;
    private boolean showText = false;

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    public ImGuiLayer(long glfwWindow, String glslVersion) {
        this.glfwWindow = glfwWindow;
        this.glslVersion = glslVersion;
    }

    public void init() {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();

        // enable viewport
        // io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);

        // enable docking
        io.setConfigFlags(ImGuiConfigFlags.DockingEnable);


        // adjust fonts
        ImFontAtlas fontAtlas = io.getFonts();
        ImFontConfig fontConfig = new ImFontConfig();
        fontConfig.setGlyphRanges(fontAtlas.getGlyphRangesDefault());
        fontConfig.setPixelSnapH(true);

        fontAtlas.addFontFromFileTTF("assets/fonts/Pragmatica-ExtraLight.ttf", 18, fontConfig);

        imGuiGlfw.init(glfwWindow, true);
        imGuiGl3.init(glslVersion);
    }

    public void update(Scene currentScene) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        setupDockSpace();
        currentScene.sceneImGui();
        GameViewWindow.imGui();
        imGui();
        tearDownDockSpace();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

//        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
//            final long backupWindowPtr = GLFW.glfwGetCurrentContext();
//            ImGui.updatePlatformWindows();
//            ImGui.renderPlatformWindowsDefault();
//            GLFW.glfwMakeContextCurrent(backupWindowPtr);
//        }
    }

    private void imGui() {
        ImGui.begin("Cool Window");

        if (ImGui.button("I am a button")) {
            showText = true;
        }

        if (showText) {
            ImGui.text("You clicked a button");
            if (ImGui.button("Stop showing text")) {
                showText = false;
            }
        }

        ImGui.end();
    }

    private void setupDockSpace() {
        int windowFlags = ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoDocking;
        ImGui.setNextWindowPos(0.0f, 0.0f, ImGuiCond.Always);
        ImGui.setNextWindowSize(Window.getInstance().getWidth(), Window.getInstance().getHeight());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0.0f);
        windowFlags |= ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus;
        ImGui.begin("Dock Space Demo", new ImBoolean(true), windowFlags);
        ImGui.popStyleVar(2);

        // dock space
        ImGui.dockSpace(ImGui.getID("Dock Space"));
    }

    private void tearDownDockSpace() {
        ImGui.end();
    }

}
