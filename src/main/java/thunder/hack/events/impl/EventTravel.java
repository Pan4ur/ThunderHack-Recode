package thunder.hack.events.impl;

import net.minecraft.util.math.Vec3d;
import thunder.hack.events.Event;

public class EventTravel extends Event {
    private Vec3d mVec;

    private boolean pre;

    public EventTravel(Vec3d mVec, boolean pre) {
        this.mVec = mVec;
        this.pre = pre;
    }

    public Vec3d getmVec() {
        return mVec;
    }

    public boolean isPre() {
        return pre;
    }

    public void set_y(double v) {
        mVec = new Vec3d(mVec.getX(), v, mVec.getZ());
    }

    public double get_y() {
        return mVec.getY();
    }

    public void set_x(double x) {
        mVec = new Vec3d(x, mVec.getY(), mVec.getZ());
    }

    public double get_x() {
        return mVec.getX();
    }

    public void set_z(double z) {
        mVec = new Vec3d(mVec.getX(), mVec.getY(), z);
    }

    public double get_z() {
        return mVec.getZ();
    }
}
