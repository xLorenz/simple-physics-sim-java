package physics.process;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import physics.structures.Vector2;

public class BatchRenderer {

    private Graphics2D g;
    private NeoPhysicsHandler pHandler;
    private final Ellipse2D.Float circle = new Ellipse2D.Float();
    private final Path2D.Float polygon = new Path2D.Float();
    private final Rectangle2D.Float rect = new Rectangle2D.Float();

    public void setHandler(NeoPhysicsHandler p) {
        this.pHandler = p;
    }

    public void setGraphics(Graphics2D g) {
        this.g = g;
    }

    public void setFill(Color c, int alpha) {
        if (g == null)
            return;
        int a = Math.max(0, Math.min(255, alpha));
        Color color = new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(color);
    }

    public void drawCircle(Vector2 pos, int radius) {
        if (g == null)
            return;

        double scale = pHandler.display.scale;
        double xi = ((pos.x + pHandler.display.offset.x) - radius) * scale;
        double yi = ((pos.y + pHandler.display.offset.y) - radius) * scale;
        double diam = radius * 2 * scale;

        circle.setFrame(xi, yi, diam, diam);

        g.fill((Shape) circle);

    }

    public void drawCircunference(Vector2 pos, int radius) {
        if (g == null)
            return;

        double scale = pHandler.display.scale;
        double xi = ((pos.x + pHandler.display.offset.x) - radius) * scale;
        double yi = ((pos.y + pHandler.display.offset.y) - radius) * scale;
        double diam = radius * 2 * scale;

        circle.setFrame(xi, yi, diam, diam);

        g.draw((Shape) circle);

    }

    public void drawRect(Vector2 center, double w, double h) {
        if (g == null)
            return;

        double scale = pHandler.display.scale;

        double xi = (center.x - w / 2 + pHandler.display.offset.x) * scale;
        double yi = (center.y - h / 2 + pHandler.display.offset.y) * scale;

        rect.setFrame(xi, yi, w * scale, h * scale);
        g.fill(rect);
    }

    public void drawRectOutline(Vector2 center, double w, double h) {
        if (g == null)
            return;

        double scale = pHandler.display.scale;

        double xi = (center.x - w / 2 + pHandler.display.offset.x) * scale;
        double yi = (center.y - h / 2 + pHandler.display.offset.y) * scale;

        rect.setFrame(xi, yi, w * scale, h * scale);
        g.draw(rect);
    }

    public void drawSquare(Vector2 pos, double w) {
        if (g == null)
            return;

        double scale = pHandler.display.scale;

        double xi = (pos.x + pHandler.display.offset.x) * scale;
        double yi = (pos.y + pHandler.display.offset.y) * scale;

        rect.setFrame(xi, yi, w * scale, w * scale);
        g.fill(rect);
    }

    public void drawTriangle(Vector2 a, Vector2 b, Vector2 c) {
        if (g == null)
            return;

        double scale = pHandler.display.scale;

        double ax = (a.x + pHandler.display.offset.x) * scale;
        double ay = (a.y + pHandler.display.offset.y) * scale;

        double bx = (b.x + pHandler.display.offset.x) * scale;
        double by = (b.y + pHandler.display.offset.y) * scale;

        double cx = (c.x + pHandler.display.offset.x) * scale;
        double cy = (c.y + pHandler.display.offset.y) * scale;

        polygon.reset();
        polygon.moveTo(ax, ay);
        polygon.lineTo(bx, by);
        polygon.lineTo(cx, cy);
        polygon.closePath();

        g.fill(polygon);
    }

    public void drawPolygon(Vector2[] verts, int count) {
        if (g == null || count < 3)
            return;

        double scale = pHandler.display.scale;

        polygon.reset();

        double x0 = (verts[0].x + pHandler.display.offset.x) * scale;
        double y0 = (verts[0].y + pHandler.display.offset.y) * scale;
        polygon.moveTo(x0, y0);

        for (int i = 1; i < count; i++) {
            double xi = (verts[i].x + pHandler.display.offset.x) * scale;
            double yi = (verts[i].y + pHandler.display.offset.y) * scale;
            polygon.lineTo(xi, yi);
        }

        polygon.closePath();
        g.fill(polygon);
    }

    public void drawPolygonOutline(Vector2[] verts, int count) {
        if (g == null || count < 3)
            return;

        double scale = pHandler.display.scale;

        polygon.reset();

        double x0 = (verts[0].x + pHandler.display.offset.x) * scale;
        double y0 = (verts[0].y + pHandler.display.offset.y) * scale;
        polygon.moveTo(x0, y0);

        for (int i = 1; i < count; i++) {
            double xi = (verts[i].x + pHandler.display.offset.x) * scale;
            double yi = (verts[i].y + pHandler.display.offset.y) * scale;
            polygon.lineTo(xi, yi);
        }

        polygon.closePath();
        g.draw(polygon);
    }

    public void end() {
        this.g = null;
    }

}