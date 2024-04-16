package thunder.hack.modules.movement;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import thunder.hack.modules.Module;
import net.minecraft.util.math.Direction;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Parent;

public class NoSlow extends Module {
    public NoSlow() {
        super("NoSlow", Category.MOVEMENT);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.NCP);
    private final Setting<Boolean> mainHand = new Setting<>("MainHand", true);
    private final Setting<Parent> selection = new Setting<>("Selection", new Parent(false, 0));
    private final Setting<Boolean> food = new Setting<>("Food", true).withParent(selection);
    private final Setting<Boolean> projectiles = new Setting<>("Projectiles", true).withParent(selection);
    private final Setting<Boolean> shield = new Setting<>("Shield", true).withParent(selection);
    public final Setting<Boolean> soulSand = new Setting<>("SoulSand", true).withParent(selection);
    public final Setting<Boolean> honey = new Setting<>("Honey", true).withParent(selection);
    public final Setting<Boolean> slime = new Setting<>("Slime", true).withParent(selection);
    public final Setting<Boolean> ice = new Setting<>("Ice", true).withParent(selection);
    public final Setting<Boolean> sweetBerryBush = new Setting<>("SweetBerryBush", true).withParent(selection);

    private boolean returnSneak;

    @Override
    public void onUpdate() {
        if (returnSneak) {
            mc.options.sneakKey.setPressed(false);
            returnSneak = false;
        }

        if (mc.player.isUsingItem() && !mc.player.isRiding() && !mc.player.isFallFlying()) {
            switch (mode.getValue()) {
                case StrictNCP -> sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                case MusteryGrief -> {
                    if (mc.player.isOnGround() && mc.options.jumpKey.isPressed()) {
                        mc.options.sneakKey.setPressed(true);
                        returnSneak = true;
                    }
                }
                case Grim -> {
                    if (mc.player.getActiveHand() == Hand.OFF_HAND) {
                        sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 8 + 1));
                        sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                    } else if (mainHand.getValue()) {
                        sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id));
                    }
                }
                case Matrix -> {
                    if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
                        mc.player.setVelocity(mc.player.getVelocity().x * 0.3, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.3);
                    } else if (mc.player.fallDistance > 0.2f)
                        mc.player.setVelocity(mc.player.getVelocity().x * 0.95f, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.95f);
                }
                case GrimNew -> {
                    if (mc.player.getActiveHand() == Hand.OFF_HAND) {
                        sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 8 + 1));
                        sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                    } else if (mainHand.getValue() && (mc.player.getItemUseTime() <= 3 || mc.player.age % 2 == 0)) {
                        sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id));
                    }
                }
                case Matrix2 -> {
                    if (mc.player.isOnGround())
                        if (mc.player.age % 2 == 0)
                            mc.player.setVelocity(mc.player.getVelocity().x * 0.5f, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.5f);
                        else
                            mc.player.setVelocity(mc.player.getVelocity().x * 0.95f, mc.player.getVelocity().y, mc.player.getVelocity().z * 0.95f);
                }
                case LFCraft -> {
                    if (mc.player.getItemUseTime() <= 3)
                        sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, mc.player.getBlockPos().up(), Direction.NORTH, id));
                }
            }
        }
    }

    public boolean canNoSlow() {
        if (!food.getValue() && mc.player.getActiveItem().isFood())
            return false;

        if (!shield.getValue() && mc.player.getActiveItem().getItem() == Items.SHIELD)
            return false;

        if (!projectiles.getValue()
                && (mc.player.getActiveItem().getItem() == Items.CROSSBOW || mc.player.getActiveItem().getItem() == Items.BOW || mc.player.getActiveItem().getItem() == Items.TRIDENT))
            return false;

        if (mode.getValue() == Mode.MusteryGrief && mc.player.isOnGround() && !mc.options.jumpKey.isPressed())
            return false;

        if (!mainHand.getValue() && mc.player.getActiveHand() == Hand.MAIN_HAND)
            return false;

        if ((mc.player.getOffHandStack().isFood() || mc.player.getOffHandStack().getItem() == Items.SHIELD)
                && (mode.getValue() == Mode.GrimNew || mode.getValue() == Mode.Grim) && mc.player.getActiveHand() == Hand.MAIN_HAND)
            return false;

        return true;
    }

    public enum Mode {
        NCP, StrictNCP, Matrix, Grim, MusteryGrief, GrimNew, Matrix2, LFCraft
    }
}
