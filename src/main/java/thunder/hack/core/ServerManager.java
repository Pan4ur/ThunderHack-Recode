package thunder.hack.core;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.utility.Timer;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;

public class ServerManager {

    private final Timer timeDelay;
    private final ArrayDeque<Float> tpsResult;
    private long time;
    private float tps;

    public ServerManager() {
        this.tpsResult = new ArrayDeque<>(20);
        this.timeDelay = new Timer();
    }


    public Timer getDelayTimer() {
        return timeDelay;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public float getTPS() {
        return round2(this.tps);
    }

    public void setTPS(float tps) {
        this.tps = tps;
    }

    public ArrayDeque<Float> getTPSResults() {
        return tpsResult;
    }

    public static float round2(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.getPacket() instanceof ChatMessageS2CPacket)) {
            getDelayTimer().reset();
        }
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            if (getTime() != 0L) {
                if (getTPSResults().size() > 20) {
                    getTPSResults().poll();
                }
                getTPSResults().add(20.0f * (1000.0f / (float) (System.currentTimeMillis() - getTime())));
                float f = 0.0f;
                for (Float value : getTPSResults()) {
                    f += Math.max(0.0f, Math.min(20.0f, value));
                }
                setTPS(f / (float) getTPSResults().size());
            }
            setTime(System.currentTimeMillis());
        }
    }
}
