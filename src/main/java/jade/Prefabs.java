package jade;

import components.*;
import org.joml.Vector2f;
import physics2d.components.Box2DCollider;
import physics2d.components.CircleCollider;
import physics2d.components.PillboxCollider;
import physics2d.components.RigidBody2D;
import physics2d.enums.BodyType;
import util.AssetPool;

public class Prefabs {

    public final static String marioSpriteSheetPath = "assets/images/spritesheet.png";
    public final static String itemSpriteSheetPath = "assets/images/items.png";
    public final static String turtleSpriteSheetPath = "assets/images/turtle.png";
    public final static String bigMarioSpriteSheetPath = "assets/images/bigSpritesheet.png";
    public final static String pipeSpriteSheetPath = "assets/images/pipes.png";


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
        SpriteSheet bigMarioSpriteSheet = AssetPool.getSpriteSheet(bigMarioSpriteSheetPath);
        GameObject mario = generateSpriteObject(marioSpriteSheet.getSprite(0), 0.25f, 0.25f);

        AnimationState run = new AnimationState();
        run.title = "Run";
        float defaultFrameTime = 0.2f;
        run.addFrame(marioSpriteSheet.getSprite(0), defaultFrameTime);
        run.addFrame(marioSpriteSheet.getSprite(2), defaultFrameTime);
        run.addFrame(marioSpriteSheet.getSprite(3), defaultFrameTime);
        run.addFrame(marioSpriteSheet.getSprite(2), defaultFrameTime);
        run.setLoop(true);

        AnimationState switchDirection = new AnimationState();
        switchDirection.title = "Switch Direction";
        switchDirection.addFrame(marioSpriteSheet.getSprite(4), 0.1f);
        switchDirection.setLoop(false);

        AnimationState idle = new AnimationState();
        idle.title = "Idle";
        idle.addFrame(marioSpriteSheet.getSprite(0), 0.1f);
        idle.setLoop(false);

        AnimationState jump = new AnimationState();
        jump.title = "Jump";
        jump.addFrame(marioSpriteSheet.getSprite(5), 0.1f);
        jump.setLoop(false);

        AnimationState die = new AnimationState();
        die.title = "Die";
        die.addFrame(marioSpriteSheet.getSprite(6), 0.1f);
        die.setLoop(false);

        // Big mario animations
        AnimationState bigRun = new AnimationState();
        bigRun.title = "BigRun";
        bigRun.addFrame(bigMarioSpriteSheet.getSprite(0), defaultFrameTime);
        bigRun.addFrame(bigMarioSpriteSheet.getSprite(1), defaultFrameTime);
        bigRun.addFrame(bigMarioSpriteSheet.getSprite(2), defaultFrameTime);
        bigRun.addFrame(bigMarioSpriteSheet.getSprite(3), defaultFrameTime);
        bigRun.addFrame(bigMarioSpriteSheet.getSprite(2), defaultFrameTime);
        bigRun.addFrame(bigMarioSpriteSheet.getSprite(1), defaultFrameTime);
        bigRun.setLoop(true);

        AnimationState bigSwitchDirection = new AnimationState();
        bigSwitchDirection.title = "Big Switch Direction";
        bigSwitchDirection.addFrame(bigMarioSpriteSheet.getSprite(4), 0.1f);
        bigSwitchDirection.setLoop(false);

        AnimationState bigIdle = new AnimationState();
        bigIdle.title = "BigIdle";
        bigIdle.addFrame(bigMarioSpriteSheet.getSprite(0), 0.1f);
        bigIdle.setLoop(false);

        AnimationState bigJump = new AnimationState();
        bigJump.title = "BigJump";
        bigJump.addFrame(bigMarioSpriteSheet.getSprite(5), 0.1f);
        bigJump.setLoop(false);

        // Fire mario animations
        int fireOffset = 21;
        AnimationState fireRun = new AnimationState();
        fireRun.title = "FireRun";
        fireRun.addFrame(bigMarioSpriteSheet.getSprite(fireOffset + 0), defaultFrameTime);
        fireRun.addFrame(bigMarioSpriteSheet.getSprite(fireOffset + 1), defaultFrameTime);
        fireRun.addFrame(bigMarioSpriteSheet.getSprite(fireOffset + 2), defaultFrameTime);
        fireRun.addFrame(bigMarioSpriteSheet.getSprite(fireOffset + 3), defaultFrameTime);
        fireRun.addFrame(bigMarioSpriteSheet.getSprite(fireOffset + 2), defaultFrameTime);
        fireRun.addFrame(bigMarioSpriteSheet.getSprite(fireOffset + 1), defaultFrameTime);
        fireRun.setLoop(true);

        AnimationState fireSwitchDirection = new AnimationState();
        fireSwitchDirection.title = "Fire Switch Direction";
        fireSwitchDirection.addFrame(bigMarioSpriteSheet.getSprite(fireOffset + 4), 0.1f);
        fireSwitchDirection.setLoop(false);

        AnimationState fireIdle = new AnimationState();
        fireIdle.title = "FireIdle";
        fireIdle.addFrame(bigMarioSpriteSheet.getSprite(fireOffset + 0), 0.1f);
        fireIdle.setLoop(false);

        AnimationState fireJump = new AnimationState();
        fireJump.title = "FireJump";
        fireJump.addFrame(bigMarioSpriteSheet.getSprite(fireOffset + 5), 0.1f);
        fireJump.setLoop(false);

        // register state machine
        StateMachine stateMachine = new StateMachine();
        stateMachine.addState(run);
        stateMachine.addState(idle);
        stateMachine.addState(switchDirection);
        stateMachine.addState(jump);
        stateMachine.addState(die);

        stateMachine.addState(bigRun);
        stateMachine.addState(bigIdle);
        stateMachine.addState(bigSwitchDirection);
        stateMachine.addState(bigJump);

        stateMachine.addState(fireRun);
        stateMachine.addState(fireIdle);
        stateMachine.addState(fireSwitchDirection);
        stateMachine.addState(fireJump);

        stateMachine.setDefaultStateTitle(idle.title);
        stateMachine.addStateTrigger(run.title, switchDirection.title, "switchDirection");
        stateMachine.addStateTrigger(run.title, idle.title, "stopRunning");
        stateMachine.addStateTrigger(run.title, jump.title, "jump");
        stateMachine.addStateTrigger(switchDirection.title, idle.title, "stopRunning");
        stateMachine.addStateTrigger(switchDirection.title, run.title, "startRunning");
        stateMachine.addStateTrigger(switchDirection.title, jump.title, "jump");
        stateMachine.addStateTrigger(idle.title, run.title, "startRunning");
        stateMachine.addStateTrigger(idle.title, jump.title, "jump");
        stateMachine.addStateTrigger(jump.title, idle.title, "stopJumping");

        stateMachine.addStateTrigger(bigRun.title, bigSwitchDirection.title, "switchDirection");
        stateMachine.addStateTrigger(bigRun.title, bigIdle.title, "stopRunning");
        stateMachine.addStateTrigger(bigRun.title, bigJump.title, "jump");
        stateMachine.addStateTrigger(bigSwitchDirection.title, bigIdle.title, "stopRunning");
        stateMachine.addStateTrigger(bigSwitchDirection.title, bigRun.title, "startRunning");
        stateMachine.addStateTrigger(bigSwitchDirection.title, bigJump.title, "jump");
        stateMachine.addStateTrigger(bigIdle.title, bigRun.title, "startRunning");
        stateMachine.addStateTrigger(bigIdle.title, bigJump.title, "jump");
        stateMachine.addStateTrigger(bigJump.title, bigIdle.title, "stopJumping");

        stateMachine.addStateTrigger(fireRun.title, fireSwitchDirection.title, "switchDirection");
        stateMachine.addStateTrigger(fireRun.title, fireIdle.title, "stopRunning");
        stateMachine.addStateTrigger(fireRun.title, fireJump.title, "jump");
        stateMachine.addStateTrigger(fireSwitchDirection.title, fireIdle.title, "stopRunning");
        stateMachine.addStateTrigger(fireSwitchDirection.title, fireRun.title, "startRunning");
        stateMachine.addStateTrigger(fireSwitchDirection.title, fireJump.title, "jump");
        stateMachine.addStateTrigger(fireIdle.title, fireRun.title, "startRunning");
        stateMachine.addStateTrigger(fireIdle.title, fireJump.title, "jump");
        stateMachine.addStateTrigger(fireJump.title, fireIdle.title, "stopJumping");

        stateMachine.addStateTrigger(run.title, bigRun.title, "powerup");
        stateMachine.addStateTrigger(idle.title, bigIdle.title, "powerup");
        stateMachine.addStateTrigger(switchDirection.title, bigSwitchDirection.title, "powerup");
        stateMachine.addStateTrigger(jump.title, bigJump.title, "powerup");
        stateMachine.addStateTrigger(bigRun.title, fireRun.title, "powerup");
        stateMachine.addStateTrigger(bigIdle.title, fireIdle.title, "powerup");
        stateMachine.addStateTrigger(bigSwitchDirection.title, fireSwitchDirection.title, "powerup");
        stateMachine.addStateTrigger(bigJump.title, fireJump.title, "powerup");

        stateMachine.addStateTrigger(bigRun.title, run.title, "damage");
        stateMachine.addStateTrigger(bigIdle.title, idle.title, "damage");
        stateMachine.addStateTrigger(bigSwitchDirection.title, switchDirection.title, "damage");
        stateMachine.addStateTrigger(bigJump.title, jump.title, "damage");
        stateMachine.addStateTrigger(fireRun.title, bigRun.title, "damage");
        stateMachine.addStateTrigger(fireIdle.title, bigIdle.title, "damage");
        stateMachine.addStateTrigger(fireSwitchDirection.title, bigSwitchDirection.title, "damage");
        stateMachine.addStateTrigger(fireJump.title, bigJump.title, "damage");

        stateMachine.addStateTrigger(run.title, die.title, "die");
        stateMachine.addStateTrigger(switchDirection.title, die.title, "die");
        stateMachine.addStateTrigger(idle.title, die.title, "die");
        stateMachine.addStateTrigger(jump.title, die.title, "die");
        stateMachine.addStateTrigger(bigRun.title, run.title, "die");
        stateMachine.addStateTrigger(bigSwitchDirection.title, switchDirection.title, "die");
        stateMachine.addStateTrigger(bigIdle.title, idle.title, "die");
        stateMachine.addStateTrigger(bigJump.title, jump.title, "die");
        stateMachine.addStateTrigger(fireRun.title, bigRun.title, "die");
        stateMachine.addStateTrigger(fireSwitchDirection.title, bigSwitchDirection.title, "die");
        stateMachine.addStateTrigger(fireIdle.title, bigIdle.title, "die");
        stateMachine.addStateTrigger(fireJump.title, bigJump.title, "die");

        mario.addComponent(stateMachine);

        PillboxCollider pillboxCollider = new PillboxCollider();
        pillboxCollider.setWidth(0.21f);
        pillboxCollider.setHeight(0.25f);
        mario.addComponent(pillboxCollider);

        RigidBody2D rigidBody2D = new RigidBody2D();
        rigidBody2D.setBodyType(BodyType.Dynamic);
        rigidBody2D.setContinuousCollision(false);
        rigidBody2D.setFixedRotation(true);
        rigidBody2D.setMass(25.0f);
        mario.addComponent(rigidBody2D);

        mario.addComponent(new PlayerController());

        mario.transform.zIndex = 10;
        return mario;
    }

    public static GameObject generateQuestionBlock() {
        SpriteSheet itemSpriteSheet = AssetPool.getSpriteSheet(itemSpriteSheetPath);
        GameObject questionBlock = generateSpriteObject(itemSpriteSheet.getSprite(0), 0.25f, 0.25f);

        AnimationState flicker = new AnimationState();
        flicker.title = "Flicker";
        float defaultFrameTime = 0.23f;
        flicker.addFrame(itemSpriteSheet.getSprite(0), 0.57f);
        flicker.addFrame(itemSpriteSheet.getSprite(1), defaultFrameTime);
        flicker.addFrame(itemSpriteSheet.getSprite(2), defaultFrameTime);
        flicker.setLoop(true);

        AnimationState inactive = new AnimationState();
        inactive.title = "Inactive";
        inactive.addFrame(itemSpriteSheet.getSprite(3), 0.1f);
        inactive.setLoop(false);

        StateMachine stateMachine = new StateMachine();
        stateMachine.addState(flicker);
        stateMachine.addState(inactive);
        stateMachine.setDefaultStateTitle(flicker.title);
        stateMachine.addStateTrigger(flicker.title, inactive.title, "setInactive");
        questionBlock.addComponent(stateMachine);
        questionBlock.addComponent(new QuestionBlock());

        RigidBody2D rigidBody2D = new RigidBody2D();
        rigidBody2D.setBodyType(BodyType.Static);
        questionBlock.addComponent(rigidBody2D);
        Box2DCollider box2DCollider = new Box2DCollider();
        box2DCollider.setHalfSize(new Vector2f(0.25f, 0.25f));
        questionBlock.addComponent(box2DCollider);
        questionBlock.addComponent(new Ground());
        return questionBlock;
    }

    public static GameObject generateBlockCoin() {
        SpriteSheet itemSpriteSheet = AssetPool.getSpriteSheet(itemSpriteSheetPath);
        GameObject coin = generateSpriteObject(itemSpriteSheet.getSprite(7), 0.25f, 0.25f);

        AnimationState coinFlip = new AnimationState();
        coinFlip.title = "CoinFlip";
        float defaultFrameTime = 0.23f;
        coinFlip.addFrame(itemSpriteSheet.getSprite(7), 0.57f);
        coinFlip.addFrame(itemSpriteSheet.getSprite(8), defaultFrameTime);
        coinFlip.addFrame(itemSpriteSheet.getSprite(9), defaultFrameTime);
        coinFlip.setLoop(true);

        StateMachine stateMachine = new StateMachine();
        stateMachine.addState(coinFlip);
        stateMachine.setDefaultStateTitle(coinFlip.title);
        coin.addComponent(stateMachine);
        coin.addComponent(new BlockCoin());

        return coin;
    }

    public static GameObject generateMushroom() {
        SpriteSheet itemSpriteSheet = AssetPool.getSpriteSheet(itemSpriteSheetPath);
        GameObject mushroom = generateSpriteObject(itemSpriteSheet.getSprite(10), 0.25f, 0.25f);

        RigidBody2D rb = new RigidBody2D();
        rb.setBodyType(BodyType.Dynamic);
        rb.setFixedRotation(true);
        rb.setContinuousCollision(false);
        mushroom.addComponent(rb);

        CircleCollider circleCollider = new CircleCollider();
        circleCollider.setRadius(0.14f);
        mushroom.addComponent(circleCollider);
        mushroom.addComponent(new MushroomAI());

        return mushroom;
    }

    public static GameObject generateFlower() {
        SpriteSheet itemSpriteSheet = AssetPool.getSpriteSheet(itemSpriteSheetPath);
        GameObject flower = generateSpriteObject(itemSpriteSheet.getSprite(20), 0.25f, 0.25f);

        RigidBody2D rb = new RigidBody2D();
        rb.setBodyType(BodyType.Static);
        rb.setFixedRotation(true);
        rb.setContinuousCollision(false);
        flower.addComponent(rb);

        CircleCollider circleCollider = new CircleCollider();
        circleCollider.setRadius(0.14f);
        flower.addComponent(circleCollider);
        flower.addComponent(new Flower());

        return flower;
    }

    public static GameObject generateGoomba() {
        SpriteSheet itemSpriteSheet = AssetPool.getSpriteSheet(marioSpriteSheetPath);
        GameObject goomba = generateSpriteObject(itemSpriteSheet.getSprite(14), 0.25f, 0.25f);

        AnimationState walk = new AnimationState();
        walk.title = "Walk";
        float defaultFrameTime = 0.23f;
        walk.addFrame(itemSpriteSheet.getSprite(14), defaultFrameTime);
        walk.addFrame(itemSpriteSheet.getSprite(15), defaultFrameTime);
        walk.setLoop(true);

        AnimationState squashed = new AnimationState();
        squashed.title = "Squashed";
        squashed.addFrame(itemSpriteSheet.getSprite(16), 0.1f);
        squashed.setLoop(false);

        StateMachine stateMachine = new StateMachine();
        stateMachine.addState(walk);
        stateMachine.addState(squashed);
        stateMachine.setDefaultStateTitle(walk.title);
        stateMachine.addStateTrigger(walk.title, squashed.title, "squashMe");
        goomba.addComponent(stateMachine);

        RigidBody2D rigidBody2D = new RigidBody2D();
        rigidBody2D.setBodyType(BodyType.Dynamic);
        rigidBody2D.setMass(0.1f);
        rigidBody2D.setFixedRotation(true);
        goomba.addComponent(rigidBody2D);
        CircleCollider circleCollider = new CircleCollider();
        circleCollider.setRadius(0.12f);
        goomba.addComponent(circleCollider);
        goomba.addComponent(new GoombaAI());
        return goomba;
    }

    public static GameObject generatePipe(int spriteIndex) {
        SpriteSheet pipesSpriteSheet = AssetPool.getSpriteSheet(pipeSpriteSheetPath);
        GameObject pipe = generateSpriteObject(pipesSpriteSheet.getSprite(spriteIndex), 0.5f, 0.5f);

        RigidBody2D rigidBody2D = new RigidBody2D();
        rigidBody2D.setBodyType(BodyType.Static);
        rigidBody2D.setFixedRotation(true);
        rigidBody2D.setContinuousCollision(false);
        pipe.addComponent(rigidBody2D);

        Box2DCollider box2DCollider = new Box2DCollider();
        box2DCollider.setHalfSize(new Vector2f(0.5f, 0.5f));
        pipe.addComponent(box2DCollider);
        pipe.addComponent(new Ground());
        pipe.addComponent(new Pipe(Direction.values()[spriteIndex]));
        return pipe;
    }

    public static GameObject generateTurtle() {
        SpriteSheet turtleSpriteSheet = AssetPool.getSpriteSheet(turtleSpriteSheetPath);
        GameObject turtle = generateSpriteObject(turtleSpriteSheet.getSprite(0), 0.25f, 0.35f);

        AnimationState walk = new AnimationState();
        walk.title = "Walk";
        float defaultFrameTime = 0.23f;
        walk.addFrame(turtleSpriteSheet.getSprite(0), defaultFrameTime);
        walk.addFrame(turtleSpriteSheet.getSprite(1), defaultFrameTime);
        walk.setLoop(true);

        AnimationState turtleShell = new AnimationState();
        turtleShell.title = "TurtleShellSpin";
        turtleShell.addFrame(turtleSpriteSheet.getSprite(2), 0.1f);
        turtleShell.setLoop(false);

        StateMachine stateMachine = new StateMachine();
        stateMachine.addState(walk);
        stateMachine.addState(turtleShell);
        stateMachine.setDefaultStateTitle(walk.title);
        stateMachine.addStateTrigger(walk.title, turtleShell.title, "squashMe");
        turtle.addComponent(stateMachine);

        RigidBody2D rigidBody2D = new RigidBody2D();
        rigidBody2D.setBodyType(BodyType.Dynamic);
        rigidBody2D.setMass(0.1f);
        rigidBody2D.setFixedRotation(true);
        turtle.addComponent(rigidBody2D);
        CircleCollider circleCollider = new CircleCollider();
        circleCollider.setRadius(0.13f);
        circleCollider.setOffset(new Vector2f(0, -0.05f));
        turtle.addComponent(circleCollider);
        turtle.addComponent(new TurtleAI());
        return turtle;
    }

    public static GameObject generateFlagTop() {
        SpriteSheet itemSpriteSheet = AssetPool.getSpriteSheet(itemSpriteSheetPath);
        GameObject flagTop = generateSpriteObject(itemSpriteSheet.getSprite(6), 0.25f, 0.25f);

        RigidBody2D rb = new RigidBody2D();
        rb.setBodyType(BodyType.Dynamic);
        rb.setFixedRotation(true);
        rb.setContinuousCollision(false);
        flagTop.addComponent(rb);

        Box2DCollider box2DCollider = new Box2DCollider();
        box2DCollider.setHalfSize(new Vector2f(0.1f, 0.25f));
        box2DCollider.setOffset(new Vector2f(-0.075f, 0f));
        flagTop.addComponent(box2DCollider);
        flagTop.addComponent(new FlagPole(true));

        return flagTop;
    }

    public static GameObject generateFlagPole() {
        SpriteSheet itemSpriteSheet = AssetPool.getSpriteSheet(itemSpriteSheetPath);
        GameObject flagTop = generateSpriteObject(itemSpriteSheet.getSprite(33), 0.25f, 0.25f);

        RigidBody2D rb = new RigidBody2D();
        rb.setBodyType(BodyType.Dynamic);
        rb.setFixedRotation(true);
        rb.setContinuousCollision(false);
        flagTop.addComponent(rb);

        Box2DCollider box2DCollider = new Box2DCollider();
        box2DCollider.setHalfSize(new Vector2f(0.1f, 0.25f));
        box2DCollider.setOffset(new Vector2f(-0.075f, 0f));
        flagTop.addComponent(box2DCollider);
        flagTop.addComponent(new FlagPole(false));

        return flagTop;
    }

    public static GameObject generateFireball(Vector2f position) {
        SpriteSheet itemSpriteSheet = AssetPool.getSpriteSheet(itemSpriteSheetPath);
        GameObject fireball = generateSpriteObject(itemSpriteSheet.getSprite(32), 0.18f, 0.18f);
        fireball.transform.position.set(position);

        RigidBody2D rb = new RigidBody2D();
        rb.setBodyType(BodyType.Dynamic);
        rb.setFixedRotation(true);
        rb.setContinuousCollision(false);
        fireball.addComponent(rb);

        CircleCollider circleCollider = new CircleCollider();
        circleCollider.setRadius(0.08f);
        fireball.addComponent(circleCollider);
        fireball.addComponent(new Fireball());

        return fireball;
    }
}
