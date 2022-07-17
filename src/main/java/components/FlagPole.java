package components;

import jade.GameObject;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;

public class FlagPole extends Component {
    private boolean isTop = false;

    public FlagPole(boolean isTop) {
        this.isTop = isTop;
    }

    @Override
    public void beginCollision(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
        PlayerController playerController = collidingObject.getComponent(PlayerController.class);
        if (playerController != null) {
            playerController.playWinAnimation(gameObject);
        }
    }
}
