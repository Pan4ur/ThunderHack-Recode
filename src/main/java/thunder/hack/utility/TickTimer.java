package thunder.hack.utility;

import static thunder.hack.core.manager.IManager.mc;

public class TickTimer {
    private int time;

    public TickTimer() {
        reset();
    }

    public boolean passedTicks(long t) {
        if (getPassedTicks() < 0)
            reset();
        return getPassedTicks() >= t;
    }

    public boolean every(long ms) {
        if (getPassedTicks() < 0)
            reset();
        boolean passed = getPassedTicks() >= ms;
        if (passed)
            reset();
        return passed;
    }

    public void set(int t) {
        time = t;
    }

    public void reset() {
        time = mc.player == null ? 0 : mc.player.age;
    }

    private int getPassedTicks() {
        return mc.player == null ? 0 : mc.player.age - time;
    }
}