package components;

import jade.Camera;
import jade.GameObject;
import jade.Window;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import physics2d.Physics2D;
import physics2d.components.RigidBody2D;
import util.AssetPool;

public class GoombaAI extends Component {

    private transient boolean onGround = false;
    private transient RigidBody2D rigidBody2D;
    private transient float walkSpeed = 0.6f;
    private transient Vector2f velocity = new Vector2f();
    private transient Vector2f acceleration = new Vector2f();
    private transient Vector2f terminalVelocity = new Vector2f();
    private transient boolean goingRight = false;
    private transient boolean isDead = false;
    private transient float timeToKill = 0.5f;
    private transient StateMachine stateMachine;

    @Override
    public void start() {
        stateMachine = gameObject.getComponent(StateMachine.class);
        rigidBody2D = gameObject.getComponent(RigidBody2D.class);
        acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
    }

    @Override
    public void update(float dt) {
        Camera camera = Window.getScene().camera();
        // if the Goomba is on the right side of the camera, do nothing
        if (this.gameObject.transform.position.x > camera.position.x + camera.getProjectionSize().x * camera.getZoom()) {
            return;
        }

        if (isDead) {
            timeToKill -= dt;
            if (timeToKill <= 0) {
                gameObject.destroy();
            }
            rigidBody2D.setVelocity(new Vector2f());
            return;
        }

        if (goingRight) {
            velocity.x = walkSpeed;
        } else {
            velocity.x = -walkSpeed;
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
    }

    @Override
    public void beginCollision(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
        if (isDead) {
            return;
        }
        PlayerController playerController = collidingObject.getComponent(PlayerController.class);
        if (playerController != null) {
            // if the player hits the Goomba from its top
            if (!playerController.isDead() && !playerController.isHurtInvincible() && hitNormal.y > 0.58f) {
                // Goomba dies
                // bounce after hitting Goomba
                playerController.enemyBounce();
                stomp();
            } else if (!playerController.isDead() && !playerController.isInvincible()) {
                // player dies
                playerController.die();
            }
        } else if (Math.abs(hitNormal.y) < 0.1f) {
            goingRight = hitNormal.x < 0;
        }
    }

    public void stomp() {
        isDead = true;
        velocity.zero();
        rigidBody2D.setVelocity(new Vector2f());
        rigidBody2D.setAngularVelocity(0f);
        rigidBody2D.setGravityScale(0f);
        stateMachine.trigger("squashMe");
        rigidBody2D.setIsSensor();
        AssetPool.getSound("assets/sounds/bump.ogg").play();
    }

    public void checkOnGround() {
        float innerPlayerWidth = 0.25f * 0.7f;
        float yVal = -0.14f;
        onGround = Physics2D.checkOnGround(this.gameObject, innerPlayerWidth, yVal);
    }

}
