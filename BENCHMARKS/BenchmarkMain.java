package BENCHMARKS;

import physics.*;
import java.util.Random;

// Simple microbenchmark: headless simulation stepping
public class BenchmarkMain {
    public static void main(String[] args) {
        int objects = 200; // number of balls
        int steps = 2000; // measured steps
        int warmup = 200;
        double dt = 1.0 / 60.0;

        PhysicsHandler handler = new PhysicsHandler(1000, 1000);
        handler.chunkDimension = 20;
        Random r = new Random(123);

        for (int i = 0; i < objects; i++) {
            PhysicsBall b = new PhysicsBall(5, 0.3, 1.0, 0);
            b.pos.set(r.nextInt(800), r.nextInt(800));
            handler.addObject(b);
        }
        // force addQueue processing
        handler.proccessAditionsAndRemovals();

        // warmup
        for (int i = 0; i < warmup; i++)
            handler.updatePhysics(dt);

        long t0 = System.nanoTime();
        for (int i = 0; i < steps; i++)
            handler.updatePhysics(dt);
        long t1 = System.nanoTime();

        double totalSec = (t1 - t0) * 1e-9;
        System.out.println("objects=" + objects + " steps=" + steps + " totalSec=" + totalSec + " avg_ms_per_step="
                + (totalSec * 1e3 / steps));
    }
}
