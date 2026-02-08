package src;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import physics.objects.PhysicsBall;
import physics.objects.PhysicsObject;
import physics.process.NeoPhysicsHandler;
import physics.structures.Vector2;

public class SimCanvas extends Canvas implements Runnable {

    private int fps;
    private int frames;
    private long fpsTimer = System.nanoTime();

    private long lastTime;
    static final double FIXED_STEP = 1.0 / 60.0;
    private double accumulator = 0.0;

    private boolean running = true;
    private Dimension size = new Dimension(1000, 1000);

    private Vector2 mousePos = new Vector2();

    private NeoPhysicsHandler nhandler = new NeoPhysicsHandler();

    private boolean leftClick = false;
    private boolean rightClick = false;
    private boolean shift = false;
    private boolean debug = false;

    private double actionCooldown = 0.5;

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

    @Override
    public void addNotify() {
        super.addNotify(); // IMPORTANT

        if (getBufferStrategy() == null) {
            createBufferStrategy(2);
        }

        nhandler.beginUpdaterThread();

    }

    @Override
    public void removeNotify() {

        nhandler.stopUpdaterThread();

        super.removeNotify();
    }

    private void setUpSim() {
        nhandler.display.offset.set(0, 0);
        nhandler.display.offsetVel.set(0, 0);
        nhandler.display.offsetAccel = 10;
        nhandler.display.offsetFriction = 0.5;
        nhandler.display.followRadius = 0;
        nhandler.display.setScreenCenter(new Vector2(size.width, size.height).scaleLocal(0.5));

        nhandler.chunkDimension = 20;
        // floor
        nhandler.addRect(new Vector2(size.width / 2, size.height), size.width - 100, 100);
        // walls
        nhandler.addRect(new Vector2(100, 0), 50, size.height * 2);
        nhandler.addRect(new Vector2(size.width - 100, 0), 50, size.height * 2);

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
            place(dt);

            frames++;
            if (now - fpsTimer >= 1_000_000_000L) {
                fps = frames;
                frames = 0;
                fpsTimer = now;

                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.setTitle("Project | FPS: " + fps + " | Count: " + nhandler.getUpdateObjectsSnapshot().size());
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
                    if (debug) {
                        nhandler.displayChunkBorders(g, size.width, size.height);
                        nhandler.drawRecordedChunks(g, size.width, size.height, true);
                        nhandler.renderDebug(g);
                    } else {
                        nhandler.render(g);
                    }

                } finally {
                    g.dispose();
                }

            } while (bs.contentsRestored());

            bs.show();
            Toolkit.getDefaultToolkit().sync();

        } while (bs.contentsLost());

    }

    private void update(double dt) {
        nhandler.display.update(dt);
        place(dt);
    }

    private void place(double dt) {

        if (actionCooldown > 0) {
            actionCooldown -= dt;
        } else {
            actionCooldown = 0;

            if (leftClick) {
                if (shift) {
                    for (PhysicsObject o : nhandler.getUpdateObjectsSnapshot()) {
                        if (nhandler.display.getMapPos(mousePos).sub(o.pos)
                                .lengthSquared() < 5000) {
                            nhandler.removeObject(o);
                        }
                    }
                } else {
                    boolean allowed = true;
                    for (PhysicsObject o : nhandler.getUpdateObjectsSnapshot()) {
                        if (o.pos.sub(nhandler.display.getMapPos(mousePos))
                                .lengthSquared() < 100) {
                            allowed = false;
                        }
                    }
                    if (allowed) {
                        nhandler.addRect(nhandler.display.getMapPos(mousePos),
                                nhandler.chunkDimension,
                                nhandler.chunkDimension, 0.0, 0.0, true);
                        actionCooldown = 0.05;
                    }
                }
            }
            if (rightClick) {
                if (shift) {
                    for (PhysicsObject o : nhandler.getUpdateObjectsSnapshot()) {
                        if (nhandler.display.getMapPos(mousePos).sub(o.pos)
                                .lengthSquared() < 100) {
                            nhandler.removeObject(o);
                        }
                    }
                } else {
                    boolean allowed = true;
                    double mx = nhandler.display.getMapPos(mousePos).x;
                    double my = nhandler.display.getMapPos(mousePos).y;

                    int dx = (int) Math.floor(mx / nhandler.chunkDimension);
                    int dy = (int) Math.floor(my / nhandler.chunkDimension);

                    int x = (int) (dx * nhandler.chunkDimension + nhandler.chunkDimension / 2);
                    int y = (int) (dy * nhandler.chunkDimension + nhandler.chunkDimension / 2);

                    for (PhysicsObject o : nhandler.getUpdateObjectsSnapshot()) {
                        if (o.pos.x == x && o.pos.y == y) {
                            allowed = false;
                        }
                    }
                    if (allowed) {

                        nhandler.addRect(new Vector2(x, y), nhandler.chunkDimension,
                                nhandler.chunkDimension);
                        actionCooldown = 0.05;
                    }
                }
            }
        }
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                if (!shift) {
                    for (int i = 0; i < 5; i++) {
                        nhandler.addBall(nhandler.display.getMapPos(mousePos),
                                10,
                                0.8,
                                0.05);
                    }
                } else {
                    if (nhandler.display.mainObject != null)
                        nhandler.display.mainObject.setDisplayColor(Color.white);
                    PhysicsBall b = new PhysicsBall(10, 0.8, 0.05, 0);
                    b.pos = nhandler.display.getMapPos(mousePos);
                    b.setDisplayColor(Color.red);
                    nhandler.addObject(b);
                    nhandler.display.mainObject = b;
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                nhandler.addBall(nhandler.display.getMapPos(mousePos),
                        50,
                        0.8,
                        5,
                        Color.darkGray);
            }
            if (e.getKeyCode() == KeyEvent.VK_C) {
                for (PhysicsObject o : nhandler.getUpdateObjectsSnapshot()) {
                    nhandler.removeObject(o);
                }
                setUpSim();
            }
            if (e.getKeyCode() == KeyEvent.VK_X) {
                debug = !debug;
            }

            if (e.getKeyCode() == KeyEvent.VK_W) {
                nhandler.display.offsetVel.y += nhandler.display.offsetAccel * 100 * 1 / nhandler.display.scale;
            }
            if (e.getKeyCode() == KeyEvent.VK_S) {
                nhandler.display.offsetVel.y -= nhandler.display.offsetAccel * 100 * 1 / nhandler.display.scale;
            }
            if (e.getKeyCode() == KeyEvent.VK_A) {
                nhandler.display.offsetVel.x += nhandler.display.offsetAccel * 100 * 1 / nhandler.display.scale;
            }
            if (e.getKeyCode() == KeyEvent.VK_D) {
                nhandler.display.offsetVel.x -= nhandler.display.offsetAccel * 100 * 1 / nhandler.display.scale;
            }
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                shift = true;
            }

            if (e.getKeyCode() == KeyEvent.VK_UP) {
                nhandler.display.setScale(nhandler.display.scale / 0.8);
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                nhandler.display.setScale(nhandler.display.scale * 0.8);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                shift = false;
            }
        }

    }

    public class MyMouseAdapter extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == 1) {
                leftClick = true;
            }
            if (e.getButton() == 3) {
                rightClick = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == 1) {
                leftClick = false;
            }
            if (e.getButton() == 3) {
                rightClick = false;
            }
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