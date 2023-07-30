package thunder.hack.utility.math;

import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
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

public class Placement {
    private final static MinecraftClient mc = MinecraftClient.getInstance();

    private final BlockPos neighbour;
    private final Direction opposite;

    private final float yaw;
    private final float pitch;

    private final Hand hand;
    private final boolean rotate;

    private final int slot;

    public Placement(BlockPos neighbour, Direction opposite, float yaw, float pitch, Hand hand, boolean rotate, int slot) {
        this.neighbour = neighbour;
        this.opposite = opposite;
        this.yaw = yaw;
        this.pitch = pitch;
        this.hand = hand;
        this.rotate = rotate;
        this.slot = slot;
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
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            }

            if (mc.player.isSprinting() && !PlaceManager.syncSprinting) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                PlaceManager.syncSprinting = true;
            }

            if (!mc.player.isSneaking() && !PlaceManager.syncSneaking && shouldSneakWhileClicking(mc.world.getBlockState(getNeighbour()).getBlock())) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                PlaceManager.syncSneaking = true;
            }

            Vec3d hitVec = new Vec3d(getNeighbour().getX() + 0.5, getNeighbour().getY() + 0.5, getNeighbour().getZ() + 0.5).add(new Vec3d(getOpposite().getUnitVector()).multiply(0.5));
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(hitVec, getOpposite(), getNeighbour(), false));

            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
        };
    }
    public static boolean shouldSneakWhileClicking(Block block) {
        return block instanceof EnderChestBlock || block instanceof AnvilBlock || block instanceof ButtonBlock || block instanceof AbstractPressurePlateBlock || block instanceof BlockWithEntity || block instanceof CraftingTableBlock || block instanceof DoorBlock || block instanceof FenceGateBlock || block instanceof NoteBlock || block instanceof TrapdoorBlock;
    }

    public boolean isRotate() {
        return rotate;
    }
}