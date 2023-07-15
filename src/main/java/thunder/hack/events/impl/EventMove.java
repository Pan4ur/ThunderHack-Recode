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


    public double get_x() {
        return this.x;
    }

    public void set_x(double x) {
        this.x = x;
    }

    public double get_y() {
        return this.y;
    }

    public void set_y(double y) {
        this.y = y;
    }

    public double get_z() {
        return this.z;
    }

    public void set_z(double z) {
        this.z = z;
    }
}