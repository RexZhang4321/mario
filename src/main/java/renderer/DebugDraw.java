package renderer;

import jade.Camera;
import jade.Window;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL41;
import util.AssetPool;
import util.JMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DebugDraw {

    private static int MAX_LINES = 3000;

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
        GL41.glLineWidth(1.0f);
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
        Camera camera = Window.getScene().camera();
        Vector2f cameraLeft = new Vector2f(camera.position).add(new Vector2f(-2.0f, -2.0f));
        Vector2f cameraRight = new Vector2f(camera.position)
                .add(new Vector2f(camera.getProjectionSize()).mul(camera.getZoom()))
                .add(new Vector2f(4.0f, 4.0f));
        boolean lineInView = from.x >= cameraLeft.x && from.x <= cameraRight.x
                && from.y >= cameraLeft.y && from.y <= cameraRight.y
                || to.x >= cameraLeft.x && to.x <= cameraRight.x
                && to.y >= cameraLeft.y && to.y <= cameraRight.y;

        if (lines.size() >= MAX_LINES || !lineInView) {
            return;
        }
        lines.add(new Line2D(from, to, color, lifeTime));
    }

    // add box2D methods
    public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation) {
        addBox2D(center, dimensions, rotation, new Vector3f(0, 1, 0), 1);
    }

    public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation, Vector3f color) {
        addBox2D(center, dimensions, rotation, color, 1);
    }

    public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation, Vector3f color, int lifeTime) {
        Vector2f min = new Vector2f(center).sub(new Vector2f(dimensions).div(2));
        Vector2f max = new Vector2f(center).add(new Vector2f(dimensions).div(2));

        Vector2f[] vertices = {
                new Vector2f(min.x, min.y),
                new Vector2f(min.x, max.y),
                new Vector2f(max.x, max.y),
                new Vector2f(max.x, min.y)
        };

        if (rotation != 0.0f) {
            for (Vector2f vec : vertices) {
                JMath.rotate(vec, rotation, center);
            }
        }

        for (int i = 0; i < 4; i++) {
            addLine2D(vertices[i], vertices[(i + 1) % 4], color, lifeTime);
        }
    }

    // add circle methods
    public static void addCircle(Vector2f center, float radius) {
        addCircle(center, radius, new Vector3f(0, 1, 0), 1);
    }

    public static void addCircle(Vector2f center, float radius, Vector3f color) {
        addCircle(center, radius, color, 1);
    }

    public static void addCircle(Vector2f center, float radius, Vector3f color, int lifeTime) {
        Vector2f[] points = new Vector2f[20];
        float increment = 360.0f / points.length;
        float currentAngle = 0;

        for (int i = 0; i < points.length; i++) {
            Vector2f tmp = new Vector2f(radius, 0);
            JMath.rotate(tmp, currentAngle, new Vector2f());
            points[i] = new Vector2f(tmp).add(center);

            if (i > 0) {
                addLine2D(points[i - 1], points[i], color, lifeTime);
            }

            currentAngle += increment;
        }

        addLine2D(points[points.length - 1], points[0], color, lifeTime);
    }
}
