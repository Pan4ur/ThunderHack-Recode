package thunder.hack.modules.client;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;

public class FastLatency extends Module {

    public static FastLatency instance;

    public FastLatency() {
        super("FastLatency", Category.CLIENT);
        instance = this;
    }

    private final Setting<Integer> delay = new Setting<>("Delay", 80, 0, 1000);

    private Timer timer = new Timer();
    private Timer  limitTimer = new Timer();
    private long ping;
    public int resolvedPing;

    @SuppressWarnings("unused")
    public void onRender3D(MatrixStack stack) {
        if (timer.passedMs(5000) && limitTimer.every(delay.getValue())) {
            sendPacket(new RequestCommandCompletionsC2SPacket(1337, "w "));
            ping = System.currentTimeMillis();
            timer.reset();
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof CommandSuggestionsS2CPacket c && c.getCompletionId() == 1337) {
            resolvedPing = (int) MathUtility.clamp(System.currentTimeMillis() - ping, 0, 1000);
            timer.setMs(5000);
        }
    }
}
