package jade;


import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;
import renderer.Shader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class LevelEditorScene extends Scene {

    private float[] vertexArray = {
            // position (x-axle, y-axle, ?) | color (r, g, b, a)
            100.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, // 0. bottom right
            -0.5f, 100.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, // 1. top left
            100.5f, 100.5f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,  // 2. top right
            -0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f // 3. bottom left
    };
    // IMPORTANT: MUST be in counter-clockwise order
    private int[] elementArray = {
            2, 1, 0, // top right triangle
            0, 1, 3  // bottom left triangle
    };

    private int vaoId, vboId, eboId;

    private Shader defaultShader;

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        System.out.println(GL41.glGetString(GL41.GL_VERSION));
        camera = new Camera(new Vector2f());
        defaultShader = new Shader("assets/shaders/default.glsl");
        defaultShader.compile();

        // generate VAO, VBO and EBO buffer objects and send to GPU
        vaoId = GL41.glGenVertexArrays();
        GL41.glBindVertexArray(vaoId);

        // create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        // create VBO upload the vertex buffer
        vboId = GL41.glGenBuffers();
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vboId);
        GL41.glBufferData(GL41.GL_ARRAY_BUFFER, vertexBuffer, GL41.GL_STATIC_DRAW);

        // create the indices and upload
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboId = GL41.glGenBuffers();
        GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, eboId);
        GL41.glBufferData(GL41.GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL41.GL_STATIC_DRAW);

        // add the vertex attribute buffers
        int positionsSize = 3;
        int colorSize = 4;
        int floatSizeBytes = 4;
        int positionSizeBytes = positionsSize * floatSizeBytes;
        int vertexSizeBytes = (positionsSize + colorSize) * floatSizeBytes;
        GL41.glVertexAttribPointer(0, positionsSize, GL41.GL_FLOAT, false, vertexSizeBytes, 0);
        GL41.glEnableVertexAttribArray(0);

        GL41.glVertexAttribPointer(1, colorSize, GL41.GL_FLOAT, false, vertexSizeBytes, positionSizeBytes);
        GL41.glEnableVertexAttribArray(1);
    }

    @Override
    public void update(float dt) {
        camera.position.x -= dt * 50.0f;
        defaultShader.use();
        defaultShader.uploadMat4f("uProjection", camera.getProjectionMatrix());
        defaultShader.uploadMat4f("uView", camera.getViewMatrix());

        // bind the VAO we are using
        GL41.glBindVertexArray(vaoId);

        // enable the vertex attribute pointers
        GL41.glEnableVertexAttribArray(0);
        GL41.glEnableVertexAttribArray(1);

        GL41.glDrawElements(GL41.GL_TRIANGLES, elementArray.length, GL41.GL_UNSIGNED_INT, 0);

        // unbind everything
        GL41.glDisableVertexAttribArray(0);
        GL41.glDisableVertexAttribArray(1);

        GL41.glBindVertexArray(0);

        defaultShader.detach();
    }
}
