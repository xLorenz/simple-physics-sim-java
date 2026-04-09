package physics.objects;

import java.awt.Color;
import java.util.ArrayList;

import physics.collisions.Collision;
import physics.process.BatchRenderer;
import physics.structures.Manifold;
import physics.structures.Vector2;

public class AreaRect extends PhysicsRect {
    private ArrayList<PhysicsObject> collisions = new ArrayList<>();

    public AreaRect(Vector2 pos, int width, int height) {
        super(width, height, 0, 0);
        this.pos = pos;
        stationary = true;
        forceAwake = true;
    }

    public ArrayList<PhysicsObject> getCollisions() {
        synchronized (collisions) {
            return new ArrayList<>(collisions);
        }
    }

    @Override
    public void draw(BatchRenderer renderer) {
        drawDebug(renderer);
    }

    @Override
    public void drawDebug(BatchRenderer renderer) {
        if (collisions.isEmpty()) {
            renderer.setFill(Color.darkGray.darker(), 255);
        } else {
            renderer.setFill(Color.gray, 255);
        }
        renderer.drawRectOutline(pos, width, height);
    }

    @Override
    public Manifold collide(PhysicsObject other) {
        Manifold m = other.collideWithRect(this);
        updateCollision(other, m);
        return null;
    }

    // hooks for double dispatch
    @Override
    public Manifold collideWithCircle(PhysicsBall b) {
        Manifold m = Collision.circleRect(b, this);
        updateCollision(b, m);
        return null;
    }

    @Override
    public Manifold collideWithRect(PhysicsRect rect) {
        Manifold m = Collision.rectRect(rect, this);
        updateCollision(rect, m);
        return null;
    }

    public void updateCollision(PhysicsObject o, Manifold m) {
        if (m != null) {
            if (!collisions.contains(o))
                collisions.add(o);
        } else {
            if (collisions.contains(o))
                collisions.remove(o);
        }
    }

}
