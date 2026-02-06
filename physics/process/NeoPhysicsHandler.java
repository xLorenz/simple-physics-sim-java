package physics.process;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import physics.objects.PhysicsObject;
import physics.structures.Chunk;
import physics.structures.Vector2;

public class NeoPhysicsHandler {

    private final PhysicsUpdater updater = new PhysicsUpdater();
    private final PhysicsRenderer renderer = new PhysicsRenderer();

    private ArrayList<PhysicsObject> updateObjects = new ArrayList<>();
    private volatile ArrayList<PhysicsObject> renderObjects = new ArrayList<>();

    private Map<Long, Chunk> chunks = new HashMap<>();
    public int chunkDimension = 25;

    public Vector2 gravity = new Vector2(0, 980);

    public NeoPhysicsHandler() {
        updater.setHandler(this);
        renderer.setHandler(this);
    }

    public void publishFrame() {
        synchronized (updateObjects) {
            renderObjects = new ArrayList<>(updateObjects); // snapshot copy
        }
    }

    public void render(Graphics2D g) {
        renderer.setGraphics(g);
        for (PhysicsObject p : getRenderObjects()) {
            if (p != null)
                p.draw(renderer);
        }
    }

    public void addObject(PhysicsObject object) {
        synchronized (updateObjects) {
            updateObjects.add(object);
        }
    }

    public ArrayList<PhysicsObject> getUpdateObjects() {
        return updateObjects;
    }

    public PhysicsUpdater getUpdater() {
        return updater;
    }

    public ArrayList<PhysicsObject> getRenderObjects() {
        return renderObjects;
    }

    public PhysicsRenderer getRenderer() {
        return renderer;
    }

    // check for a chunk or add one to the map
    public Chunk getOrCreateChunk(int cx, int cy) {
        return chunks.computeIfAbsent(keyFor(cx, cy), k -> new Chunk());
    }

    // get key for a chunk
    public long keyFor(int cx, int cy) {
        return ((long) cx << 32) ^ (cy & 0xffffffffL);
    }

}