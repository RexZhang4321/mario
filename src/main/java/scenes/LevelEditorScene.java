package scenes;


import components.*;
import imgui.ImGui;
import imgui.ImVec2;
import jade.Camera;
import jade.GameObject;
import jade.Prefabs;
import jade.Transform;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import renderer.DebugDraw;
import util.AssetPool;
import util.Settings;

public class LevelEditorScene extends Scene {

    private final String spriteSheetPath = "assets/images/spritesheets/decorationsAndBlocks.png";

    private SpriteSheet spriteSheet;

    GameObject levelEditorComponents = new GameObject("LevelEditor", new Transform(new Vector2f()), 0);

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        levelEditorComponents.addComponent(new MouseControls());
        levelEditorComponents.addComponent(new GridLines());

        loadResources();

        camera = new Camera(new Vector2f(-250, 0));

        if (levelLoaded) {
            // this.activeGameObject = gameObjects.get(0);
            return;
        }

        /*
        // red
        GameObject gameObject1 = new GameObject(
                "Object 1", new Transform(new Vector2f(200, 100), new Vector2f(256, 256)), 1);
        SpriteRenderer spriteRenderer1 = new SpriteRenderer();
        spriteRenderer1.setColor(new Vector4f(1, 0, 0, 1));
        gameObject1.addComponent(spriteRenderer1);
        gameObject1.addComponent(new RigidBody());

        // green
        GameObject gameObject2 = new GameObject("Object 2",
                new Transform(new Vector2f(400, 100), new Vector2f(256, 256)), 2);
        SpriteRenderer spriteRenderer2 = new SpriteRenderer();
        Sprite sprite = new Sprite();
        sprite.setTexture(AssetPool.getTexture("assets/images/blendImage2.png"));
        spriteRenderer2.setSprite(sprite);
        gameObject2.addComponent(spriteRenderer2);

        this.addGameObjectToScene(gameObject1);
        this.addGameObjectToScene(gameObject2);

        this.activeGameObject = gameObject1;
         */
    }

    float angle = 0.0f;
    @Override
    public void update(float dt) {
        // System.out.println("FPS: " + 1.0f / dt);
        levelEditorComponents.update(dt);

        angle += 1.0f;
        DebugDraw.addBox2D(new Vector2f(400, 200), new Vector2f(64, 32), angle, new Vector3f(0, 1, 0), 1);
        DebugDraw.addCircle(new Vector2f(600, 400), 64, new Vector3f(0, 1, 0), 1);
        // this.gameObjects.get(0).transform.position.x += 10 * dt;

        for (GameObject gameObject : gameObjects) {
            gameObject.update(dt);
        }

        renderer.render();
    }

    @Override
    public void imGui() {
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

    private void loadResources() {
        AssetPool.getShader("assets/shaders/default.glsl");

        AssetPool.addSpriteSheet(spriteSheetPath, new SpriteSheet(AssetPool.getTexture(spriteSheetPath), 16, 16, 81, 0));
        AssetPool.getTexture("assets/images/blendImage2.png");

        spriteSheet = AssetPool.getSpriteSheet(spriteSheetPath);
    }
}
