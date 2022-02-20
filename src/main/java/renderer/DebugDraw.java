package renderer;

import jade.Window;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL41;
import util.AssetPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DebugDraw {

    private static int MAX_LINES = 500;

    private static List<Line2D> lines = new ArrayList<>();

    // 6 floats per vertex (x, y, z, r, g, b), 2 vertices per line
    private static float[] vertexArray = new float[MAX_LINES * 6 * 2];
    private static Shader shader = AssetPool.getShader("assets/shaders/debugLine2D.glsl");

    private static int vaoId;
    private static int vboId;

    private static boolean started = false;

    public static void start() {
        // generate vao
        vaoId = GL41.glGenVertexArrays();
        GL41.glBindVertexArray(vaoId);

        // create vbo and buffer some memory
        vboId = GL41.glGenBuffers();
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vboId);
        GL41.glBufferData(GL41.GL_ARRAY_BUFFER, vertexArray.length * Float.BYTES, GL41.GL_DYNAMIC_DRAW);

        // enable vertex array attributes
        GL41.glVertexAttribPointer(0, 3, GL41.GL_FLOAT, false, 6 * Float.BYTES, 0);
        GL41.glEnableVertexAttribArray(0);

        GL41.glVertexAttribPointer(1, 3, GL41.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        GL41.glEnableVertexAttribArray(1);

        // set line width
        GL41.glLineWidth(2.0f);
    }

    public static void beginFrame() {
        if (!started) {
            start();
            started = true;
        }

        // remove the dead lines
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).beginFrame() < 0) {
                lines.remove(i);
                i--;
            }
        }
    }

    public static void draw() {
        if (lines.isEmpty()) {
            return;
        }

        int index = 0;
        for (Line2D line2D : lines) {
            for (int i = 0; i < 2; i++) {
                Vector2f position = i == 0 ? line2D.getFrom() : line2D.getTo();
                Vector3f color = line2D.getColor();

                // load position
                vertexArray[index] = position.x;
                vertexArray[index + 1] = position.y;
                vertexArray[index + 2] = -10.0f; // no use for now as no depth is enabled

                // load color
                vertexArray[index + 3] = color.x;
                vertexArray[index + 4] = color.y;
                vertexArray[index + 5] = color.z;
                index += 6;
            }
        }

        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vboId);
        GL41.glBufferSubData(GL41.GL_ARRAY_BUFFER, 0, Arrays.copyOfRange(vertexArray, 0, lines.size() * 6 * 2));

        // use shader
        shader.use();
        shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());

        // bind vao
        GL41.glBindVertexArray(vaoId);
        GL41.glEnableVertexAttribArray(0);
        GL41.glEnableVertexAttribArray(1);

        // draw the batch
        GL41.glDrawArrays(GL41.GL_LINES, 0, lines.size() * 6 * 2);

        // disable location
        GL41.glDisableVertexAttribArray(0);
        GL41.glDisableVertexAttribArray(1);
        GL41.glBindVertexArray(0);

        // unbind shader
        shader.detach();
    }

    // add line2D methods
    public static void addLine2D(Vector2f from, Vector2f to) {
        addLine2D(from, to, new Vector3f(0, 1, 0), 1);
    }

    public static void addLine2D(Vector2f from, Vector2f to, Vector3f color) {
        addLine2D(from, to, color, 1);
    }

    public static void addLine2D(Vector2f from, Vector2f to, Vector3f color, int lifeTime) {
        if (lines.size() >= MAX_LINES) {
            System.out.println("no more room for lines");
            return;
        }
        lines.add(new Line2D(from, to, color, lifeTime));
    }
}
