package jade;


import components.SpriteRenderer;
import org.joml.Vector2f;
import util.AssetPool;

public class LevelEditorScene extends Scene {

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        camera = new Camera(new Vector2f());

        GameObject gameObject1 = new GameObject("Object 1", new Transform(new Vector2f(100, 100), new Vector2f(256, 256)));
        gameObject1.addComponent(new SpriteRenderer(AssetPool.getTexture("assets/images/testImage.png")));
        this.addGameObjectToScene(gameObject1);

        GameObject gameObject2 = new GameObject("Object 2", new Transform(new Vector2f(400, 100), new Vector2f(256, 256)));
        gameObject2.addComponent(new SpriteRenderer(AssetPool.getTexture("assets/images/testImage2.png")));
        this.addGameObjectToScene(gameObject2);

        loadResources();
    }

    @Override
    public void update(float dt) {
        System.out.println("FPS: " + 1.0f / dt);

        for (GameObject gameObject : gameObjects) {
            gameObject.update(dt);
        }

        renderer.render();
    }

    private void loadResources() {
        AssetPool.getShader("assets/shaders/default.glsl");
    }
}
