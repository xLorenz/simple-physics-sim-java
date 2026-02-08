package physics.collisions;

import physics.objects.PhysicsObject;
import physics.structures.Manifold;

public abstract class CollisionListener {
    public abstract void action(PhysicsObject o, Manifold m);
}
