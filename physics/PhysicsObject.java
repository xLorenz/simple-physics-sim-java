package physics;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class PhysicsObject {

    public static final double SUPPORT_NORMAL_Y = 0.75;
    public static final double SUPPORT_VELOCITY_EPS = 0.1;
    public static final double PENETRATION_EPS = 0.0001;
    public static final double VEL_EPS = 2.0;
    public static final int MAX_SLEEP_FRAMES = 20;
    public static final double WAKE_VEL_THRESHOLD = 5.0;
    public static final double WAKE_PENETRATION_THRESHOLD = 0.9;

    public Vector2 pos = new Vector2(); // pos, for all objects, its center
    public int cx, cy; // center chunkPos
    public Vector2 vel = new Vector2(); // velocity
    public int cMinCx, cMaxCx, cMinCy, cMaxCy; // chunks boundingBox for big objects
    public double mass;
    public double invMass = Double.NaN;
    public double elasticity;
    public double friction = 0.0;
    public long id; // identifier

    public boolean stationary = false;
    public boolean supported = false;
    public boolean sleeping = false;
    public int sleepFrames = 0;

    public List<Contact> contacts = new ArrayList<Contact>();

    public Color displayColor = Color.white;
    private CollisionListener collisionListener = null;

    PhysicsObject(long id) {
        this.id = id;
    }

    public double getInverseMass() {
        return (mass == 0) ? 0 : 1.0 / mass;

    }

    public double getInverseMass(double m) {
        return (m == 0) ? 0 : 1.0 / m;

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
        if (vel.lengthSquared() < VEL_EPS)
            vel.set(0, 0);
        pos.addLocal(vel.scale(dt));
    }

    public void updateSupportState() {
        supported = false;
        Vector2 normal = new Vector2();
        double masses = 0;
        for (Contact c : contacts) {
            if (c.normal.y > 0) {
                if (c.other.mass == 0) {
                    masses = 0;
                }
                normal.addLocal(c.normal);
                masses += c.other.mass;
            }
        }
        if (normal.normalize().y > SUPPORT_NORMAL_Y && getInverseMass(masses) < invMass) {
            supported = true;
        }
    }

    public void forceWake() {
        if (!sleeping || stationary)
            return;
        sleepFrames = 0;
        sleeping = false;
        forceWakeContacts();
    }

    public void wake(double relVelAlongNormal, double penetration) {
        if (sleeping) {
            if (relVelAlongNormal > WAKE_VEL_THRESHOLD || penetration > WAKE_PENETRATION_THRESHOLD) {
                sleeping = false;
                sleepFrames = 0;
                forceWakeContacts();
            }
        }
    }

    public void forceWakeContacts() {
        for (Contact c : contacts) {
            c.other.forceWake(); // wake contacted objects
        }
    }

    public void updateSleepState() {
        if (vel.lengthSquared() < VEL_EPS && !sleeping && (supported || stationary)) {
            sleepFrames++;
            if (sleepFrames >= MAX_SLEEP_FRAMES) {
                sleeping = true;
            }
        }
        // if (vel.lengthSquared() > VEL_EPS && sleeping) {
        // forceWake();
        // }
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