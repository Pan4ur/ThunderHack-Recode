package dev.thunderhack.modules.movement;

import dev.thunderhack.event.events.EventSync;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import dev.thunderhack.core.ModuleManager;

public class NoSlow extends Module {
    public static Setting<Mode> mode = new Setting<>("Mode", Mode.NCP);

    public NoSlow() {
        super("NoSlow", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventSync event) {
        if (mc.player.isUsingItem()) {
            if (mode.getValue() == Mode.StrictNCP || mode.getValue() == Mode.NCP) {
                if (!mc.player.isRiding() && !mc.player.isSneaking()) {
                    if (mode.getValue() == Mode.StrictNCP)
                        sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                }
            }

            if (mode.getValue() == Mode.MusteryGrief && mc.player.getActiveHand() == Hand.OFF_HAND) {
                if (!mc.player.isRiding() && !mc.player.isSneaking()) {
                    sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                    if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
                        mc.player.setVelocity(mc.player.getVelocity().x * 0.3, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.3);
                    } else if (mc.player.fallDistance > 0.2f)
                        mc.player.setVelocity(mc.player.getVelocity().x * 0.95f, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.95f);
                }
            }

            if (mode.getValue() == Mode.GrimOffHand && mc.player.getActiveHand() == Hand.OFF_HAND) {
                sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 8 + 1));
                sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            }

            if (mode.getValue() == Mode.Matrix) {
                if (!ModuleManager.strafe.isEnabled()) {
                    if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
                        mc.player.setVelocity(mc.player.getVelocity().x * 0.3, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.3);
                    } else if (mc.player.fallDistance > 0.2f)
                        mc.player.setVelocity(mc.player.getVelocity().x * 0.95f, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.95f);
                } else {
                    if (!mc.player.isOnGround() && mc.player.fallDistance > 0.2f)
                        mc.player.setVelocity(mc.player.getVelocity().x * 0.7, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.7f);
                }
            }
        }
    }

    public enum Mode {
        NCP, StrictNCP, Matrix, GrimOffHand, MusteryGrief
    }

}
