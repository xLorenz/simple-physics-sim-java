package physics;

import java.util.ArrayList;
import java.util.List;

public class Manifold {

    public PhysicsObject o1;
    public PhysicsObject o2;

    public double accumulatedNormalImpulse = 0.0;
    public double accumulatedTangentImpulse = 0.0;

    public boolean collided;
    public Vector2 normal;
    public double penetration;
    public List<Vector2> contacts = new ArrayList<>();

    // simple object pool for Manifold
    private static final java.util.ArrayDeque<Manifold> POOL = new java.util.ArrayDeque<>();

    static Manifold obtain() {
        Manifold m = POOL.pollLast();
        if (m == null) {
            m = new Manifold();
        }
        // reset fields
        m.o1 = null;
        m.o2 = null;
        m.accumulatedNormalImpulse = 0.0;
        m.accumulatedTangentImpulse = 0.0;
        m.collided = false;
        m.normal = null;
        m.penetration = 0.0;
        m.contacts.clear();
        return m;
    }

    static void release(Manifold m) {
        if (m == null)
            return;
        m.o1 = null;
        m.o2 = null;
        m.accumulatedNormalImpulse = 0.0;
        m.accumulatedTangentImpulse = 0.0;
        m.collided = false;
        m.normal = null;
        m.penetration = 0.0;
        m.contacts.clear();
        POOL.addLast(m);
    }
}
