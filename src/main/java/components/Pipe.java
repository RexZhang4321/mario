package components;

import jade.Direction;
import jade.GameObject;
import jade.KeyListener;
import jade.Window;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import util.AssetPool;

public class Pipe extends Component {
    private Direction direction;
    private String connectingPipeName = "";
    private boolean isEntrance = false;
    private transient GameObject connectingPipe = null;
    private transient float entranceVectorTolerance = 0.6f;
    private transient PlayerController collidingPlayer = null;

    public Pipe(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void start() {
        connectingPipe = Window.getScene().getGameObjectByName(connectingPipeName);

    }

    @Override
    public void update(float dt) {
        if (connectingPipe == null) {
            return;
        }

        if (collidingPlayer != null && shouldTeleport(direction)) {
            System.out.println("should teleport");
            collidingPlayer.setPosition(getPlayerPosition(connectingPipe));
            AssetPool.getSound("assets/sounds/pipe.ogg").play();
        }
    }

    @Override
    public void beginCollision(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
        PlayerController playerController = collidingObject.getComponent(PlayerController.class);
        if (playerController != null) {
            if (direction == Direction.Up && hitNormal.y < entranceVectorTolerance) {
                return;
            }
            if (direction == Direction.Down && hitNormal.y > -entranceVectorTolerance) {
                return;
            }
            if (direction == Direction.Left && hitNormal.x > -entranceVectorTolerance) {
                System.out.println("not hit from right: " + hitNormal.x);
                return;
            }
            if (direction == Direction.Right && hitNormal.x < entranceVectorTolerance) {
                System.out.println("not hit from left: " + hitNormal.x);
                return;
            }
        }
        collidingPlayer = playerController;
    }

    @Override
    public void endCollision(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
        PlayerController playerController = collidingObject.getComponent(PlayerController.class);
        if (playerController != null) {
            collidingPlayer = null;
        }
    }

    private boolean shouldTeleport(Direction direction) {
        if (direction == Direction.Up && (KeyListener.isKeyPressed(GLFW.GLFW_KEY_DOWN) || KeyListener.isKeyPressed(GLFW.GLFW_KEY_S)) && isEntrance) {
            return true;
        }
        if (direction == Direction.Down && (KeyListener.isKeyPressed(GLFW.GLFW_KEY_UP) || KeyListener.isKeyPressed(GLFW.GLFW_KEY_W)) && isEntrance) {
            return true;
        }
        if (direction == Direction.Left && (KeyListener.isKeyPressed(GLFW.GLFW_KEY_RIGHT) || KeyListener.isKeyPressed(GLFW.GLFW_KEY_D)) && isEntrance) {
            return true;
        }
        return direction == Direction.Right && (KeyListener.isKeyPressed(GLFW.GLFW_KEY_LEFT) || KeyListener.isKeyPressed(GLFW.GLFW_KEY_A)) && isEntrance;
    }

    private Vector2f getPlayerPosition(GameObject pipe) {
        Pipe pipeComponent = pipe.getComponent(Pipe.class);
        return switch (pipeComponent.direction) {
            case Up -> new Vector2f(pipe.transform.position).add(0.0f, 0.5f);
            case Down -> new Vector2f(pipe.transform.position).add(0.0f, -0.5f);
            case Left -> new Vector2f(pipe.transform.position).add(-0.5f, 0.0f);
            case Right -> new Vector2f(pipe.transform.position).add(0.5f, 0.0f);
        };
    }
}
