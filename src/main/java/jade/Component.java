package jade;

public abstract class Component {

    // pointer to its parent
    public GameObject gameObject;

    public void start() {

    }

    public abstract void update(float dt);

}
