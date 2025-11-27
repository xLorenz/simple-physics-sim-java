package src;

import java.awt.Graphics;

public class PhysicsRect extends PhysicsObject {
    public int width;
    public int height;

    PhysicsRect(int width, int height, long id) {
        super(id);
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(double gravity, double dt) {

    }

    @Override
    public void addVelocity(Vector2 vel) {

    }

    @Override
    public void draw(Graphics g) {
        g.setColor(displayColor);
        int[] c = getCorners();
        g.fillRect(c[0], c[1], width, height);
    }

    public int[] getCorners() {
        /* Gets the corners of the shape [left, top, right, bottom] */
        int[] result = new int[4];

        result[0] = (int) (pos.x - width / 2);
        result[2] = (int) (pos.y - height / 2);
        result[1] = (int) (pos.x + width / 2);
        result[3] = (int) (pos.y + height / 2);

        return result;
    }

    @Override
    public int[] getOccuppiedChunks(int chunkDim) {
        int[] result = new int[4];
        int[] corners = getCorners();

        result[0] = corners[0] / chunkDim;
        result[1] = corners[1] / chunkDim;
        result[2] = corners[2] / chunkDim;
        result[3] = corners[3] / chunkDim;

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