package src;

import java.awt.Graphics;

public class PhysicsRect extends PhysicsObject {
    private int width;
    private int height;

    PhysicsRect(int width, int height) {
        super();
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(double dt) {

    }

    @Override
    public void addVelocity(double[] v) {

    }

    @Override
    public void draw(Graphics g) {
        g.setColor(displayColor);
        g.drawRect(pos[0], pos[1], width, height);
    }

    public int[] getCenter() {
        int[] center = new int[2];
        center[0] = pos[0] + width / 2;
        center[1] = pos[1] + height / 2;
        return center;
    }
}