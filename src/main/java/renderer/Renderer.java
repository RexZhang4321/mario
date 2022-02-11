package renderer;

import components.SpriteRenderer;
import jade.GameObject;

import java.util.ArrayList;
import java.util.List;

public class Renderer {

    private final int MAX_BATCH_SIZE = 1000;
    private List<RenderBatch> batches;

    public Renderer() {
        batches = new ArrayList<>();
    }

    public void add(GameObject gameObject) {
        SpriteRenderer spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
        if (spriteRenderer != null) {
            add(spriteRenderer);
        }
    }

    public void render() {
        for (RenderBatch batch : batches) {
            batch.render();
        }
    }

    private void add(SpriteRenderer spriteRenderer) {
        boolean added = false;
        for (RenderBatch batch : batches) {
            if (batch.hasRoom()) {
                batch.addSprite(spriteRenderer);
                added = true;
                break;
            }
        }

        if (!added) {
            RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE);
            newBatch.start();
            batches.add(newBatch);
            newBatch.addSprite(spriteRenderer);
        }
    }

}
