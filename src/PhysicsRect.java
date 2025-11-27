package src;

import java.awt.Graphics;

public class PhysicsRect extends PhysicsObject {
    private int width;
    private int height;

    PhysicsRect(int width, int height, long id) {
        super(id);
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(double gravity, double dt) {

    }

    @Override
    public void addVelocity(double vx, double vy) {

    }

    @Override
    public void draw(Graphics g) {
        g.setColor(displayColor);
        g.fillRect(x, y, width, height);
    }

    public int[] getCenter() {
        int[] center = new int[2];
        center[0] = x + width / 2;
        center[1] = y + height / 2;
        return center;
    }

    @Override
    public int[] getOccuppiedChunks(int chunkDim) {
        int[] result = new int[4];
        result[0] = (int) x / chunkDim;
        result[1] = (int) (x + width) / chunkDim;
        result[2] = (int) y / chunkDim;
        result[3] = (int) (y + height) / chunkDim;

        return result;
    }
}