package physics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class PhysicsRect extends PhysicsObject {
    public int width;
    public int height;
    public Rectangle2D.Float rect = new Rectangle2D.Float();

    public PhysicsRect(int width, int height, double mass, long id) {
        super(id);
        this.width = width;
        this.height = height;
        this.mass = mass;
        this.elasticity = 1.0;
        this.stationary = true;
        this.invMass = getInverseMass();
    }

    @Override
    public void update(double dt) {

    }

    @Override
    public void draw(Graphics2D g, Vector2 offset, double scale) {
        int cx = (int) (pos.x + offset.x);
        int cy = (int) (pos.y + offset.y);
        int xi = cx - width / 2;
        int yi = cy - height / 2;

        rect.setFrame(xi * scale, yi * scale, width * scale, height * scale);

        g.setColor(displayColor);
        g.fill(rect);

        g.setColor(displayColorDarker);
        g.draw(rect);
    }

    @Override
    public void drawDebug(Graphics2D g, Vector2 offset, double scale) {
        int cx = (int) (pos.x + offset.x);
        int cy = (int) (pos.y + offset.y);
        int xi = cx - width / 2;
        int yi = cy - height / 2;

        rect.setFrame(xi * scale, yi * scale, width * scale, height * scale);

        if (!sleeping) {
            g.setColor(displayColor);
        } else {
            g.setColor(displayColorDarker);
        }

        g.fill(rect);

        if (!supported) {
            g.setColor(Color.green);
        } else {
            g.setColor(displayColorDarker);
        }

        g.draw(rect);
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
        result[0] = (int) Math.floor((double) left / chunkDim) - 1;
        result[1] = (int) Math.floor((double) right / chunkDim) + 1;
        result[2] = (int) Math.floor((double) top / chunkDim) - 1;
        result[3] = (int) Math.floor((double) bottom / chunkDim) + 1;

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