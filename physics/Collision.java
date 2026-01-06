package physics;

public class Collision {
    private static final double EPSILON = 1e-6;

    public static Manifold circleCircle(PhysicsBall b1, PhysicsBall b2) {

        Vector2 distance = b1.pos.sub(b2.pos); // b2 -> b1
        double rSum = b1.radius + b2.radius;

        double distanceSqrd = distance.lengthSquared();
        if (distanceSqrd >= rSum * rSum)
            return null;

        double dist = Math.sqrt(distanceSqrd);

        Manifold m = Manifold.obtain();
        m.o1 = b1;
        m.o2 = b2;
        m.collided = true;

        // same center or extremely close — pick a stable normal
        if (dist < EPSILON) {
            m.penetration = rSum;
            m.normal = new Vector2(1, 0);
            m.contacts.add(b1.pos);
        } else {
            m.penetration = rSum - dist;
            m.normal = distance.scale(1.0 / dist);
            m.contacts.add(b2.pos.add(m.normal.scale(b2.radius - m.penetration * 0.5)));
        }

        // ensure normal is normalized
        if (m.normal != null) {
            m.normal.normalizeLocal();
        }

        return m;
    }

    public static Manifold circleRect(PhysicsBall b, PhysicsRect r) {

        double halfW = r.width / 2;
        double halfH = r.height / 2;

        // vetor from rect to circ
        Vector2 d = b.pos.sub(r.pos);

        // clamp d to the rectangle extents
        double clampedX = clamp(d.x, -halfW, halfW);
        double clampedY = clamp(d.y, -halfH, halfH);

        Vector2 closestPoint = r.pos.add(new Vector2(clampedX, clampedY));

        // from closest point to circle
        Vector2 distanceToClosestPoint = b.pos.sub(closestPoint);
        double distSqrd = distanceToClosestPoint.lengthSquared();
        double rSqrd = b.radius * b.radius;

        if (distSqrd > rSqrd) {
            return null;
        }

        Manifold m = Manifold.obtain();
        m.o1 = b;
        m.o2 = r;
        m.collided = true;

        boolean insideX = Math.abs(clampedX - d.x) < EPSILON;
        boolean insideY = Math.abs(clampedY - d.y) < EPSILON;

        // if circle is exactly on the border ( closest == center )
        // choose the nearest rectangle face as the separation direction
        if (insideX && insideY) {

            double distLeft = Math.abs(d.x + halfW);
            double distTop = Math.abs(d.y + halfH);
            double distRight = Math.abs(halfW - d.x);
            double distBottom = Math.abs(halfH - d.y);

            // choose smallest distance to a face — that'll be the separation direction
            double minDist = distLeft;
            Vector2 normal = new Vector2(-1, 0);

            if (distRight < minDist) {
                minDist = distRight;
                normal = new Vector2(1, 0);
            }
            if (distTop < minDist) {
                minDist = distTop;
                normal = new Vector2(0, -1);
            }
            if (distBottom < minDist) {
                minDist = distBottom;
                normal = new Vector2(0, 1);
            }

            // penetration magnitude when center is inside the rect
            m.penetration = b.radius + minDist;
            m.normal = normal;
            // approximate contact point as the point on the rect face nearest the circle
            Vector2 contactPoint = r.pos.add(normal.scale(-minDist));
            m.contacts.add(contactPoint);
            // make sure normal is unit length
            return m;
        }

        // normal from closest point to circ
        double dist = Math.sqrt(distSqrd);
        // defensive guard for tiny distances
        if (dist < EPSILON) {
            // fallback: point from rect center to circle center
            Vector2 fallback = b.pos.sub(r.pos);
            if (fallback.lengthSquared() < EPSILON) {
                // choose up
                m.normal = new Vector2(0, -1);
            } else {
                m.normal = fallback.scale(1.0 / fallback.length());
            }
            m.penetration = b.radius;
        } else {
            m.penetration = b.radius - dist;
            m.normal = distanceToClosestPoint.scale(1.0 / dist);
        }
        // normalize
        if (m.normal != null)
            m.normal.normalizeLocal();
        m.contacts.add(closestPoint);

        return m;
    }

    public static Manifold rectRect(PhysicsRect r1, PhysicsRect r2) {
        int[] sides1 = r1.getCorners();
        int[] sides2 = r2.getCorners();

        double r1xMin = (double) sides1[0];
        double r1yMin = (double) sides1[1];
        double r1xMax = (double) sides1[2];
        double r1yMax = (double) sides1[3];

        double r2xMin = (double) sides2[0];
        double r2yMin = (double) sides2[1];
        double r2xMax = (double) sides2[2];
        double r2yMax = (double) sides2[3];

        double overlapX = Math.min(r1xMax, r2xMax) - Math.max(r1xMin, r2xMin);
        if (overlapX <= 0)
            return null;
        double overlapY = Math.min(r1yMax, r2yMax) - Math.max(r1yMin, r2yMin);
        if (overlapY <= 0)
            return null;

        Manifold m = Manifold.obtain();
        m.o1 = r1;
        m.o2 = r2;
        m.collided = true;

        if (overlapX < overlapY) {
            m.penetration = overlapX;
            m.normal = new Vector2(r1.pos.x < r2.pos.x ? -1 : 1, 0);
        } else {
            m.penetration = overlapY;
            m.normal = new Vector2(0, r1.pos.y < r2.pos.y ? -1 : 1);
        }

        // contact approximation
        m.contacts.add(new Vector2((Math.max(r1xMin, r2xMin) + Math.min(r1xMax, r2xMax)) / 2,
                (Math.max(r1yMin, r2yMin) + Math.min(r1yMax, r2yMax)) / 2));

        return m;
    }

    private static double clamp(double val, double min, double max) {
        if (val < min)
            return min;
        if (val > max)
            return max;
        return val;
    }

}
