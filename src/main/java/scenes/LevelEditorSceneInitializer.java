package scenes;


import components.*;
import imgui.ImGui;
import imgui.ImVec2;
import jade.*;
import org.joml.Vector2f;
import util.AssetPool;
import util.Settings;

public class LevelEditorSceneInitializer extends SceneInitializer {

    private final String spriteSheetPath = "assets/images/spritesheets/decorationsAndBlocks.png";
    private final String gizmoPath = "assets/images/gizmos.png";

    private SpriteSheet spriteSheet;

    private GameObject levelEditorComponents;

    public LevelEditorSceneInitializer() {

    }

    @Override
    public void init(Scene scene) {
        SpriteSheet gizmos = AssetPool.getSpriteSheet(gizmoPath);

        levelEditorComponents = scene.createGameObject("LevelEditor");
        levelEditorComponents.setNoSerialize();
        levelEditorComponents.addComponent(new MouseControls());
        levelEditorComponents.addComponent(new GridLines());
        levelEditorComponents.addComponent(new EditorCamera(scene.camera()));
        levelEditorComponents.addComponent(new GizmoSystem(gizmos));
        scene.addGameObjectToScene(levelEditorComponents);
    }

    @Override
    public void imGui() {
        ImGui.begin("Level Editor Stuff");
        levelEditorComponents.imGui();
        ImGui.end();

        ImGui.begin("Test window");

        ImVec2 windowPost = new ImVec2();
        ImGui.getWindowPos(windowPost);
        ImVec2 windowSize = new ImVec2();
        ImGui.getWindowSize(windowSize);
        ImVec2 itemSpacing = new ImVec2();
        ImGui.getStyle().getItemSpacing(itemSpacing);

        float windowX2 = windowPost.x + windowSize.x;
        for (int i = 0; i < spriteSheet.getSize(); i++) {
            Sprite sprite = spriteSheet.getSprite(i);
            float spriteWidth = sprite.getWidth() * 2;
            float spriteHeight = sprite.getHeight() * 2;
            int texId = sprite.getTexId();
            Vector2f[] texCoords = sprite.getTexCoords();

            ImGui.pushID(i);
            if (ImGui.imageButton(texId, spriteWidth, spriteHeight, texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
                GameObject gameObject = Prefabs.generateSpriteObject(sprite, Settings.GRID_WIDTH, Settings.GRID_HEIGHT);
                // attach this to the mouse cursor
                levelEditorComponents.getComponent(MouseControls.class).pickUpObject(gameObject);
            }
            ImGui.popID();

            ImVec2 lastButtonPos = new ImVec2();
            ImGui.getItemRectMax(lastButtonPos);
            float lastButtonX2 = lastButtonPos.x;
            float nextButtonX2 = lastButtonX2 + itemSpacing.x + spriteWidth;
            if (i + 1 < spriteSheet.getSize() && nextButtonX2 < windowX2) {
                ImGui.sameLine();
            }
        }

        ImGui.end();
    }

    @Override
    public void loadResource(Scene scene) {
        AssetPool.getShader("assets/shaders/default.glsl");

        AssetPool.addSpriteSheet(spriteSheetPath, new SpriteSheet(AssetPool.getTexture(spriteSheetPath), 16, 16, 81, 0));
        AssetPool.addSpriteSheet(gizmoPath, new SpriteSheet(AssetPool.getTexture(gizmoPath), 24, 48, 3, 0));
        AssetPool.getTexture("assets/images/blendImage2.png");

        spriteSheet = AssetPool.getSpriteSheet(spriteSheetPath);

        for (GameObject gameObject : scene.getGameObjects()) {
            if (gameObject.getComponent(SpriteRenderer.class) != null) {
                SpriteRenderer spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
                if (spriteRenderer.getTexture() != null) {
                    spriteRenderer.setTexture(AssetPool.getTexture(spriteRenderer.getTexture().getFilePath()));
                }
            }
        }
    }
}
