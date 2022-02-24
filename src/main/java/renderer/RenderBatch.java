package renderer;

import components.SpriteRenderer;
import jade.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL41;
import util.AssetPool;

import java.util.ArrayList;
import java.util.List;

public class RenderBatch implements Comparable<RenderBatch> {

    /*
     * Vertex
     * ======
     * Pos              Color                          tex coords       tex id
     * float, float,    float, float, float, float,    float, float,    float
     */
    private final int POS_SIZE = 2;
    private final int COLOR_SIZE = 4;
    private final int TEX_COORDS_SIZE = 2;
    private final int TEX_ID_SIZE = 1;
    private final int ENTITY_ID_SIZE = 1;

    private final int POS_OFFSET = 0;
    private final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
    private final int TEX_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
    private final int TEX_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;
    private final int ENTITY_ID_OFFSET = TEX_ID_OFFSET + TEX_ID_SIZE * Float.BYTES;
    private final int VERTEX_SIZE = 10;
    private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private final int MAX_N_TEXTURE_SLOT = 8;

    private SpriteRenderer[] sprites;
    private int numSprites;
    private boolean hasRoom;
    private float[] vertices;
    private int[] texSlots = {0, 1, 2, 3, 4, 5, 6, 7};

    private List<Texture> textures;
    private int vaoId, vboId;
    private int maxBatchSize;

    private int zIndex;

    public RenderBatch(int maxBatchSize, int zIndex) {
        this.zIndex = zIndex;
        this.maxBatchSize = maxBatchSize;
        sprites = new SpriteRenderer[maxBatchSize];

        // 4 vertices quads
        // 4 vertices per sprite, each vertex needs VERTEX_SIZE, and we can store at most maxBatchSize sprites
        vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];

        textures = new ArrayList<>();

        numSprites = 0;
        hasRoom = true;
    }

    @Override
    public int compareTo(RenderBatch o) {
        return Integer.compare(this.zIndex, o.zIndex());
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

        GL41.glVertexAttribPointer(2, TEX_COORDS_SIZE, GL41.GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
        GL41.glEnableVertexAttribArray(2);

        GL41.glVertexAttribPointer(3, TEX_ID_SIZE, GL41.GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ID_OFFSET);
        GL41.glEnableVertexAttribArray(3);

        GL41.glVertexAttribPointer(4, ENTITY_ID_SIZE, GL41.GL_FLOAT, false, VERTEX_SIZE_BYTES, ENTITY_ID_OFFSET);
        GL41.glEnableVertexAttribArray(4);
    }

    public void addSprite(SpriteRenderer spriteRenderer) {
        // get index and add renderObject
        int index = numSprites;
        sprites[index] = spriteRenderer;
        numSprites++;

        if (spriteRenderer.getTexture() != null) {
            if (!textures.contains(spriteRenderer.getTexture())) {
                textures.add(spriteRenderer.getTexture());
            }
        }

        // add properties to local vertices array
        loadVertexProperties(index);

        if (numSprites >= this.maxBatchSize) {
            hasRoom = false;
        }
    }

    public void render() {
        boolean reBufferData = false;
        for (int i = 0; i < numSprites; i++) {
            SpriteRenderer spriteRenderer = sprites[i];
            if (spriteRenderer.isDirty()) {
                loadVertexProperties(i);
                spriteRenderer.setClean();
                reBufferData = true;
            }
        }

        if (reBufferData) {
            GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vboId);
            GL41.glBufferSubData(GL41.GL_ARRAY_BUFFER, 0, vertices);
        }

        // use shader
        Shader shader = Renderer.getBoundShader();
        shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());

        // bind texture
        for (int i = 0; i < textures.size(); i++) {
            GL41.glActiveTexture(GL41.GL_TEXTURE0 + i + 1);
            textures.get(i).bind();
        }
        shader.uploadIntArray("uTextures", texSlots);

        GL41.glBindVertexArray(vaoId);
        GL41.glEnableVertexAttribArray(0);
        GL41.glEnableVertexAttribArray(1);
        GL41.glEnableVertexAttribArray(2);
        GL41.glEnableVertexAttribArray(3);

        GL41.glDrawElements(GL41.GL_TRIANGLES, numSprites * 6, GL41.GL_UNSIGNED_INT, 0);

        GL41.glDisableVertexAttribArray(0);
        GL41.glDisableVertexAttribArray(1);
        GL41.glDisableVertexAttribArray(2);
        GL41.glDisableVertexAttribArray(3);
        GL41.glBindVertexArray(0);

        for (Texture texture : textures) {
            texture.unbind();
        }

        shader.detach();
    }

    private void loadVertexProperties(int index) {
        SpriteRenderer spriteRenderer = sprites[index];

        // find offset within array (4 vertices per sprite)
        int offset = index * 4 * VERTEX_SIZE;

        Vector4f color = spriteRenderer.getColor();
        Vector2f[] texCoords = spriteRenderer.getTextureCoordinates();

        // texture id 0 is reserved for pure color
        int texId = 0;
        if (spriteRenderer.getTexture() != null) {
            for (int i = 0; i < textures.size(); i++) {
                if (textures.get(i).equals(spriteRenderer.getTexture())) {
                    texId = i + 1;
                    break;
                }
            }
        }

        // add vertices with the appropriate properties
        // 3 0
        // 2 1
        float[][] vertexOffset = new float[][] {
                {1.0f, 1.0f},
                {1.0f, 0.0f},
                {0.0f, 0.0f},
                {0.0f, 1.0f}
        };
        for (int i = 0; i < vertexOffset.length; i++) {
            float[] vOffset = vertexOffset[i];
            // position
            vertices[offset] = spriteRenderer.gameObject.transform.position.x + vOffset[0] * spriteRenderer.gameObject.transform.scale.x;
            vertices[offset + 1] = spriteRenderer.gameObject.transform.position.y + vOffset[1] * spriteRenderer.gameObject.transform.scale.y;

            // color
            vertices[offset + 2] = color.x;
            vertices[offset + 3] = color.y;
            vertices[offset + 4] = color.z;
            vertices[offset + 5] = color.w;

            // texture coordinates
            vertices[offset + 6] = texCoords[i].x;
            vertices[offset + 7] = texCoords[i].y;

            // texture id
            vertices[offset + 8] = texId;

            // load entity id
            //
            vertices[offset + 9] = spriteRenderer.gameObject.getUid() + 1;

            offset += VERTEX_SIZE;
        }
    }

    public boolean hasRoom() {
        return hasRoom;
    }

    public boolean hasTextureRoom() {
        return this.textures.size() < MAX_N_TEXTURE_SLOT;
    }

    public boolean hasTexture(Texture texture) {
        return this.textures.contains(texture);
    }

    public int zIndex() {
        return zIndex;
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
