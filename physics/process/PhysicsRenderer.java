package physics.process;

import java.awt.Graphics2D;

public class PhysicsRenderer {

    private NeoPhysicsHandler handler;
    private Graphics2D graphics;

    public PhysicsRenderer() {

    }

    public void setHandler(NeoPhysicsHandler handler) {
        this.handler = handler;
    }

    public void setGraphics(Graphics2D g) {
        this.graphics = g;
    }
}