package physics.process;

import java.util.ArrayList;
import java.util.concurrent.locks.LockSupport;

import physics.objects.PhysicsObject;
import physics.structures.Chunk;
import physics.structures.Contact;
import physics.structures.Manifold;
import physics.structures.Vector2;

public class PhysicsUpdater implements Runnable {

    private PhysicsHandler handler;

    private static final float FIXED_DT = 1f / 60; // physics rate
    private static final long NANOS_PER_UPDATE = (long) (1_000_000_000 * FIXED_DT);

    private volatile boolean running = true;

    public java.util.HashSet<Long> processedPairs = new java.util.HashSet<>();
    public ArrayList<Manifold> frameManifolds = new ArrayList<>();

    // scratch temporaries to reduce per-frame allocations
    private final Vector2 _tmpA = new Vector2();
    private final Vector2 _tmpB = new Vector2();
    private final Vector2 _tmpC = new Vector2();

    public int POS_ITERS = 3;
    public int SOLVER_ITERS = 20;

    public double POSCORR_SLOP = 0.01; // allowance
    public double POSCORR_PERCENT = 0.1; // return
    public double MIN_VEL_FOR_RESTITUTION = 8.0;

    private int updates = 0;
    private long lastTime = System.nanoTime();
    private volatile int ups = 0;

    public void setHandler(PhysicsHandler handler) {
        this.handler = handler;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        long previous = System.nanoTime();
        long accumulator = 0L;

        while (running) {
            long now = System.nanoTime();
            long frameTime = now - previous;
            previous = now;
            accumulator += frameTime;

            // Cap to avoid spiral of death after long pause
            if (accumulator > NANOS_PER_UPDATE * 16)
                accumulator = NANOS_PER_UPDATE * 16;

            handler.proccessAditionsAndRemovals(); // add/remove objects in queue

            while (accumulator >= NANOS_PER_UPDATE) {
                fixedUpdate();
                accumulator -= NANOS_PER_UPDATE;
                updates++;
            }
            if (now - lastTime >= 1_000_000_000L) {
                ups = updates;
                updates = 0;
                lastTime = now;
            }

            handler.publishFrame(); // publish snapshot for rendering

            // sleep a tiny amount to reduce CPU usage (tunable)
            LockSupport.parkNanos(1_000_000L); // 1ms

        }

    }

    public int getUps() {
        return ups;
    }

    private void fixedUpdate() {
        ArrayList<PhysicsObject> objects = new ArrayList<>(handler.getUpdateObjects());
        int size = objects.size();
        ArrayList<PhysicsObject> dynamicObjects = handler.getUpdateObjects();
        int dynamicObjectsSize = dynamicObjects.size();

        synchronized (objects) {
            synchronized (dynamicObjects) {

                for (int i = 0; i < dynamicObjectsSize; i++) {
                    updateObjectsChunks(dynamicObjects.get(i));
                    updateObjectsSupportState(dynamicObjects.get(i));
                }
                for (int i = 0; i < size; i++) {
                    clearObjectsContacts(objects.get(i));
                }

                for (int i = 0; i < dynamicObjectsSize; i++) {
                    addGravity(dynamicObjects.get(i));
                }

                for (int i = 0; i < dynamicObjectsSize; i++) {
                    updateObjectsVelocities(dynamicObjects.get(i));
                }

                // check by pairs
                processedPairs.clear();
                proccessCollisionsByPairs(dynamicObjects);

                createPerObjectContacts();

                // small positional correction passes
                for (int p = 0; p < POS_ITERS; p++) {
                    for (Manifold m : frameManifolds)
                        positionalCorrection(m);
                }
                // iterative velocity solver
                for (int it = 0; it < SOLVER_ITERS; it++) {
                    for (Manifold m : frameManifolds)
                        resolveVelocityImpulse(m);
                }

                // release pooled Manifolds
                releaseManifolds();

                for (int i = 0; i < size; i++) {
                    objects.get(i).updateSleepState(); // +1 sleepFrames if vel == threshold
                    objects.get(i).update(FIXED_DT);
                }
            }

        }

    }

    private void updateObjectsChunks(PhysicsObject o) {

        int chunkDimension = handler.chunkDimension;

        int ncx = (int) (Math.floor(o.pos.x / chunkDimension));
        int ncy = (int) (Math.floor(o.pos.y / chunkDimension));

        // if (ncx == o.cx && ncy == o.cy)
        // return;

        // if the chunk changed, and the object is large, it will occupy different
        // chunks

        int[] occuppiedChunks = o.getOccuppiedChunks(chunkDimension);
        int minCx = occuppiedChunks[0];
        int maxCx = occuppiedChunks[1];
        int minCy = occuppiedChunks[2];
        int maxCy = occuppiedChunks[3];

        if (minCx == o.cMinCx && maxCx == o.cMaxCx && minCy == o.cMinCy && maxCy == o.cMaxCy)
            return;

        // remove from olds
        for (int cx = o.cMinCx; cx <= o.cMaxCx; cx++) {
            for (int cy = o.cMinCy; cy <= o.cMaxCy; cy++) {
                Chunk old = handler.getOrCreateChunk(cx, cy);
                if (old != null)
                    old.objects.remove(o);
            }
        }

        // add to news
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cy = minCy; cy <= maxCy; cy++) {
                Chunk old = handler.getOrCreateChunk(cx, cy);
                if (old != null)
                    old.objects.add(o);
            }
        }

        // update object chunk pos
        o.cx = ncx;
        o.cy = ncy;
        o.updateOccupiedChunks(occuppiedChunks);
    }

    private void updateObjectsSupportState(PhysicsObject o) {
        if (!o.stationary)
            o.updateSupportState();
    }

    private void clearObjectsContacts(PhysicsObject o) {

        if (o != null) {

            if (!o.sleeping) {
                for (Contact c : o.contacts) {
                    Contact.release(c);
                }
                o.contacts.clear();

            }
        }
    }

    private void addGravity(PhysicsObject o) {
        if (!o.stationary && !o.supported && !o.sleeping) {
            o.addForce(handler.gravity, FIXED_DT);
        }
    }

    private void updateObjectsVelocities(PhysicsObject o) {
        if (!o.stationary && !o.sleeping)
            o.integrateVelocity(FIXED_DT);
    }

    private void proccessCollisionsByPairs(ArrayList<PhysicsObject> objects) {
        // iterate objects
        for (PhysicsObject o1 : objects) {
            if (!o1.sleeping)
                for (int cx = o1.cMinCx; cx <= o1.cMaxCx; cx++) {
                    for (int cy = o1.cMinCy; cy <= o1.cMaxCy; cy++) {
                        Chunk ch = handler.getOrCreateChunk(cx, cy);
                        if (ch == null)
                            continue;
                        for (PhysicsObject o2 : ch.objects) {

                            long a = Math.min(o1.id, o2.id);
                            long b = Math.max(o1.id, o2.id);
                            long pairKey = (a << 32) | (b & 0xffffffffL);

                            if (processedPairs.contains(pairKey))
                                continue; // already handled this unordered pair in another chunk
                            processedPairs.add(pairKey);
                            Manifold m = o1.collide(o2); // normal o2 -> o1
                            if (m != null) {
                                if (m.collided) {
                                    frameManifolds.add(m);
                                } else {
                                    Manifold.release(m);
                                }
                            }
                        }
                    }
                }
        }
    }

    private void createPerObjectContacts() {

        for (Manifold m : frameManifolds) {
            canonicalizeNormal(m);
            m.o1.addContact(m.o2, m.normal, m.penetration);
            m.o2.addContact(m.o1, m.normal.scale(-1), m.penetration);
        }
    }

    private void canonicalizeNormal(Manifold m) {

        // Ensure manifold.normal points from o1 -> o2 (handleCollision expects this)
        if (m.normal == null || m.normal.lengthSquared() < 1e-9) {
            // fallback: use vector from o1 -> o2
            _tmpA.setSub(m.o2.pos, m.o1.pos);
            if (_tmpA.lengthSquared() < 1e-9) {
                // unresolvable direction; pick up
                m.normal = new Vector2(0, -1);
            } else {
                m.normal = new Vector2(_tmpA.x / _tmpA.length(), _tmpA.y / _tmpA.length());
            }
        } else {
            _tmpA.setSub(m.o2.pos, m.o1.pos); // vector from o1 to o2
            double dot = m.normal.dot(_tmpA);
            if (dot < 0) {
                // flip normal so it points from o1 to o2
                m.normal = m.normal.scale(-1.0);
            }
            // normalize to be safe
            m.normal.normalizeLocal();
        }

    }

    private void positionalCorrection(Manifold m) {
        PhysicsObject a = m.o1;
        PhysicsObject b = m.o2;
        double invA = a.invMass;
        double invB = b.invMass;
        double invSum = invA + invB;
        if (invSum == 0.0)
            return;

        double correctionMag = Math.max(m.penetration - POSCORR_SLOP, 0.0) / invSum * POSCORR_PERCENT;
        correctionMag = Math.min(correctionMag, Math.max(m.penetration * 0.5, 0.001));

        a.pos.subLocal(m.normal.scale(correctionMag * invA));
        b.pos.addLocal(m.normal.scale(correctionMag * invB));

    }

    private void resolveVelocityImpulse(Manifold m) {

        PhysicsObject a = m.o1, b = m.o2;
        double invA = a.invMass;
        double invB = b.invMass;
        double invSum = invA + invB;
        if (invSum == 0)
            return;

        // relative velocity
        _tmpA.setSub(b.vel, a.vel);
        Vector2 rv = _tmpA;
        double velAlongNormal = rv.dot(m.normal);

        // wake objects (use magnitude of relative speed so approaching or separating
        // wakes)
        a.wake(Math.abs(velAlongNormal), m.penetration);
        b.wake(Math.abs(velAlongNormal), m.penetration);

        // Normal impulse - use conservative minimum restitution between objects using
        // threshold velocity
        double e = Math.abs(velAlongNormal) < MIN_VEL_FOR_RESTITUTION ? 0.0
                : Math.min(1.0, Math.min(a.elasticity, b.elasticity));
        // System.out.println(velAlongNormal + " " + e);

        if (velAlongNormal > 0) {
            // objects separating — no normal impulse
        } else {
            double j = -(1.0 + e) * velAlongNormal;
            j /= invSum;

            // accumulate and clamp (optional), use m.accumulatedNormalImpulse
            double oldImpulse = m.accumulatedNormalImpulse;
            double newImpulse = oldImpulse + j;
            if (newImpulse < 0.0)
                newImpulse = 0;
            double appliedImpulse = newImpulse - oldImpulse;
            m.accumulatedNormalImpulse = newImpulse;

            _tmpB.setScale(m.normal, appliedImpulse);
            _tmpC.setScale(_tmpB, invA);
            a.vel.subLocal(_tmpC);
            _tmpC.setScale(_tmpB, invB);
            b.vel.addLocal(_tmpC);
        }

        // Friction (Coulomb)
        // recompute relative velocity after normal impulse applied

        _tmpA.setSub(b.vel, a.vel);
        rv = _tmpA;
        double rvDot = rv.dot(m.normal);
        _tmpB.setScale(m.normal, rvDot);
        _tmpC.setSub(rv, _tmpB);
        Vector2 tangent = _tmpC;
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

            _tmpA.setScale(tangent, appliedT);
            _tmpB.setScale(_tmpA, invA);
            a.vel.subLocal(_tmpB);
            _tmpB.setScale(_tmpA, invB);
            b.vel.addLocal(_tmpB);
        }
    }

    private void releaseManifolds() {
        for (Manifold m : frameManifolds) {
            Manifold.release(m);
        }
        frameManifolds.clear();
    }
}