package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import thunder.hack.events.impl.EventPlayerJump;
import thunder.hack.events.impl.EventPlayerTravel;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.EventTick;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;
import thunder.hack.utility.player.PlaceUtility;
import thunder.hack.utility.world.HoleUtility;

import java.util.ArrayList;

public class HoleSnap extends Module {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Rotate);
    private final Setting<Integer> maxCollides = new Setting<>("Max Stuck Count", 15, 1, 20, value -> mode.getValue() == Mode.Walk);

    private int currentCollides;
    private BlockPos hole;
    private float prevClientYaw;

    public HoleSnap() {
        super("HoleSnap", Category.MOVEMENT);
    }

    private enum Mode {
        Rotate,
        Walk
    }

    @Override
    public void onEnable() {
        currentCollides = 0;
        hole = null;

        hole = findHole();
    }

    @EventHandler
    private void onTick(EventTick event) {
        if (currentCollides >= maxCollides.getValue() && mode.getValue() == Mode.Walk) {
            disable();
            return;
        }

        if (hole != null) doWalkLogic();

        if (mc.player.horizontalCollision) currentCollides++;
        else currentCollides = 0;
    }

    @EventHandler
    public void modifyVelocity(EventPlayerTravel e) {
        if (mc.player.age % 10 == 0) hole = findHole();
        if (hole == null) return;
        if (mc.player.getX() == hole.toCenterPos().x && mc.player.getZ() == hole.toCenterPos().z) {
            disable();
            return;
        }

        switch (mode.getValue()) {
            case Rotate -> {
                if (e.isPre()) {
                    prevClientYaw = mc.player.getYaw();
                    mc.player.setYaw(PlaceUtility.calculateAngle(hole.toCenterPos())[0]);
                } else {
                    mc.player.setYaw(prevClientYaw);
                }
            }
            case Walk -> doWalkLogic();
        }
    }

    @EventHandler
    public void modifyJump(EventPlayerJump e) {
        if (hole == null) return;

        switch (mode.getValue()) {
            case Rotate -> {
                if (e.isPre()) {
                    prevClientYaw = mc.player.getYaw();
                    mc.player.setYaw(PlaceUtility.calculateAngle(hole.toCenterPos())[0]);
                } else {
                    mc.player.setYaw(prevClientYaw);
                }
            }
            case Walk -> doWalkLogic();
        }
    }

    private void doWalkLogic() {
        final double nowSpeed = MovementUtility.getSpeed();
        final double distance = mc.player.getPos().distanceTo(hole.toCenterPos());
        final float yaw = PlaceUtility.calculateAngle(hole.toCenterPos())[0];
        final double finalSpeed = Math.min(distance, nowSpeed);

        mc.player.move(MovementType.PLAYER, new Vec3d(-Math.sin(yaw) * finalSpeed, 0, Math.cos(yaw) * finalSpeed));
    }

    private BlockPos findHole() {
        ArrayList<BlockPos> bloks = new ArrayList<>();
        BlockPos centerPos = mc.player.getBlockPos();
        for (int i = centerPos.getX() - 3; i < centerPos.getX() + 3; i++) {
            for (int j = centerPos.getY() - 4; j < centerPos.getY() + 2; j++) {
                for (int k = centerPos.getZ() - 3; k < centerPos.getZ() + 3; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (HoleUtility.validIndestructible(pos)) {
                        bloks.add(new BlockPos(pos));
                    } else if (HoleUtility.validBedrock(pos)) {
                        bloks.add(new BlockPos(pos));
                    } else if (HoleUtility.validTwoBlockBedrockXZ(pos)) {
                        bloks.add(new BlockPos(pos));
                    } else if (HoleUtility.validTwoBlockIndestructibleXZ(pos)) {
                        bloks.add(new BlockPos(pos));
                    } else if (HoleUtility.validTwoBlockBedrockXZ1(pos)) {
                        bloks.add(new BlockPos(pos));
                    } else if (HoleUtility.validTwoBlockIndestructibleXZ1(pos)) {
                        bloks.add(new BlockPos(pos));
                    } else if (HoleUtility.validQuadBedrock(pos)) {
                        bloks.add(new BlockPos(pos));
                    } else if (HoleUtility.validQuadIndestructible(pos)) {
                        bloks.add(new BlockPos(pos));
                    }
                }
            }
        }

        float nearestDistance = 10;
        BlockPos fbp = null;
        for (BlockPos bp : bloks) {
            if (BlockPos.ofFloored(mc.player.getPos()).equals(bp)) {
                disable();
                return null;
            }
            if (mc.player.squaredDistanceTo(bp.toCenterPos()) < nearestDistance) {
                nearestDistance = (float) mc.player.squaredDistanceTo(bp.toCenterPos());
                fbp = bp;
            }
        }
        return fbp;
    }
}
