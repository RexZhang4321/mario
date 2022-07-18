package components;

import jade.Camera;
import jade.GameObject;
import jade.Window;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import physics2d.Physics2D;
import physics2d.components.RigidBody2D;
import util.AssetPool;

public class TurtleAI extends Component {
    private transient boolean goingRight = false;
    private transient RigidBody2D rigidBody2D;
    private transient float walkSpeed = 0.6f;
    private transient Vector2f velocity = new Vector2f();
    private transient Vector2f acceleration = new Vector2f();
    private transient Vector2f terminalVelocity = new Vector2f(2.1f, 3.1f);
    private transient boolean onGround = false;
    private transient boolean isDead = false;
    private transient boolean isMoving = false;
    private transient StateMachine stateMachine;
    private transient float movingDebounce = 0.32f;

    @Override
    public void start() {
        stateMachine = gameObject.getComponent(StateMachine.class);
        rigidBody2D = gameObject.getComponent(RigidBody2D.class);
        acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
    }

    @Override
    public void update(float dt) {
        movingDebounce -= dt;
        Camera camera = Window.getScene().camera();
        if (gameObject.transform.position.x > camera.position.x + camera.getProjectionSize().x * camera.getZoom()) {
            return;
        }
        if (!isDead || isMoving) {
            if (goingRight) {
                velocity.x = walkSpeed;
                gameObject.transform.scale.x = -0.25f;
            } else {
                velocity.x = -walkSpeed;
                gameObject.transform.scale.x = 0.25f;
            }
        } else {
            velocity.x = 0;
        }

        checkOnGround();
        if (onGround) {
            acceleration.y = 0;
            velocity.y = 0;
        } else {
            acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
        }

        velocity.y += acceleration.y * dt;
        velocity.y = Math.max(Math.min(velocity.y, terminalVelocity.y), -terminalVelocity.y);
        rigidBody2D.setVelocity(velocity);

        if (gameObject.transform.position.x < camera.position.x - 0.5f) {
            gameObject.destroy();
        }
    }

    public void checkOnGround() {
        float innerPlayerWidth = 0.25f * 0.7f;
        float yVal = -0.2f;
        onGround = Physics2D.checkOnGround(this.gameObject, innerPlayerWidth, yVal);
    }

    public void stomp() {
        isDead = true;
        isMoving = false;
        velocity.zero();
        rigidBody2D.setVelocity(velocity);
        rigidBody2D.setAngularVelocity(0f);
        rigidBody2D.setGravityScale(0f);
        stateMachine.trigger("squashMe");
        AssetPool.getSound("assets/sounds/bump.ogg").play();
    }

    @Override
    public void beginCollision(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
        PlayerController playerController = collidingObject.getComponent(PlayerController.class);
        if (playerController != null) {
            if (!isDead && !playerController.isDead() && !playerController.isHurtInvincible() && hitNormal.y > 0.58f) {
                playerController.enemyBounce();
                stomp();
                walkSpeed *= 3.0f;
            } else if (movingDebounce < 0 && !playerController.isDead() && !playerController.isHurtInvincible() && (isMoving || !isDead) && hitNormal.y < 0.58f) {
                playerController.die();
            } else if (!playerController.isDead() && !playerController.isHurtInvincible()) {
                if (isDead && hitNormal.y > 0.58f) {
                    playerController.enemyBounce();
                    isMoving = !isMoving;
                    goingRight = hitNormal.x < 0;
                } else if (isDead && !isMoving) {
                    isMoving = true;
                    goingRight = hitNormal.x < 0;
                    movingDebounce = 0.32f;
                }
            }
        } else if (Math.abs(hitNormal.y) < 0.1f && !collidingObject.isDead()) {
            goingRight = hitNormal.x < 0;
            if (isMoving && isDead) {
                AssetPool.getSound("assets/sounds/bump.ogg").play();
            }
        }

        if (collidingObject.getComponent(Fireball.class) != null) {
            if (!isDead) {
                walkSpeed *= 3.0f;
                stomp();
            } else {
                isMoving = !isMoving;
                goingRight = hitNormal.x < 0;
            }
            collidingObject.getComponent(Fireball.class).disappear();
            contact.setEnabled(false);
        }
    }

    @Override
    public void preSolve(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
        GoombaAI goomba = collidingObject.getComponent(GoombaAI.class);
        if (isDead && isMoving && goomba != null) {
            goomba.stomp();
            contact.setEnabled(false);
            AssetPool.getSound("assets/sounds/kick.ogg").play();
        }
    }
}
