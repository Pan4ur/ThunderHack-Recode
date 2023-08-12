package thunder.hack.utility.math;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import thunder.hack.cmd.Command;
import thunder.hack.core.PlaceManager;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import thunder.hack.utility.player.PlaceUtility;
import thunder.hack.utility.player.PlayerUtility;

import static thunder.hack.modules.Module.mc;

public class Placement {
    private final BlockPos neighbour;
    private final Direction opposite;

    private final float yaw;
    private final float pitch;

    private final Hand hand;
    private final boolean rotate;

    private final int slot;
    private final PlaceUtility.PlaceMode mode;

    public Placement(BlockPos neighbour, Direction opposite, float yaw, float pitch, Hand hand, boolean rotate, int slot, PlaceUtility.PlaceMode mode) {
        this.neighbour = neighbour;
        this.opposite = opposite;
        this.yaw = yaw;
        this.pitch = pitch;
        this.hand = hand;
        this.rotate = rotate;
        this.slot = slot;
        this.mode = mode;
    }

    public BlockPos getNeighbour() {
        return neighbour;
    }

    public Direction getOpposite() {
        return opposite;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public Runnable getAction() {
        return () -> {
            if (hand == Hand.MAIN_HAND && slot != -1 && mc.player.getInventory().selectedSlot != slot) {
                mc.player.getInventory().selectedSlot = slot;
                sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            }

            if (mc.player.isSprinting() && !PlaceManager.syncSprinting) {
                sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                PlaceManager.syncSprinting = true;
            }
            if (!mc.player.isSneaking() && !PlaceManager.syncSneaking && shouldSneakWhileClicking(mc.world.getBlockState(getNeighbour()).getBlock())) {
                sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                PlaceManager.syncSneaking = true;
            }

            Vec3d hitVec = new Vec3d(getNeighbour().getX() + 0.5, getNeighbour().getY() + 0.5, getNeighbour().getZ() + 0.5).add(new Vec3d(getOpposite().getUnitVector()).multiply(0.5));
            BlockHitResult hitResult = new BlockHitResult(hitVec, getOpposite(), getNeighbour(), false);

            if (mode == PlaceUtility.PlaceMode.Packet || mode == PlaceUtility.PlaceMode.All) {
                sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult, PlayerUtility.getWorldActionId(mc.world)));
            }
            if (mode == PlaceUtility.PlaceMode.Normal || mode == PlaceUtility.PlaceMode.All) {
                mc.interactionManager.interactBlock(mc.player, hand, hitResult);
            }
            sendPacket(new HandSwingC2SPacket(hand));
        };
    }

    public static boolean shouldSneakWhileClicking(Block block) {
        return block instanceof EnderChestBlock || block instanceof AnvilBlock || block instanceof ButtonBlock || block instanceof AbstractPressurePlateBlock || block instanceof BlockWithEntity || block instanceof CraftingTableBlock || block instanceof DoorBlock || block instanceof FenceGateBlock || block instanceof NoteBlock || block instanceof TrapdoorBlock;
    }

    private void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) return;

        mc.getNetworkHandler().sendPacket(packet);
    }

    public boolean isRotate() {
        return rotate;
    }
}