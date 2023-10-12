package dev.thunderhack.modules.player;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.math.MathUtility;
import dev.thunderhack.utils.player.MovementUtility;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static java.lang.Math.abs;
import static dev.thunderhack.modules.client.MainSettings.isRu;

public class HitBoxTricks extends Module {
    public HitBoxTricks() {
        super("HitBoxTricks", Category.PLAYER);
    }

    private static final double MAGIC_OFFSET = .200009968835369999878673424677777777777761;

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Desync);
    private final Setting<Integer> interval = new Setting<>("Interval", 5, 1, 10, v-> mode.getValue() == Mode.City);

    private enum Mode {
        Desync,
        City
    }

    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.Desync) {
            Vec3d offset = new Vec3d(mc.player.getHorizontalFacing().getUnitVector());
            Vec3d fin = merge(Vec3d.of(BlockPos.ofFloored(mc.player.getBoundingBox().getCenter())).add(.5, 0, .5).add(offset.multiply(MAGIC_OFFSET)), mc.player.getHorizontalFacing());
            mc.player.setPosition(fin.x == 0 ? mc.player.getX() : fin.x, mc.player.getY(), fin.z == 0 ? mc.player.getZ() : fin.z);
            disable(isRu() ? "Хитбокс сдвинут! Отключаю.." : "Hitbox desynced! Disabling..");
        } else {
            if (MovementUtility.isMoving()) {
                mc.player.input.movementForward = 0F;
                mc.player.input.movementSideways = 0F;
                mc.player.setVelocity(0, mc.player.getVelocity().getY(), 0);
                mc.player.input.movementForward = 0F;
                mc.player.input.movementSideways = 0F;
            }
            if (!mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(0.01, 0, 0.01)).iterator().hasNext()) {
                mc.player.setPosition(roundToClosest(mc.player.getX(), Math.floor(mc.player.getX()) + 0.301, Math.floor(mc.player.getX()) + 0.699), mc.player.getY(), roundToClosest(mc.player.getZ(), Math.floor(mc.player.getZ()) + 0.301, Math.floor(mc.player.getZ()) + 0.699));
            } else if (mc.player.age % interval.getValue() == 0) {
                mc.player.setPosition(mc.player.getX() + MathUtility.clamp(roundToClosest(mc.player.getX(), Math.floor(mc.player.getX()) + 0.241, Math.floor(mc.player.getX()) + 0.759) - mc.player.getX(), -0.03, 0.03), mc.player.getY(), mc.player.getZ() + MathUtility.clamp(roundToClosest(mc.player.getZ(), Math.floor(mc.player.getZ()) + 0.241, Math.floor(mc.player.getZ()) + 0.759) - mc.player.getZ(), -0.03, 0.03));
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(roundToClosest(mc.player.getX(), Math.floor(mc.player.getX()) + 0.23, Math.floor(mc.player.getX()) + 0.77), mc.player.getY(), roundToClosest(mc.player.getZ(), Math.floor(mc.player.getZ()) + 0.23, Math.floor(mc.player.getZ()) + 0.77), true));
            }
        }
    }

    private double roundToClosest(double num, double low, double high) {
        double d1 = num - low;
        double d2 = high - num;
        if (d2 > d1) return low;
        else return high;
    }

    private Vec3d merge(Vec3d a, Direction facing) {
        return new Vec3d(a.x * abs(facing.getUnitVector().x()), a.y * abs(facing.getUnitVector().y()), a.z * abs(facing.getUnitVector().z()));
    }
}
