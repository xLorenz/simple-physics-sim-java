package physics.structures;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Vector2 {
    public static Random rand = new Random();
    public double x, y;

    public Vector2() {
        this(0, 0);
    }

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2 random(int xMin, int xMax, int yMin, int yMax) {
        if (xMin > xMax) {
            int t = xMin;
            xMin = xMax;
            xMax = t;
        }
        if (yMin > yMax) {
            int t = yMin;
            yMin = yMax;
            yMax = t;
        }

        int x = ThreadLocalRandom.current().nextInt(xMin, xMax + 1);
        int y = ThreadLocalRandom.current().nextInt(yMin, yMax + 1);

        return new Vector2(x, y);

    }

    public static Vector2 random(double xMin, double xMax, double yMin, double yMax) {
        if (xMin > xMax) {
            double t = xMin;
            xMin = xMax;
            xMax = t;
        }
        if (yMin > yMax) {
            double t = yMin;
            yMin = yMax;
            yMax = t;
        }

        double x = ThreadLocalRandom.current().nextDouble(xMin, xMax);
        double y = ThreadLocalRandom.current().nextDouble(yMin, yMax);

        return new Vector2(x, y);
    }

    public static Vector2 random(int[] xBounds, int[] yBounds) {
        if (xBounds == null || yBounds == null || xBounds.length != 2 || yBounds.length != 2)
            throw new IllegalArgumentException("Bounds arrays must have exactly 2 elements");

        return random(xBounds[0], xBounds[1], yBounds[0], yBounds[1]);
    }

    public static Vector2 random(Vector2 corner1, Vector2 corner2) {
        return random(corner1.x, corner2.x, corner1.y, corner2.y);
    }

    public static Vector2 random(Vector2 center, double minRadius, double maxRadius) {
        double radius = ThreadLocalRandom.current().nextDouble(minRadius, maxRadius);
        double angle = ThreadLocalRandom.current().nextDouble(0, 360);
        return center.add(new Vector2(1, 0).rotate(angle).scale(radius));
    }

    public static Vector2 random(Vector2 center, double radius) {
        double angle = ThreadLocalRandom.current().nextDouble(0, 360);
        return center.add(new Vector2(1, 0).rotate(angle).scale(radius));
    }

    public Vector2 set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2 set(Vector2 o) {
        this.x = o.x;
        this.y = o.y;
        return this;
    }

    public Vector2 add(Vector2 o) {
        return new Vector2(x + o.x, y + o.y);
    }

    public Vector2 sub(Vector2 o) {
        return new Vector2(x - o.x, y - o.y);
    }

    public void addLocal(Vector2 o) {
        x += o.x;
        y += o.y;
    }

    public void subLocal(Vector2 o) {
        x -= o.x;
        y -= o.y;
    }

    public Vector2 scale(double s) {

        // scale both components by s
        return new Vector2(x * s, y * s);
    }

    public Vector2 scaleLocal(double s) {
        x *= s;
        y *= s;
        return this;
    }

    public double dot(Vector2 o) {
        return x * o.x + y * o.y;
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public double lengthSquared() {
        return x * x + y * y;
    }

    public Vector2 normalizeLocal() {
        double len = length();
        if (len != 0) {
            x /= len;
            y /= len;
        }
        return this;
    }

    public Vector2 normalize() {
        Vector2 normal = new Vector2(this.x, this.y);
        double len = normal.length();
        if (len != 0) {
            normal.x /= len;
            normal.y /= len;
        }
        return normal;
    }

    public Vector2 perpLocal() {
        double oldX = x;
        x = -y;
        y = oldX;
        return this;
    }

    public Vector2 rotate(double angleDegrees) {
        double angle = Math.toRadians(angleDegrees);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double newX = x * cos - y * sin;
        double newY = x * sin + y * cos;

        return new Vector2(newX, newY);
    }

    public void rotateLocal(double angleDegrees) {
        double angle = Math.toRadians(angleDegrees);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double dx = x * cos - y * sin;
        double dy = x * sin + y * cos;
        set(dx, dy);

    }

    // non-allocating setters to avoid temporaries
    public Vector2 setCopy(Vector2 a) {
        this.x = a.x;
        this.y = a.y;
        return this;
    }

    public Vector2 setSub(Vector2 a, Vector2 b) {
        this.x = a.x - b.x;
        this.y = a.y - b.y;
        return this;
    }

    public Vector2 setScale(Vector2 a, double s) {
        this.x = a.x * s;
        this.y = a.y * s;
        return this;
    }

    public Vector2 setPerp(Vector2 a) {
        this.x = -a.y;
        this.y = a.x;
        return this;
    }

    public Vector2 round() {
        return new Vector2((int) x, (int) y);
    }

    public void roundLocal() {
        x = (int) x;
        y = (int) y;
    }

    public void print() {
        System.out.println("x: " + x + "; y: " + y);
    }

    public String getString() {
        return (String) ("x: " + x + "; y: " + y);
    }

    public static ArrayList<Vector2> traceRay(
            double x0, double y0,
            double x1, double y1,
            int tileSize) {

        ArrayList<Vector2> list = new ArrayList<>();

        int x = (int) Math.floor(x0 / tileSize);
        int y = (int) Math.floor(y0 / tileSize);

        int endX = (int) Math.floor(x1 / tileSize);
        int endY = (int) Math.floor(y1 / tileSize);

        double dx = x1 - x0;
        double dy = y1 - y0;

        int stepX = (dx > 0) ? 1 : -1;
        int stepY = (dy > 0) ? 1 : -1;

        double tMaxX, tMaxY;
        double tDeltaX, tDeltaY;

        if (dx != 0) {
            double nextGridX = (stepX > 0 ? (x + 1) * tileSize : x * tileSize);
            tMaxX = (nextGridX - x0) / dx;
            tDeltaX = tileSize / Math.abs(dx);
        } else {
            tMaxX = Double.POSITIVE_INFINITY;
            tDeltaX = Double.POSITIVE_INFINITY;
        }
        if (dy != 0) {
            double nextGridY = (stepY > 0 ? (x + 1) * tileSize : y * tileSize);
            tMaxY = (nextGridY - y0) / dy;
            tDeltaY = tileSize / Math.abs(dy);
        } else {
            tMaxY = Double.POSITIVE_INFINITY;
            tDeltaY = Double.POSITIVE_INFINITY;
        }

        // traverse grid
        while (true) {
            list.add(new Vector2(x, y));

            if (x == endX && y == endY)
                break;

            if (tMaxX < tMaxY) {
                tMaxX += tDeltaX;
                x += stepX;
            } else {
                tMaxY += tDeltaY;
                y += stepY;
            }
        }

        return list;
    }

    public static ArrayList<Vector2> drawTileLine(
            int x0, int y0,
            int x1, int y1) {
        ArrayList<Vector2> list = new ArrayList<>();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;

        while (true) {
            list.add(new Vector2(x0, y0));

            if (x0 == x1 && y0 == y1)
                break;

            int e2 = 2 * err;

            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }

            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }

        return list;
    }

}