package physics.process;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import physics.objects.PhysicsBall;
import physics.objects.PhysicsObject;
import physics.objects.PhysicsRect;
import physics.structures.Chunk;
import physics.structures.Contact;
import physics.structures.Vector2;

public class NeoPhysicsHandler {

    private final PhysicsUpdater updater = new PhysicsUpdater();
    private final BatchRenderer renderer = new BatchRenderer();
    public final PhysicsDisplay display = new PhysicsDisplay();

    private ArrayList<PhysicsObject> updateObjects = new ArrayList<>();
    private volatile ArrayList<PhysicsObject> renderObjects = new ArrayList<>();

    private ArrayList<PhysicsObject> addQueue = new ArrayList<>();
    private ArrayList<PhysicsObject> removeQueue = new ArrayList<>();

    private Thread updaterThread;

    private Map<Long, Chunk> chunks = new HashMap<>();
    public int chunkDimension = 25;

    public Vector2 gravity = new Vector2(0, 980);
    private Long nextId = 1L;

    public NeoPhysicsHandler() {
        updater.setHandler(this);
        renderer.setHandler(this);
    }

    public void beginUpdaterThread() {
        System.out.println("starting physics update thread");
        if (updaterThread == null || !updaterThread.isAlive()) {
            updaterThread = new Thread(updater, "Physics-Updater");
            updaterThread.setDaemon(true);
            updaterThread.start();
        }
    }

    public void stopUpdaterThread() {

        if (updater != null) {
            updater.stop();
        }
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
        System.out.println("adding object");
        synchronized (addQueue) {
            if (!addQueue.contains(object)) {
                object.id = nextId++;
                addQueue.add(object);
            }
        }
    }

    public void removeObject(PhysicsObject object) {
        synchronized (removeQueue) {
            if (!removeQueue.contains(object)) {
                object.forceWake();
                removeQueue.add(object);
            }
        }
    }

    public void proccessAditionsAndRemovals() {

        // first, process any pending additions/removals queued from other threads
        synchronized (addQueue) {
            if (!addQueue.isEmpty()) {
                for (PhysicsObject o : addQueue) {
                    updateObjects.add(o);
                }
                addQueue.clear();
            }
        }
        synchronized (removeQueue) {
            if (!removeQueue.isEmpty()) {
                for (PhysicsObject o : removeQueue) {
                    o.forceWake();
                    // release contacts owned by the removed object
                    for (Contact c : o.contacts) {
                        Contact.release(c);
                    }
                    o.contacts.clear();
                    updateObjects.remove(o);
                    // also remove from any chunks the object occupied
                    for (int cx = o.cMinCx; cx <= o.cMaxCx; cx++) {
                        for (int cy = o.cMinCy; cy <= o.cMaxCy; cy++) {
                            Chunk ch = chunks.get(keyFor(cx, cy));
                            if (ch == null)
                                continue;
                            ch.objects.remove(o);
                            for (PhysicsObject o2 : ch.objects) {
                                o2.forceWake();
                            }
                        }
                    }
                }
                removeQueue.clear();
            }
        }

    }

    public void addBall(Vector2 pos, int radius, double elasticity, double mass) {
        PhysicsBall ball = new PhysicsBall(radius, elasticity, mass, 0);
        ball.pos = pos;
        addObject(ball);
    }

    public void addBall(Vector2 pos, int radius, double elasticity, double mass, Color color) {
        PhysicsBall ball = new PhysicsBall(radius, elasticity, mass, 0);
        ball.pos = pos;
        ball.setDisplayColor(color);
        addObject(ball);
    }

    public void addRect(Vector2 center, int width, int height) {
        PhysicsRect rect = new PhysicsRect(width, height, 0, 0);
        rect.pos = center;
        addObject(rect);
    }

    public void addRect(Vector2 center, int width, int height, double mass, double elasticity, boolean stationary) {
        PhysicsRect rect = new PhysicsRect(width, height, mass, 0);
        rect.pos = center;
        rect.elasticity = elasticity;
        rect.stationary = stationary;
        addObject(rect);
    }

    public ArrayList<PhysicsObject> getUpdateObjects() {
        return updateObjects;
    }

    public List<PhysicsObject> getUpdateObjectsSnapshot() {
        synchronized (updateObjects) {
            return Collections.unmodifiableList(new ArrayList<>(updateObjects));
        }
    }

    public PhysicsUpdater getUpdater() {
        return updater;
    }

    public ArrayList<PhysicsObject> getRenderObjects() {
        return renderObjects;
    }

    public BatchRenderer getRenderer() {
        return renderer;
    }

    public PhysicsDisplay getDisplay() {
        return display;
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