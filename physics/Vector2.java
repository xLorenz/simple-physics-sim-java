package physics;

public class Vector2 {
    public double x, y;

    public Vector2() {
        this(0, 0);
    }

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
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

        x = x * cos - y * sin;
        y = x * sin + y * cos;

    }

}
