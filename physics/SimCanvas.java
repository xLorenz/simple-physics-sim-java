package physics;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class SimCanvas extends Canvas implements Runnable {

    private int fps;
    private int frames;
    private long fpsTimer = System.nanoTime();

    private long lastTime;
    private double dt;
    static final double FIXED_STEP = 1.0 / 60.0;
    private double accumulator = 0.0;

    private boolean running = true;
    private Dimension size = new Dimension(1000, 800);

    private Vector2 mousePos = new Vector2();

    private PhysicsHandler handler = new PhysicsHandler((int) (size.width * 0.2), (int) (size.height * 0.2),
            (int) (size.width * 0.8), (int) (size.height * 0.8));

    private Random random = new Random();

    private static final RenderingHints HINTS = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

    static {
        HINTS.put(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
    }

    public SimCanvas() {

        this.setPreferredSize(size);
        this.setIgnoreRepaint(true);
        this.setBackground(new Color(12, 13, 20));
        this.setFocusable(true);

        this.addKeyListener(new MyKeyAdapter());
        this.addMouseListener(new MyMouseAdapter());
        this.addMouseMotionListener(new MyMouseMotionAdapter());

        setUpSim();
    }

    private void setUpSim() {
        handler.chunkDimension = 25;
        handler.anchorFollowVelocity = 100;
        handler.anchorFollowFriction = 0.5;

        // floor
        handler.addRect(size.width / 2, size.height, size.width - 100, 100);
        // walls
        handler.addRect(100, size.height / 2, 50, size.height * 2);
        handler.addRect(size.width - 100, size.height / 2, 50, size.height * 2);

    }

    @Override
    public void run() {

        while (running) {
            long now = System.nanoTime();
            double dt = (now - lastTime) * 1e-9f; // seconds
            lastTime = now;
            dt = Math.min(dt, 0.25); // avoid large jump

            accumulator += dt;
            while (accumulator >= FIXED_STEP) {
                update(FIXED_STEP);
                accumulator -= FIXED_STEP;
            }

            render();

            frames++;
            if (now - fpsTimer >= 1_000_000_000L) {
                fps = frames;
                frames = 0;
                fpsTimer = now;

                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.setTitle("Project | FPS: " + fps + " | Count: " + handler.objects.size());
            }
        }
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null)
            return;

        do {
            do {
                Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                try {
                    g.addRenderingHints(HINTS);

                    g.setColor(getBackground());
                    g.fillRect(0, 0, size.width, size.height);

                    // draw game

                    handler.displayChunkBorders(g, size.width, size.height);
                    // handler.drawRecordedChunks(g);
                    handler.displayObjects(g);
                    // collision debug overlay
                    // handler.displayCollisionDebug(g);

                } finally {
                    g.dispose();
                }

            } while (bs.contentsRestored());

            bs.show();
            Toolkit.getDefaultToolkit().sync();

        } while (bs.contentsLost());

    }

    private void update(double dt) {
        handler.updatePhysics(dt);
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                for (int i = 0; i < 5; i++) {
                    handler.addBall((int) (mousePos.x - handler.mapAnchor.x), (int) (mousePos.y - handler.mapAnchor.y),
                            5,
                            0.5);
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                PhysicsBall b = new PhysicsBall(50, 0.2, 5, 0);
                b.pos.set((int) (mousePos.x - handler.mapAnchor.x), (int) (mousePos.y - handler.mapAnchor.y));
                b.displayColor = Color.darkGray;
                handler.addBall(b);
            }
            if (e.getKeyCode() == KeyEvent.VK_W) {
                handler.mapAnchorVelocity.y += handler.anchorFollowVelocity;
            }
            if (e.getKeyCode() == KeyEvent.VK_S) {
                handler.mapAnchorVelocity.y -= handler.anchorFollowVelocity;
            }
            if (e.getKeyCode() == KeyEvent.VK_A) {
                handler.mapAnchorVelocity.x += handler.anchorFollowVelocity;
            }
            if (e.getKeyCode() == KeyEvent.VK_D) {
                handler.mapAnchorVelocity.x -= handler.anchorFollowVelocity;
            }
        }

    }

    public class MyMouseAdapter extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                // System.out.println("Double click at " + e.getX() + "," + e.getY());
            }
        }
    }

    public class MyMouseMotionAdapter extends MouseMotionAdapter {

        @Override
        public void mouseMoved(MouseEvent e) {
            mouseMovedOrDragged(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mouseMovedOrDragged(e);
        }

        private void mouseMovedOrDragged(MouseEvent e) {

            mousePos.set(e.getX(), e.getY());
            // Optionally get global screen position:
            // Point screenPoint = e.getLocationOnScreen();
            // System.out.println("Screen pos: " + screenPoint.x + "," + screenPoint.y);
        }
    }
}