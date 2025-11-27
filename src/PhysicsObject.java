package src;

import java.awt.Color;
import java.awt.Graphics;

abstract class PhysicsObject {

    public int x, y; // pos, for all objects, its center
    public int cx, cy; // center chunkPos
    public double vx, vy; // velocity
    public int cMinCx, cMaxCx, cMinCy, cMaxCy; // chunks boundingBox for big objects
    public final long id; // identifier

    public Color displayColor = Color.red;
    private CollisionListener collisionListener = null;

    PhysicsObject(long id) {
        this.id = id;
    }

    public void notifyListener(PhysicsObject o) {
        if (collisionListener != null) {
            collisionListener.action(o);
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

    public abstract void addVelocity(double vx, double vy);

    public abstract void update(double gravity, double dt);

    public abstract void draw(Graphics g);

    public abstract int[] getOccuppiedChunks(int chunkDim);

}
