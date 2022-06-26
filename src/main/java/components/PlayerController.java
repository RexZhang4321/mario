package components;

import jade.KeyListener;
import jade.Window;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import physics2d.components.RigidBody2D;

public class PlayerController extends Component {
    public float walkSpeed = 1.9f;
    public float jumpBoost = 1.0f;
    public float jumpImpulse = 3.0f;
    public float slowDownForce = 0.05f;
    public Vector2f terminalVelocity = new Vector2f(2.1f, 3.1f);

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

    @Override
    public void start() {
        rigidBody2D = gameObject.getComponent(RigidBody2D.class);
        stateMachine = gameObject.getComponent(StateMachine.class);
        rigidBody2D.setGravityScale(0.0f);
    }

    @Override
    public void update(float dt) {
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

        acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
        velocity.x += acceleration.x * dt;
        velocity.y += acceleration.y * dt;
        velocity.x = Math.max(Math.min(velocity.x, terminalVelocity.x), -terminalVelocity.x);
        velocity.y = Math.max(Math.min(velocity.y, terminalVelocity.y), -terminalVelocity.y);

        rigidBody2D.setVelocity(velocity);
        rigidBody2D.setAngularVelocity(0.0f);
    }
}
