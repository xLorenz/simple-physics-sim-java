package physics;

public abstract class CollisionListener {
    public abstract void action(PhysicsObject o, Manifold m);
}
