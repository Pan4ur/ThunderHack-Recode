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

    public Vec3d getmVec() {return mVec;}
    public boolean isPre() {return pre;}

    public void setX(double x) {mVec = new Vec3d(x, mVec.getY(), mVec.getZ());}
    public double getX() {return mVec.getX();}

    public void setY(double v) {mVec = new Vec3d(mVec.getX(), v, mVec.getZ());}
    public double getY() {return mVec.getY();}

    public void setZ(double z) {mVec = new Vec3d(mVec.getX(), mVec.getY(), z);}
    public double getZ() {return mVec.getZ();}
}
