package components;

import jade.GameObject;
import jade.KeyListener;
import jade.Window;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import physics2d.Physics2D;
import physics2d.RaycastInfo;
import physics2d.components.PillboxCollider;
import physics2d.components.RigidBody2D;
import physics2d.enums.BodyType;
import renderer.DebugDraw;
import scenes.LevelEditorSceneInitializer;
import util.AssetPool;

public class PlayerController extends Component {

    private enum PlayerState {
        Small,
        Big,
        Fire,
        Invincible
    }

    public float walkSpeed = 1.9f;
    public float jumpBoost = 1.0f;
    public float jumpImpulse = 3.0f;
    public float slowDownForce = 0.05f;
    public Vector2f terminalVelocity = new Vector2f(2.1f, 3.1f);

    private PlayerState playerState = PlayerState.Small;
    public transient boolean onGround = false;
    private transient float groundDebounce = 0.0f;

    // how much time the player has after leaving the block/ground but still able to jump
    private transient float groundDebounceTime = 0.1f;
    private transient RigidBody2D rigidBody2D;
    private transient StateMachine stateMachine;

    private transient float bigJumpBoostFactor = 1.0f;
    private transient float playerWidth = 0.25f;

    // if pressing and holding the space, how long can we jump
    private transient int jumpTime = 0;

    private transient Vector2f acceleration = new Vector2f();
    private transient Vector2f velocity = new Vector2f();
    private transient boolean isDead = false;
    private transient int enemyBounce = 0;

    private transient float hurtInvincibililityTimeLeft = 0;

    private transient float hurtInvincibilityTime = 1.4f;

    private transient float deadMaxHeight = 0;
    private transient float deadMinHeight = 0;
    private transient boolean deadGoingUp = true;
    private transient float blinkTime = 0;

    private transient SpriteRenderer spriteRenderer;

    @Override
    public void start() {
        rigidBody2D = gameObject.getComponent(RigidBody2D.class);
        stateMachine = gameObject.getComponent(StateMachine.class);
        rigidBody2D.setGravityScale(0.0f);
        spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
    }

    @Override
    public void update(float dt) {
        if (isDead) {
            if (gameObject.transform.position.y < deadMaxHeight && deadGoingUp) {
                gameObject.transform.position.y += dt * walkSpeed / 2.0f;
            } else if (gameObject.transform.position.y >= deadMaxHeight && deadGoingUp) {
                deadGoingUp = false;
            } else if (!deadGoingUp && gameObject.transform.position.y > deadMinHeight) {
                rigidBody2D.setBodyType(BodyType.Kinematic);
                acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
                velocity.y += acceleration.y * dt;
                velocity.y = Math.max(Math.min(velocity.y, terminalVelocity.y), -terminalVelocity.y);
                rigidBody2D.setVelocity(velocity);
                rigidBody2D.setAngularVelocity(0);
            } else if (!deadGoingUp && gameObject.transform.position.y <= deadMinHeight) {
                Window.changeScene(new LevelEditorSceneInitializer());
            }
            return;
        }

        if (hurtInvincibililityTimeLeft > 0) {
            hurtInvincibililityTimeLeft -= dt;
            blinkTime -= dt;

            if (blinkTime <= 0) {
                blinkTime = 0.2f;
                if (spriteRenderer.getColor().w == 1) {
                    spriteRenderer.setColor(new Vector4f(1, 1, 1, 0));
                } else  {
                    spriteRenderer.setColor(new Vector4f(1, 1, 1, 1));
                }
            } else {
                if (spriteRenderer.getColor().w == 0) {
                    spriteRenderer.setColor(new Vector4f(1, 1, 1, 1));
                }
            }
        }

        if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_RIGHT) || KeyListener.isKeyPressed(GLFW.GLFW_KEY_D)) {
            gameObject.transform.scale.x = playerWidth;
            acceleration.x = walkSpeed;

            if (velocity.x < 0) {
                stateMachine.trigger("switchDirection");
                velocity.x += slowDownForce;
            } else {
                stateMachine.trigger("startRunning");
            }
        } else if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_LEFT) || KeyListener.isKeyPressed(GLFW.GLFW_KEY_A)) {
            gameObject.transform.scale.x = -playerWidth;
            acceleration.x = -walkSpeed;

            if (velocity.x > 0) {
                stateMachine.trigger("switchDirection");
                velocity.x -= slowDownForce;
            } else {
                stateMachine.trigger("startRunning");
            }
        } else {
            acceleration.x = 0;
            if (velocity.x > 0) {
                velocity.x = Math.max(0, velocity.x - slowDownForce);
            } else if (velocity.x < 0) {
                velocity.x = Math.min(0, velocity.x + slowDownForce);
            }

            if (velocity.x == 0) {
                stateMachine.trigger("stopRunning");
            } else {
                stateMachine.trigger("startRunning");
            }
        }

        checkOnGround();

        if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_SPACE) && (jumpTime > 0 || onGround || groundDebounce > 0)) {
            if (jumpTime == 0) {
                // when just press the jump key
                AssetPool.getSound("assets/sounds/jump-small.ogg").play();
                jumpTime = 28;
                velocity.y = jumpImpulse;
            } else if (jumpTime > 0) {
                jumpTime--;
                velocity.y = jumpTime / 2.2f * jumpBoost;
            } else {
                velocity.y = 0;
            }
            groundDebounce = 0;
        } else if (enemyBounce > 0) {
            enemyBounce--;
            velocity.y = (enemyBounce / 2.2f) * jumpBoost;
        } else if (!onGround) {
            // if in the middle of the jump
            if (jumpTime > 0) {
                velocity.y *= 0.35f;
                jumpTime = 0;
            }
            groundDebounce -= dt;
            acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
        } else {
            // if we are on the ground
            velocity.y = 0;
            acceleration.y = 0;
            groundDebounce = groundDebounceTime;
        }

        acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
        velocity.x += acceleration.x * dt;
        velocity.y += acceleration.y * dt;
        velocity.x = Math.max(Math.min(velocity.x, terminalVelocity.x), -terminalVelocity.x);
        velocity.y = Math.max(Math.min(velocity.y, terminalVelocity.y), -terminalVelocity.y);

        rigidBody2D.setVelocity(velocity);
        rigidBody2D.setAngularVelocity(0.0f);

        if (!onGround) {
            stateMachine.trigger("jump");
        } else {
            stateMachine.trigger("stopJumping");
        }
    }

    public void checkOnGround() {
        float innerPlayerWidth = this.playerWidth * 0.6f;
        float yVal = playerState == PlayerState.Small ? -0.14f : -0.24f;
        onGround = Physics2D.checkOnGround(gameObject, innerPlayerWidth, yVal);
    }

    @Override
    public void beginCollision(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
        if (isDead) {
            return;
        }
        if (collidingObject.getComponent(Ground.class) != null) {
            // if we hit the object horizontally
            if (Math.abs(hitNormal.x) > 0.8f) {
                velocity.x = 0;
            } else {
                // if we hit the top of the player
                velocity.y = 0;
                acceleration.y = 0;
                jumpTime = 0;
            }
        }
    }

    public boolean isSmall() {
        return playerState == PlayerState.Small;
    }

    public void powerup() {
        if (playerState == PlayerState.Small) {
            playerState = PlayerState.Big;
            AssetPool.getSound("assets/sounds/powerup.ogg").play();
            gameObject.transform.scale.y = 0.42f;
            PillboxCollider pillboxCollider = gameObject.getComponent(PillboxCollider.class);
            if (pillboxCollider != null) {
                jumpBoost *= bigJumpBoostFactor;
                walkSpeed *= bigJumpBoostFactor;
                pillboxCollider.setHeight(0.42f);
            }
        } else if (playerState == PlayerState.Big) {
            // TODO: FIX the situation where the player hit a mushroom in the big state, currently the player will still powerup
            playerState = PlayerState.Fire;
            AssetPool.getSound("assets/sounds/powerup.ogg").play();
        }

        stateMachine.trigger("powerup");
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean isHurtInvincible() {
        return hurtInvincibililityTimeLeft > 0;
    }

    public boolean isInvincible() {
        return playerState == PlayerState.Invincible || isHurtInvincible();
    }

    public void enemyBounce() {
        enemyBounce = 8;
    }

    public void die() {
        stateMachine.trigger("die");
        if (playerState == PlayerState.Small) {
            velocity.zero();
            acceleration.zero();
            rigidBody2D.setVelocity(new Vector2f());
            isDead = true;
            rigidBody2D.setIsSensor();
            AssetPool.getSound("assets/sounds/mario_die.ogg").play();
            deadMaxHeight = gameObject.transform.position.y + 0.3f;
            rigidBody2D.setBodyType(BodyType.Static);
            if (gameObject.transform.position.y > 0) {
                deadMinHeight = -0.25f;
            }
        } else if (playerState == PlayerState.Big) {
            playerState = PlayerState.Small;
            gameObject.transform.scale.y = 0.25f;
            PillboxCollider pillboxCollider = gameObject.getComponent(PillboxCollider.class);
            if (pillboxCollider != null) {
                jumpBoost /= bigJumpBoostFactor;
                walkSpeed /= bigJumpBoostFactor;
                pillboxCollider.setHeight(0.25f);
            }
            hurtInvincibililityTimeLeft = hurtInvincibilityTime;
            AssetPool.getSound("assets/sounds/pipe.ogg").play();
        } else if (playerState == PlayerState.Fire) {
            playerState = PlayerState.Big;
            hurtInvincibililityTimeLeft = hurtInvincibilityTime;
            AssetPool.getSound("assets/sounds/pipe.ogg").play();
        }
    }
}
