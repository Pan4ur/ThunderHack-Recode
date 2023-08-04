package thunder.hack.modules.combat;

import com.mojang.logging.LogUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import thunder.hack.Thunderhack;
import thunder.hack.core.ModuleManager;
import thunder.hack.core.PlaceManager;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.injection.accesors.IWorldRenderer;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.modules.player.SpeedMine;
import thunder.hack.modules.render.HoleEsp;
import thunder.hack.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtil;
import thunder.hack.utility.player.PlaceUtility;
import thunder.hack.utility.player.PlayerUtil;

public class CevBreaker extends Module {
    private final Setting<Double> range = new Setting<>("Target Range", 5., 0., 7.);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);
    private final Setting<Boolean> strictDirection = new Setting<>("Strict Direction", false);
    private final Setting<Integer> actionShift = new Setting<>("Place Per Tick", 1, 1, 5);
    private final Setting<Integer> actionInterval = new Setting<>("Delay", 0, 0, 5);
    private final Setting<BreakMode> breakMode = new Setting<>("Break Mode", BreakMode.Packet);
    private final Setting<Boolean> swing = new Setting<>("Swing", true);

    private PlayerEntity target;
    private Vec3d rotations;
    private BlockPos targetSurroundBlockPos;

    public CevBreaker() {
        super("CevBreaker", "Банит гринхолфагов", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        findTarget();
        targetSurroundBlockPos = null;

        super.onEnable();
    }

    @Override
    public void onUpdate() {
        // Find target
        if (target == null
                || target.isDead()
                || target.getHealth() + target.getAbsorptionAmount() <= 0
                || target.distanceTo(((mc.player))) > range.getValue())
            findTarget();

        // Checking if structure placed, start breaking, place crystal, if it is necessary
        if (mc.world.getBlockState(target.getBlockPos().add(0, 3, 0)).getBlock().equals(Blocks.OBSIDIAN)) {
            LogUtils.getLogger().warn(String.valueOf(SpeedMine.progress));
            if (SpeedMine.progress >= 90 && SpeedMine.minePosition.equals(target.getBlockPos().add(0, 3, 0))) {
                placeCrystal(target.getBlockPos().add(0, 3, 0));
                return;
            } else {
                final boolean[] globalReturn = {false};

                ((IWorldRenderer) mc.worldRenderer).getBlockBreakingInfos().forEach((integer, destroyBlockProgress) -> {
                    if (target.getBlockPos().add(0, 3, 0).equals(destroyBlockProgress.getPos())) {
                        if (destroyBlockProgress.getStage() >= 90) {
                            placeCrystal(target.getBlockPos().add(0, 3, 0));
                        }
                        globalReturn[0] = true;
                    }
                });

                if (globalReturn[0]) return;
            }

            if (breakMode.getValue() == BreakMode.Packet && ModuleManager.speedMine.isEnabled() && SpeedMine.progress != 0)
                return;
            else if (breakMode.getValue() == BreakMode.Vanilla)
                mc.player.getInventory().selectedSlot = InventoryUtil.findItem(PickaxeItem.class) != -1 ? InventoryUtil.findItem(PickaxeItem.class) : mc.player.getInventory().selectedSlot;


            // Breaking obsidian
            mc.interactionManager.attackBlock(target.getBlockPos().add(0, 3, 0), PlaceUtility.getBreakDirection(target.getBlockPos().add(0, 3, 0), true));
            if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
        }

        // Generate structure
        if (targetSurroundBlockPos == null) {
            BlockPos[] surroundBlocks = new BlockPos[]{
                    target.getBlockPos().add(0, 1, 0).west(),
                    target.getBlockPos().add(0, 1, 0).south(),
                    target.getBlockPos().add(0, 1, 0).north(),
                    target.getBlockPos().add(0, 1, 0).east()
            };
            targetSurroundBlockPos = surroundBlocks[0];

            for (BlockPos pos : surroundBlocks) {
                if (!mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) {
                    targetSurroundBlockPos = pos;
                    break;
                }
                if (!mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR))
                    targetSurroundBlockPos = pos;
            }
        }

        // Build structure
        int actions = 0;
        while (actions < actionShift.getValue()) {
            if (!mc.world.getBlockState(targetSurroundBlockPos.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) {
                BlockPos targetedPos = target.getBlockPos().add(0, 3, 0);

                // Place obsidian under target's head
                if (placeObsidian(targetedPos)) {
                    if (breakMode.getValue() == BreakMode.Packet && ModuleManager.speedMine.isEnabled() && SpeedMine.progress != 0) {
                        return;
                    }

                    // Breaking obsidian
                    mc.interactionManager.attackBlock(targetedPos, PlaceUtility.getBreakDirection(targetedPos, true));
                    if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
                }

                actions++;
                continue;
            } else if (!mc.world.getBlockState(targetSurroundBlockPos.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) {
                // Second step place obsidian
                placeObsidian(targetSurroundBlockPos.add(0, 2, 0));
                actions++;
                continue;
            }

            // First step place obsidian
            actions += placeObsidian(targetSurroundBlockPos.add(0, 1, 0)) ? 1 : 0;
        }

        // Rotations
        if (rotate.getValue()) {
            float[] yp = PlaceUtility.calculateAngle(rotations);

            double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
            yp[0] = (float) (yp[0] - (yp[0] - ((IClientPlayerEntity) ((mc.player))).getLastYaw()) % gcdFix);
            yp[1] = (float) (yp[1] - (yp[1] - ((IClientPlayerEntity) ((mc.player))).getLastYaw()) % gcdFix);

            PlaceManager.setTrailingRotation(yp);
            rotations = null;
        }
    }

    private void findTarget() {
        if (!(AutoCrystal.CAtarget instanceof PlayerEntity)
                || (!HoleEsp.validBedrock(AutoCrystal.CAtarget.getBlockPos())
                && !HoleEsp.validIndestructible(AutoCrystal.CAtarget.getBlockPos()))
                || AutoCrystal.CAtarget.distanceTo(((mc.player))) > range.getValue()) {

            for (PlayerEntity player : Thunderhack.asyncManager.getAsyncPlayers()) {
                if (Thunderhack.friendManager.isFriend(player)) continue;
                if (player == mc.player) continue;
                if (player.distanceTo(((mc.player))) > range.getValue()) continue;
                if (player.isDead()) continue;
                if (player.getHealth() + player.getAbsorptionAmount() <= 0) continue;
                if (!HoleEsp.validBedrock(player.getBlockPos())
                        && !HoleEsp.validIndestructible(player.getBlockPos()))
                    continue;

                target = player;
                break;
            }

            if (target == null) {
                Thunderhack.notificationManager.publicity("CevBreaker",
                        MainSettings.language.getValue() == MainSettings.Language.RU ? "Не удалось найти подходящую цель. Если игрок есть, он не в холке." : "There are no valid target. If player exists, maybe he not in hole.", 500, Notification.Type.ERROR);

                disable();
                return;
            }
        } else target = (PlayerEntity) AutoCrystal.CAtarget;
    }

    private boolean placeCrystal(BlockPos pos) {
        if (mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL)) {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.OFF_HAND,
                    handlePlaceRotation(pos), PlayerUtil.getWorldActionId(mc.world)));

            if (swing.getValue()) mc.player.swingHand(Hand.OFF_HAND);
            return true;
        }

        int preSlot = mc.player.getInventory().selectedSlot;
        int crystalSlot = InventoryUtil.getCrystalSlot();

        if (crystalSlot == -1) {
            return false;
        } else if (mc.player.getInventory().selectedSlot != crystalSlot) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(crystalSlot));
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                    handlePlaceRotation(pos), PlayerUtil.getWorldActionId(mc.world)));
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(preSlot));
        }
        return true;
    }

    private BlockHitResult handlePlaceRotation(BlockPos pos) {
        Vec3d eyesPos = PlaceUtility.getEyesPos(((mc.player)));

        if (strictDirection.getValue()) {
            Vec3d closestPoint = null;
            Direction closestDirection = null;
            double closestDistance = 999D;

            for (Vec3d point : PlaceUtility.multiPoint) {
                Vec3d p = new Vec3d(pos.getX() + point.getX(), pos.getY() + point.getY(), pos.getZ() + point.getZ());
                double dist = p.distanceTo(eyesPos);
                if ((dist < closestDistance && closestDirection == null)) {
                    closestPoint = p;
                    closestDistance = dist;
                }

                BlockHitResult result = mc.world.raycast(new RaycastContext(eyesPos, p, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ((mc.player))));

                if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(pos)) {
                    double visDist = result.getPos().distanceTo(eyesPos);
                    if (closestDirection == null || visDist < closestDistance) {
                        closestDirection = result.getSide();
                        closestDistance = visDist;
                        closestPoint = result.getPos();
                    }
                }
            }

            if (closestPoint != null) {
                if (rotate.getValue()) {
                    rotations = closestPoint;
                }

                return new BlockHitResult(closestPoint, closestDirection == null ? Direction.getFacing(eyesPos.x - closestPoint.x, eyesPos.y - closestPoint.y, eyesPos.z - closestPoint.z) : closestDirection, pos, false);
            }

            return null;
        }

        if (rotate.getValue()) {
            rotations = new Vec3d(pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D);
        }
        return new BlockHitResult(new Vec3d(pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D), Direction.UP, pos, false);
    }

    private boolean placeObsidian(BlockPos pos) {
        int slot = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN);

        if (slot == -1) {
            Thunderhack.notificationManager.publicity("CevBreaker", MainSettings.language.getValue() == MainSettings.Language.RU ? "В хотбаре не найден обсидиан!" : "No obsidian in hotbar", 500, Notification.Type.ERROR);
            disable();
            return false;
        }

        return PlaceUtility.place(pos, rotate.getValue(), strictDirection.getValue(), Hand.MAIN_HAND, slot, false);
    }

    public enum BreakMode {
        Packet,
        Vanilla
    }
}
