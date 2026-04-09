package physics.objects;

import java.awt.Color;
import java.util.ArrayList;

import physics.collisions.Collision;
import physics.process.BatchRenderer;
import physics.structures.Manifold;
import physics.structures.Vector2;

public class AreaCircle extends PhysicsBall {
    private ArrayList<PhysicsObject> collisions = new ArrayList<>();

    public AreaCircle(Vector2 pos, int radius) {
        super(radius, 0.0, 0.0, 0);
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
        renderer.drawCircunference(pos, radius);
    }

    @Override
    public int[] getOccuppiedChunks(int chunkDim) {

        int[] result = new int[4];
        result[0] = (int) Math.floor((pos.x - radius) / chunkDim) - 1;
        result[1] = (int) Math.floor((pos.x + radius) / chunkDim) + 1;
        result[2] = (int) Math.floor((pos.y - radius) / chunkDim) - 1;
        result[3] = (int) Math.floor((pos.y + radius) / chunkDim) + 1;

        return result;
    }

    @Override
    public Manifold collide(PhysicsObject other) {
        Manifold m = other.collideWithCircle(this);
        updateCollision(other, m);
        return null;
    }

    // hooks for double dispatch
    @Override
    public Manifold collideWithCircle(PhysicsBall b) {
        Manifold m = Collision.circleCircle(b, this);
        updateCollision(b, m);
        return null;
    }

    @Override
    public Manifold collideWithRect(PhysicsRect rect) {
        Manifold m = Collision.circleRect(this, rect);
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
