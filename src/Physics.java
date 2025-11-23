package src;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class Physics {

    private List<PhysicsObject> objects = new ArrayList<>();
    

    Physics() {

    }

    public List<PhysicsObject> getObjects(){
        return objects;
    }
    public void addObject(PhysicsObject o) {
        objects.add(o);
    }
    public void removeObject(PhysicsObject o) {
        if (objects.contains(o)){
            objects.remove(o);
        }
    }

    public class Handler {
        private double gravity = 9.8;
        
        public double getGravity(){return gravity;}
        public void setGravity(double gravity){this.gravity = gravity;}

        Handler(){

        }

        public void updatePhysics(double dt) {
            for (PhysicsObject obj : objects) {
                obj.update(dt);
            }
        }
    }

    abstract class PhysicsObject {
        
        public int[] pos = new int[2];

        PhysicsObject() {
            addObject(this);
        }

        public abstract void update(double dt);
        public abstract void draw(Graphics g);
    }

    public class PhysicsBall extends PhysicsObject {
        private int radius;
        private double elasticity;
        private Color displayColor = Color.red;

        PhysicsBall(int radius, double elasticity) {
            super();
            this.radius = radius;
            this.elasticity = elasticity;
        }
        public void setDisplayColor (Color color) {
            this.displayColor = color;
        }

        @Override
        public void update(double dt) {
            
        }

        @Override
        public void draw(Graphics g) {
            g.setColor(displayColor);
            g.drawOval(pos[0] + radius/2, pos[0] +radius/2, radius, radius);
        }
    }

    public class PhysicsRect extends PhysicsObject {
        @Override
        public void update(double dt) {

        }
        @Override
        public void draw(Graphics g) {

        }
    }
}