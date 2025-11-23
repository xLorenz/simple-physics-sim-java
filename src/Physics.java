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
        private int chunkDimension = 25;
        
        public double getGravity(){return gravity;}
        public void setGravity(double gravity){this.gravity = gravity;}
        public int getChunkDimention(){return chunkDimension;}
        public void setChunkDimension(int dim){this.chunkDimension = dim;}

        Handler(){

        }


        public void updatePhysics(double dt) {
            for (PhysicsObject obj : objects) {
                obj.update(dt);
            }
        }
    }
    
    public class Chunk {
        private int[] coordinates = new int[2];
        private boolean populated;
        
    }

    abstract class PhysicsObject {
        
        public int[] pos = new int[2];

        PhysicsObject() {
            addObject(this);
        }

        public abstract void update(double dt);
        public abstract void draw(Graphics g);
        public abstract int getChunk();
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
        public int getChunk(){

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