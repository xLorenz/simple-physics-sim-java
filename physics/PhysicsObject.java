package physics;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public abstract class PhysicsObject {

    public Vector2 pos = new Vector2(); // pos, for all objects, its center
    public int cx, cy; // center chunkPos
    public Vector2 vel = new Vector2(); // velocity
    public int cMinCx, cMaxCx, cMinCy, cMaxCy; // chunks boundingBox for big objects
    public double mass;
    public double elasticity;
    public long id; // identifier

    public Color displayColor = Color.red;
    private CollisionListener collisionListener = null;

    PhysicsObject(long id) {
        this.id = id;
        setColor();
    }

    public void setColor() {
        Random r = new Random();
        displayColor = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
    }

    public void notifyListener(PhysicsObject o, Manifold m) {
        if (collisionListener != null) {
            collisionListener.action(o, m);
        }
    }

    public void setListener(CollisionListener l) {
        collisionListener = l;
    }

    public void updateOccupiedChunks(int[] chunkCoords) {

        cMinCx = chunkCoords[0];
        cMaxCx = chunkCoords[1];
        cMinCy = chunkCoords[2];
        cMaxCy = chunkCoords[3];
    }

    public abstract void update(double gravity, double dt);

    public abstract void draw(Graphics g);

    public abstract int[] getOccuppiedChunks(int chunkDim, Vector2 mapAnchor);

    abstract Manifold collide(PhysicsObject other);

    // hooks for double dispatch
    abstract Manifold collideWithCircle(PhysicsBall c);

    abstract Manifold collideWithRect(PhysicsRect aabb);

}