package thunder.hack.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class WorldTweaks extends Module {
    public WorldTweaks() {
        super("WorldTweaks", Category.MISC);
    }

    public final Setting<Boolean> ctime = new Setting<>("ChangeTime", false);
    public final Setting<Integer> ctimeVal = new Setting("Time", 21, 0, 23);

    long oldTime;

    @Override
    public void onEnable() {
        oldTime = mc.world.getTime();
    }

    @Override
    public void onDisable() {
        mc.world.setTimeOfDay(oldTime);
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket && ctime.getValue()) {
            oldTime = ((WorldTimeUpdateS2CPacket) event.getPacket()).getTime();
            event.cancel();
        }
    }

    @Override
    public void onUpdate() {
        if (ctime.getValue()) mc.world.setTimeOfDay(ctimeVal.getValue() * 1000);
    }
}
