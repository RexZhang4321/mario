package physics2d;

import jade.GameObject;
import jade.Transform;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;
import org.joml.Vector2f;
import physics2d.components.Box2DCollider;
import physics2d.components.CircleCollider;
import physics2d.components.RigidBody2D;

public class Physics2D {
    private Vec2 gravity = new Vec2(0, -10.0f);
    private World world = new World(gravity);

    private float physicsTime = 0.0f;
    private float physicsTimeStep = 1.0f / 60.0f;
    private int velocityIterations = 8;
    private int positionIterations = 3;

    public void add(GameObject gameObject) {
        RigidBody2D rigidBody2D = gameObject.getComponent(RigidBody2D.class);
        if (rigidBody2D != null && rigidBody2D.getRawBody() == null) {
            Transform transform = gameObject.transform;

            BodyDef bodyDef = new BodyDef();
            bodyDef.angle = (float) Math.toRadians(transform.rotation);
            bodyDef.position.set(transform.position.x, transform.position.y);
            bodyDef.angularDamping = rigidBody2D.getAngularDamping();
            bodyDef.linearDamping = rigidBody2D.getLinearDamping();
            bodyDef.fixedRotation = rigidBody2D.isFixedRotation();
            bodyDef.bullet = rigidBody2D.isContinuousCollision();

            switch (rigidBody2D.getBodyType()) {
                case Kinematic -> bodyDef.type = BodyType.KINEMATIC;
                case Static -> bodyDef.type = BodyType.STATIC;
                case Dynamic -> bodyDef.type = BodyType.DYNAMIC;
            }

            PolygonShape shape = new PolygonShape();
            CircleCollider circleCollider;
            Box2DCollider box2DCollider;
            if ((circleCollider = gameObject.getComponent(CircleCollider.class)) != null) {
                shape.setRadius(circleCollider.getRadius());
            } else if ((box2DCollider = gameObject.getComponent(Box2DCollider.class)) != null) {
                Vector2f halfSize = new Vector2f(box2DCollider.getHalfSize().mul(0.5f));
                Vector2f offset = box2DCollider.getOffset();
                Vector2f origin = new Vector2f(box2DCollider.getOrigin());
                shape.setAsBox(halfSize.x, halfSize.y, new Vec2(origin.x, origin.y), 0);

                Vec2 pos = bodyDef.position;
                float xPos = pos.x + offset.x;
                float yPos = pos.y + offset.y;
                bodyDef.position.set(xPos, yPos);
            }

            Body body = world.createBody(bodyDef);
            rigidBody2D.setRawBody(body);
            body.createFixture(shape, rigidBody2D.getMass());
        }
    }

    public void update(float dt) {
        physicsTime += dt;
        if (physicsTime >= 0.0f) {
            physicsTime -= physicsTimeStep;
            world.step(physicsTimeStep, velocityIterations, positionIterations);
        }
    }

    public void destroyGameObject(GameObject gameObject) {
        RigidBody2D rigidBody2D = gameObject.getComponent(RigidBody2D.class);
        if (rigidBody2D != null) {
            if (rigidBody2D.getRawBody() != null) {
                world.destroyBody(rigidBody2D.getRawBody());
                rigidBody2D.setRawBody(null);
            }
        }
    }

}
