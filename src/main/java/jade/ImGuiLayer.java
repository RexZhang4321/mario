package jade;


import imgui.ImGui;

public class ImGuiLayer {

    private boolean showText = false;

    public void imGui() {
        ImGui.begin("Coll Window");

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

}
