package jade;


import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class LevelEditorScene extends Scene {

    private String vertexShaderSrc = "#version 410 core\n" +
            "layout (location=0) in vec3 aPos;\n" +
            "layout (location=1) in vec4 aColor;\n" +
            "\n" +
            "out vec4 fColor;\n" +
            "\n" +
            "void main() {\n" +
            "    fColor = aColor;\n" +
            "    gl_Position = vec4(aPos, 1.0);\n" +
            "}";
    private String fragmentShaderSrc = "#version 410 core\n" +
            "\n" +
            "in vec4 fColor;\n" +
            "\n" +
            "out vec4 color;\n" +
            "\n" +
            "void main() {\n" +
            "    color = fColor;\n" +
            "}";
    private int vertexId, fragmentId, shaderProgram;

    private float[] vertexArray = {
            // position (normalized, x-axle, y-axle, ?) | color (r, g, b, a)
            0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, // 0. bottom right
            -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, // 1. top left
            0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,  // 2. top right
            -0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f // 3. bottom left
    };
    // IMPORTANT: MUST be in counter-clockwise order
    private int[] elementArray = {
            2, 1, 0, // top right triangle
            0, 1, 3  // bottom left triangle
    };

    private int vaoId, vboId, eboId;

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        System.out.println(GL41.glGetString(GL41.GL_VERSION));
        // compile and link shaders
        // 1. load and compile the vertex shader
        vertexId = GL41.glCreateShader(GL41.GL_VERTEX_SHADER);
        // 2. pass the shader source to the GPU
        GL41.glShaderSource(vertexId, vertexShaderSrc);
        GL41.glCompileShader(vertexId);
        // 3. check for errors in compilation
        int success = GL41.glGetShaderi(vertexId, GL41.GL_COMPILE_STATUS);
        if (success == GL41.GL_FALSE) {
            int len = GL41.glGetShaderi(vertexId, GL41.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\t vertex shader compilation failed.");
            System.out.println(GL41.glGetShaderInfoLog(vertexId, len));
            assert false : "";
        }

        // 1. load and compile the fragment shader
        fragmentId = GL41.glCreateShader(GL41.GL_FRAGMENT_SHADER);
        // 2. pass the shader source to the GPU
        GL41.glShaderSource(fragmentId, fragmentShaderSrc);
        GL41.glCompileShader(fragmentId);
        // 3. check for errors in compilation
        success = GL41.glGetShaderi(fragmentId, GL41.GL_COMPILE_STATUS);
        if (success == GL41.GL_FALSE) {
            int len = GL41.glGetShaderi(fragmentId, GL41.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\t vertex shader compilation failed.");
            System.out.println(GL41.glGetShaderInfoLog(fragmentId, len));
            assert false : "";
        }

        // link shaders and check for errors
        shaderProgram = GL41.glCreateProgram();
        GL41.glAttachShader(shaderProgram, vertexId);
        GL41.glAttachShader(shaderProgram, fragmentId);
        GL41.glLinkProgram(shaderProgram);

        // check for linking errors
        success = GL41.glGetProgrami(shaderProgram, GL41.GL_LINK_STATUS);
        if (success == GL41.GL_FALSE) {
            int len = GL41.glGetProgrami(shaderProgram, GL41.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\t linking of shaders failed.");
            System.out.println(GL41.glGetProgramInfoLog(fragmentId, len));
            assert false : "";
        }

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
        // bind shader program
        GL41.glUseProgram(shaderProgram);

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

        GL41.glUseProgram(0);
    }
}
