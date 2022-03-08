package jade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import components.Component;
import components.ComponentSerDeser;
import components.SpriteRenderer;
import imgui.ImGui;
import util.AssetPool;

import java.util.ArrayList;
import java.util.List;

public class GameObject {
    private static int ID_COUNTER = 0;
    private int uid = -1;

    private String name;
    private List<Component> components;
    public transient Transform transform;

    private boolean shouldSerialize = true;
    private boolean isDead = false;

    public GameObject(String name) {
        this.name = name;
        components = new ArrayList<>();
        this.uid = ID_COUNTER;
        ID_COUNTER++;
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (Component component : components) {
            if (componentClass.isAssignableFrom(component.getClass())) {
                return componentClass.cast(component);
            }
        }

        return null;
    }

    public <T extends Component> void removeComponent(Class<T> componentClass) {
        for (int i = 0; i < components.size(); i++) {
            Component component = components.get(i);
            if (componentClass.isAssignableFrom(component.getClass())) {
                components.remove(i);
                return;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void addComponent(Component component) {
        component.generateId();
        components.add(component);
        component.gameObject = this;
    }

    public void update(float dt) {
        for (Component component : components) {
            component.update(dt);
        }
    }

    public void editorUpdate(float dt) {
        for (Component component : components) {
            component.editorUpdate(dt);
        }
    }

    public void start() {
        for (int i = 0; i < components.size(); i++) {
            components.get(i).start();
        }
    }

    public void destroy() {
        this.isDead = true;
        for (int i = 0; i < components.size(); i++) {
            components.get(i).destroy();
        }
    }

    public void imGui() {
        for (Component component : components) {
            if (ImGui.collapsingHeader(component.getClass().getSimpleName())) {
                component.imGui();
            }
        }
    }

    public int getUid() {
        return this.uid;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setNoSerialize() {
        shouldSerialize = false;
    }

    public boolean shouldSerialize() {
        return shouldSerialize;
    }

    public static void init(int maxId) {
        ID_COUNTER = maxId;
    }

    public boolean isDead() {
        return isDead;
    }

    public void generateUid() {
        this.uid = ID_COUNTER;
        ID_COUNTER++;
    }

    public GameObject copy() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Component.class, new ComponentSerDeser())
                .registerTypeAdapter(GameObject.class, new GameObjectDeserializer())
                .create();
        String objAsJson = gson.toJson(this);
        GameObject obj = gson.fromJson(objAsJson, GameObject.class);
        obj.generateUid();
        for (Component component : obj.getComponents()) {
            component.generateId();
        }

        SpriteRenderer spriteRenderer = obj.getComponent(SpriteRenderer.class);
        if (spriteRenderer != null && spriteRenderer.getTexture() != null) {
            spriteRenderer.setTexture(AssetPool.getTexture(spriteRenderer.getTexture().getFilePath()));
        }
        return obj;
    }
}
