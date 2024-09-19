package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventKeyboardInput;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventPlayerJump;
import thunder.hack.events.impl.EventPlayerTravel;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.world.HoleUtility;

import java.util.ArrayList;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class HoleSnap extends Module {
    public HoleSnap() {
        super("HoleSnap", Category.MOVEMENT);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Yaw);
    private final Setting<Integer> searchRange = new Setting<>("Search Range", 5, 1, 20);
    private final Setting<Integer> searchFOV = new Setting<>("Search FOV", 360, 1, 360);
    private final Setting<Boolean> useTimer = new Setting<>("Use Timer", false);
    private final Setting<Float> timerValue = new Setting<>("Timer Value", 1f, 0f, 20f);

    private final Setting<SettingGroup> autoDisable = new Setting<>("Auto Disable", new SettingGroup(false, 0));
    private final Setting<Boolean> onDeath = new Setting<>("On Death", true).addToGroup(autoDisable);
    private final Setting<Boolean> onInHole = new Setting<>("In Hole", true).addToGroup(autoDisable);
    private final Setting<Boolean> onNoHoleFound = new Setting<>("No Holes", false).addToGroup(autoDisable);

    private BlockPos hole;
    private float prevClientYaw;

    @Override
    public void onEnable() {
        hole = findHole();
        if (useTimer.getValue()) {
            ThunderHack.TICK_TIMER = timerValue.getValue();
        }
    }

    @Override
    public void onDisable() {
        hole = null;
        if (useTimer.getValue()) {
            ThunderHack.TICK_TIMER = 1;
        }
    }

    @Override
    public void onUpdate() {
        if (onDeath.getValue() && mc.player != null
                && (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= 0 || mc.player.isDead()))
            disable(isRu() ? "Вы умерли! Выключаюсь..." : "You died! Disabling...");
        if (mc.player != null && mc.player.getBlockPos().equals(hole) && onInHole.getValue())
            disable(isRu() ? "Ты в холке! Отключаю.." : "You're in a hole already! Disabling..");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    private void onMove(EventMove event) {
        if (mc.player == null) return;

        BlockPos bp = BlockPos.ofFloored(mc.player.getPos());

        for (int i = 1; i < 5; i++) {
            if (!HoleUtility.isSingleHole(bp.down(i))) continue;

            Vec3d center = new Vec3d(
                    Math.floor(mc.player.getX()) + 0.5,
                    mc.player.getY(),
                    Math.floor(mc.player.getZ()) + 0.5
            );
            if (center.distanceTo(mc.player.getPos()) < 0.15f) {
                event.setX(0);
                event.setZ(0);
                event.cancel();
            }

            break;
        }

        if (mc.player != null && mc.player.horizontalCollision && mc.player.isOnGround())
            mc.player.jump();

        if (mode.getValue() == Mode.Move && hole != null) {
            final double newYaw = Math.cos(Math.toRadians(getNewYaw(hole.toCenterPos()) + 90.0f));
            final double newPitch = Math.sin(Math.toRadians(getNewYaw(hole.toCenterPos()) + 90.0f));
            final double diffX = hole.toCenterPos().getX() - mc.player.getX();
            final double diffZ = hole.toCenterPos().getZ() - mc.player.getZ();
            final double x = 0.29 * newYaw;
            final double z = 0.29 * newPitch;

            event.setX(Math.abs(x) < Math.abs(diffX) ? x : diffX);
            event.setZ(Math.abs(z) < Math.abs(diffZ) ? z : diffZ);
            event.cancel();
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void modifyVelocity(EventPlayerTravel event) {
        if (mc.player == null) return;
        if (mc.player.age % 10 == 0) hole = findHole();

        doYawModeLogic(event.isPre());
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void modifyJump(EventPlayerJump event) {
        if (mc.player == null) return;

        doYawModeLogic(event.isPre());
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onKeyboardInput(EventKeyboardInput e) {
        if (mc.player == null || mode.getValue() != Mode.Yaw || hole == null)
            return;

        mc.player.input.movementForward = 1;
    }

    private @Nullable BlockPos findHole() {
        if (mc.player == null)
            return null;

        ArrayList<BlockPos> blocks = new ArrayList<>();
        BlockPos centerPos = mc.player.getBlockPos();

        for (int i = centerPos.getX() - searchRange.getValue(); i < centerPos.getX() + searchRange.getValue(); i++) {
            for (int j = centerPos.getY() - 4; j < centerPos.getY() + 2; j++) {
                for (int k = centerPos.getZ() - searchRange.getValue(); k < centerPos.getZ() + searchRange.getValue(); k++) {
                    BlockPos pos = new BlockPos(i, j, k);;
                    if (HoleUtility.isSingleHole(pos) && InteractionUtility.isVecInFOV(pos.toCenterPos(), searchFOV.getValue() / 2)) {
                        blocks.add(new BlockPos(pos));
                    }
                }
            }
        }

        float nearestDistance = 10;
        BlockPos fbp = null;

        for (BlockPos bp : blocks) {
            if (BlockPos.ofFloored(mc.player.getPos()).equals(bp) && onInHole.getValue()) {
                disable(isRu() ? "Ты в холке! Отключаю.." : "You're in a hole already! Disabling..");
                return null;
            }
            if (mc.player.squaredDistanceTo(bp.toCenterPos()) < nearestDistance) {
                nearestDistance = (float) mc.player.squaredDistanceTo(bp.toCenterPos());
                fbp = bp;
            }
        }

        if (fbp == null && onNoHoleFound.getValue())
            disable(isRu() ? "Холка не найдена! Выключение..." : "Hole not found! Disabling...");

        return fbp;
    }

    private float getNewYaw(@NotNull Vec3d pos) {
        if (mc.player == null)
            return 0;

        return mc.player.getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(pos.getZ() - mc.player.getZ(), pos.getX() - mc.player.getX())) - mc.player.getYaw() - 90);
    }

    private void doYawModeLogic(boolean isPreEvent) {
        if (hole == null || mc.player == null || mode.getValue() != Mode.Yaw) return;

        if (isPreEvent) {
            prevClientYaw = mc.player.getYaw();
            mc.player.setYaw(InteractionUtility.calculateAngle(hole.toCenterPos())[0]);
        } else
            mc.player.setYaw(prevClientYaw);
    }

    private enum Mode {
        Move,
        Yaw
    }

    private enum JumpMode {
        Normal,
        Move
    }
}
