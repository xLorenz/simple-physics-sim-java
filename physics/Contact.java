package physics;

class Contact {
    PhysicsObject other;
    Vector2 normal; // normal pointing from other -> this
    double penetration;

    // Simple object pool
    private static final java.util.ArrayDeque<Contact> POOL = new java.util.ArrayDeque<>();

    static Contact obtain() {
        Contact c = POOL.pollLast();
        if (c == null) {
            c = new Contact();
        }
        return c;
    }

    static void release(Contact c) {
        if (c == null)
            return;
        c.other = null;
        c.normal = null;
        c.penetration = 0.0;
        POOL.addLast(c);
    }
}
