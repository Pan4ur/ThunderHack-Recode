package thunder.hack.utility;

public class Timer {
    private long time;

    public Timer() {
        reset();
    }

    public boolean passedS(double s) {
        return getMs(System.nanoTime() - time) >= (long) (s * 1000.0);
    }

    public boolean passedMs(long ms) {
        return getMs(System.nanoTime() - time) >= ms;
    }

    public boolean every(long ms) {
        boolean passed = getMs(System.nanoTime() - time) >= ms;
        if (passed)
            reset();
        return passed;
    }

    public void setMs(long ms) {
        this.time = System.nanoTime() - ms * 1000000L;
    }

    public long getPassedTimeMs() {
        return getMs(System.nanoTime() - time);
    }

    public void reset() {
        this.time = System.nanoTime();
    }

    public long getMs(long time) {
        return time / 1000000L;
    }

    public long getTimeMs() {
        return getMs(System.nanoTime() - time);
    }
}