package physics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class PhysicsBall extends PhysicsObject {
    public int radius;
    public Ellipse2D.Float oval = new Ellipse2D.Float();

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
    public void draw(Graphics2D g, Vector2 offset, double scale) {
        int cx = (int) (pos.x + offset.x);
        int cy = (int) (pos.y + offset.y);
        int xi = cx - radius;
        int yi = cy - radius;
        int diam = radius * 2;

        oval.setFrame(xi * scale, yi * scale, diam * scale, diam * scale);

        g.setColor(displayColor);
        g.fill(oval);
        g.setColor(displayColorDarker);
        g.draw(oval);
    }

    @Override
    public void drawDebug(Graphics2D g, Vector2 offset, double scale) {
        int cx = (int) (pos.x + offset.x);
        int cy = (int) (pos.y + offset.y);
        int xi = cx - radius;
        int yi = cy - radius;
        int diam = radius * 2;

        oval.setFrame(xi * scale, yi * scale, diam * scale, diam * scale);

        if (sleeping) {
            g.setColor(displayColorDarker);
            g.fill(oval);
        } else {
            g.setColor(displayColor);
            g.fill(oval);
        }
        if (!supported) {
            g.setColor(Color.blue);
            g.draw(oval);
        } else {
            g.setColor(displayColorDarker);
            g.draw(oval);
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