package src;

public class TestCollision {
    public static void main(String[] args) {
        // circle-circle
        PhysicsBall b1 = new PhysicsBall(10, 0.5, 1.0, 1);
        PhysicsBall b2 = new PhysicsBall(10, 0.5, 1.0, 2);
        b1.pos.set(100, 100);
        b2.pos.set(110, 100);

        Manifold m1 = Collision.circleCircle(b1, b2);
        System.out.println(
                "circleCircle: collided=" + m1.collided + " pen=" + m1.penetration + " normal=" + vec(m1.normal));
        assertTrue(m1.collided, "circle-circle should collide");
        assertApprox(m1.penetration, 10.0, 1e-6, "circle-circle penetration");
        assertApprox(m1.normal.length(), 1.0, 1e-6, "circle-circle normal is unit length");

        // circle-rect: circle to the right, touching
        PhysicsRect r = new PhysicsRect(40, 40, 0.0, 3);
        r.pos.set(150, 100);
        PhysicsBall b3 = new PhysicsBall(20, 0.5, 1.0, 4);
        b3.pos.set(170, 100);
        Manifold m2 = Collision.circleRect(b3, r);
        System.out.println("circleRect (right-touching): collided=" + m2.collided + " pen=" + m2.penetration
                + " normal=" + vec(m2.normal));
        assertTrue(m2.collided, "circle-rect (right-touching) should collide");
        assertTrue(Math.abs(m2.normal.x - 1.0) < 1e-6 || Math.abs(m2.normal.x + 1.0) < 1e-6,
                "circle-rect normal approx horizontal");

        // circle center inside rect
        PhysicsBall b4 = new PhysicsBall(10, 0.6, 1.0, 5);
        PhysicsRect r2 = new PhysicsRect(60, 40, 0.0, 6);
        r2.pos.set(300, 300);
        b4.pos.set(300, 300); // center inside
        Manifold m3 = Collision.circleRect(b4, r2);
        System.out.println("circleRect (inside center): collided=" + m3.collided + " pen=" + m3.penetration + " normal="
                + vec(m3.normal) + " contact0=" + (m3.contacts.size() > 0 ? vec(m3.contacts.get(0)) : "none"));
        assertTrue(m3.collided, "circle-rect (inside) should collide");
        assertTrue(Math.abs(m3.normal.y) > 0.5, "inside-case normal should point vertically in our test");

        // circle near top edge
        PhysicsBall b5 = new PhysicsBall(8, 0.4, 1.0, 7);
        b5.pos.set(300, 280); // slightly above center
        Manifold m4 = Collision.circleRect(b5, r2);
        System.out.println(
                "circleRect (top): collided=" + m4.collided + " pen=" + m4.penetration + " normal=" + vec(m4.normal));
        assertTrue(m4.collided, "circle-rect (top) should collide");

        // Now test collision resolution via PhysicsHandler
        PhysicsHandler handler = new PhysicsHandler(0, 0, 500, 500);
        handler.objects.clear(); // we'll add by hand for deterministic ids

        PhysicsBall A = new PhysicsBall(10, 0.5, 10.0, 1);
        PhysicsBall B = new PhysicsBall(10, 0.5, 10.0, 2);
        A.pos.set(100, 100);
        B.pos.set(110, 100);
        handler.objects.add(A);
        handler.objects.add(B);

        // direct collision resolution test — don't run full update (no gravity, no
        // friction)
        A.vel.set(50, 0);
        B.vel.set(-50, 0);

        System.out.println("Before direct resolve: A.vel=" + vec(A.vel) + " B.vel=" + vec(B.vel));
        handler.handleCollision(B, A); // handler expects o1,o2; choose o1=B, o2=A to trigger resolution
        System.out.println("After direct resolve: A.vel=" + vec(A.vel) + " B.vel=" + vec(B.vel));
        // with equal masses, after resolution head-on the velocity component along the
        // normal should be reduced
        assertTrue(Math.abs(A.vel.x) < 1e-6 && Math.abs(B.vel.x) < 1e-6,
                "direct resolve should remove head-on x velocity for equal masses");

        // Now repeat a full updatePhysics run (this includes gravity + friction)
        PhysicsHandler handler2 = new PhysicsHandler(0, 0, 500, 500);
        handler2.addBall(100, 100, 10, 0.5);
        handler2.addBall(110, 100, 10, 0.5);
        PhysicsObject C = handler2.objects.get(0);
        PhysicsObject D = handler2.objects.get(1);
        C.vel.set(50, 0);
        D.vel.set(-50, 0);
        System.out.println("Before full update: C.vel=" + vec(C.vel) + " D.vel=" + vec(D.vel));
        handler2.updatePhysics(1.0 / 60.0);
        System.out.println("After full update: C.vel=" + vec(C.vel) + " D.vel=" + vec(D.vel));
        // x velocities should be reduced by collisions + friction
        assertTrue(Math.abs(C.vel.x) < 50.0, "C.x should be reduced by collision/friction");
        assertTrue(Math.abs(D.vel.x) < 50.0, "D.x should be reduced by collision/friction");

        System.out.println("ALL TESTS PASSED");
    }

    static String vec(Vector2 v) {
        if (v == null)
            return "null";
        return String.format("(%.3f,%.3f)", v.x, v.y);
    }

    static void assertTrue(boolean cond, String msg) {
        if (!cond)
            throw new AssertionError(msg);
    }

    static void assertApprox(double a, double b, double tol, String msg) {
        if (Double.isNaN(a) || Math.abs(a - b) > tol) {
            throw new AssertionError(msg + " (expected ~" + b + " got " + a + ")");
        }
    }
}
