package src;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicsHandler {

    public double gravity = 980;

    public Boundary boindaries;

    public int chunkDimension = 25; // pixels

    public Map<Long, Chunk> chunks = new HashMap<>();
    public List<PhysicsObject> objects = new ArrayList<>();

    // store debug info for the most recent collisions so the renderer can draw them
    public static class CollisionDebug {
        public long a, b;
        public Vector2 contactPoint;
        public Vector2 normal;
        public double penetration;

        CollisionDebug(long a, long b, Vector2 contactPoint, Vector2 normal, double penetration) {
            this.a = a;
            this.b = b;
            this.contactPoint = contactPoint;
            this.normal = normal;
            this.penetration = penetration;
        }
    }

    public List<CollisionDebug> recentCollisions = new ArrayList<>();

    private long nextId = 1L; // ids to keep track of objects

    PhysicsHandler(int right, int top, int left, int bottom) {
        this.boindaries = new Boundary(right, top, left, bottom);
    }

    public class Boundary {
        int right, top, left, bottom;

        Boundary(int right, int top, int left, int bottom) {
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
                Chunk old = getOrCreateChunk(cx, cy);
                if (old != null)
                    old.objects.remove(o);
            }
        }

        // add to news
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cy = minCy; cy <= maxCy; cy++) {
                Chunk old = getOrCreateChunk(cx, cy);
                if (old != null)
                    old.objects.add(o);
            }
        }

        // update object chunk pos
        o.cx = ncx;
        o.cy = ncy;
        o.updateOccupiedChunks(occuppiedChunks);

    }

    public void updatePhysics(double dt) {

        for (PhysicsObject o : objects) {
            o.update(gravity, dt);
        }

        for (PhysicsObject o : objects) {
            updateObjectsChunk(o);
        }

        java.util.HashSet<Long> processedPairs = new java.util.HashSet<>();
        recentCollisions.clear();
        for (PhysicsObject o1 : objects) {
            for (int cx = o1.cMinCx; cx <= o1.cMaxCx; cx++) {
                for (int cy = o1.cMinCy; cy <= o1.cMaxCy; cy++) {
                    Chunk ch = chunks.get(keyFor(cx, cy));
                    if (ch == null)
                        continue;
                    for (PhysicsObject o2 : ch.objects) {
                        if (o1.id <= o2.id)
                            continue; // check unordered pair only once
                        long pairKey = ((o1.id) << 32) ^ o2.id;
                        if (processedPairs.contains(pairKey))
                            continue; // already handled this unordered pair in another chunk
                        processedPairs.add(pairKey);
                        handleCollision(o1, o2);
                    }
                }
            }
        }
    }

    public void handleCollision(PhysicsObject o1, PhysicsObject o2) {
        Manifold m = o1.collide(o2);

        if (!m.collided)
            return;

        // Ensure manifold.normal points from o1 -> o2 (handleCollision expects this)
        if (m.normal == null || m.normal.lengthSquared() < 1e-9) {
            // fallback: use vector from o1 -> o2
            Vector2 dir = o2.pos.sub(o1.pos);
            if (dir.lengthSquared() < 1e-9) {
                // unresolvable direction; pick up
                m.normal = new Vector2(0, -1);
            } else {
                m.normal = dir.scale(1.0 / dir.length());
            }
        } else {
            Vector2 dir = o2.pos.sub(o1.pos); // vector from o1 to o2
            double dot = m.normal.dot(dir);
            if (dot < 0) {
                // flip normal so it points from o1 to o2
                m.normal = m.normal.scale(-1.0);
            }
            // normalize to be safe
            m.normal.normalizeLocal();
        }

        // (debug logs removed) collision detected

        // inverse masses
        double invMass1 = (o1.mass == 0.0) ? 0.0 : 1.0 / o1.mass;
        double invMass2 = (o2.mass == 0.0) ? 0.0 : 1.0 / o2.mass;
        double invMassSum = invMass1 + invMass2;

        if (invMassSum == 0.0)
            return; // both inmovable

        // resolve velocity using an impulse, use elasticity
        Vector2 relVel = o2.vel.sub(o1.vel);
        double velAlongNormal = relVel.dot(m.normal); // positive if separating

        // if vels are separating already, don't apply impulse
        if (velAlongNormal <= 0.0) {
            double e = Math.max(o1.elasticity, o2.elasticity);
            // compute impulse scalar
            double j = -(1.0 + e) * velAlongNormal;
            j /= (invMassSum);

            // apply impulse
            Vector2 impulse = m.normal.scale(j);

            o1.vel.subLocal(impulse.scale(invMass1));
            o2.vel.addLocal(impulse.scale(invMass2));
        }

        // positional correction
        final double percent = 0.8; // 20% left to avoid jitter
        final double slop = 0.01; // small penetration allowance
        double correctionMag = Math.max(m.penetration - slop, 0.0) * percent / (invMassSum);
        // safety clamp — don't attempt to correct unbelievably large amounts in a
        // single step
        correctionMag = Math.min(correctionMag, Math.max(m.penetration * 0.5, 0.001));
        Vector2 correction = m.normal.scale(correctionMag);

        // move objs proportionally to inverse mass
        o1.pos.subLocal(correction.scale(invMass1));
        o2.pos.addLocal(correction.scale(invMass2));

        // record debug info for rendering
        Vector2 contact = m.contacts.size() > 0 ? m.contacts.get(0)
                : new Vector2((o1.pos.x + o2.pos.x) * 0.5, (o1.pos.y + o2.pos.y) * 0.5);
        recentCollisions
                .add(new CollisionDebug(o1.id, o2.id, contact, new Vector2(m.normal.x, m.normal.y), m.penetration));

    }

    public void addBall(int x, int y, int radius, double elasticity) {
        PhysicsBall ball = new PhysicsBall(radius, elasticity, 0.001, nextId++);
        ball.pos.x = x;
        ball.pos.y = y;
        objects.add(ball);
    }

    public void addRect(int x, int y, int width, int height) {
        PhysicsRect rect = new PhysicsRect(width, height, 0, nextId++);
        rect.pos.x = x;
        rect.pos.y = y;
        rect.elasticity = 0.8;
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

    // render recent collision debug info (contact points + normals)
    public void displayCollisionDebug(Graphics g) {
        Color old = g.getColor();
        g.setColor(Color.magenta);
        for (CollisionDebug cd : recentCollisions) {
            int x = (int) Math.round(cd.contactPoint.x);
            int y = (int) Math.round(cd.contactPoint.y);
            // contact point
            g.fillOval(x - 3, y - 3, 6, 6);
            // normal line
            int nx = (int) Math.round(x + cd.normal.x * 30);
            int ny = (int) Math.round(y + cd.normal.y * 30);
            g.drawLine(x, y, nx, ny);
            // penetration label
            g.drawString(String.format("%.2f", cd.penetration), x + 4, y - 4);
        }
        g.setColor(old);
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
