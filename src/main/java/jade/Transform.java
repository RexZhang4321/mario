package jade;

import components.Component;
import editor.JImGui;
import org.joml.Vector2f;

import java.util.Objects;

public class Transform extends Component {

    public Vector2f position;
    public Vector2f scale;
    public float rotation;
    public int zIndex;

    public Transform() {
        init(new Vector2f(), new Vector2f());
    }

    public Transform(Vector2f position) {
        init(position, new Vector2f());
    }

    public Transform(Vector2f position, Vector2f scale) {
        init(position, scale);
    }

    public void init(Vector2f position, Vector2f scale) {
        this.position = position;
        this.scale = scale;
        this.zIndex = 0;
        this.rotation = 0.0f;
    }

    public Transform copy() {
        return new Transform(new Vector2f(this.position), new Vector2f(this.scale));
    }

    public void copyTo(Transform to) {
        to.position.set(this.position);
        to.scale.set(this.scale);
        to.rotation = this.rotation;
        to.zIndex = this.zIndex;
    }

    @Override
    public void imGui() {
        JImGui.drawVec2Control("Position", this.position);
        JImGui.drawVec2Control("Scale", this.scale, 32.0f);
        this.zIndex = JImGui.dragInt("Z-Index", this.zIndex);
        this.rotation = JImGui.dragFloat("Rotation", this.rotation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transform transform = (Transform) o;
        return Float.compare(transform.rotation, rotation) == 0 && zIndex == transform.zIndex && Objects.equals(position, transform.position) && Objects.equals(scale, transform.scale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, scale, rotation, zIndex);
    }
}
