package renderer;

import components.SpriteRenderer;
import jade.Window;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL41;
import util.AssetPool;

public class RenderBatch {

    /*
     * Vertex
     * ======
     * Pos                      Color
     * float, float,            float, float, float, float
     */
    private final int POS_SIZE = 2;
    private final int COLOR_SIZE = 4;

    private final int POS_OFFSET = 0;
    private final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
    private final int VERTEX_SIZE = 6;
    private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private SpriteRenderer[] sprites;
    private int numSprites;
    private boolean hasRoom;
    private float[] vertices;

    private int vaoId, vboId;
    private int maxBatchSize;
    private Shader shader;

    public RenderBatch(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
        shader = AssetPool.getShader("assets/shaders/default.glsl");
        sprites = new SpriteRenderer[maxBatchSize];

        // 4 vertices quads
        // 4 vertices per sprite, each vertex needs VERTEX_SIZE, and we can store at most maxBatchSize sprites
        vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];

        numSprites = 0;
        hasRoom = true;
    }

    public void start() {
        // generate and bind a vertex array object
        vaoId = GL41.glGenVertexArrays();
        GL41.glBindVertexArray(vaoId);

        // allocate spaces for vertices
        vboId = GL41.glGenBuffers();
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vboId);
        GL41.glBufferData(GL41.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL41.GL_DYNAMIC_DRAW);

        // create and upload indices buffer
        int eboId = GL41.glGenBuffers();
        int[] indices = generateIndices();
        GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, eboId);
        GL41.glBufferData(GL41.GL_ELEMENT_ARRAY_BUFFER, indices, GL41.GL_STATIC_DRAW);

        // enable the buffer attribute pointers
        GL41.glVertexAttribPointer(0, POS_SIZE, GL41.GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_OFFSET);
        GL41.glEnableVertexAttribArray(0);

        GL41.glVertexAttribPointer(1, COLOR_SIZE, GL41.GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
        GL41.glEnableVertexAttribArray(1);

    }

    public void addSprite(SpriteRenderer spriteRenderer) {
        // get index and add renderObject
        int index = numSprites;
        sprites[index] = spriteRenderer;
        numSprites++;

        // add properties to local vertices array
        loadVertexProperties(index);

        if (numSprites >= this.maxBatchSize) {
            hasRoom = false;
        }
    }

    public void render() {
        // for now, we will re-buffer all data every frame
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vboId);
        GL41.glBufferSubData(GL41.GL_ARRAY_BUFFER, 0, vertices);

        // use shader
        shader.use();
        shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());

        GL41.glBindVertexArray(vaoId);
        GL41.glEnableVertexAttribArray(0);
        GL41.glEnableVertexAttribArray(1);

        GL41.glDrawElements(GL41.GL_TRIANGLES, numSprites * 6, GL41.GL_UNSIGNED_INT, 0);

        GL41.glDisableVertexAttribArray(0);
        GL41.glDisableVertexAttribArray(1);
        GL41.glBindVertexArray(0);

        shader.detach();
    }

    private void loadVertexProperties(int index) {
        SpriteRenderer spriteRenderer = sprites[index];

        // find offset within array (4 vertices per sprite)
        int offset = index * 4 * VERTEX_SIZE;

        Vector4f color = spriteRenderer.getColor();

        // add vertices with the appropriate properties
        // 3 0
        // 2 1
        float[][] vertexOffset = new float[][] {
                {1.0f, 1.0f},
                {1.0f, 0.0f},
                {0.0f, 0.0f},
                {0.0f, 1.0f}
        };
        for (float[] vOffset : vertexOffset) {
            // position
            vertices[offset] = spriteRenderer.gameObject.transform.position.x + vOffset[0] * spriteRenderer.gameObject.transform.scale.x;
            vertices[offset + 1] = spriteRenderer.gameObject.transform.position.y + vOffset[1] * spriteRenderer.gameObject.transform.scale.y;

            // color
            vertices[offset + 2] = color.x;
            vertices[offset + 3] = color.y;
            vertices[offset + 4] = color.z;
            vertices[offset + 5] = color.w;

            offset += VERTEX_SIZE;
        }
    }

    public boolean hasRoom() {
        return hasRoom;
    }

    private int[] generateIndices() {
        // 6 indices per quad (3 per triangle * 2)
        int[] elements = new int[6 * maxBatchSize];
        for (int i = 0; i < maxBatchSize; i++) {
            loadElementIndices(elements, i);
        }
        return elements;
    }

    private void loadElementIndices(int[] elements, int index) {
        int elementOffset = 6 * index;
        int vertexOffset = 4 * index;

        // one quad
        // 3   0
        // 2   1
        int[] verticesOrder = new int[] {3, 2, 0, 0, 2, 1};
        for (int i = 0; i < 6; i++) {
            elements[elementOffset + i] = vertexOffset + verticesOrder[i];
        }
    }

}
