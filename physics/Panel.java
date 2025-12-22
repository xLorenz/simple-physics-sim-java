package physics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Panel extends JPanel implements ActionListener {

    // set display dimensions
    static final int SCR_WIDTH = 1000;
    static final int SCR_HEIGHT = 800;

    boolean running = false;
    Timer timer;
    Random random;

    long lastTime; // used to get delta time
    double dt; // delta time
    final double FIXED_STEP = 1.0 / 60.0; // 60 Hz physics
    double accumulator = 0.0;

    PhysicsHandler handler;

    // constructor
    Panel() {
        random = new Random();
        handler = new PhysicsHandler(0, 0, SCR_WIDTH, SCR_HEIGHT);
        handler.addRect(SCR_WIDTH / 2, SCR_HEIGHT - 100, SCR_WIDTH, 60);
        handler.addRect(100, SCR_HEIGHT / 2, 60, 750);
        handler.addRect(900, SCR_HEIGHT / 2, 60, 750);
        handler.addBall(SCR_WIDTH / 2, SCR_HEIGHT / 2, 100, 0);

        this.setPreferredSize(new Dimension(SCR_WIDTH, SCR_HEIGHT));
        this.setBackground(new Color(12, 13, 20));
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        startSimulation();
    }

    public void startSimulation() {
        running = true;
        lastTime = System.nanoTime();
        timer = new Timer(16, this);
        timer.start();

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // antialias
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        draw(g);
    }
    // ----------------------------------------//

    public void draw(Graphics g) {
        handler.displayChunkBorders(g, SCR_WIDTH, SCR_HEIGHT);
        handler.displayObjects(g);
        // collision debug overlay
        // handler.displayCollisionDebug(g);
    }

    // ----------------------------------------//

    public void update() {
        long now = System.nanoTime();
        dt = (now - lastTime) / 1_000_000_000.0; // seconds
        lastTime = now;
        dt = Math.min(dt, 0.25); // avoid huge jumps
        // accumulate and run fixed-step updates for deterministic physics
        accumulator += dt;
        while (accumulator >= FIXED_STEP) {
            for (int i = 0; i < 3; i++) {
                handler.updatePhysics(FIXED_STEP); // implement physics update using fixed step
                accumulator -= FIXED_STEP;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            update(); // update physics
        }
        // render once per frame (use the remaining fractional time if needed)
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_SPACE:
                    handler.addBall(100 + random.nextInt(800), 100 + random.nextInt(200),
                            10, 0.8);
                    break;

                default:
                    break;
            }
        }
    }

}
