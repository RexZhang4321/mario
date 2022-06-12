package scenes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import components.Component;
import components.ComponentSerDeser;
import jade.Camera;
import jade.GameObject;
import jade.GameObjectDeserializer;
import jade.Transform;
import org.joml.Vector2f;
import physics2d.Physics2D;
import renderer.Renderer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Scene {

    private Renderer renderer;
    private Camera camera;
    private List<GameObject> gameObjects;

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Component.class, new ComponentSerDeser())
            .registerTypeAdapter(GameObject.class, new GameObjectDeserializer())
            .create();

    private boolean isRunning;

    private SceneInitializer sceneInitializer;

    private Physics2D physics2D;

    public Scene(SceneInitializer sceneInitializer) {
        this.sceneInitializer = sceneInitializer;
        this.physics2D = new Physics2D();
        this.renderer = new Renderer();
        this.gameObjects = new ArrayList<>();
        this.isRunning = false;
    }

    public void init() {
        camera = new Camera(new Vector2f(-250, 0));
        this.sceneInitializer.loadResource(this);
        this.sceneInitializer.init(this);
    }

    public void start() {
        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject gameObject = this.gameObjects.get(i);
            gameObject.start();
            renderer.add(gameObject);
            physics2D.add(gameObject);
        }
        isRunning = true;
    }

    public void destroy() {
        for (GameObject gameObject : gameObjects) {
            gameObject.destroy();
        }
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public void addGameObjectToScene(GameObject gameObject) {
        if (!isRunning) {
            gameObjects.add(gameObject);
        } else {
            gameObjects.add(gameObject);
            gameObject.start();
            renderer.add(gameObject);
            physics2D.add(gameObject);
        }
    }

    public void editorUpdate(float dt) {
        camera.adjustProjection();
        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject gameObject = gameObjects.get(i);
            gameObject.editorUpdate(dt);

            if (gameObject.isDead()) {
                gameObjects.remove(i);
                this.renderer.destroyGameObject(gameObject);
                this.physics2D.destroyGameObject(gameObject);
                i--;
            }
        }
    }

    public void update(float dt) {
        camera.adjustProjection();
        physics2D.update(dt);
        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject gameObject = gameObjects.get(i);
            gameObject.update(dt);

            if (gameObject.isDead()) {
                gameObjects.remove(i);
                this.renderer.destroyGameObject(gameObject);
                this.physics2D.destroyGameObject(gameObject);
                i--;
            }
        }
    }

    public void render() {
        this.renderer.render();
    }

    public Camera camera() {
        return camera;
    }

    public void imGui() {
        this.sceneInitializer.imGui();
    }

    public GameObject createGameObject(String name) {
        GameObject gameObject = new GameObject(name);
        gameObject.addComponent(new Transform());
        gameObject.transform = gameObject.getComponent(Transform.class);
        return gameObject;
    }

    public void save() {
        try {
            FileWriter writer = new FileWriter("level.txt", false);
            writer.write(gson.toJson(this.gameObjects.stream().filter(GameObject::shouldSerialize).collect(Collectors.toList())));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (Files.notExists(Paths.get("level.txt"))) {
            return;
        }
        String inFile = "";
        try {
            inFile = new String(Files.readAllBytes(Paths.get("level.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!"".equals(inFile)) {
            int maxGameObjectId = -1;
            int maxComponentId = -1;
            GameObject[] gameObjects = gson.fromJson(inFile, GameObject[].class);
            for (GameObject gameObject : gameObjects) {
                addGameObjectToScene(gameObject);

                for (Component component : gameObject.getComponents()) {
                    maxComponentId = Math.max(maxComponentId, component.getUid());
                }
                maxGameObjectId = Math.max(maxGameObjectId, gameObject.getUid());
            }

            maxGameObjectId++;
            maxComponentId++;
            GameObject.init(maxGameObjectId);
            Component.init(maxComponentId);
        }
    }

    public GameObject getGameObject(int gameObjectId) {
        return gameObjects.stream().filter(it -> it.getUid() == gameObjectId).findFirst().orElse(null);
    }

    public Physics2D getPhysics2D() {
        return physics2D;
    }
}
