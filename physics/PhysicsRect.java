package physics;

import java.awt.Graphics;

public class PhysicsRect extends PhysicsObject {
    public int width;
    public int height;

    public PhysicsRect(int width, int height, double mass, long id) {
        super(id);
        this.width = width;
        this.height = height;
        this.mass = mass;
        this.elasticity = 0.0;
    }

    @Override
    public void update(double gravity, double dt) {

    }

    @Override
    public void draw(Graphics g) {
        g.setColor(displayColor);
        g.fillRect((int) (pos.x - width / 2), (int) (pos.y - height / 2), width, height);
    }

    public int[] getCorners() {
        /* Gets the corners of the shape [left, top, right, bottom] */
        int[] result = new int[4];

        result[0] = (int) (pos.x - width / 2);
        result[1] = (int) (pos.y - height / 2);
        result[2] = (int) (pos.x + width / 2);
        result[3] = (int) (pos.y + height / 2);

        return result;
    }

    @Override
    public int[] getOccuppiedChunks(int chunkDim) {
        int[] result = new int[4];
        int[] corners = getCorners();

        // corners = [left, top, right, bottom]
        int left = corners[0];
        int top = corners[1];
        int right = corners[2];
        int bottom = corners[3];

        // minCx, maxCx, minCy, maxCy
        result[0] = (int) Math.floor((double) left / chunkDim);
        result[1] = (int) Math.floor((double) right / chunkDim);
        result[2] = (int) Math.floor((double) top / chunkDim);
        result[3] = (int) Math.floor((double) bottom / chunkDim);

        return result;
    }

    @Override
    public Manifold collide(PhysicsObject other) {
        return other.collideWithRect(this);
    }

    // hooks for double dispatch
    @Override
    public Manifold collideWithCircle(PhysicsBall b) {
        return Collision.circleRect(b, this);
    }

    @Override
    public Manifold collideWithRect(PhysicsRect rect) {
        return Collision.rectRect(rect, this);
    }
}