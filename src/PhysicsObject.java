package src;

import java.awt.Color;
import java.awt.Graphics;

abstract class PhysicsObject {

    protected int[] pos = new int[2];
    protected Color displayColor = Color.red;

    PhysicsObject() {
    }

    public abstract void update(double dt);

    public abstract void draw(Graphics g);

    public abstract int getChunk();

    public void setDisplayColor(Color color) {
        this.displayColor = color;
    }
}
