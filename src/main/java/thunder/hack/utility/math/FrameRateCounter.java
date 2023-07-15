package thunder.hack.utility.math;

import java.util.ArrayList;
import java.util.List;

public class FrameRateCounter {
    public static final FrameRateCounter INSTANCE = new FrameRateCounter();
    final List<Long> records = new ArrayList<>();

    public void recordFrame() {
        long c = System.currentTimeMillis();
        records.add(c);
    }

    public int getFps() {
        records.removeIf(aLong -> aLong + 1000 < System.currentTimeMillis());
        return records.size() / 2;
    }
}
