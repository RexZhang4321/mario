package physics2d;

import jade.GameObject;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import org.joml.Vector2f;

public class RaycastInfo implements RayCastCallback {
    public Fixture fixture;
    public Vector2f hitPoint;
    public Vector2f hitNormal;
    public float fraction;
    public boolean isHit;
    public GameObject hitObject;

    private GameObject requestingObject;

    public RaycastInfo(GameObject gameObject) {
        fixture = null;
        hitPoint = new Vector2f();
        hitNormal = new Vector2f();
        fraction = 0.0f;
        isHit = false;
        hitObject = null;
        requestingObject = gameObject;
    }

    @Override
    public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
        if (fixture.m_userData == requestingObject) {
            return 1;
        }

        this.fixture = fixture;
        this.hitPoint = new Vector2f(point.x, point.y);
        this.hitNormal = new Vector2f(normal.x, normal.y);
        this.fraction = fraction;
        this.isHit = fraction != 0;
        this.hitObject = ((GameObject) fixture.m_userData);

        return fraction;
    }
}
