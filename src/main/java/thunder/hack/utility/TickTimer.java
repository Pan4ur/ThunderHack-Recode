package thunder.hack.utility;

import static thunder.hack.core.IManager.mc;

public class TickTimer {
    private int time;

    public TickTimer() {
        reset();
    }

    public boolean passedTicks(long t) {
        return getPassedTicks() >= t;
    }

    public boolean every(long ms) {
        boolean passed = getPassedTicks() >= ms;
        if (passed)
            reset();
        return passed;
    }

    public void set(int t) {
        time = getPassedTicks() - t;
    }

    public void reset() {
        time = mc.player == null ? 0 : mc.player.age;
    }

    private int getPassedTicks() {
        return mc.player == null ? 0 : mc.player.age - time;
    }
}