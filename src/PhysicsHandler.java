package src;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicsHandler {

    public double gravity = 9.8;

    public Boundary boindaries;

    public int chunkDimension = 50; // pixels

    public Map<Long, Chunk> chunks = new HashMap<>();
    public List<PhysicsObject> objects = new ArrayList<>();

    private long nextId = 1L; // ids to keep track of objects

    PhysicsHandler(int left, int top, int right, int bottom) {
        this.boindaries = new Boundary(left, right, top, bottom);
    }

    public class Boundary {
        int left, right, top, bottom;

        Boundary(int left, int right, int top, int bottom) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }
    }

    public void setBoundary(int left, int top, int right, int bottom) {
        this.boindaries = new Boundary(left, right, top, bottom);
    }

    // get key for a chunk
    public long keyFor(int cx, int cy) {
        return ((long) cx << 32) ^ (cy & 0xffffffffL);
    }

    // check for a chunk or add one to the map
    public Chunk getOrCreateChunk(int cx, int cy) {
        return chunks.computeIfAbsent(keyFor(cx, cy), k -> new Chunk());
    }

    public void updateObjectsChunk(PhysicsObject o) {
        int ncx = (int) Math.floor(o.pos.x / chunkDimension);
        int ncy = (int) Math.floor(o.pos.y / chunkDimension);

        if (ncx == o.cx && ncy == o.cy)
            return;

        // if the chunk changed, and the object is large, it will occupy different
        // chunks

        int[] occuppiedChunks = o.getOccuppiedChunks(chunkDimension);
        int minCx = occuppiedChunks[0];
        int maxCx = occuppiedChunks[1];
        int minCy = occuppiedChunks[2];
        int maxCy = occuppiedChunks[3];

        // remove from olds
        for (int cx = o.cMinCx; cx <= o.cMaxCx; cx++) {
            for (int cy = o.cMinCy; cy <= o.cMaxCy; cy++) {
                Chunk old = chunks.get(keyFor(cx, cy));
                if (old != null)
                    old.objects.remove(o);
            }
        }

        // add to news
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cy = minCy; cy <= maxCy; cy++) {
                Chunk old = chunks.get(keyFor(cx, cy));
                if (old != null)
                    old.objects.remove(o);
            }
        }

        // update object chunk pos
        o.cx = ncx;
        o.cy = ncy;
        o.updateOccupiedChunks(occuppiedChunks);

    }

    public void updatePhysics(double dt) {

        for (PhysicsObject o1 : objects) {
            o1.update(gravity, dt); // update positions
            updateObjectsChunk(o1); // update chunk membership

            // itterate over every chunk the object is in
            for (int cx = o1.cMinCx; cx <= o1.cMaxCx; cx++) {
                for (int cy = o1.cMinCy; cy <= o1.cMaxCy; cy++) {

                    Chunk ch = chunks.get(keyFor(cx, cy));
                    if (ch == null)
                        continue;
                    // check every object of every chunk the object is in
                    for (PhysicsObject o2 : ch.objects) {
                        if (o1.id <= o2.id)
                            continue; // check unordered pair only once
                        handleCollision(o1, o2); // calculate collision and update objects velocities
                    }
                }
            }
        }
    }

    public void handleCollision(PhysicsObject o1, PhysicsObject o2) {

    }

    public void addBall(int x, int y, int radius, double elasticity) {
        PhysicsBall ball = new PhysicsBall(radius, elasticity, nextId++);
        ball.pos.x = x;
        ball.pos.y = y;
        objects.add(ball);
    }

    public void addRect(int x, int y, int width, int height) {
        PhysicsRect rect = new PhysicsRect(width, height, nextId++);
        rect.pos.x = x;
        rect.pos.y = y;
        objects.add(rect);
    }

    public void removeObject(PhysicsObject o) {
        // remove from list
        if (objects.contains(o)) {
            objects.remove(o);
        }
        // remove from chunks
        for (int cx = o.cMinCx; cx <= o.cMaxCx; cx++) {
            for (int cy = o.cMinCy; cy <= o.cMaxCy; cy++) {
                Chunk old = chunks.get(keyFor(cx, cy));
                if (old != null)
                    old.objects.remove(o);
            }
        }
    }

    public void displayObjects(Graphics g) {
        for (PhysicsObject o : objects) {
            o.draw(g);
        }
    }

    public void displayChunkBorders(Graphics g, int scrWidth, int scrHeight) {
        // draw grid
        g.setColor(Color.gray);
        for (int i = 0; i < scrWidth / chunkDimension; i++) {
            g.drawLine(i * chunkDimension, 0, i * chunkDimension, scrHeight);
        }
        for (int i = 0; i < scrHeight / chunkDimension; i++) {
            g.drawLine(0, i * chunkDimension, scrWidth, i * chunkDimension);
        }
        // g.fillRect(scrWidth / 2 - 5, scrHeight / 2 - 5, 10, 10); // point middle
    }
}
