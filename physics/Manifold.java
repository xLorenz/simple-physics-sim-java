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

    static Manifold noCollision() {
        Manifold m = new Manifold();
        m.collided = false;
        return m;
    }
}
