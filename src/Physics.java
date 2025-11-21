package src;

import java.util.ArrayList;
import java.util.List;

public class Physics {
    private List<PhysicsObject> objects = new ArrayList<>();

    Physics() {

    }

    public class Handler {
        public void updatePhysics(double dt) {
            for (PhysicsObject obj : objects) {
                obj.update(dt);
            }
        }
    }

    abstract class PhysicsObject {
        PhysicsObject() {
            objects.add(this);
        }

        public abstract void update(double dt);
    }

    public class PhysicsBall extends PhysicsObject {
        @Override
        public void update(double dt) {

        }
    }

    public class PhysicsRect extends PhysicsObject {
        @Override
        public void update(double dt) {

        }
    }
}