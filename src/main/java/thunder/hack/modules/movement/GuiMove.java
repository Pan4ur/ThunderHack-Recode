package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class GuiMove extends Module {

    public Setting<Boolean> rotateOnArrows = new Setting<>("RotateOnArrows", true);
    public Setting<Boolean> clickBypass = new Setting<>("strict", false);
    public Setting<Boolean> sneak = new Setting<>("sneak", false);

    public GuiMove() {
        super("GuiMove", Category.MOVEMENT);
    }

    public static boolean pause = false;

    @Override
    public void onUpdate() {
        if (mc.currentScreen != null) {
            if (!(mc.currentScreen instanceof ChatScreen)) {
                mc.player.setSprinting(true);

                for (KeyBinding k : new KeyBinding[]{mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey, mc.options.sprintKey}) {
                    k.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(k.getBoundKeyTranslationKey()).getCode()));
                }
                if (rotateOnArrows.getValue()) {
                    if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 264))
                        mc.player.setPitch(mc.player.getPitch() + 5);

                    if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 265))
                        mc.player.setPitch(mc.player.getPitch() - 5);

                    if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 262))
                        mc.player.setYaw(mc.player.getYaw() + 5);

                    if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 263))
                        mc.player.setYaw(mc.player.getYaw() - 5);

                    if (mc.player.getPitch() > 90) mc.player.setYaw(90);
                    if (mc.player.getPitch() < -90) mc.player.setYaw(-90);
                }
                if (sneak.getValue()) mc.options.sneakKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()));
            }
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (pause) {
            pause = false;
            return;
        }
        if (e.getPacket() instanceof ClickSlotC2SPacket) {
            if (clickBypass.getValue() && mc.player.isOnGround() && MovementUtility.isMoving() && !mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, 0.0656, 0.0)).iterator().hasNext()) {
                if (mc.player.isSprinting()) sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));

                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0656, mc.player.getZ(), false));
            }
        }
    }

    @EventHandler
    public void onPacketSendPost(PacketEvent.SendPost e) {
        if (e.getPacket() instanceof ClickSlotC2SPacket) {
            if (mc.player.isSprinting()) sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
    }
}