package src;

public class Collision {

    public static Manifold circleCircle(PhysicsBall b1, PhysicsBall b2) {

        Vector2 distance = b1.pos.sub(b2.pos);
        int rSum = b1.radius + b2.radius;

        double distanceSqrd = distance.lengthSquared();
        if (distanceSqrd >= rSum * rSum)
            return Manifold.noCollision();

        double dist = Math.sqrt(distanceSqrd);

        Manifold m = new Manifold();
        m.collided = true;

        // same center
        if (dist == 0) {
            m.penetration = rSum;
            m.normal = new Vector2(1, 0);
            m.contacts.add(b1.pos);
        } else {
            m.penetration = rSum - dist;
            m.normal = distance.scale(1.0 / dist);
            m.contacts.add(b1.pos.add(m.normal.scale(b1.radius - m.penetration * 0.5)));
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
            return Manifold.noCollision();
        }

        Manifold m = new Manifold();
        m.collided = true;

        // if circle is exactly on the border ( closest == center )
        // choose the nearest rectangle face as the separation direction
        if (distSqrd == 0.0) {
            // dist from circle center to each side
            int[] sides = r.getCorners();

            double dl = Math.abs(b.pos.x - sides[0]);
            double dt = Math.abs(sides[1] - b.pos.y);
            double dr = Math.abs(sides[2] - b.pos.x);
            double db = Math.abs(b.pos.y - sides[3]);

            // choose smalles distance
            double min = dl;
            Vector2 normal = new Vector2(-1, 0); // left default

            if (dr < min) {// nearest right, normal left
                min = dr;
                normal.set(1, 0);
            }

            if (db < min) {// nearest bottom, normal down
                min = db;
                normal.set(0, -1);
            }
            if (dt < min) {// nearest top, normal up
                min = dt;
                normal.set(0, 1);
            }

            // penetration, when center inside, move center to outide + radius
            m.penetration = b.radius + min;
            m.normal = normal;
            // aproximate contact point as the point on the rect face nearest the circ
            Vector2 contactPoint = new Vector2(
                    Math.max(r.pos.x - halfW, Math.min(b.pos.x, r.pos.x + halfW)),
                    Math.max(r.pos.y - halfH, Math.min(b.pos.y, r.pos.y + halfH)));
            m.contacts.add(contactPoint);
            return m;
        }

        // normal from closest point to circ
        double dist = Math.sqrt(distSqrd);
        m.penetration = b.radius - dist;
        m.normal = distanceToClosestPoint.scale(1.0 / dist);
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
            return Manifold.noCollision();
        double overlapY = Math.min(r1yMax, r2yMax) - Math.max(r1yMin, r2yMin);
        if (overlapY <= 0)
            return Manifold.noCollision();

        Manifold m = new Manifold();
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
