package scenes;


import components.*;
import imgui.ImGui;
import imgui.ImVec2;
import jade.*;
import org.joml.Vector2f;
import physics2d.components.Box2DCollider;
import physics2d.components.RigidBody2D;
import physics2d.enums.BodyType;
import util.AssetPool;
import util.Settings;

import java.io.File;
import java.util.Collection;

import static jade.Prefabs.*;

public class LevelEditorSceneInitializer extends SceneInitializer {

    private final String objectSpriteSheetPath = "assets/images/spritesheets/decorationsAndBlocks.png";
    private final String gizmoPath = "assets/images/gizmos.png";

    private GameObject levelEditorComponents;

    public LevelEditorSceneInitializer() {

    }

    @Override
    public void init(Scene scene) {
        SpriteSheet gizmos = AssetPool.getSpriteSheet(gizmoPath);

        levelEditorComponents = scene.createGameObject("LevelEditor");
        levelEditorComponents.setNoSerialize();
        levelEditorComponents.addComponent(new MouseControls());
        levelEditorComponents.addComponent(new KeyControls());
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

        ImGui.begin("Objects");

        if (ImGui.beginTabBar("WindowTabBar")) {
            if (ImGui.beginTabItem("Blocks")) {
                SpriteSheet objectSpriteSheet = AssetPool.getSpriteSheet(objectSpriteSheetPath);
                ImVec2 windowPost = new ImVec2();
                ImGui.getWindowPos(windowPost);
                ImVec2 windowSize = new ImVec2();
                ImGui.getWindowSize(windowSize);
                ImVec2 itemSpacing = new ImVec2();
                ImGui.getStyle().getItemSpacing(itemSpacing);

                float windowX2 = windowPost.x + windowSize.x;
                for (int i = 0; i < objectSpriteSheet.getSize(); i++) {
                    // skip sprites that do not need box colliders
                    if (i == 34) {
                        continue;
                    }
                    if (i >= 38 && i < 61) {
                        continue;
                    }
                    Sprite sprite = objectSpriteSheet.getSprite(i);
                    float spriteWidth = sprite.getWidth() * 2;
                    float spriteHeight = sprite.getHeight() * 2;
                    int texId = sprite.getTexId();
                    Vector2f[] texCoords = sprite.getTexCoords();

                    ImGui.pushID(i);
                    if (ImGui.imageButton(texId, spriteWidth, spriteHeight, texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
                        GameObject gameObject = Prefabs.generateSpriteObject(sprite, Settings.GRID_WIDTH, Settings.GRID_HEIGHT);

                        RigidBody2D rigidBody2D = new RigidBody2D();
                        rigidBody2D.setBodyType(BodyType.Static);
                        gameObject.addComponent(rigidBody2D);
                        Box2DCollider box2DCollider = new Box2DCollider();
                        box2DCollider.setHalfSize(new Vector2f(0.25f, 0.25f));
                        gameObject.addComponent(box2DCollider);
                        gameObject.addComponent(new Ground());
                        if (i == 12) {
                            gameObject.addComponent(new BreakableBrick());
                        }
                        // attach this to the mouse cursor
                        levelEditorComponents.getComponent(MouseControls.class).pickUpObject(gameObject);
                    }
                    ImGui.popID();

                    ImVec2 lastButtonPos = new ImVec2();
                    ImGui.getItemRectMax(lastButtonPos);
                    float lastButtonX2 = lastButtonPos.x;
                    float nextButtonX2 = lastButtonX2 + itemSpacing.x + spriteWidth;
                    if (i + 1 < objectSpriteSheet.getSize() && nextButtonX2 < windowX2) {
                        ImGui.sameLine();
                    }
                }
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem("Decoration Blocks")) {
                SpriteSheet objectSpriteSheet = AssetPool.getSpriteSheet(objectSpriteSheetPath);
                ImVec2 windowPost = new ImVec2();
                ImGui.getWindowPos(windowPost);
                ImVec2 windowSize = new ImVec2();
                ImGui.getWindowSize(windowSize);
                ImVec2 itemSpacing = new ImVec2();
                ImGui.getStyle().getItemSpacing(itemSpacing);

                float windowX2 = windowPost.x + windowSize.x;
                for (int i = 34; i < 61; i++) {
                    // skip sprites that do not need box colliders
                    if (i >= 35 && i < 38 || i >= 42 && i < 45) {
                        continue;
                    }
                    Sprite sprite = objectSpriteSheet.getSprite(i);
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
                    if (i + 1 < objectSpriteSheet.getSize() && nextButtonX2 < windowX2) {
                        ImGui.sameLine();
                    }
                }
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem("Prefabs")) {
                int uid = 0;
                SpriteSheet playerSprites = AssetPool.getSpriteSheet(marioSpriteSheetPath);
                Sprite sprite = playerSprites.getSprite(0);
                float spriteWidth = sprite.getWidth() * 2;
                float spriteHeight = sprite.getHeight() * 2;
                int texId = sprite.getTexId();
                Vector2f[] texCoords = sprite.getTexCoords();

                ImGui.pushID(uid++);
                if (ImGui.imageButton(texId, spriteWidth, spriteHeight, texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
                    GameObject gameObject = Prefabs.generateMario();
                    // attach this to the mouse cursor
                    levelEditorComponents.getComponent(MouseControls.class).pickUpObject(gameObject);
                }
                ImGui.popID();
                ImGui.sameLine();

                SpriteSheet itemSprites = AssetPool.getSpriteSheet(itemSpriteSheetPath);
                Sprite itemSprite = itemSprites.getSprite(0);
                float itemSpriteWidth = itemSprite.getWidth() * 2;
                float itemSpriteHeight = itemSprite.getHeight() * 2;
                int itemSpriteTexId = itemSprite.getTexId();
                Vector2f[] itemSpriteTexCoords = itemSprite.getTexCoords();

                ImGui.pushID(uid++);
                if (ImGui.imageButton(itemSpriteTexId, itemSpriteWidth, itemSpriteHeight, itemSpriteTexCoords[2].x, itemSpriteTexCoords[0].y, itemSpriteTexCoords[0].x, itemSpriteTexCoords[2].y)) {
                    GameObject gameObject = Prefabs.generateQuestionBlock();
                    // attach this to the mouse cursor
                    levelEditorComponents.getComponent(MouseControls.class).pickUpObject(gameObject);
                }
                ImGui.popID();
                ImGui.sameLine();

                Sprite goombaSprite = playerSprites.getSprite(14);
                float goombaSpriteWidth = goombaSprite.getWidth() * 2;
                float goombaSpriteHeight = goombaSprite.getHeight() * 2;
                int goombaSpriteTexId = goombaSprite.getTexId();
                Vector2f[] goombaSpriteTexCoords = goombaSprite.getTexCoords();

                ImGui.pushID(uid++);
                if (ImGui.imageButton(goombaSpriteTexId, goombaSpriteWidth, goombaSpriteHeight, goombaSpriteTexCoords[2].x, goombaSpriteTexCoords[0].y, goombaSpriteTexCoords[0].x, goombaSpriteTexCoords[2].y)) {
                    GameObject gameObject = Prefabs.generateGoomba();
                    // attach this to the mouse cursor
                    levelEditorComponents.getComponent(MouseControls.class).pickUpObject(gameObject);
                }
                ImGui.popID();

                for (int i = 0; i < 4; i++) {
                    ImGui.sameLine();
                    SpriteSheet pipes = AssetPool.getSpriteSheet(pipeSpriteSheetPath);
                    Sprite pipeSprite = pipes.getSprite(i);
                    float pipeSpriteWidth = pipeSprite.getWidth();
                    float pipeSpriteHeight = pipeSprite.getHeight();
                    int pipeTexId = pipeSprite.getTexId();
                    Vector2f[] pipeTexCoords = pipeSprite.getTexCoords();

                    ImGui.pushID(uid++);
                    if (ImGui.imageButton(pipeTexId, pipeSpriteWidth, pipeSpriteHeight, pipeTexCoords[2].x, pipeTexCoords[0].y, pipeTexCoords[0].x, pipeTexCoords[2].y)) {
                        GameObject gameObject = Prefabs.generatePipe(i);
                        // attach this to the mouse cursor
                        levelEditorComponents.getComponent(MouseControls.class).pickUpObject(gameObject);
                    }
                    ImGui.popID();
                }

                ImGui.sameLine();
                SpriteSheet turtleSpriteSheet = AssetPool.getSpriteSheet(turtleSpriteSheetPath);
                Sprite turtleSprite = turtleSpriteSheet.getSprite(0);
                float turtleSpriteWidth = turtleSprite.getWidth() * 2;
                float turtleSpriteHeight = turtleSprite.getHeight() * 1.5f;
                int turtleSpriteTexId = turtleSprite.getTexId();
                Vector2f[] turtleSpriteTexCoords = turtleSprite.getTexCoords();

                ImGui.pushID(uid++);
                if (ImGui.imageButton(turtleSpriteTexId, turtleSpriteWidth, turtleSpriteHeight, turtleSpriteTexCoords[2].x, turtleSpriteTexCoords[0].y, turtleSpriteTexCoords[0].x, turtleSpriteTexCoords[2].y)) {
                    GameObject gameObject = Prefabs.generateTurtle();
                    // attach this to the mouse cursor
                    levelEditorComponents.getComponent(MouseControls.class).pickUpObject(gameObject);
                }
                ImGui.popID();

                ImGui.sameLine();
                Sprite flagSprite = itemSprites.getSprite(6);
                float flagSpriteWidth = flagSprite.getWidth() * 2;
                float flagSpriteHeight = flagSprite.getHeight() * 2;
                int flagSpriteTexId = flagSprite.getTexId();
                Vector2f[] flagSpriteTexCoords = flagSprite.getTexCoords();

                ImGui.pushID(uid++);
                if (ImGui.imageButton(flagSpriteTexId, flagSpriteWidth, flagSpriteHeight, flagSpriteTexCoords[2].x, flagSpriteTexCoords[0].y, flagSpriteTexCoords[0].x, flagSpriteTexCoords[2].y)) {
                    GameObject gameObject = Prefabs.generateFlagTop();
                    // attach this to the mouse cursor
                    levelEditorComponents.getComponent(MouseControls.class).pickUpObject(gameObject);
                }
                ImGui.popID();

                ImGui.sameLine();
                Sprite flagPoleSprite = itemSprites.getSprite(33);
                float flagPoleSpriteWidth = flagPoleSprite.getWidth() * 2;
                float flagPoleSpriteHeight = flagPoleSprite.getHeight() * 2;
                int flagPoleSpriteTexId = flagPoleSprite.getTexId();
                Vector2f[] flagPoleSpriteTexCoords = flagPoleSprite.getTexCoords();

                ImGui.pushID(uid++);
                if (ImGui.imageButton(flagPoleSpriteTexId, flagPoleSpriteWidth, flagPoleSpriteHeight, flagPoleSpriteTexCoords[2].x, flagPoleSpriteTexCoords[0].y, flagPoleSpriteTexCoords[0].x, flagPoleSpriteTexCoords[2].y)) {
                    GameObject gameObject = Prefabs.generateFlagPole();
                    // attach this to the mouse cursor
                    levelEditorComponents.getComponent(MouseControls.class).pickUpObject(gameObject);
                }
                ImGui.popID();

                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem("Sounds")) {
                Collection<Sound> sounds = AssetPool.getAllSounds();
                for (Sound sound : sounds) {
                    File tmp = new File(sound.getFilePath());
                    if (ImGui.button(tmp.getName())) {
                        if (!sound.isPlaying()) {
                            sound.play();
                        } else {
                            sound.stop();
                        }
                    }
                }
                ImGui.endTabItem();
            }
            ImGui.endTabBar();
        }

        ImGui.end();
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
