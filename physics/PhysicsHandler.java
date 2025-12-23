package physics;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicsHandler {

    public Vector2 gravity = new Vector2(0, 980);

    public Boundary boundaries;

    public int chunkDimension = 25; // pixels
    public Vector2 mapAnchor = new Vector2(); // map anchor controls the position from where the world is rendered, it
                                              // doesnt affect chunk calculations
    public Vector2 mapAnchorVelocity = new Vector2();
    public Vector2 mapAnchorVelocityScaled = new Vector2();
    public PhysicsObject mainObject = null; // the main object is what the camera "follows"
    public double anchorFollowVelocity = 200;
    public double anchorFollowFriction = 0.99;

    public Map<Long, Chunk> chunks = new HashMap<>();
    public List<PhysicsObject> objects = new ArrayList<>();
    public List<PhysicsObject> addQueue = new ArrayList<>();
    public List<PhysicsObject> removeQueue = new ArrayList<>();

    public List<Manifold> frameManifolds = new ArrayList<>();

    public static int POS_ITERS = 2;
    public static int SOLVER_ITERS = 20;

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

    public PhysicsHandler(int right, int top, int left, int bottom) {
        this.boundaries = new Boundary(right, top, left, bottom);
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
        this.boundaries = new Boundary(left, right, top, bottom);
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
        int ncx = (int) (Math.floor(o.pos.x / chunkDimension));
        int ncy = (int) (Math.floor(o.pos.y / chunkDimension));

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

        proccessAditionsAndRemovals();

        // update chunks and clear contacts
        for (PhysicsObject o : objects) {
            updateObjectsChunk(o);
            o.contacts.clear();
        }

        // update objects positions and velocities
        for (PhysicsObject o : objects) {
            if (!o.stationary && !o.supported)
                o.addForce(gravity, dt);
        }

        for (PhysicsObject o : objects) {
            if (!o.stationary)
                o.integrateVelocity(dt);
        }

        // check by pairs
        java.util.HashSet<Long> processedPairs = new java.util.HashSet<>();
        recentCollisions.clear();

        // iterate objects
        for (PhysicsObject o1 : objects) {
            for (int cx = o1.cMinCx; cx <= o1.cMaxCx; cx++) {
                for (int cy = o1.cMinCy; cy <= o1.cMaxCy; cy++) {
                    Chunk ch = chunks.get(keyFor(cx, cy));
                    if (ch == null)
                        continue;
                    for (PhysicsObject o2 : ch.objects) {
                        if (o1.id <= o2.id)
                            continue;
                        // check unordered pair only once
                        long a = Math.min(o1.id, o2.id);
                        long b = Math.max(o1.id, o2.id);
                        long pairKey = (a << 32) | (b & 0xffffffffL);

                        if (processedPairs.contains(pairKey))
                            continue; // already handled this unordered pair in another chunk
                        processedPairs.add(pairKey);
                        Manifold m = o1.collide(o2); // normal o2 -> o1
                        if (m.collided) {
                            frameManifolds.add(m);
                        }
                    }
                }
            }
        }
        // canonicalize normals create per-object contacts
        for (Manifold m : frameManifolds) {
            canonicalizeNormal(m);
            m.o1.addContact(m.o2, m.normal, m.penetration);
            m.o2.addContact(m.o1, m.normal.scale(-1), m.penetration);
        }

        // small positional correction passes
        for (int p = 0; p < POS_ITERS; p++) {
            for (Manifold m : frameManifolds)
                positionalCorrection(m);
        }

        // warm start
        for (Manifold m : frameManifolds)
            warmStart(m);

        // iterative velocity solver
        for (int it = 0; it < SOLVER_ITERS; it++) {
            for (Manifold m : frameManifolds)
                resolveVelocityImpulse(m);
        }

        frameManifolds.clear();

        for (PhysicsObject o : objects) {
            if (!o.stationary)
                o.updateSupportState();
            o.update(dt);
            // o.pos.addLocal(mapAnchorVelocityScaled); // camera
        }

        updateAnchor(dt);
    }

    public void canonicalizeNormal(Manifold m) {

        // Ensure manifold.normal points from o1 -> o2 (handleCollision expects this)
        if (m.normal == null || m.normal.lengthSquared() < 1e-9) {
            // fallback: use vector from o1 -> o2
            Vector2 dir = m.o2.pos.sub(m.o1.pos);
            if (dir.lengthSquared() < 1e-9) {
                // unresolvable direction; pick up
                m.normal = new Vector2(0, -1);
            } else {
                m.normal = dir.scale(1.0 / dir.length());
            }
        } else {
            Vector2 dir = m.o2.pos.sub(m.o1.pos); // vector from o1 to o2
            double dot = m.normal.dot(dir);
            if (dot < 0) {
                // flip normal so it points from o1 to o2
                m.normal = m.normal.scale(-1.0);
            }
            // normalize to be safe
            m.normal.normalizeLocal();
        }

    }

    public void positionalCorrection(Manifold m) {
        PhysicsObject a = m.o1;
        PhysicsObject b = m.o2;
        double invA = (a.mass == 0.0) ? 0.0 : 1.0 / a.mass;
        double invB = (b.mass == 0.0) ? 0.0 : 1.0 / b.mass;
        double invSum = invA + invB;
        if (invSum == 0.0)
            return;

        double slop = 0.01;
        double percent = 0.2;
        double correctionMag = Math.max(m.penetration - slop, 0.0) / invSum * percent;
        correctionMag = Math.min(correctionMag, Math.max(m.penetration * 0.5, 0.001));

        Vector2 corr = m.normal.scale(correctionMag);
        a.pos.subLocal(corr.scale(invA));
        b.pos.addLocal(corr.scale(invB));
    }

    public void warmStart(Manifold m) {
        if (m.accumulatedNormalImpulse == 0 && m.accumulatedTangentImpulse == 0)
            return;
        PhysicsObject a = m.o1, b = m.o2;
        double invA = (a.mass == 0) ? 0 : 1.0 / a.mass;
        double invB = (b.mass == 0) ? 0 : 1.0 / b.mass;

        // normal impulse
        Vector2 Pn = m.normal.scale(m.accumulatedNormalImpulse);
        a.vel.subLocal(Pn.scale(invA));
        b.vel.addLocal(Pn.scale(invB));

        // tangent impulse
        Vector2 tangent = new Vector2(-m.normal.y, m.normal.x);
        Vector2 Pt = tangent.scale(m.accumulatedTangentImpulse);
        a.vel.subLocal(Pt.scale(invA));
        b.vel.addLocal(Pt.scale(invB));
    }

    public void resolveVelocityImpulse(Manifold m) {
        PhysicsObject a = m.o1, b = m.o2;
        double invA = (a.mass == 0) ? 0 : 1.0 / a.mass;
        double invB = (b.mass == 0) ? 0 : 1.0 / b.mass;
        double invSum = invA + invB;
        if (invSum == 0)
            return;

        Vector2 n = m.normal;

        // relative velocity
        Vector2 rv = b.vel.sub(a.vel);
        double velAlongNormal = rv.dot(n);

        // Normal impulse
        double e = (a.elasticity + b.elasticity < 1.0) ? a.elasticity + b.elasticity : 1.0;
        if (velAlongNormal > 0) {
            // objects separating — no normal impulse
        } else {
            double j = -(1.0 + e) * velAlongNormal;
            j /= invSum;

            // accumulate and clamp (optional), use m.accumulatedNormalImpulse
            double oldImpulse = m.accumulatedNormalImpulse;
            double newImpulse = oldImpulse + j;
            // if you want to clamp to non-negative:
            if (newImpulse < 0)
                newImpulse = 0;
            double appliedImpulse = newImpulse - oldImpulse;
            m.accumulatedNormalImpulse = newImpulse;

            Vector2 P = n.scale(appliedImpulse);
            a.vel.subLocal(P.scale(invA));
            b.vel.addLocal(P.scale(invB));
        }

        // Friction (Coulomb)
        // recompute relative velocity after normal impulse applied
        rv = b.vel.sub(a.vel);
        Vector2 tangent = rv.sub(n.scale(rv.dot(n)));
        double tLen2 = tangent.lengthSquared();
        if (tLen2 > 1e-9) {
            tangent.normalizeLocal();
            double jt = -rv.dot(tangent);
            jt /= invSum;

            // approximate friction coefficient
            double mu = Math.sqrt(a.friction * b.friction); // combine mu
            double maxFriction = mu * m.accumulatedNormalImpulse;

            double oldT = m.accumulatedTangentImpulse;
            double newT = oldT + jt;
            // clamp
            if (Math.abs(newT) > maxFriction) {
                newT = Math.signum(newT) * maxFriction;
            }
            double appliedT = newT - oldT;
            m.accumulatedTangentImpulse = newT;

            Vector2 Pt = tangent.scale(appliedT);
            a.vel.subLocal(Pt.scale(invA));
            b.vel.addLocal(Pt.scale(invB));
        }
    }

    public void updateAnchor(double dt) {
        // move camera
        mapAnchor.addLocal(mapAnchorVelocityScaled); // move anchor

        if (mainObject != null) {
            // update anchor velocity
            if (mainObject.pos.x < boundaries.left) {
                mapAnchorVelocity.x += anchorFollowVelocity * dt;
            }
            if (mainObject.pos.x > boundaries.right) {
                mapAnchorVelocity.x -= anchorFollowVelocity * dt;
            }
            if (mainObject.pos.y < boundaries.top) {
                mapAnchorVelocity.y += anchorFollowVelocity * dt;
            }
            if (mainObject.pos.y > boundaries.bottom) {
                mapAnchorVelocity.y -= anchorFollowVelocity * dt;
            }
        }

        // friction and scaling
        if (mapAnchorVelocity.lengthSquared() > 0.00000001) {
            mapAnchorVelocityScaled = mapAnchorVelocity.scale(dt);
            mapAnchorVelocityScaled.round();
            mapAnchorVelocity.scaleLocal(anchorFollowFriction);
        } else {
            mapAnchorVelocityScaled.set(0, 0);
            mapAnchorVelocity.set(0, 0);
        }
    }

    public void proccessAditionsAndRemovals() {

        // first, process any pending additions/removals queued from other threads
        synchronized (addQueue) {
            if (!addQueue.isEmpty()) {
                for (PhysicsObject o : addQueue) {
                    objects.add(o);
                }
                addQueue.clear();
            }
        }
        synchronized (removeQueue) {
            if (!removeQueue.isEmpty()) {
                for (PhysicsObject o : removeQueue) {
                    objects.remove(o);
                    // also remove from any chunks the object occupied
                    for (int cx = o.cMinCx; cx <= o.cMaxCx; cx++) {
                        for (int cy = o.cMinCy; cy <= o.cMaxCy; cy++) {
                            Chunk ch = chunks.get(keyFor(cx, cy));
                            if (ch == null)
                                continue;
                            ch.objects.remove(o);
                        }
                    }
                }
                removeQueue.clear();
            }
        }

    }

    public void addBall(int x, int y, int radius, double elasticity) {
        PhysicsBall ball = new PhysicsBall(radius, elasticity, 0.01, 0);
        ball.pos.x = x;
        ball.pos.y = y;
        synchronized (addQueue) {
            ball.id = nextId++;
            addQueue.add(ball);
        }
    }

    public void addBall(PhysicsBall ball) {
        synchronized (addQueue) {
            if (!addQueue.contains(ball) && !objects.contains(ball)) {
                ball.id = nextId++;
                addQueue.add(ball);
            }
        }
    }

    public void addRect(int x, int y, int width, int height) {
        PhysicsRect rect = new PhysicsRect(width, height, 0, 0);
        rect.pos.x = x;
        rect.pos.y = y;
        rect.elasticity = 0.0;
        synchronized (addQueue) {
            rect.id = nextId++;
            addQueue.add(rect);
        }
    }

    public void removeObject(PhysicsObject o) {
        synchronized (removeQueue) {
            if (!removeQueue.contains(o))
                removeQueue.add(o);
        }
    }

    public void displayObjects(Graphics g) {
        // iterate over a snapshot to avoid ConcurrentModificationException if objects
        // are
        // mutated from another thread
        java.util.List<PhysicsObject> snapshot;
        synchronized (objects) {
            snapshot = new ArrayList<>(objects);
        }
        for (PhysicsObject o : snapshot) {
            o.draw(g, mapAnchor);
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
        // draw anchor
        g.setColor(Color.blue);
        g.drawOval((int) mapAnchor.x - 5, (int) mapAnchor.y - 5, 10, 10);
        // draw grid
        g.setColor(Color.gray);
        for (int i = 0; i < scrWidth / chunkDimension; i++) {
            g.drawLine((i * chunkDimension) + (int) mapAnchor.x, 0, (i * chunkDimension) + (int) mapAnchor.x,
                    scrHeight); // vertical
        }
        for (int i = 0; i < scrHeight / chunkDimension; i++) {
            g.drawLine(0, (i * chunkDimension) + (int) mapAnchor.y, scrWidth, (i * chunkDimension) + (int) mapAnchor.y); // horizontal
        }
        // drawRecordedChunks(g, true);
    }

    public void drawRecordedChunks(Graphics g, boolean fillActiveChunks) {
        for (Map.Entry<Long, Chunk> entry : chunks.entrySet()) {
            g.setColor(Color.yellow);
            long key = entry.getKey();
            Chunk chunk = entry.getValue();

            int cx = (int) (key >> 32);
            int cy = (int) key;

            // convert chunk coordinates to world coordinates
            int worldY = cy * chunkDimension + (int) mapAnchor.y;
            int worldX = cx * chunkDimension + (int) mapAnchor.x;

            g.drawRect(worldX, worldY, chunkDimension, chunkDimension);

            if (fillActiveChunks) {
                g.setColor(Color.green);
                if (!chunk.objects.isEmpty()) {
                    g.setColor(Color.green);
                    g.fillRect(worldX, worldY, chunkDimension, chunkDimension);
                }
            }
        }
    }
}