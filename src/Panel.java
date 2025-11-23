package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Panel extends JPanel implements ActionListener {

    // set display dimensions
    static final int SCR_WIDTH = 950;
    static final int SCR_HEIGHT = 750;

    boolean running = false;
    Timer timer;
    Random random;

    long lastTime; // used to get delta time
    double dt; // delta time
    final double FIXED_STEP = 1.0 / 60.0; // 60 Hz physics
    double accumulator = 0.0;

    Physics p;
    Physics.Handler handler;

    // constructor
    Panel() {
        random = new Random();
        p = new Physics();
        handler = p.new Handler();

        this.setPreferredSize(new Dimension(SCR_WIDTH, SCR_HEIGHT));
        this.setBackground(new Color(12, 13, 20));
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        startSimulation();
    }

    public void startSimulation() {
        lastTime = System.nanoTime();
        timer = new Timer(16, this);
        timer.start();

    }
    // ----------------------------------------//

    public void updatePhysics(double step) {
        handler.updatePhysics(step);
    }

    public void drawObjects(Graphics g) {
        for(Physics.PhysicsObject o : p.getObjects()){
            o.draw(g);
        }
    }

    // ----------------------------------------//

    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        dt = (now - lastTime) / 1_000_000_000.0; // seconds
        lastTime = now;
        dt = Math.min(dt, 0.25); // avoid huge jumps
        // accumulate and run fixed-step updates for deterministic physics
        accumulator += dt;
        while (accumulator >= FIXED_STEP) {
            updatePhysics(FIXED_STEP); // implement physics update using fixed step
            accumulator -= FIXED_STEP;
        }

        // render once per frame (use the remaining fractional time if needed)
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {

    }

}
