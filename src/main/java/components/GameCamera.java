package components;

import jade.Camera;
import jade.GameObject;
import jade.Window;
import org.joml.Vector4f;

public class GameCamera extends Component {
    private transient GameObject player;
    private transient Camera gameCamera;
    private transient float highestX = Float.NEGATIVE_INFINITY;
    private transient float undergroundYLevel = 0.0f;
    private transient float cameraBuffer = 1.5f;
    private transient float playerBuffer = 0.25f;

    private Vector4f skyColor = new Vector4f(92.0f / 255.0f, 148.0f / 255.0f, 252.0f / 255.0f, 1.0f);
    private Vector4f undergroundColor = new Vector4f(0, 0, 0, 1);

    public GameCamera(Camera camera) {
        this.gameCamera = camera;
    }

    @Override
    public void start() {
        this.player = Window.getScene().getGameObjectWithClass(PlayerController.class);
        this.gameCamera.clearColor.set(skyColor);
        this.undergroundYLevel = gameCamera.position.y - gameCamera.getProjectionSize().y - cameraBuffer;
    }

    @Override
    public void update(float dt) {
//        System.out.println(String.format("GameCamera position x: %f, player transform position x: %f", gameCamera.position.x, player.transform.position.x));
        System.out.println(String.format("GameCamera position y: %f, player transform position x: %f", gameCamera.position.y, player.transform.position.y));
        if (player != null && !player.getComponent(PlayerController.class).hasWon()) {
//            gameCamera.position.x = Math.max(player.transform.position.x - 2.5f, highestX);
//            System.out.println(Math.max(player.transform.position.x - 2.5f, highestX));
//            highestX = Math.max(highestX, gameCamera.position.x);
            gameCamera.position.x = player.transform.position.x - 1.5f;

            if (player.transform.position.y < -playerBuffer) {
                gameCamera.position.y = undergroundYLevel;
                gameCamera.clearColor.set(undergroundColor);
            } else if (player.transform.position.y >= 0f) {
                gameCamera.position.y = 0f;
                gameCamera.clearColor.set(skyColor);
            }
        }
    }
}
