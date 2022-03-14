package components;

import editor.JImGui;
import jade.Transform;
import org.joml.Vector2f;
import org.joml.Vector4f;
import renderer.Texture;

public class SpriteRenderer extends Component {

    private Vector4f color = new Vector4f(1, 1, 1, 1);
    private Sprite sprite = new Sprite();

    private transient Transform lastTransform;

    private transient boolean isDirty = true;

    @Override
    public void start() {
        super.start();
        this.lastTransform = gameObject.transform.copy();
    }

    @Override
    public void update(float dt) {
        if (!this.lastTransform.equals(this.gameObject.transform)) {
            this.gameObject.transform.copyTo(lastTransform);
            this.isDirty = true;
        }
    }

    @Override
    public void editorUpdate(float dt) {
        if (!this.lastTransform.equals(this.gameObject.transform)) {
            this.gameObject.transform.copyTo(lastTransform);
            this.isDirty = true;
        }
    }

    @Override
    public void imGui() {
        if (JImGui.colorPicker4("Color Picker: ", this.color)) {
            this.isDirty = true;
        }
    }

    public void setColor(Vector4f color) {
        if (!color.equals(this.color)) {
            this.color.set(color);
            this.isDirty = true;
        }
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
        this.isDirty = true;
    }

    public Vector4f getColor() {
        return color;
    }

    public Texture getTexture() {
        return sprite.getTexture();
    }

    public Vector2f[] getTextureCoordinates() {
        return sprite.getTexCoords();
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setClean() {
        this.isDirty = false;
    }

    public void setTexture(Texture texture) {
        this.sprite.setTexture(texture);
    }

    public void setDirty() {
        this.isDirty = true;
    }
}
