package jade;

public class LevelScene extends Scene {

    public LevelScene() {
        System.out.println("Inside Level Scene");
        Window.getInstance().r = 1.0f;
        Window.getInstance().g = 1.0f;
        Window.getInstance().b = 1.0f;
    }

    @Override
    public void update(float dt) {

    }
}
