package physics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class PhysicsObject {

    public static double SUPPORT_NORMAL_Y = 0.60;
    public static double VEL_EPS = 5.0;
    public static int MAX_SLEEP_FRAMES = 50;
    public static double WAKE_VEL_THRESHOLD = 1.0;
    public static double WAKE_PENETRATION_THRESHOLD = 0.5;

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
    public boolean forceAwake = false;

    public List<Contact> contacts = new ArrayList<Contact>();

    public Color displayColor = Color.white;
    public Color displayColorDarker = displayColor.darker();
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
        setDisplayColor(new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
    }

    public void setDisplayColor(Color c) {
        this.displayColor = c;
        this.displayColorDarker = (c == null) ? Color.black : c.darker();
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
        Contact c1 = Contact.obtain();
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
        for (Contact c : contacts) {
            if (c.normal.y > 0) {
                normal.addLocal(c.normal);
            }
        }
        if (normal.normalize().y > SUPPORT_NORMAL_Y) {
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
        if (!forceAwake)
            if (vel.lengthSquared() < VEL_EPS && !sleeping && (supported || stationary)) {
                sleepFrames++;
                if (sleepFrames >= MAX_SLEEP_FRAMES) {
                    sleeping = true;
                }
            }
    }

    public void update(double dt) {
    }

    public abstract void draw(Graphics2D g, Vector2 offset, double scale);

    public abstract void drawDebug(Graphics2D g, Vector2 offset, double scale);

    public abstract int[] getOccuppiedChunks(int chunkDim);

    abstract Manifold collide(PhysicsObject other);

    // hooks for double dispatch
    abstract Manifold collideWithCircle(PhysicsBall c);

    abstract Manifold collideWithRect(PhysicsRect aabb);

}