package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedGoldenAppleItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.core.PlaceManager;
import thunder.hack.events.impl.EventPlaceBlock;
import thunder.hack.events.impl.EventSync;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.injection.accesors.IMinecraftClient;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.ExplosionUtility;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlaceUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.block.RespawnAnchorBlock.CHARGES;


public class AutoAnchor extends Module {
    public AutoAnchor() {
        super("AnchorAura", Category.COMBAT);
    }


    public static Setting<Mode> mode = new Setting<>("Mode", Mode.Legit);
    public Setting<Integer> swapDelay = new Setting<>("SwapDelay", 100, 0, 1000, v -> mode.getValue() == Mode.Legit);
    public Setting<Integer> charge = new Setting<>("Charge", 5, 1, 5, v -> mode.getValue() == Mode.Legit);

    public Setting<Boolean> rotate = new Setting<>("Rotate", true, v -> mode.getValue() == Mode.Rage);
    public Setting<YawStepMode> yawStep = new Setting<>("YawStep", YawStepMode.Off, v -> rotate.getValue() && mode.getValue() == Mode.Rage);
    public Setting<Integer> yawAngle = new Setting<>("YawAngle", 54, 5, 180, v -> rotate.getValue() && yawStep.getValue() != YawStepMode.Off && mode.getValue() == Mode.Rage);
    public Setting<Boolean> strictDirection = new Setting<>("StrictDirection", true, v -> mode.getValue() == Mode.Rage);
    public Setting<Integer> placeDelay = new Setting<>("PlaceDelay", 0, 0, 1000, v -> mode.getValue() == Mode.Rage);
    public Setting<Integer> chargeDelay = new Setting<>("ChargeDelay", 0, 0, 1000, v -> mode.getValue() == Mode.Rage);
    public Setting<Integer> explodeDelay = new Setting<>("ExplodeDelay", 0, 0, 1000, v -> mode.getValue() == Mode.Rage);
    public Setting<Float> placeRange = new Setting<>("Range", 4F, 1F, 6F, v -> mode.getValue() == Mode.Rage);
    public Setting<PriorityMode> priorityMode = new Setting<>("PlacePriority", PriorityMode.MaxDamage, v -> mode.getValue() == Mode.Rage);
    public Setting<Float> enemyRange = new Setting<>("TargetRange", 8F, 4F, 20F, v -> mode.getValue() == Mode.Rage);
    public static Setting<Integer> predictTicks = new Setting<>("PredictTicks", 3, 0, 10, v -> mode.getValue() == Mode.Rage);
    public Setting<Float> minDamage = new Setting<>("MinDamage", 6F, 0F, 20F, v -> mode.getValue() == Mode.Rage);
    public Setting<Float> maxSelfDamage = new Setting<>("MaxSelfDmg", 12F, 0F, 20F, v -> mode.getValue() == Mode.Rage);
    public Setting<Boolean> pauseWhileMining = new Setting<>("PauseWhenMining", false, v -> mode.getValue() == Mode.Rage);
    public Setting<Boolean> pauseWhileGapping = new Setting<>("PauseWhenGapping", false, v -> mode.getValue() == Mode.Rage);
    public Setting<Boolean> pauseWhenAura = new Setting<>("PauseWhenAura", true, v -> mode.getValue() == Mode.Rage);
    public Setting<Float> pauseHealth = new Setting<>("PauseHealth", 2f, 0f, 10f, v -> mode.getValue() == Mode.Rage);
    public Setting<Boolean> render = new Setting<>("Render", true, v -> mode.getValue() == Mode.Rage);

    private ArrayList<BlockPos> chargedAnchors = new ArrayList<>();

    private enum Mode {
        Legit,
        Rage
    }

    @EventHandler
    public void onBlockPlace(EventPlaceBlock event) {
        if (mode.getValue() == Mode.Rage) return;
        if (event.getBlock() == Blocks.RESPAWN_ANCHOR && mc.options.useKey.isPressed()) {
            int glowSlot = InventoryUtility.getItemSlotHotbar(Items.GLOWSTONE);
            if (glowSlot == -1) return;
            new LegitThread(glowSlot, mc.player.getInventory().selectedSlot, swapDelay.getValue()).start();
        }
    }

    public class LegitThread extends Thread {
        int glowSlot, originalSlot, delay;

        public LegitThread(int glowSlot, int originalSlot, int delay) {
            this.glowSlot = glowSlot;
            this.originalSlot = originalSlot;
            this.delay = delay;
        }

        @Override
        public void run() {
            try {
                sleep(delay);
            } catch (Exception ignored) {
            }

            mc.player.getInventory().selectedSlot = glowSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(glowSlot));
            try {
                sleep(delay);
            } catch (Exception ignored) {
            }
            for (int i = 0; i < charge.getValue(); i++) {
                ((IMinecraftClient) mc).idoItemUse();
            }

            try {
                sleep(delay);
            } catch (Exception ignored) {
            }

            mc.player.getInventory().selectedSlot = originalSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));

            try {
                sleep(delay);
            } catch (Exception ignored) {
            }
            if (charge.getValue() < 5) ((IMinecraftClient) mc).idoItemUse();

            super.run();
        }
    }


    private enum YawStepMode {Off, On}

    private enum PriorityMode {MaxDamage, Balance}

    private BlockPos threadedBp = null;
    private BlockPos cachePos = null;

    private final Timer placeTimer = new Timer();
    private final Timer chargeTimer = new Timer();
    private final Timer explodeTimer = new Timer();

    private Vec3d rotations;
    private float renderDmg;

    @Override
    public void onEnable() {
        placeTimer.reset();
        chargeTimer.reset();
        explodeTimer.reset();

        rotations = null;
        threadedBp = null;
        cachePos = null;
        renderDmg = 0;
    }


    @EventHandler
    public void onEntitySync(EventSync event) {

        if (check() && mode.getValue() == Mode.Rage)
            if (!explodeAnchor())
                if (!chargeAnchor())
                    placeAnchor();

        if (rotate.getValue() && rotations != null && mode.getValue() == Mode.Rage) {
            float[] yp = PlaceUtility.calculateAngle(rotations);
            if (yawStep.getValue() == YawStepMode.On) {
                float yawDiff = MathHelper.wrapDegrees(yp[0] - ((IClientPlayerEntity) ((mc.player))).getLastYaw());
                if (Math.abs(yawDiff) > yawAngle.getValue()) {
                    yp[0] = ((IClientPlayerEntity) ((mc.player))).getLastYaw() + (yawDiff * (yawAngle.getValue() / Math.abs(yawDiff)));
                    PlaceManager.trailingBreakAction = null;
                    PlaceManager.trailingPlaceAction = null;
                    PlaceManager.trailingChargeAction = null;
                }
            }
            double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
            yp[0] = (float) (yp[0] - (yp[0] - ((IClientPlayerEntity) ((mc.player))).getLastYaw()) % gcdFix);
            yp[1] = (float) (yp[1] - (yp[1] - ((IClientPlayerEntity) ((mc.player))).getLastYaw()) % gcdFix);
            PlaceManager.setTrailingRotation(yp);
            if (placeTimer.passedMs(2000)) rotations = null;
        }
    }

    @Override
    public void onThread() {
        if (mode.getValue() == Mode.Rage)
            threadedBp = findPlacePosition();
    }

    public boolean explodeAnchor() {
        if (!explodeTimer.passedMs(explodeDelay.getValue())) return false;

        BlockPos result;
        if (cachePos != null && mc.world.getBlockState(cachePos).getBlock() instanceof RespawnAnchorBlock && mc.world.getBlockState(cachePos).get(CHARGES) != 0) {
            result = cachePos;
        } else {
            result = findAnchorTarget(getTargetsInRange(), true);
        }

        if (result == null) return false;

        SearchInvResult anchorResult = InventoryUtility.getAnchor();
        if (anchorResult.found()) {
            PlaceManager.trailingBreakAction = () -> {
                BlockHitResult bhr = handleInteractRotation(result);
                if (bhr != null) {
                    anchorResult.switchTo();
                    mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                    mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, bhr, PlayerUtility.getWorldActionId(mc.world)));
                    mc.player.swingHand(Hand.OFF_HAND);
                }
            };

            chargeTimer.reset();
            placeTimer.reset();
            explodeTimer.reset();
            return true;
        }
        return false;
    }

    public boolean chargeAnchor() {
        if (!chargeTimer.passedMs(chargeDelay.getValue())) return true;

        BlockPos result;
        if (cachePos != null && mc.world.getBlockState(cachePos).getBlock() instanceof RespawnAnchorBlock && mc.player.squaredDistanceTo(cachePos.toCenterPos()) < placeRange.getPow2Value()) {
            result = cachePos;
        } else {
            result = findAnchorTarget(getTargetsInRange(), false);
        }

        if (result == null) {
            return false;
        }

        if (chargedAnchors.contains(result)) {
            chargedAnchors.remove(result);
            return true;
        }

        if (mc.world.getBlockState(result).get(CHARGES) > 0) return true;

        SearchInvResult glowResult = InventoryUtility.getGlowStone();

        if (glowResult.found()) {
            PlaceManager.trailingChargeAction = () -> {
                glowResult.switchTo();
                BlockHitResult bhr = handleInteractRotation(result);
                if (bhr != null) {
                    mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                    mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bhr, PlayerUtility.getWorldActionId(mc.world)));
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
            };

            chargedAnchors.add(result);

            chargeTimer.reset();
            placeTimer.reset();
            explodeTimer.reset();
            return true;

        }
        return false;
    }

    public boolean placeAnchor() {
        if (!placeTimer.passedMs(placeDelay.getValue())) return false;
        if (cachePos != null && mc.world.getBlockState(cachePos).getBlock() instanceof RespawnAnchorBlock && isValidAnchorTarget(cachePos))
            return false;
        SearchInvResult anchorResult = InventoryUtility.getAnchor();
        if (threadedBp != null && anchorResult.found()) {
            PlaceManager.trailingPlaceAction = () -> {
                int prev_slot = mc.player.getInventory().selectedSlot;
                PlaceUtility.forcePlace(threadedBp, strictDirection.getValue(), Hand.MAIN_HAND, anchorResult.slot(), false, PlaceUtility.PlaceMode.Normal);
                if (threadedBp != null)
                    rotations = PlaceUtility.calcVector(threadedBp, strictDirection.getValue(), false);
                mc.player.getInventory().selectedSlot = prev_slot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prev_slot));
            };

            chargeTimer.reset();
            placeTimer.reset();
            explodeTimer.reset();

            cachePos = threadedBp;

            return true;
        }
        return false;
    }


    public void onRender3D(MatrixStack e) {
        if (cachePos != null && mode.getValue() == Mode.Rage) {
            Render3DEngine.drawBoxOutline(new Box(cachePos), new Color(0xC7FFFFFF, true), 2f);
            Render3DEngine.drawTextIn3D(String.valueOf(MathUtility.round2(renderDmg)), cachePos.toCenterPos(), 0, 0.1, 0, Render2DEngine.injectAlpha(Color.WHITE, 255));
        }

    }


    private BlockPos findPlacePosition() {
        PlayerEntity target = Thunderhack.combatManager.getNearestTarget(enemyRange.getValue());

        if (target == null)
            return null;

        BlockPos targetBp = BlockPos.ofFloored(target.getPos());

        if (PlaceUtility.canPlaceBlock(targetBp.up().up(), strictDirection.getValue(), true) || mc.world.getBlockState(targetBp.up().up()).getBlock() instanceof RespawnAnchorBlock)
            return targetBp.up().up();

        List<BlockPos> blocks = findAnchorBlocks();

        if (blocks.isEmpty())
            return null;

        BlockPos bestPos = null;

        float bestDamage = 0.0f;

        float bestBalance = 0.0f;

        for (BlockPos block : blocks) {
            float damageToSelf = mc.player.isCreative() ? 0 : ExplosionUtility.getAnchorExplosionDamage(block, mc.player);
            if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= damageToSelf + 2F) continue;
            if (damageToSelf > maxSelfDamage.getValue()) continue;

            float damageToTarget = ExplosionUtility.getAnchorExplosionDamage(block, target);

            if ((damageToTarget < minDamage.getValue())) continue;


            if (priorityMode.getValue() == PriorityMode.MaxDamage) {
                if (damageToTarget > bestDamage) {
                    bestDamage = damageToTarget;
                    bestPos = block;
                }
            } else {
                if (damageToTarget / damageToSelf > bestBalance) {
                    bestDamage = damageToTarget;
                    bestBalance = damageToTarget / damageToSelf;
                    bestPos = block;
                }
            }
        }
        if (bestPos != null) {
            renderDmg = bestDamage;
        }
        return bestPos;
    }


    private boolean check() {
        if ((pauseWhileMining.getValue() && mc.interactionManager.isBreakingBlock())
                || (pauseWhileGapping.getValue() && mc.player.getActiveItem().getItem() instanceof EnchantedGoldenAppleItem)
                || (mc.player.getHealth() + mc.player.getAbsorptionAmount() < pauseHealth.getValue())
                || (pauseWhenAura.getValue() && Thunderhack.moduleManager.get(Aura.class).isEnabled())
                || Thunderhack.moduleManager.get(Burrow.class).isEnabled()
                || (Thunderhack.moduleManager.get(Surround.class).isEnabled() && !Surround.inactivityTimer.passedMs(500))
                || (Thunderhack.moduleManager.get(AutoTrap.class).isEnabled() && !AutoTrap.inactivityTimer.passedMs(500))
                || (Thunderhack.moduleManager.get(Blocker.class).isEnabled() && !Blocker.inactivityTimer.passedMs(500))
                || (Thunderhack.moduleManager.get(HoleFill.class).isEnabled() && !HoleFill.inactivityTimer.passedMs(500))
        ) {
            return false;
        }
        if (pauseWhileGapping.getValue() && mc.options.useKey.isPressed()) {
            return false;
        }
        if (!(mc.player.getInventory().getMainHandStack().getItem() == Items.RESPAWN_ANCHOR)) {
            return InventoryUtility.getAnchor().found() && InventoryUtility.getGlowStone().found();
        }
        return true;
    }

    private List<BlockPos> findAnchorBlocks() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos centerPos = ((mc.player)).getBlockPos();
        int r = (int) Math.ceil(placeRange.getValue()) + 1;
        int h = placeRange.getValue().intValue();
        for (int i = centerPos.getX() - r; i < centerPos.getX() + r; i++) {
            for (int j = centerPos.getY() - h; j < centerPos.getY() + h; j++) {
                for (int k = centerPos.getZ() - r; k < centerPos.getZ() + r; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (mc.player.squaredDistanceTo(pos.toCenterPos()) < placeRange.getPow2Value() + 2 && PlaceUtility.canPlaceBlock(pos, strictDirection.getValue(), true)) {
                        positions.add(pos);
                    }
                }
            }
        }
        return positions;
    }

    private List<PlayerEntity> getTargetsInRange() {
        List<PlayerEntity> list = new ArrayList<>();
        for (PlayerEntity player : Thunderhack.asyncManager.getAsyncPlayers()) {
            if (Thunderhack.friendManager.isFriend(player)) continue;
            if (player == mc.player) continue;
            if (player.distanceTo(((mc.player))) > enemyRange.getValue()) continue;
            if (player.isDead()) continue;
            if (player.getHealth() + player.getAbsorptionAmount() <= 0) continue;
            list.add(player);
        }
        return list.stream().sorted(Comparator.comparing(e -> (e.distanceTo(((mc.player)))))).limit(1).collect(Collectors.toList());
    }

    private BlockHitResult handleInteractRotation(BlockPos pos) {
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
        if (rotate.getValue()) rotations = new Vec3d(pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D);
        return new BlockHitResult(new Vec3d(pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D), Direction.UP, pos, false);
    }

    private BlockPos findAnchorTarget(List<PlayerEntity> targetsInRange, boolean charged) {
        BlockPos bestAnchor = null;
        List<BlockPos> anchorsInRange = getAnchorsInRange();
        if (anchorsInRange.isEmpty()) return null;
        double bestDamage = 0.0D;
        for (BlockPos anchor : anchorsInRange) {
            if (((!charged) || mc.world.getBlockState(anchor).get(CHARGES) != 0)) {
                double selfDamage = mc.player.isCreative() ? 0f : ExplosionUtility.getAnchorExplosionDamage(anchor, mc.player);
                if (selfDamage > maxSelfDamage.getValue()) continue;
                double damage = 0.0D;
                for (PlayerEntity target : targetsInRange) {
                    double targetDamage = ExplosionUtility.getAnchorExplosionDamage(anchor, target);
                    damage += targetDamage;
                }
                if (damage < minDamage.getValue() || damage < selfDamage) continue;
                if (damage > bestDamage || bestDamage == 0D) {
                    bestDamage = damage;
                    bestAnchor = anchor;
                }
            }
        }
        return bestAnchor;
    }


    private List<BlockPos> getAnchorsInRange() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos centerPos = ((mc.player)).getBlockPos();
        int r = (int) Math.ceil(placeRange.getValue()) + 1;
        int h = placeRange.getValue().intValue();
        for (int i = centerPos.getX() - r; i < centerPos.getX() + r; i++) {
            for (int j = centerPos.getY() - h; j < centerPos.getY() + h; j++) {
                for (int k = centerPos.getZ() - r; k < centerPos.getZ() + r; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR && isValidAnchorTarget(pos)) {
                        positions.add(pos);
                    }
                }
            }
        }
        return positions;
    }

    private boolean isValidAnchorTarget(BlockPos bp) {
        if (PlaceUtility.getEyesPos(((mc.player))).distanceTo(bp.toCenterPos()) > placeRange.getValue()) return false;
        return !(ExplosionUtility.getAnchorExplosionDamage(bp, mc.player) + 2F >= mc.player.getHealth() + mc.player.getAbsorptionAmount());
    }
}
