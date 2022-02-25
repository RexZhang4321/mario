package jade;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {

    // maps the real world coordinates to normalized device coordinates
    // and what type of the projection it is (perspective projection OR orthographic projection)
    private Matrix4f projectionMatrix, inverseProjection;

    // lookAt matrix -- where the camera is looking from in the world
    // Right_x R_y R_z 0
    // Up_x    U_y U_z 0
    // Down_x  D_y D_z 0
    // 0       0   0   1
    // *
    // 1 0 0 -Pos_x
    // 0 1 1 -P_y
    // 0 0 1 -P_z
    // 0 0 0 1
    private Matrix4f viewMatrix, inverseView;

    public Vector2f position;

    private Vector2f projectionSize = new Vector2f(32.0f * 40.0f, 32.0f * 21.0f);

    private float zoom = 1.0f;

    public Camera(Vector2f position) {
        this.position = position;
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.inverseProjection = new Matrix4f();
        this.inverseView = new Matrix4f();
        adjustProjection();
    }

    public void adjustProjection() {
        projectionMatrix.identity();
        projectionMatrix.ortho(0.0f, projectionSize.x * zoom, 0.0f, projectionSize.y * zoom, 0.0f, 100.0f);
        projectionMatrix.invert(inverseProjection);
    }

    public Vector2f getProjectionSize() {
        return projectionSize;
    }

    public Matrix4f getViewMatrix() {
        Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
        viewMatrix.identity();
        viewMatrix.lookAt(
                // this is where the camera is in the world space
                new Vector3f(position.x, position.y, 20.0f),
                // what direction we are looking at
                cameraFront.add(position.x, position.y, 0.0f),
                // which direction is up
                cameraUp
        );
        viewMatrix.invert(inverseView);
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getInverseProjection() {
        return inverseProjection;
    }

    public Matrix4f getInverseView() {
        return inverseView;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public void addZoom(float val) {
        this.zoom += val;
    }
}
