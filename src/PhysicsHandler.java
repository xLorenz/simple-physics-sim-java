package src;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class PhysicsHandler {

    private List<PhysicsObject> objects = new ArrayList<>();
    private List<Chunk> chunks = new ArrayList<>();

    private double gravity = 9.8;
    private int chunkDimension = 25; // pixels
    private int chunkGridDimension = 10; // 10x10 grid of chunks

    PhysicsHandler() {

    }

    public void displayObjects(Graphics g) {

    }

    public List<PhysicsObject> getObjects() {
        return objects;
    }

    public void addObject(PhysicsObject o) {
        objects.add(o);
    }

    public void removeObject(PhysicsObject o) {
        if (objects.contains(o)) {
            objects.remove(o);
        }
    }

    public double getGravity() {
        return gravity;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public int getChunkDimention() {
        return chunkDimension;
    }

    public void setChunkDimension(int dim) {
        this.chunkDimension = dim;
    }

    public void updatePhysics(double dt) {
        for (PhysicsObject obj : objects) {
            obj.update(dt);
        }
    }
}
