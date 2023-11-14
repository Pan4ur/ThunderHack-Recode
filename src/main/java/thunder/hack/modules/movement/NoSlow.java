package thunder.hack.modules.movement;

import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.PlayerUtility;

public class NoSlow extends Module {
    public static Setting<Mode> mode = new Setting<>("Mode", Mode.NCP);
    public Setting<Boolean> mainHand = new Setting<>("MainHand", true, v -> mode.getValue() == Mode.Grim || mode.getValue() == Mode.MusteryGrief);

    private boolean returnSneak;

    public NoSlow() {
        super("NoSlow", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        if (returnSneak) {
            mc.options.sneakKey.setPressed(false);
            returnSneak = false;
        }

        if (mc.player.isUsingItem() && !mc.player.isRiding() && !mc.player.isFallFlying()) {
            if (mode.getValue() == Mode.StrictNCP)
                sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));

            if (mode.getValue() == Mode.MusteryGrief) {
                if (mc.player.isOnGround() && mc.options.jumpKey.isPressed()) {
                    mc.options.sneakKey.setPressed(true);
                    returnSneak = true;
                }
            }

            if (mode.getValue() == Mode.Grim) {
                if (mc.player.getActiveHand() == Hand.OFF_HAND) {
                    sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 8 + 1));
                    sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                } else if (mainHand.getValue()) {
                    sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, PlayerUtility.getWorldActionId(mc.world)));
                }
            }

            if (mode.getValue() == Mode.Matrix) {
                if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
                    mc.player.setVelocity(mc.player.getVelocity().x * 0.3, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.3);
                } else if (mc.player.fallDistance > 0.2f)
                    mc.player.setVelocity(mc.player.getVelocity().x * 0.95f, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.95f);
            }
        }
    }

    public boolean canNoSlow() {
        if (mode.getValue() == Mode.MusteryGrief && mc.player.isOnGround() && !mc.options.jumpKey.isPressed())
            return false;
        if (!mainHand.getValue() && mc.player.getActiveHand() == Hand.MAIN_HAND)
            return mode.getValue() != Mode.MusteryGrief && mode.getValue() != Mode.Grim;
        return true;
    }

    public enum Mode {
        NCP, StrictNCP, Matrix, Grim, MusteryGrief
    }

}
