package jade;

import components.Component;

import java.util.ArrayList;
import java.util.List;

public class GameObject {
    private static int ID_COUNTER = 0;
    private int uid = -1;

    private String name;
    private List<Component> components;
    public Transform transform;

    private int zIndex;
    private boolean shouldSerialize = true;

    public GameObject(String name, Transform transform, int zIndex) {
        this.name = name;
        components = new ArrayList<>();
        this.transform = transform;
        this.zIndex = zIndex;
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

    public void start() {
        for (int i = 0; i < components.size(); i++) {
            components.get(i).start();
        }
    }

    public int zIndex() {
        return zIndex;
    }

    public void imGui() {
        for (Component component : components) {
            component.imGui();
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
}
