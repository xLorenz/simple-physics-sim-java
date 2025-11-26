package src;

import java.awt.Color;
import java.awt.Graphics;

abstract class PhysicsObject {

    public int[] pos = new int[2];
    public double[] vel = new double[2];
    public Color displayColor = Color.red;
    private CollisionListener collisionListener = null;

    PhysicsObject() {
    }

    public void notifyListener(PhysicsObject o) {
        if (collisionListener != null) {
            collisionListener.action(o);
        }
    }

    public void setListener(CollisionListener l) {
        collisionListener = l;
    }

    public abstract void addVelocity(double[] v);

    public abstract void update(double dt);

    public abstract void draw(Graphics g);

}
