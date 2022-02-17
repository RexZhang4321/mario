package jade;


import components.SpriteRenderer;
import components.SpriteSheet;
import org.joml.Vector2f;
import util.AssetPool;

public class LevelEditorScene extends Scene {

    private final String spriteSheetPath = "assets/images/spritesheet.png";

    private GameObject gameObject1;
    private SpriteSheet spriteSheet;

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        loadResources();

        camera = new Camera(new Vector2f());

        spriteSheet = AssetPool.getSpriteSheet(spriteSheetPath);

        gameObject1 = new GameObject("Object 1", new Transform(new Vector2f(100, 100), new Vector2f(256, 256)));
        gameObject1.addComponent(new SpriteRenderer(spriteSheet.getSprite(0)));
        this.addGameObjectToScene(gameObject1);

        GameObject gameObject2 = new GameObject("Object 2", new Transform(new Vector2f(400, 100), new Vector2f(256, 256)));
        gameObject2.addComponent(new SpriteRenderer(spriteSheet.getSprite(15)));
        this.addGameObjectToScene(gameObject2);
    }

    private int spriteIndex = 0;
    private float spriteFlipTime = 0.2f;
    private float spriteFlipTimeLeft = 0.0f;

    @Override
    public void update(float dt) {
        System.out.println("FPS: " + 1.0f / dt);

        gameObject1.transform.position.x += 10 * dt;

        spriteFlipTimeLeft -= dt;
        if (spriteFlipTimeLeft <= 0) {
            spriteFlipTimeLeft = spriteFlipTime;
            spriteIndex++;
            if (spriteIndex > 3) {
                spriteIndex = 0;
            }
            gameObject1.getComponent(SpriteRenderer.class).setSprite(spriteSheet.getSprite(spriteIndex));
        }

        for (GameObject gameObject : gameObjects) {
            gameObject.update(dt);
        }

        renderer.render();
    }

    private void loadResources() {
        AssetPool.getShader("assets/shaders/default.glsl");

        AssetPool.addSpriteSheet(spriteSheetPath, new SpriteSheet(AssetPool.getTexture(spriteSheetPath), 16, 16, 26, 0));
    }
}
