package dev.thunderhack.core;

import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.utils.Timer;
import dev.thunderhack.utils.math.MathUtility;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;

public class ServerManager {
    private final Timer timeDelay = new Timer();
    private final ArrayDeque<Float> tpsResult = new ArrayDeque<>(20);
    private long time;
    private long ticktime;
    private float tps;

    public float getTPS() {
        return round2(tps);
    }

    public float getTPS2() {
        return round2(20.0f * ((float) ticktime / 1000f));
    }

    public static float round2(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.getPacket() instanceof ChatMessageS2CPacket)) {
            timeDelay.reset();
        }
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            if (time != 0L) {
                ticktime = System.currentTimeMillis() - time;

                if (tpsResult.size() > 20)
                    tpsResult.poll();

                tpsResult.add(20.0f * (1000.0f / (float) (ticktime)));

                float average = 0.0f;

                for (Float value : tpsResult) average += MathUtility.clamp(value, 0f, 20f);

                tps = average / (float) tpsResult.size();
            }
            time = System.currentTimeMillis();
        }
    }
}
