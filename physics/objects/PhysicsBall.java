package physics.objects;

import java.awt.Color;

import physics.collisions.Collision;
import physics.process.BatchRenderer;
import physics.structures.Manifold;

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
    public void draw(BatchRenderer renderer) {
        renderer.setFill(displayColor, 255);
        renderer.drawCircle(pos, radius);
        renderer.setFill(displayColor.darker(), 255);
        renderer.drawCircunference(pos, radius);
    }

    @Override
    public void drawDebug(BatchRenderer renderer) {

        if (sleeping) {
            renderer.setFill(displayColor.darker(), 255);
            renderer.drawCircle(pos, radius);
        } else {
            renderer.setFill(displayColor, 255);
            renderer.drawCircle(pos, radius);
        }
        if (!supported) {
            renderer.setFill(Color.blue, 255);
            renderer.drawCircunference(pos, radius);
        } else {
            renderer.setFill(displayColor.darker(), 255);
            renderer.drawCircunference(pos, radius);
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