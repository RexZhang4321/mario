package editor;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import jade.MouseListener;
import jade.Window;
import observers.EventSystem;
import observers.events.Event;
import observers.events.EventType;
import org.joml.Vector2f;

public class GameViewWindow {

    private float leftX, rightX, topY, bottomY;
    private boolean isPlaying = false;

    public void imGui() {
        ImGui.begin("Game Viewport", ImGuiWindowFlags.NoScrollbar
                | ImGuiWindowFlags.NoScrollWithMouse
                | ImGuiWindowFlags.MenuBar);

        ImGui.beginMenuBar();
        if (ImGui.menuItem("Play", "", isPlaying, !isPlaying)) {
            isPlaying = true;
            EventSystem.notify(null, new Event(EventType.GameEngineStartPlay));
        }
        if (ImGui.menuItem("Stop", "", !isPlaying, isPlaying)) {
            isPlaying = false;
            EventSystem.notify(null, new Event(EventType.GameEngineStopPlay));
        }
        ImGui.endMenuBar();

        ImVec2 windowSize = getLargestSizeForViewport();
        ImVec2 windowPos = getCenteredPositionForViewport(windowSize);
        ImGui.setCursorPos(windowPos.x, windowPos.y);

        ImVec2 topLeft = ImGui.getCursorScreenPos();
        leftX = topLeft.x;
        bottomY = topLeft.y;
        rightX = topLeft.x + windowSize.x;
        topY = topLeft.y + windowSize.y;

        int textureId = Window.getInstance().getFramebuffer().getTextureId();
        ImGui.image(textureId, windowSize.x, windowSize.y, 0, 1, 1, 0);

        // TODO: after resizing the window, the mouse position is completely off
        MouseListener.getInstance().setGameViewportPos(new Vector2f(topLeft.x, topLeft.y - 24));
        MouseListener.getInstance().setGameViewportSize(new Vector2f(windowSize.x, windowSize.y));

        ImGui.end();
    }

    public boolean shouldCaptureMouse() {
        return MouseListener.getX() >= leftX
                && MouseListener.getX() <= rightX
                && MouseListener.getY() >= bottomY
                && MouseListener.getY() <= topY;
    }

    private ImVec2 getLargestSizeForViewport() {
        ImVec2 windowSize = ImGui.getContentRegionAvail();

        // try to fill the width
        float aspectWidth = windowSize.x;
        float aspectHeight = aspectWidth / Window.getTargetAspectRatio();
        // can't fill the width
        if (aspectHeight > windowSize.y) {
            // fill the height instead
            aspectHeight = windowSize.y;
            aspectWidth = aspectHeight * Window.getTargetAspectRatio();
        }
        return new ImVec2(aspectWidth, aspectHeight);
    }

    // return the top left point of a centered viewport
    private ImVec2 getCenteredPositionForViewport(ImVec2 aspectSize) {
        ImVec2 windowSize = ImGui.getContentRegionAvail();

        float viewportX = windowSize.x / 2.0f - aspectSize.x / 2.0f;
        float viewportY = windowSize.y / 2.0f - aspectSize.y / 2.0f;

        return new ImVec2(viewportX + ImGui.getCursorPosX(), viewportY + ImGui.getCursorPosY());
    }

    public boolean getWantCaptureMouse() {
        return MouseListener.getX() >= leftX && MouseListener.getX() <= rightX &&
                MouseListener.getY() >= bottomY && MouseListener.getY() <= topY;
    }

}
