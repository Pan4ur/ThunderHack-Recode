package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class DamageFly extends Module {
    public DamageFly() {
        super("DamageFly", Category.MOVEMENT);
    }

    public Setting<Integer> boostTicks = new Setting<>("Ticks", 8, 0, 40);
    public Setting<Integer> hurtTime = new Setting<>("HurtTime", 9, 1, 10);

    private boolean canBoost, damage, isVelocity;
    private int ticks;
    private double motion;

    @EventHandler(priority = EventPriority.HIGH)
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket velPacket) {
            if (velPacket.getVelocityY() > 0)
                isVelocity = true;

            if (velPacket.getVelocityY() / 8000.0 > 0.2) {
                motion = velPacket.getVelocityY() / 8000.0;
                canBoost = true;
            }
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player.hurtTime == hurtTime.getValue())
            damage = true;

        if (damage && isVelocity) {
            if (canBoost) {
                mc.player.setVelocity(mc.player.getVelocity().x, motion, mc.player.getVelocity().getZ());
                ++ticks;
            }
            if (ticks >= boostTicks.getValue()) {
                isVelocity = false;
                canBoost = false;
                damage = false;
                ticks = 0;
            }
        }
    }

    @Override
    public void onEnable() {
        damage = false;
        canBoost = false;
        ticks = 0;
    }
}