package thunder.hack.events.impl;

import thunder.hack.events.Event;

public class PushEvent extends Event {

    private double pushX;
    private double pushY;
    private double pushZ;

    public PushEvent(double pushX, double pushY, double pushZ) {
        this.pushX = pushX;
        this.pushY = pushY;
        this.pushZ = pushZ;
    }

    public double getPushX() {
        return pushX;
    }

    public void setPushX(double pushX) {
        this.pushX = pushX;
    }

    public double getPushY() {
        return pushY;
    }

    public void setPushY(double pushY) {
        this.pushY = pushY;
    }

    public double getPushZ() {
        return pushZ;
    }

    public void setPushZ(double pushZ) {
        this.pushZ = pushZ;
    }
}
