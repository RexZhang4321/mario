package scenes;


import components.*;
import imgui.ImGui;
import imgui.ImVec2;
import jade.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.DebugDraw;
import util.AssetPool;
import util.Settings;

public class LevelEditorScene extends Scene {

    private final String spriteSheetPath = "assets/images/spritesheets/decorationsAndBlocks.png";
    private final String gizmoPath = "assets/images/gizmos.png";

    private SpriteSheet spriteSheet;

    GameObject levelEditorComponents = this.createGameObject("LevelEditor");

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        camera = new Camera(new Vector2f(-250, 0));
        loadResources();

        SpriteSheet gizmos = AssetPool.getSpriteSheet(gizmoPath);

        levelEditorComponents.addComponent(new MouseControls());
        levelEditorComponents.addComponent(new GridLines());
        levelEditorComponents.addComponent(new EditorCamera(camera));
        levelEditorComponents.addComponent(new GizmoSystem(gizmos));
        levelEditorComponents.start();

        if (levelLoaded) {
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
        camera.adjustProjection();

        angle += 1.0f;
        DebugDraw.addBox2D(new Vector2f(400, 200), new Vector2f(64, 32), angle, new Vector3f(0, 1, 0), 1);
        DebugDraw.addCircle(new Vector2f(600, 400), 64, new Vector3f(0, 1, 0), 1);
        // this.gameObjects.get(0).transform.position.x += 10 * dt;

        for (GameObject gameObject : gameObjects) {
            gameObject.update(dt);
        }
    }

    @Override
    public void render() {
        renderer.render();
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

    private void loadResources() {
        AssetPool.getShader("assets/shaders/default.glsl");

        AssetPool.addSpriteSheet(spriteSheetPath, new SpriteSheet(AssetPool.getTexture(spriteSheetPath), 16, 16, 81, 0));
        AssetPool.addSpriteSheet(gizmoPath, new SpriteSheet(AssetPool.getTexture(gizmoPath), 24, 48, 3, 0));
        AssetPool.getTexture("assets/images/blendImage2.png");

        spriteSheet = AssetPool.getSpriteSheet(spriteSheetPath);

        for (GameObject gameObject : gameObjects) {
            if (gameObject.getComponent(SpriteRenderer.class) != null) {
                SpriteRenderer spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
                if (spriteRenderer.getTexture() != null) {
                    spriteRenderer.setTexture(AssetPool.getTexture(spriteRenderer.getTexture().getFilePath()));
                }
            }
        }
    }
}
