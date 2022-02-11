package renderer;

import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Shader {

    private int shaderProgramId;
    private boolean beingUsed;

    private String vertexSource;
    private String fragmentSource;
    private String filePath;

    public Shader(String filePath) {
        this.filePath = filePath;
        try {
            String source = new String(Files.readAllBytes(Paths.get(filePath)));
            String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");

            // find the first pattern after `#type 'pattern'`
            int index = source.indexOf("#type") + 6;
            int eol = source.indexOf("\r\n", index);
            String firstPattern = source.substring(index, eol).trim();

            // find the second pattern after `#type 'pattern'`
            index = source.indexOf("#type", eol) + 6;
            eol = source.indexOf("\r\n", index);
            String secondPattern = source.substring(index, eol).trim();

            if (firstPattern.equals("vertex")) {
                vertexSource = splitString[1];
            } else if (firstPattern.equals("fragment")) {
                fragmentSource = splitString[1];
            } else {
                throw new IOException("Unexpected token: '" + firstPattern + "'");
            }

            if (secondPattern.equals("vertex")) {
                vertexSource = splitString[2];
            } else if (secondPattern.equals("fragment")) {
                fragmentSource = splitString[2];
            } else {
                throw new IOException("Unexpected token: '" + secondPattern + "'");
            }
        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error: Could not open file for shader: " + filePath;
        }
    }

    public void compile() {
        // compile and link shaders

        // 1. load and compile the vertex shader
        int vertexId = GL41.glCreateShader(GL41.GL_VERTEX_SHADER);
        // 2. pass the shader source to the GPU
        GL41.glShaderSource(vertexId, vertexSource);
        GL41.glCompileShader(vertexId);
        // 3. check for errors in compilation
        int success = GL41.glGetShaderi(vertexId, GL41.GL_COMPILE_STATUS);
        if (success == GL41.GL_FALSE) {
            int len = GL41.glGetShaderi(vertexId, GL41.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + filePath + "'\n\t vertex shader compilation failed.");
            System.out.println(GL41.glGetShaderInfoLog(vertexId, len));
            assert false : "";
        }

        // 1. load and compile the fragment shader
        int fragmentId = GL41.glCreateShader(GL41.GL_FRAGMENT_SHADER);
        // 2. pass the shader source to the GPU
        GL41.glShaderSource(fragmentId, fragmentSource);
        GL41.glCompileShader(fragmentId);
        // 3. check for errors in compilation
        success = GL41.glGetShaderi(fragmentId, GL41.GL_COMPILE_STATUS);
        if (success == GL41.GL_FALSE) {
            int len = GL41.glGetShaderi(fragmentId, GL41.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + filePath + "'\n\t vertex shader compilation failed.");
            System.out.println(GL41.glGetShaderInfoLog(fragmentId, len));
            assert false : "";
        }

        // link shaders and check for errors
        shaderProgramId = GL41.glCreateProgram();
        GL41.glAttachShader(shaderProgramId, vertexId);
        GL41.glAttachShader(shaderProgramId, fragmentId);
        GL41.glLinkProgram(shaderProgramId);

        // check for linking errors
        success = GL41.glGetProgrami(shaderProgramId, GL41.GL_LINK_STATUS);
        if (success == GL41.GL_FALSE) {
            int len = GL41.glGetProgrami(shaderProgramId, GL41.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\t linking of shaders failed.");
            System.out.println(GL41.glGetProgramInfoLog(fragmentId, len));
            assert false : "";
        }
    }

    public void use() {
        if (beingUsed) {
            return;
        }
        // bind shader program
        GL41.glUseProgram(shaderProgramId);
        beingUsed = true;
    }

    public void detach() {
        GL41.glUseProgram(0);
        beingUsed = false;
    }

    public void uploadMat4f(String varName, Matrix4f mat4) {
        int varLocation = GL41.glGetUniformLocation(shaderProgramId, varName);
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16); // 4 * 4 matrix
        mat4.get(matBuffer);
        GL41.glUniformMatrix4fv(varLocation, false, matBuffer);
    }

    public void uploadMat3f(String varName, Matrix3f mat3) {
        int varLocation = GL41.glGetUniformLocation(shaderProgramId, varName);
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9); // 4 * 4 matrix
        mat3.get(matBuffer);
        GL41.glUniformMatrix3fv(varLocation, false, matBuffer);
    }

    public void uploadVec4f(String varName, Vector4f vec4f) {
        int varLocation = GL41.glGetUniformLocation(shaderProgramId, varName);
        use();
        GL41.glUniform4f(varLocation, vec4f.x, vec4f.y, vec4f.z, vec4f.w);
    }

    public void uploadVec3f(String varName, Vector3f vec3f) {
        int varLocation = GL41.glGetUniformLocation(shaderProgramId, varName);
        use();
        GL41.glUniform3f(varLocation, vec3f.x, vec3f.y, vec3f.z);
    }

    public void uploadVec2f(String varName, Vector2f vec2f) {
        int varLocation = GL41.glGetUniformLocation(shaderProgramId, varName);
        use();
        GL41.glUniform2f(varLocation, vec2f.x, vec2f.y);
    }

    public void uploadFloat(String varName, float val) {
        int varLocation = GL41.glGetUniformLocation(shaderProgramId, varName);
        use();
        GL41.glUniform1f(varLocation, val);
    }

    public void uploadInt(String varName, int val) {
        int varLocation = GL41.glGetUniformLocation(shaderProgramId, varName);
        use();
        GL41.glUniform1i(varLocation, val);
    }

    public void uploadTexture(String varName, int slot) {
        int varLocation = GL41.glGetUniformLocation(shaderProgramId, varName);
        use();
        GL41.glUniform1i(varLocation, slot);
    }
}
