package physics;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class PhysicsObject {

    public static final double SUPPORT_NORMAL_Y = 1;
    public static final double SUPPORT_VELOCITY_EPS = 0.01;
    public static final double PENETRATION_EPS = 0.0001;
    public static final double VEL_EPS = 0.0001;

    public Vector2 pos = new Vector2(); // pos, for all objects, its center
    public int cx, cy; // center chunkPos
    public Vector2 vel = new Vector2(); // velocity
    public int cMinCx, cMaxCx, cMinCy, cMaxCy; // chunks boundingBox for big objects
    public double mass;
    public double elasticity;
    public double friction = 0.0;
    public long id; // identifier

    public boolean stationary = false;
    public boolean supported = false;

    public List<Contact> contacts = new ArrayList<Contact>();

    public Color displayColor = Color.white;
    private CollisionListener collisionListener = null;

    PhysicsObject(long id) {
        this.id = id;
    }

    public void randomizeColor() {
        Random r = new Random();
        displayColor = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
    }

    public void notifyListener(PhysicsObject o, Manifold m) {
        if (collisionListener != null) {
            collisionListener.action(o, m);
        }
    }

    public void setListener(CollisionListener l) {
        collisionListener = l;
    }

    public void updateOccupiedChunks(int[] chunkCoords) {

        cMinCx = chunkCoords[0];
        cMaxCx = chunkCoords[1];
        cMinCy = chunkCoords[2];
        cMaxCy = chunkCoords[3];
    }

    public void addContact(PhysicsObject o2, Vector2 normal, double penetration) {

        Contact c1 = new Contact();

        c1.other = o2;
        c1.normal = normal; // from 1 to 2
        c1.penetration = penetration;

        contacts.add(c1);
    }

    public void addForce(Vector2 force, double dt) {
        vel.addLocal(force.scale(dt));
    }

    public void integrateVelocity(double dt) {
        pos.addLocal(vel.scale(dt));
        if (vel.lengthSquared() < VEL_EPS)
            vel.set(0, 0);
    }

    public void updateSupportState() {
        supported = false;
        for (Contact c : contacts) {
            if (c.normal.y > SUPPORT_NORMAL_Y && vel.y <= SUPPORT_VELOCITY_EPS && c.penetration > PENETRATION_EPS) {
                supported = true;
                break;
            }
        }
    }

    public void update(double dt) {
    }

    public abstract void draw(Graphics g, Vector2 offset);

    public abstract int[] getOccuppiedChunks(int chunkDim);

    abstract Manifold collide(PhysicsObject other);

    // hooks for double dispatch
    abstract Manifold collideWithCircle(PhysicsBall c);

    abstract Manifold collideWithRect(PhysicsRect aabb);

}