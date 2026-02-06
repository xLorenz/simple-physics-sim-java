package physics.process;

import physics.objects.PhysicsObject;
import physics.structures.Vector2;

public class PhysicsDisplay {
    // handles mapAnchor, mapAnchor updates and zoom
    private Vector2 displayOffset = new Vector2();
    private double displayScale = 1.0;

    private PhysicsObject mainObject = null;
    private Vector2 displayOffsetVel = new Vector2();
    private double displayOffsetAccel = 200;
    private double displayOffsetFriction = 0.99;
    private int displayFollowRadius = 0;

    private Vector2 screenCenter = new Vector2();

    public void updateMapOffset(double dt) {

    }

    public void setScreenCenter(Vector2 center) {
        this.screenCenter = center;
    }

    public void setMainObject(PhysicsObject o) {
        this.mainObject = o;
    }

    public Vector2 getOffset() {
        return displayOffset;
    }

    public double getDisplayScale() {
        return displayScale;
    }

    public PhysicsObject getMainObject() {
        return mainObject;
    }

    public Vector2 getDisplayPos(Vector2 screenPos) {
        return screenPos.sub(displayOffset.scale(displayScale)).scale(1 / displayScale);
    }

}
