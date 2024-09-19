package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import thunder.hack.events.impl.EventKeyboardInput;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;

public class NoSlow extends Module {
    public NoSlow() {
        super("NoSlow", Category.MOVEMENT);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.NCP);
    private final Setting<Boolean> mainHand = new Setting<>("MainHand", true);
    private final Setting<SettingGroup> selection = new Setting<>("Selection", new SettingGroup(false, 0));
    private final Setting<Boolean> food = new Setting<>("Food", true).addToGroup(selection);
    private final Setting<Boolean> projectiles = new Setting<>("Projectiles", true).addToGroup(selection);
    private final Setting<Boolean> shield = new Setting<>("Shield", true).addToGroup(selection);
    public final Setting<Boolean> soulSand = new Setting<>("SoulSand", true).addToGroup(selection);
    public final Setting<Boolean> honey = new Setting<>("Honey", true).addToGroup(selection);
    public final Setting<Boolean> slime = new Setting<>("Slime", true).addToGroup(selection);
    public final Setting<Boolean> ice = new Setting<>("Ice", true).addToGroup(selection);
    public final Setting<Boolean> sweetBerryBush = new Setting<>("SweetBerryBush", true).addToGroup(selection);
    public final Setting<Boolean> sneak = new Setting<>("Sneak", false).addToGroup(selection);
    public final Setting<Boolean> crawl = new Setting<>("Crawl", false).addToGroup(selection);

    private boolean returnSneak;

    @Override
    public void onUpdate() {
        if (returnSneak) {
            mc.options.sneakKey.setPressed(false);
            mc.player.setSprinting(true);
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
                        sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 7 + 2));
                        sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                    } else if (mainHand.getValue()) {
                        // TODO rotations
                        sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
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
                        sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 7 + 2));
                        sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                    } else if (mainHand.getValue() && (mc.player.getItemUseTime() <= 3 || mc.player.age % 2 == 0)) {
                        sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
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

    @EventHandler
    public void onKeyboardInput(EventKeyboardInput e) {
        if (mode.getValue() == Mode.Matrix3 && mc.player.isUsingItem() && !mc.player.isFallFlying()) {
            mc.player.input.movementForward *= 5f;
            mc.player.input.movementSideways *= 5f;
            float mult = 1f;

            if (mc.player.isOnGround()) {
                if (mc.player.input.movementForward != 0 && mc.player.input.movementSideways != 0) {
                    mc.player.input.movementForward *= 0.35f;
                    mc.player.input.movementSideways *= 0.35f;
                } else {
                    mc.player.input.movementForward *= 0.5f;
                    mc.player.input.movementSideways *= 0.5f;
                }
            } else {
                if (mc.player.input.movementForward != 0 && mc.player.input.movementSideways != 0) {
                    mult = 0.47f;
                } else {
                    mult = 0.67f;
                }
            }
            mc.player.input.movementForward *= mult;
            mc.player.input.movementSideways *= mult;
        }
    }

    public boolean canNoSlow() {
        if (mode.getValue() == Mode.Matrix3)
            return false;

        if (!food.getValue() && mc.player.getActiveItem().getComponents().contains(DataComponentTypes.FOOD))
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

        if ((mc.player.getOffHandStack().getComponents().contains(DataComponentTypes.FOOD) || mc.player.getOffHandStack().getItem() == Items.SHIELD)
                && (mode.getValue() == Mode.GrimNew || mode.getValue() == Mode.Grim) && mc.player.getActiveHand() == Hand.MAIN_HAND)
            return false;

        return true;
    }

    public enum Mode {
        NCP, StrictNCP, Matrix, Grim, MusteryGrief, GrimNew, Matrix2, LFCraft, Matrix3
    }
}
