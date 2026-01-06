package src;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import physics.PhysicsBall;
import physics.PhysicsHandler;
import physics.PhysicsObject;
import physics.Vector2;

public class SimCanvas extends Canvas implements Runnable {

    private int fps;
    private int frames;
    private long fpsTimer = System.nanoTime();

    private long lastTime;
    private double dt;
    static final double FIXED_STEP = 1.0 / 60.0;
    private double accumulator = 0.0;

    private boolean running = true;
    private Dimension size = new Dimension(1000, 1000);

    private Vector2 mousePos = new Vector2();

    private PhysicsHandler handler = new PhysicsHandler(size.width, size.height);

    private Random random = new Random();

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

    private void setUpSim() {
        handler.mapAnchor.set(0, 0);
        handler.mapAnchorVelocity.set(0, 0);
        handler.chunkDimension = 20;
        handler.anchorFollowVelocity = 10;
        handler.anchorFollowFriction = 0.0;
        handler.anchorFollowRadius = 0;

        // floor
        handler.addRect(new Vector2(size.width / 2, size.height), size.width - 100, 100);
        // walls
        handler.addRect(new Vector2(100, 0), 50, size.height * 2);
        handler.addRect(new Vector2(size.width - 100, 0), 50, size.height * 2);

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
                    if (debug) {
                        handler.displayChunkBorders(g, size.width, size.height);
                        handler.drawRecordedChunks(g, true);
                        handler.displayObjectsDebug(g);
                    } else {
                        handler.displayObjects(g);
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
        handler.updatePhysics(dt);
    }

    private void place(double dt) {

        if (actionCooldown > 0) {
            actionCooldown -= dt;
        } else {
            actionCooldown = 0;

            if (leftClick) {
                if (shift) {
                    for (PhysicsObject o : handler.objects) {
                        if (handler.getMapPos(mousePos).sub(o.pos)
                                .lengthSquared() < 5000) {
                            handler.removeObject(o);
                        }
                    }
                } else {
                    boolean allowed = true;
                    for (PhysicsObject o : handler.objects) {
                        if (o.pos.sub(handler.getMapPos(mousePos))
                                .lengthSquared() < 100) {
                            allowed = false;
                        }
                    }
                    if (allowed) {
                        handler.addRect(handler.getMapPos(mousePos),
                                handler.chunkDimension,
                                handler.chunkDimension, 0.0, 0.0, true);
                        actionCooldown = 0.05;
                    }
                }
            }
            if (rightClick) {
                if (shift) {
                    for (PhysicsObject o : handler.objects) {
                        if (handler.getMapPos(mousePos).sub(o.pos)
                                .lengthSquared() < 100) {
                            handler.removeObject(o);
                        }
                    }
                } else {
                    boolean allowed = true;
                    double mx = handler.getMapPos(mousePos).x;
                    double my = handler.getMapPos(mousePos).y;

                    int dx = (int) Math.floor(mx / handler.chunkDimension);
                    int dy = (int) Math.floor(my / handler.chunkDimension);

                    int x = (int) (dx * handler.chunkDimension + handler.chunkDimension / 2);
                    int y = (int) (dy * handler.chunkDimension + handler.chunkDimension / 2);

                    for (PhysicsObject o : handler.objects) {
                        if (o.pos.x == x && o.pos.y == y) {
                            allowed = false;
                        }
                    }
                    if (allowed) {

                        handler.addRect(new Vector2(x, y), handler.chunkDimension,
                                handler.chunkDimension);
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
                        handler.addBall(handler.getMapPos(mousePos),
                                10,
                                0.8,
                                0.05);
                    }
                } else {
                    if (handler.mainObject != null)
                        handler.mainObject.setDisplayColor(Color.white);
                    PhysicsBall b = new PhysicsBall(10, 0.8, 0.05, 0);
                    b.pos = handler.getMapPos(mousePos);
                    b.setDisplayColor(Color.red);
                    handler.addObject(b);
                    handler.mainObject = b;
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                handler.addBall(handler.getMapPos(mousePos),
                        50,
                        0.8,
                        5,
                        Color.darkGray);
            }
            if (e.getKeyCode() == KeyEvent.VK_C) {
                for (PhysicsObject o : handler.objects) {
                    handler.removeObject(o);
                }
                setUpSim();
            }
            if (e.getKeyCode() == KeyEvent.VK_X) {
                debug = !debug;
            }

            if (e.getKeyCode() == KeyEvent.VK_W) {
                handler.mapAnchorVelocity.y += handler.anchorFollowVelocity * 100 * 1 / handler.displayScale;
            }
            if (e.getKeyCode() == KeyEvent.VK_S) {
                handler.mapAnchorVelocity.y -= handler.anchorFollowVelocity * 100 * 1 / handler.displayScale;
            }
            if (e.getKeyCode() == KeyEvent.VK_A) {
                handler.mapAnchorVelocity.x += handler.anchorFollowVelocity * 100 * 1 / handler.displayScale;
            }
            if (e.getKeyCode() == KeyEvent.VK_D) {
                handler.mapAnchorVelocity.x -= handler.anchorFollowVelocity * 100 * 1 / handler.displayScale;
            }
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                shift = true;
            }

            if (e.getKeyCode() == KeyEvent.VK_UP) {
                handler.displayScale /= 0.8;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                handler.displayScale *= 0.8;
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