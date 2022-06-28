package components;

import jade.GameObject;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import physics2d.components.RigidBody2D;
import util.AssetPool;

public class MushroomAI extends Component {
    private transient boolean goingRight = true;
    private transient RigidBody2D rigidBody2D;
    private transient Vector2f speed = new Vector2f(1.0f, 0.0f);
    private transient float maxSpeed = 0.8f;
    private transient boolean hitPlayer = false;

    @Override
    public void start() {
        rigidBody2D = gameObject.getComponent(RigidBody2D.class);
        AssetPool.getSound("assets/sounds/powerup_appears.ogg").play();
    }

    @Override
    public void update(float dt) {
        if (goingRight && Math.abs(rigidBody2D.getVelocity().x) < maxSpeed) {
            rigidBody2D.addVelocity(speed);
        } else if (!goingRight && Math.abs(rigidBody2D.getVelocity().x) < maxSpeed) {
            rigidBody2D.addVelocity(new Vector2f(-speed.x, speed.y));
        }
    }

    @Override
    public void preSolve(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
        // if the mushroom touches the player, disable the physics so that the mushroom can run through the player
        PlayerController playerController = collidingObject.getComponent(PlayerController.class);
        if (playerController != null) {
            contact.setEnabled(false);
            if (!hitPlayer) {
                playerController.powerup();
                gameObject.destroy();
                hitPlayer = true;
            }
        }

        // if the mushroom touches anything horizontally, switch the direction
        if (Math.abs(hitNormal.y) < 0.1f) {
            goingRight = hitNormal.x < 0;
        }
    }
}
