package physics2d;

import jade.GameObject;
import jade.Transform;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.joml.Vector2f;
import physics2d.components.Box2DCollider;
import physics2d.components.CircleCollider;
import physics2d.components.RigidBody2D;

import java.util.Objects;

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
            bodyDef.userData = rigidBody2D.gameObject;
            bodyDef.bullet = rigidBody2D.isContinuousCollision();
            bodyDef.gravityScale = rigidBody2D.getGravityScale();
            bodyDef.angularVelocity = rigidBody2D.getAngularVelocity();

            switch (rigidBody2D.getBodyType()) {
                case Kinematic -> bodyDef.type = BodyType.KINEMATIC;
                case Static -> bodyDef.type = BodyType.STATIC;
                case Dynamic -> bodyDef.type = BodyType.DYNAMIC;
            }

            Body body = world.createBody(bodyDef);
            body.m_mass = rigidBody2D.getMass();
            rigidBody2D.setRawBody(body);

            Box2DCollider box2DCollider;
            CircleCollider circleCollider;
            if ((circleCollider = gameObject.getComponent(CircleCollider.class)) != null) {
                addCircleCollider(rigidBody2D, circleCollider);
            }

            if ((box2DCollider = gameObject.getComponent(Box2DCollider.class)) != null) {
                addBox2DCollider(rigidBody2D, box2DCollider);
            }
        }
    }

    public void update(float dt) {
        physicsTime += dt;
        if (physicsTime >= 0.0f) {
            physicsTime -= physicsTimeStep;
            world.step(physicsTimeStep, velocityIterations, positionIterations);
        }
    }

    public void setIsSensor(RigidBody2D rigidBody2D) {
        Body body = rigidBody2D.getRawBody();
        if (body == null) {
            return;
        }

        Fixture fixture = body.getFixtureList();
        while (fixture != null) {
            fixture.m_isSensor = true;
            fixture = fixture.m_next;
        }
    }

    public void setNotSensor(RigidBody2D rigidBody2D) {
        Body body = rigidBody2D.getRawBody();
        if (body == null) {
            return;
        }

        Fixture fixture = body.getFixtureList();
        while (fixture != null) {
            fixture.m_isSensor = false;
            fixture = fixture.m_next;
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

    public void resetCircleCollider(RigidBody2D rigidBody2D, CircleCollider circleCollider) {
        Body body = rigidBody2D.getRawBody();
        if (body == null) {
            return;
        }

        int size = fixtureListSize(body);
        for (int i = 0; i < size; i++) {
            body.destroyFixture(body.getFixtureList());
        }

        addCircleCollider(rigidBody2D, circleCollider);
        body.resetMassData();
    }

    public void addCircleCollider(RigidBody2D rigidBody2D, CircleCollider circleCollider) {
        Body body = rigidBody2D.getRawBody();
        Objects.requireNonNull(body, "Raw body must not be null");

        CircleShape shape = new CircleShape();
        shape.setRadius(circleCollider.getRadius());
        shape.m_p.set(circleCollider.getOffset().x, circleCollider.getOffset().y);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.userData = circleCollider.gameObject;

        body.createFixture(fixtureDef);
    }

    public void resetBox2DCollider(RigidBody2D rigidBody2D, Box2DCollider box2DCollider) {
        Body body = rigidBody2D.getRawBody();
        if (body == null) {
            return;
        }

        int size = fixtureListSize(body);
        for (int i = 0; i < size; i++) {
            body.destroyFixture(body.getFixtureList());
        }

        addBox2DCollider(rigidBody2D, box2DCollider);
        body.resetMassData();
    }

    public void addBox2DCollider(RigidBody2D rigidBody2D, Box2DCollider box2DCollider) {
        Body body = rigidBody2D.getRawBody();
        Objects.requireNonNull(body, "Raw body must not be null");

        PolygonShape shape = new PolygonShape();
        Vector2f halfSize = new Vector2f(box2DCollider.getHalfSize()).mul(0.5f);
        Vector2f offset = box2DCollider.getOffset();
        shape.setAsBox(halfSize.x, halfSize.y, new Vec2(offset.x, offset.y), 0);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.userData = box2DCollider.gameObject;

        body.createFixture(fixtureDef);
    }

    public RaycastInfo raycast(GameObject requestingObject, Vector2f point1, Vector2f point2) {
        RaycastInfo callback = new RaycastInfo(requestingObject);
        world.raycast(callback, new Vec2(point1.x, point1.y), new Vec2(point2.x, point2.y));
        return callback;
    }

    private int fixtureListSize(Body body) {
        int size = 0;
        Fixture fixture = body.getFixtureList();
        while (fixture != null) {
            size++;
            fixture = fixture.m_next;
        }
        return size;
    }
}
