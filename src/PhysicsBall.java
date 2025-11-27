package src;

import java.awt.Graphics;

public class PhysicsBall extends PhysicsObject {
    public int radius;
    public double elasticity;

    PhysicsBall(int radius, double elasticity, long id) {
        super(id);
        this.radius = radius;
        this.elasticity = elasticity;
    }

    @Override
    public void update(double gravity, double dt) {
        vel.y += gravity;

        pos.addLocal(vel.scale(dt));
        // friction
        vel.x *= 0.9;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(displayColor);
        g.fillOval((int) (pos.x - radius), (int) (pos.y - radius), radius * 2, radius * 2);
    }

    @Override
    public void addVelocity(Vector2 vel) {

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
