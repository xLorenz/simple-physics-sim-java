package physics.process;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import physics.objects.PhysicsBall;
import physics.objects.PhysicsObject;
import physics.objects.PhysicsRect;
import physics.structures.Chunk;
import physics.structures.Contact;
import physics.structures.Vector2;

public class PhysicsHandler {

    private final PhysicsUpdater updater = new PhysicsUpdater();
    private final BatchRenderer renderer = new BatchRenderer();
    public final Display display = new Display();

    private ArrayList<PhysicsObject> updateObjects = new ArrayList<>();
    private volatile ArrayList<PhysicsObject> renderObjects = new ArrayList<>();

    private ArrayList<PhysicsObject> addQueue = new ArrayList<>();
    private ArrayList<PhysicsObject> removeQueue = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private Thread updaterThread;

    private Map<Long, Chunk> chunks = new HashMap<>();
    public int chunkDimension = 25;

    public Vector2 gravity = new Vector2(0, 980);
    private Long nextId = 1L;

    public PhysicsHandler() {
        updater.setHandler(this);
        renderer.setDisplay(this.display);
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
        lock.readLock().lock();
        try {
            synchronized (updateObjects) {
                renderObjects = new ArrayList<>(updateObjects); // snapshot copy
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public void render(Graphics2D g) {
        renderer.setGraphics(g);
        for (PhysicsObject p : getRenderObjects()) {
            if (p != null)
                p.draw(renderer);
        }
    }

    public void renderDebug(Graphics2D g) {
        renderer.setGraphics(g);
        for (PhysicsObject p : getRenderObjects()) {
            if (p != null)
                p.drawDebug(renderer);
        }
    }

    public void addObject(PhysicsObject object) {
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
                if (display.mainObject == object)
                    display.mainObject = null;
                object.forceWake();
                removeQueue.add(object);
            }
        }
    }

    public void proccessAditionsAndRemovals() {

        // first, process any pending additions/removals queued from other threads
        lock.writeLock().lock();

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
        lock.writeLock().unlock();

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
        lock.readLock().lock();
        try {
            synchronized (updateObjects) {
                return Collections.unmodifiableList(new ArrayList<>(updateObjects));
            }
        } finally {
            lock.readLock().unlock();
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

    public Display getDisplay() {
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

    public void displayChunkBorders(Graphics2D g, int scrWidth, int scrHeight) {
        double scale = display.scale;
        if (scale <= 0)
            return;

        final int cd = chunkDimension; // chunk size in world units
        final double ox = display.offset.x; // world offset (unscaled)
        final double oy = display.offset.y;

        // draw anchor (transformed by offset & scale)
        int anchorX = (int) Math.round(ox * scale);
        int anchorY = (int) Math.round(oy * scale);

        Color prevColor = g.getColor();
        g.setColor(Color.RED);
        g.fillOval(anchorX - 5, anchorY - 5, 10, 10);

        // compute world-space bounds of the viewport (unscaled world coords)
        double viewLeft = -ox;
        double viewTop = -oy;
        double viewRight = scrWidth / scale - ox;
        double viewBottom = scrHeight / scale - oy;

        // compute chunk index range that intersects the viewport (add a 1-chunk margin)
        int startCx = (int) Math.floor(viewLeft / cd) - 1;
        int endCx = (int) Math.floor(viewRight / cd) + 1;
        int startCy = (int) Math.floor(viewTop / cd) - 1;
        int endCy = (int) Math.floor(viewBottom / cd) + 1;

        g.setColor(Color.GRAY);

        // vertical grid lines (one line at each chunk boundary)
        for (int cx = startCx; cx <= endCx; cx++) {
            int x = (int) Math.round((cx * cd + ox) * scale);
            g.drawLine(x, 0, x, scrHeight);
        }

        // horizontal grid lines
        for (int cy = startCy; cy <= endCy; cy++) {
            int y = (int) Math.round((cy * cd + oy) * scale);
            g.drawLine(0, y, scrWidth, y);
        }

        g.setColor(prevColor);
    }

    public void drawRecordedChunks(Graphics2D g, int scrWidth, int scrHeight, boolean fillActiveChunks) {

        final int cd = chunkDimension;
        final double offsetX = display.offset.x;
        final double offsetY = display.offset.y;

        // Compute unscaled screen bounds (world coords before applying scale)
        double worldRight = scrWidth / display.scale;
        double worldBottom = scrHeight / display.scale;

        // A chunk at cx has unscaled x origin = cx * cd + offsetX
        // We want chunks whose screen rect intersects [0,scrWidth] so:
        // (cx*cd + offsetX) + cd > 0 && (cx*cd + offsetX) < worldRight
        // => cx ∈ ((-offsetX - cd) / cd, (worldRight - offsetX) / cd)
        int minCx = (int) Math.floor((-offsetX - cd) / (double) cd);
        int maxCx = (int) Math.floor((worldRight - offsetX) / (double) cd);

        int minCy = (int) Math.floor((-offsetY - cd) / (double) cd);
        int maxCy = (int) Math.floor((worldBottom - offsetY) / (double) cd);

        // clamp ranges if you have world limits (optional)
        // e.g. minCx = Math.max(minCx, WORLD_MIN_CX);

        int tileScreenSize = (int) Math.ceil(cd * display.scale);

        g.setColor(Color.yellow);

        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cy = minCy; cy <= maxCy; cy++) {
                long key = (((long) cx) << 32) | (cy & 0xffffffffL);
                Chunk chunk = chunks.get(key);
                if (chunk == null)
                    continue;

                // compute screen coords
                int worldX = (int) Math.round((cx * cd + offsetX) * display.scale);
                int worldY = (int) Math.round((cy * cd + offsetY) * display.scale);

                // quick intersection safety (redundant but cheap)
                if (worldX + tileScreenSize <= 0 || worldX >= scrWidth
                        || worldY + tileScreenSize <= 0 || worldY >= scrHeight) {
                    continue;
                }

                g.setColor(Color.yellow);
                g.drawRect(worldX, worldY, tileScreenSize, tileScreenSize);

                if (fillActiveChunks) {
                    if (!chunk.objects.isEmpty()) {
                        g.setColor(Color.green.darker());
                        g.fillRect(worldX, worldY, tileScreenSize, tileScreenSize);
                    }
                }
            }
        }
    }

}