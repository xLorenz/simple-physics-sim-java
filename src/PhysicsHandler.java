package src;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicsHandler {

    public double gravity = 9.8;
    public int chunkGridSize = 10; // 10x10 grid of chunks
    public int chunkDimension = 50; // pixels

    public Map<Long, Chunk> chunks = new HashMap<>();
    public List<PhysicsObject> objects = new ArrayList<>();

    PhysicsHandler() {

    }

    // get key for a chunk
    public long keyFor(int cx, int cy) {
        return 0xffffffffL;
    }

    // check for a chunk or add one to the map
    public Chunk getOrCreateChunk(int cx, int cy) {
        return new Chunk();
    }

    public void updateObjectsChunk(PhysicsObject o) {

    }

    public void updatePhysics(double dt) {

    }

    public void calcCollision(PhysicsObject o1, PhysicsObject o2) {

    }

    public void addObject(PhysicsObject o) {

    }

    public void removeObject(PhysicsObject o) {

    }

    public void displayObjects(Graphics g) {

    }
}
