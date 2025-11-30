package physics;

import java.util.ArrayList;
import java.util.List;

public class Manifold {
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
