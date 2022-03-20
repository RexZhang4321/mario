package jade;

import components.*;
import util.AssetPool;

public class Prefabs {

    public final static String marioSpriteSheetPath = "assets/images/spritesheet.png";
    public final static String itemSpriteSheetPath = "assets/images/items.png";

    public static GameObject generateSpriteObject(Sprite sprite, float sizeX, float sizeY) {
        GameObject block = Window.getScene().createGameObject("Sprite_Object_Gen");
        block.transform.scale.x = sizeX;
        block.transform.scale.y = sizeY;
        SpriteRenderer renderer = new SpriteRenderer();
        renderer.setSprite(sprite);
        block.addComponent(renderer);

        return block;
    }

    public static GameObject generateMario() {
        SpriteSheet marioSpriteSheet = AssetPool.getSpriteSheet(marioSpriteSheetPath);
        GameObject mario = generateSpriteObject(marioSpriteSheet.getSprite(0), 0.25f, 0.25f);

        AnimationState run = new AnimationState();
        run.title = "Run";
        float defaultFrameTime = 0.2f;
        run.addFrame(marioSpriteSheet.getSprite(0), defaultFrameTime);
        run.addFrame(marioSpriteSheet.getSprite(2), defaultFrameTime);
        run.addFrame(marioSpriteSheet.getSprite(3), defaultFrameTime);
        run.addFrame(marioSpriteSheet.getSprite(2), defaultFrameTime);
        run.setLoop(true);

        StateMachine stateMachine = new StateMachine();
        stateMachine.addState(run);
        stateMachine.setDefaultStateTitle(run.title);
        mario.addComponent(stateMachine);
        return mario;
    }

    public static GameObject generateQuestionBlock() {
        SpriteSheet itemSpriteSheet = AssetPool.getSpriteSheet(itemSpriteSheetPath);
        GameObject questionBlock = generateSpriteObject(itemSpriteSheet.getSprite(0), 0.25f, 0.25f);

        AnimationState run = new AnimationState();
        run.title = "Flicker";
        float defaultFrameTime = 0.2f;
        run.addFrame(itemSpriteSheet.getSprite(0), defaultFrameTime);
        run.addFrame(itemSpriteSheet.getSprite(1), defaultFrameTime);
        run.addFrame(itemSpriteSheet.getSprite(2), defaultFrameTime);
        run.setLoop(true);

        StateMachine stateMachine = new StateMachine();
        stateMachine.addState(run);
        stateMachine.setDefaultStateTitle(run.title);
        questionBlock.addComponent(stateMachine);
        return questionBlock;
    }

}
