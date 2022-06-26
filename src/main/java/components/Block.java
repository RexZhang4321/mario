package components;

import jade.GameObject;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import util.AssetPool;

public abstract class Block extends Component {
    private transient boolean bopGoingUp = true;
    private transient boolean doBopAnimation = false;
    private transient Vector2f bopStart;
    private transient Vector2f topBopLocation;
    private transient boolean active = true;

    public float bopSpeed = 0.4f;

    @Override
    public void start() {
        bopStart = new Vector2f(gameObject.transform.position);
        topBopLocation = new Vector2f(bopStart).add(0f, 0.02f);
    }

    @Override
    public void update(float dt) {
        if (doBopAnimation) {
            if (bopGoingUp) {
                if (gameObject.transform.position.y < topBopLocation.y) {
                    gameObject.transform.position.y += bopSpeed * dt;
                } else {
                    bopGoingUp = false;
                }
            } else {
                // bop going down
                if (gameObject.transform.position.y > bopStart.y) {
                    gameObject.transform.position.y -= bopSpeed * dt;
                } else {
                    gameObject.transform.position.y = bopStart.y;
                    bopGoingUp = true;
                    doBopAnimation = false;
                }
            }
        }
    }

    @Override
    public void beginCollision(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
        PlayerController playerController = collidingObject.getComponent(PlayerController.class);
        if (active && playerController != null && hitNormal.y < -0.8f) {
            doBopAnimation = true;
            AssetPool.getSound("assets/sounds/bump.ogg").play();
            playerHit(playerController);
        }
    }

    public void setInactive() {
        active = false;
    }

    abstract void playerHit(PlayerController playerController);
}
