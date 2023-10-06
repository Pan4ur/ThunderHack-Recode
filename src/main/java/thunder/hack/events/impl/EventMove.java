package thunder.hack.events.impl;

import thunder.hack.events.Event;

public class EventMove extends Event {
    public double x, y, z;

    public EventMove( double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }
}