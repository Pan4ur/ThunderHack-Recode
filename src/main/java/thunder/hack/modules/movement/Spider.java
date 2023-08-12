package thunder.hack.modules.movement;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtil;
import thunder.hack.utility.player.PlayerUtil;

public class Spider extends Module {

    public final Setting<Integer> delay = new Setting("delay", 2, 1, 15);
    private final Setting<mode> a = new Setting("Mode", mode.Matrix);

    public Spider() {
        super("Spider", "можно по стенам хуярить-как черт", Category.MOVEMENT);
    }

    public static Direction getPlaceableSide(BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos neighbour = pos.offset(side);
            if (mc.world.isAir(neighbour)) {
                continue;
            }
            if (!mc.world.getBlockState(neighbour).isReplaceable()) {
                return side;
            }
        }
        return null;
    }

    @Override
    public void onUpdate() {
        if (!mc.player.horizontalCollision) return;

        if (a.getValue() == mode.Default) {
            mc.player.setVelocity(mc.player.getVelocity().offset(Direction.UP,0.2));
        } else if (a.getValue() == mode.Matrix) {
            if (mc.player.age % delay.getValue() == 0) {
                mc.player.setOnGround(true);
            } else {
                mc.player.setOnGround(false);
            }
            mc.player.prevY -= 2.0E-232;
            if (mc.player.isOnGround()) mc.player.setVelocity(mc.player.getVelocity().offset(Direction.UP,0.42));
        }
    }


    @EventHandler
    public void onSync(EventSync event) {
        if (mc.options.jumpKey.isPressed() && mc.player.getVelocity().getY() <= -0.3739040364667221 && a.getValue() == mode.MatrixNew) {
            mc.player.setOnGround(true);
            mc.player.setVelocity(mc.player.getVelocity().offset(Direction.UP,0.481145141919180));
        }
        if (mc.player.age % delay.getValue() == 0 && mc.player.horizontalCollision && MovementUtil.isMoving() && a.getValue() == mode.Blocks) {
            int find = -2;
            for (int i = 0; i <= 8; i++)
                if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem) find = i;
            if (find == -2) return;
            BlockPos pos = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ());
            Direction side = getPlaceableSide(pos);
            if (side != null) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(find));
                BlockPos neighbour = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ()).offset(side);
                Direction opposite = side.getOpposite();
                Vec3d hitVec = new Vec3d(neighbour.getX() + 0.5, neighbour.getY() + 0.5, neighbour.getZ() + 0.5).add(new Vec3d(opposite.getUnitVector()).multiply(0.5));
                mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket( Hand.MAIN_HAND, new BlockHitResult(hitVec, opposite, neighbour, false), PlayerUtil.getWorldActionId(mc.world)));
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, 2, 0)).getBlock() != Blocks.AIR) {
                    mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, neighbour, opposite));
                    mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, neighbour, opposite));
                }
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
            mc.player.setOnGround(true);
            mc.player.jump();
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        }
    }

    public enum mode {
        Default, Matrix, MatrixNew, Blocks
    }
}