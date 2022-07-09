package physics2d;

import components.Ground;
import components.PlayerController;
import jade.GameObject;
import jade.Transform;
import jade.Window;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import physics2d.components.Box2DCollider;
import physics2d.components.CircleCollider;
import physics2d.components.PillboxCollider;
import physics2d.components.RigidBody2D;
import renderer.DebugDraw;

import java.util.Objects;

public class Physics2D {
    private World world = new World(new Vec2(0, -10.0f));

    private float physicsTime = 0.0f;
    private float physicsTimeStep = 1.0f / 60.0f;
    private int velocityIterations = 8;
    private int positionIterations = 3;

    public Physics2D() {
        world.setContactListener(new JadeContactListener());
    }

    public Vector2f getGravity() {
        return new Vector2f(world.getGravity().x, world.getGravity().y);
    }

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
            PillboxCollider pillboxCollider;

            if ((circleCollider = gameObject.getComponent(CircleCollider.class)) != null) {
                addCircleCollider(rigidBody2D, circleCollider);
            }

            if ((box2DCollider = gameObject.getComponent(Box2DCollider.class)) != null) {
                addBox2DCollider(rigidBody2D, box2DCollider);
            }

            if ((pillboxCollider = gameObject.getComponent(PillboxCollider.class)) != null) {
                addPillboxCollider(rigidBody2D, pillboxCollider);
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

    // TODO: this is broken
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

    public boolean isLocked() {
        return world.isLocked();
    }

    // shoot a ray cast of the player to the ground to see if the player hit anything to determine whether the player is on the ground
    public static boolean checkOnGround(GameObject gameObject, float innerPlayerWidth, float height) {
        // check if the left side of the player is on the ground
        Vector2f raycastBegin = new Vector2f(gameObject.transform.position);
        raycastBegin.sub(innerPlayerWidth / 2.0f, 0.0f);

        Vector2f raycastEnd = new Vector2f(raycastBegin).add(0.0f, height);
        RaycastInfo info = Window.getPhysics().raycast(gameObject, raycastBegin, raycastEnd);

        // check if the right side of the player is on the ground
        Vector2f raycast2Begin = new Vector2f(raycastBegin).add(innerPlayerWidth, 0f);
        Vector2f raycast2End = new Vector2f(raycastEnd).add(innerPlayerWidth, 0f);
        RaycastInfo info2 = Window.getPhysics().raycast(gameObject, raycast2Begin, raycast2End);

        DebugDraw.addLine2D(raycastBegin, raycastEnd, new Vector3f(1f, 0f, 0f));
        DebugDraw.addLine2D(raycast2Begin, raycast2End, new Vector3f(1f, 0f, 0f));

        return  (info.isHit && info.hitObject != null && info.hitObject.getComponent(Ground.class) != null)
                || (info2.isHit && info2.hitObject != null && info2.hitObject.getComponent(Ground.class) != null);
    }

    public void addPillboxCollider(RigidBody2D rigidBody2D, PillboxCollider pillboxCollider) {
        Body body = rigidBody2D.getRawBody();
        Objects.requireNonNull(body, "Raw body must not be null");

        addBox2DCollider(rigidBody2D, pillboxCollider.getBox());
        addCircleCollider(rigidBody2D, pillboxCollider.getTopCircle());
        addCircleCollider(rigidBody2D, pillboxCollider.getBottomCircle());
    }

    public void resetPillboxCollider(RigidBody2D rigidBody2D, PillboxCollider pillboxCollider) {
        Body body = rigidBody2D.getRawBody();
        if (body == null) {
            return;
        }

        int size = fixtureListSize(body);
        for (int i = 0; i < size; i++) {
            body.destroyFixture(body.getFixtureList());
        }

        addPillboxCollider(rigidBody2D, pillboxCollider);
        body.resetMassData();
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
