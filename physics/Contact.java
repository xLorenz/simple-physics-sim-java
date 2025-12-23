package physics;

class Contact {
    PhysicsObject other;
    Vector2 normal; // normal pointing from other -> this
    double penetration;
}
