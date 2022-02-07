package jade;


import org.lwjgl.opengl.GL41;

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
    }

    @Override
    public void update(float dt) {

    }
}
