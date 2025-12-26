package physics;

import java.awt.Color;
import java.awt.Graphics;

public class PhysicsBall extends PhysicsObject {
    public int radius;

    public PhysicsBall(int radius, double elasticity, double mass, long id) {
        super(id);
        this.radius = radius;
        this.elasticity = elasticity;
        this.mass = mass;
        this.invMass = getInverseMass();
    }

    @Override
    public void update(double dt) {
    }

    @Override
    public void draw(Graphics g, Vector2 offset) {
        double x = pos.x + offset.x;
        double y = pos.y + offset.y;

        g.setColor(displayColor);
        g.fillOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
        g.setColor(displayColor.darker());
        g.drawOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
        if (sleeping) {

            g.setColor(displayColor.darker());
            g.fillOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
        }
        if (!supported) {

            g.setColor(Color.green);
            g.drawOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
        }
    }

    @Override
    public int[] getOccuppiedChunks(int chunkDim) {

        int[] result = new int[4];
        result[0] = (int) Math.floor((pos.x - radius) / chunkDim);
        result[1] = (int) Math.floor((pos.x + radius) / chunkDim);
        result[2] = (int) Math.floor((pos.y - radius) / chunkDim);
        result[3] = (int) Math.floor((pos.y + radius) / chunkDim);

        return result;
    }

    @Override
    public Manifold collide(PhysicsObject other) {
        return other.collideWithCircle(this);
    }

    // hooks for double dispatch
    @Override
    public Manifold collideWithCircle(PhysicsBall b) {
        return Collision.circleCircle(b, this);
    }

    @Override
    public Manifold collideWithRect(PhysicsRect rect) {
        return Collision.circleRect(this, rect);
    }

}