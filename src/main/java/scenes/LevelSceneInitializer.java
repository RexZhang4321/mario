package scenes;


import components.*;
import jade.GameObject;
import util.AssetPool;

import java.io.File;

import static jade.Prefabs.*;

public class LevelSceneInitializer extends SceneInitializer {

    private final String objectSpriteSheetPath = "assets/images/spritesheets/decorationsAndBlocks.png";
    private final String gizmoPath = "assets/images/gizmos.png";

    private GameObject cameraObject;

    public LevelSceneInitializer() {

    }

    @Override
    public void init(Scene scene) {
        cameraObject = scene.createGameObject("GameCamera");
        cameraObject.addComponent(new GameCamera(scene.camera()));
        cameraObject.start();
        scene.addGameObjectToScene(cameraObject);
    }

    @Override
    public void imGui() {
    }

    @Override
    public void loadResource(Scene scene) {
        AssetPool.getShader("assets/shaders/default.glsl");

        AssetPool.addSpriteSheet(objectSpriteSheetPath, new SpriteSheet(AssetPool.getTexture(objectSpriteSheetPath), 16, 16, 81, 0));
        AssetPool.addSpriteSheet(gizmoPath, new SpriteSheet(AssetPool.getTexture(gizmoPath), 24, 48, 3, 0));
        AssetPool.addSpriteSheet(marioSpriteSheetPath, new SpriteSheet(AssetPool.getTexture(marioSpriteSheetPath), 16, 16, 26, 0));
        AssetPool.addSpriteSheet(itemSpriteSheetPath, new SpriteSheet(AssetPool.getTexture(itemSpriteSheetPath), 16, 16, 43, 0));
        AssetPool.addSpriteSheet(turtleSpriteSheetPath,
                new SpriteSheet(AssetPool.getTexture(turtleSpriteSheetPath),
                        16, 24, 4, 0));
        AssetPool.addSpriteSheet(bigMarioSpriteSheetPath,
                new SpriteSheet(AssetPool.getTexture(bigMarioSpriteSheetPath),
                        16, 32, 42, 0));
        AssetPool.addSpriteSheet(pipeSpriteSheetPath,
                new SpriteSheet(AssetPool.getTexture(pipeSpriteSheetPath),
                        32, 32, 4, 0));

        AssetPool.getTexture("assets/images/blendImage2.png");

        File soundDir = new File("assets/sounds/");
        for (String soundPath : soundDir.list()) {
            AssetPool.addSound("assets/sounds/" + soundPath, false);
        }

        for (GameObject gameObject : scene.getGameObjects()) {
            if (gameObject.getComponent(SpriteRenderer.class) != null) {
                SpriteRenderer spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
                if (spriteRenderer.getTexture() != null) {
                    spriteRenderer.setTexture(AssetPool.getTexture(spriteRenderer.getTexture().getFilePath()));
                }
            }

            if (gameObject.getComponent(StateMachine.class) != null) {
                StateMachine stateMachine = gameObject.getComponent(StateMachine.class);
                stateMachine.refreshTextures();
            }
        }
    }
}
