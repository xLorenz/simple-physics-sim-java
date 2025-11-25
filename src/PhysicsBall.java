package src;

import java.awt.Graphics;

public class PhysicsBall extends PhysicsObject {
    private int radius;
    private double elasticity;

    PhysicsBall(int radius, double elasticity) {
        super();
        this.radius = radius;
        this.elasticity = elasticity;
    }

    @Override
    public void update(double dt) {
    }

    @Override
    public int getChunk() {
        return 0;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(displayColor);
        g.drawOval(pos[0] + radius / 2, pos[0] + radius / 2, radius, radius);
    }
}
