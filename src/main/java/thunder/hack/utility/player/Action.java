package thunder.hack.utility.player;

import thunder.hack.utility.math.Placement;

public class Action {
    private final float yaw;
    private final float pitch;

    private final Runnable action;

    private final boolean rotate;
    private final boolean optional;

    public Action(Runnable action) {
        this(0F, 0F, action, false, false);
    }

    public Action(float yaw, float pitch, Runnable action) {
        this(yaw, pitch, action, true, false);
    }

    public Action(float yaw, float pitch, Runnable action, boolean optional) {
        this(yaw, pitch, action, true, optional);
    }

    public Action(float yaw, float pitch, Runnable action, boolean rotate, boolean optional) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.action = action;
        this.rotate = rotate;
        this.optional = optional;
    }

    public Action(Placement placement) {
        this(placement.getYaw(), placement.getPitch(), placement.getAction(), placement.isRotate(), false);
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public Runnable getAction() {
        return action;
    }

    public boolean isRotate() {
        return rotate;
    }

    public boolean isOptional() {
        return optional;
    }


}