package components;

import jade.GameObject;
import jade.Window;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import physics2d.Physics2D;
import physics2d.components.RigidBody2D;

public class Fireball extends Component {
    private static int fireballCount = 0;
    private transient RigidBody2D rigidBody2D;
    private transient float fireballSpeed = 1.7f;
    private transient Vector2f velocity = new Vector2f();
    private transient Vector2f acceleration = new Vector2f();
    private transient Vector2f terminalVelocity = new Vector2f(2.1f, 3.1f);
    private transient boolean onGround = false;
    private transient float lifetime = 4.0f;

    public transient boolean goingRight = true;

    @Override
    public void start() {
        rigidBody2D = gameObject.getComponent(RigidBody2D.class);
        acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
        fireballCount++;
    }

    @Override
    public void update(float dt) {
        lifetime -= dt;
        if (lifetime <= 0) {
            disappear();
            return;
        }

        if (goingRight) {
            velocity.x = fireballSpeed;
        } else {
            velocity.x = -fireballSpeed;
        }

        checkOnGround();
        if (onGround) {
            acceleration.y = 1.5f;
            velocity.y = 2.5f;
        } else {
            acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
        }

        velocity.y += acceleration.y * dt;
        velocity.y = Math.max(Math.min(velocity.y, terminalVelocity.y), -terminalVelocity.y);
        rigidBody2D.setVelocity(velocity);
    }

    @Override
    public void beginCollision(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
        if (Math.abs(hitNormal.x) > 0.8f) {
            goingRight = hitNormal.x < 0;
        }
    }

    @Override
    public void preSolve(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
        if (collidingObject.getComponent(PlayerController.class) != null || collidingObject.getComponent(Fireball.class) != null) {
            contact.setEnabled(false);
        }
    }

    public void disappear() {
        fireballCount--;
        gameObject.destroy();
    }

    public static boolean canSpawn() {
        return fireballCount < 4;
    }

    public void checkOnGround() {
        float innerPlayerWidth = 0.25f * 0.7f;
        float yVal = -0.09f;
        onGround = Physics2D.checkOnGround(this.gameObject, innerPlayerWidth, yVal);
    }

}
