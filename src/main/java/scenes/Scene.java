package scenes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import components.Component;
import components.ComponentSerDeser;
import imgui.ImGui;
import jade.Camera;
import jade.GameObject;
import jade.GameObjectDeserializer;
import renderer.Renderer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Scene {

    protected Renderer renderer = new Renderer();
    protected Camera camera;
    protected List<GameObject> gameObjects = new ArrayList<>();

    protected Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Component.class, new ComponentSerDeser())
            .registerTypeAdapter(GameObject.class, new GameObjectDeserializer())
            .create();
    protected boolean levelLoaded = false;

    private boolean isRunning = false;

    public Scene() {

    }

    public abstract void init();

    public void start() {
        for (GameObject gameObject : gameObjects) {
            gameObject.start();
            renderer.add(gameObject);
        }
        isRunning = true;
    }

    public void addGameObjectToScene(GameObject gameObject) {
        if (!isRunning) {
            gameObjects.add(gameObject);
        } else {
            gameObjects.add(gameObject);
            gameObject.start();
            renderer.add(gameObject);
        }
    }

    public abstract void update(float dt);

    public abstract void render();

    public Camera camera() {
        return camera;
    }

    public void imGui() {

    }

    public void saveExit() {
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
            this.levelLoaded = true;
        }
    }

    public GameObject getGameObject(int gameObjectId) {
        return gameObjects.stream().filter(it -> it.getUid() == gameObjectId).findFirst().orElse(null);
    }
}
