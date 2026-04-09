package physics.process;

import physics.objects.PhysicsObject;
import physics.structures.Vector2;

public class Display {
    // handles mapAnchor, mapAnchor updates and zoom

    public Vector2 offset = new Vector2();
    public double scale = 1.0;

    public PhysicsObject mainObject = null;
    public Vector2 offsetVel = new Vector2();
    public double offsetAccel = 200;
    public double offsetFriction = 0.99;
    public int followRadius = 0;

    public int renderDistance = 32; // Render distance in chunks

    private Vector2 _tmp = new Vector2();

    private Vector2 screenCenter = new Vector2();

    public Display() {
    }

    public void update(double dt) {
        // move camera
        offset.addLocal(offsetVel.scale(dt));

        if (mainObject != null) {
            _tmp.setSub(mainObject.pos.scale(scale).add(offset.scale(scale)), screenCenter);
            double diff = _tmp.length();
            if (diff > followRadius) {
                offsetVel.subLocal(_tmp.scale(offsetAccel));
            }
        }

        // friction
        offsetVel.scaleLocal(offsetFriction);

        if (offsetVel.lengthSquared() < 1e-10)
            offsetVel.set(0, 0);

    }

    public void setScale(double newScale) {
        if (newScale <= 0 || scale == newScale) {
            return;
        }

        offset.addLocal(screenCenter.scale(1.0 / newScale - 1.0 / scale));

        scale = newScale;
    }

    public int[] getMainObjectViewDistanceBorders() {
        if (mainObject == null) {
            int[] nul = { 0, 0, 0, 0 };
            return nul;
        }

        int[] borders = {
                mainObject.cx - renderDistance, // viewMinCx
                mainObject.cx + renderDistance, // viewMaxCx
                mainObject.cy - renderDistance, // viewMinCy
                mainObject.cy + renderDistance, // viewMaxCy
        };

        return borders;
    }

    public int[] getMainObjectViewDistanceBorders(int distance) {
        if (mainObject == null) {
            int[] nul = { 0, 0, 0, 0 };
            return nul;
        }

        int[] borders = {
                mainObject.cx - distance, // viewMinCx
                mainObject.cx + distance, // viewMaxCx
                mainObject.cy - distance, // viewMinCy
                mainObject.cy + distance, // viewMaxCy
        };

        return borders;
    }

    public boolean inViewDistance(PhysicsObject o, int[] borders) {
        return o.cMaxCx >= borders[0] &&
                o.cMinCx <= borders[1] &&
                o.cMaxCy >= borders[2] &&
                o.cMinCy <= borders[3];
    }

    public void setScreenCenter(Vector2 center) {
        this.screenCenter = center;
    }

    public void setMainObject(PhysicsObject o) {
        this.mainObject = o;
    }

    public Vector2 getOffset() {
        return offset;
    }

    public double getDisplayScale() {
        return scale;
    }

    public PhysicsObject getMainObject() {
        return mainObject;
    }

    public Vector2 getMapPos(Vector2 screenPos) {
        return screenPos.sub(offset.scale(scale)).scale(1 / scale);
    }

}
