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
            collidingPlayer = playerController;
        }
    }

    @Override
    public void endCollision(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
        PlayerController playerController = collidingObject.getComponent(PlayerController.class);
        if (playerController != null) {
            collidingPlayer = null;
        }
    }

    private boolean shouldTeleport(Direction direction) {
        if (direction == Direction.Up && (KeyListener.isKeyPressed(GLFW.GLFW_KEY_DOWN) || KeyListener.isKeyPressed(GLFW.GLFW_KEY_S)) && isEntrance && playerAtEntrance()) {
            return true;
        }
        if (direction == Direction.Down && (KeyListener.isKeyPressed(GLFW.GLFW_KEY_UP) || KeyListener.isKeyPressed(GLFW.GLFW_KEY_W)) && isEntrance && playerAtEntrance()) {
            return true;
        }
        if (direction == Direction.Left && (KeyListener.isKeyPressed(GLFW.GLFW_KEY_RIGHT) || KeyListener.isKeyPressed(GLFW.GLFW_KEY_D)) && isEntrance && playerAtEntrance()) {
            return true;
        }
        return direction == Direction.Right && (KeyListener.isKeyPressed(GLFW.GLFW_KEY_LEFT) || KeyListener.isKeyPressed(GLFW.GLFW_KEY_A)) && isEntrance && playerAtEntrance();
    }

    public boolean playerAtEntrance() {
        if (collidingPlayer == null) {
            return false;
        }

        Vector2f min = new Vector2f(gameObject.transform.position)
                .sub(new Vector2f(gameObject.transform.scale).mul(0.5f));
        Vector2f max = new Vector2f(gameObject.transform.position)
                .add(new Vector2f(gameObject.transform.scale).mul(0.5f));

        Vector2f playerMin = new Vector2f(collidingPlayer.gameObject.transform.position)
                .sub(new Vector2f(collidingPlayer.gameObject.transform.scale).mul(0.5f));
        Vector2f playerMax = new Vector2f(collidingPlayer.gameObject.transform.position)
                .add(new Vector2f(collidingPlayer.gameObject.transform.scale).mul(0.5f));

        System.out.println(String.format("min: %f, max: %f, player min: %f, player max: %f", min.x, max.x, playerMin.x, playerMax.x));
        System.out.println(playerMax.x <= min.x);
        System.out.println(playerMax.y > min.y);
        System.out.println(playerMin.y < max.y);

        return switch (direction) {
            case Up -> playerMin.y >= max.y && playerMax.x > min.x && playerMin.x < max.x;
            case Down -> playerMax.y <= min.y && playerMax.x > min.x && playerMin.x < max.x;
            case Left -> playerMin.x <= min.x && playerMax.y > min.y && playerMin.y < max.y;
            case Right -> playerMin.x >= max.x && playerMax.y > min.y && playerMin.y < max.y;
        };
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
