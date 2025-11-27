package src;

import java.awt.Graphics;

public class PhysicsBall extends PhysicsObject {
    private int radius;
    private double elasticity;

    PhysicsBall(int radius, double elasticity, long id) {
        super(id);
        this.radius = radius;
        this.elasticity = elasticity;
    }

    @Override
    public void update(double gravity, double dt) {
        vy += gravity;
        x += vx * dt;
        y += vy * dt;
        // friction
        vx *= 0.9;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(displayColor);
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    @Override
    public void addVelocity(double vx, double vy) {

    }

    @Override
    public int[] getOccuppiedChunks(int chunkDim) {

        int[] result = new int[4];
        result[0] = (int) Math.floor((x - radius) / chunkDim);
        result[1] = (int) Math.floor((x + radius) / chunkDim);
        result[2] = (int) Math.floor((y - radius) / chunkDim);
        result[3] = (int) Math.floor((y + radius) / chunkDim);

        return result;
    }

}
