package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.EventEntityRemoved;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.injection.accesors.IWorldRenderer;
import thunder.hack.modules.Module;
import thunder.hack.modules.player.SpeedMine;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.math.ExplosionUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.world.HoleUtility;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static thunder.hack.modules.client.MainSettings.isRu;

public final class CevBreaker extends Module {
    private final Setting<Float> range = new Setting<>("Target Range", 5.f, 0.f, 7.f);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);
    private final Setting<Boolean> trap = new Setting<>("Trap Player", false);
    private final Setting<Boolean> autoSwap = new Setting<>("Auto Swap", true);
    private final Setting<Boolean> oldMode = new Setting<>("1.12 Mode", false);
    private final Setting<Boolean> antiWeakness = new Setting<>("Anti Weakness", false);
    private final Setting<Integer> blocksPerTick = new Setting<>("Block/Tick", 1, 1, 5);
    private final Setting<Integer> placeDelay = new Setting<>("Delay/Place", 0, 0, 5);
    private final Setting<Integer> breakCrystalDelay = new Setting<>("BreakCrystalDelay", 50, 0, 500);

    private final Setting<BreakMode> breakMode = new Setting<>("Break Mode", BreakMode.Packet);
    private final Setting<Boolean> swing = new Setting<>("Swing", true);
    private final Setting<Boolean> autoDisable = new Setting<>("Auto Disable", true);

    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.Normal);
    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Strict);

    private final Setting<Parent> pause = new Setting<>("Pause", new Parent(false, 0));
    private final Setting<Boolean> onEat = new Setting<>("Pause On Eat", false).withParent(pause);
    private final Setting<Boolean> onMine = new Setting<>("Pause On Mine", false).withParent(pause);
    private final Setting<Boolean> onAura = new Setting<>("Pause On Aura", false).withParent(pause);

    private final Setting<Parent> render = new Setting<>("Render", new Parent(false, 0));
    private final Setting<Boolean> renderTrap = new Setting<>("Render Trap", false).withParent(render);
    private final Setting<ColorSetting> trapFillColor = new Setting<>("Trap Fill Color", new ColorSetting(new Color(255, 0, 0, 50)), value -> renderTrap.getValue()).withParent(render);
    private final Setting<ColorSetting> trapLineColor = new Setting<>("Trap Line Color", new ColorSetting(new Color(255, 0, 0, 200)), value -> renderTrap.getValue()).withParent(render);
    private final Setting<Integer> trapLineWidth = new Setting<>("Trap Line Width", 2, 1, 5, value -> renderTrap.getValue()).withParent(render);

    private final Setting<Boolean> renderStructure = new Setting<>("Render Struct", true).withParent(render);
    private final Setting<ColorSetting> structureFillColor = new Setting<>("Struct Fill Color", new ColorSetting(new Color(100, 0, 200, 50)), value -> renderStructure.getValue()).withParent(render);
    private final Setting<ColorSetting> structureLineColor = new Setting<>("Struct Line Color", new ColorSetting(new Color(100, 0, 200, 200)), value -> renderStructure.getValue()).withParent(render);
    private final Setting<Integer> structureLineWidth = new Setting<>("Struct Line Width", 2, 1, 5, value -> renderTrap.getValue()).withParent(render);

    private final Setting<Boolean> renderTarget = new Setting<>("Render Target", false).withParent(render);
    private final Setting<ColorSetting> targetFillColor = new Setting<>("Target Fill Color", new ColorSetting(new Color(255, 111, 0, 50)), value -> renderTarget.getValue()).withParent(render);
    private final Setting<ColorSetting> targetLineColor = new Setting<>("Target Line Color", new ColorSetting(new Color(255, 111, 0, 200)), value -> renderTarget.getValue()).withParent(render);
    private final Setting<Integer> targetLineWidth = new Setting<>("Target Line Width", 2, 1, 5, value -> renderTarget.getValue()).withParent(render);

    private BlockPos currentMineBlockPos;
    private boolean mine = false;
    private boolean newCycle;

    private int delay = 0;
    private double placeCrystalProgress;
    private PlayerEntity target;
    private Vec3d rotations;
    private BlockPos targetSurroundBlockPos;
    private boolean canPlaceBlock;

    private final ConcurrentHashMap<BlockPos, Long> renderTrapPoses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<BlockPos, Long> renderStructurePoses = new ConcurrentHashMap<>();

    private static CevBreaker instance;

    public CevBreaker() {
        super("CevBreaker", Category.COMBAT);
        instance = this;
    }

    @Override
    public void onEnable() {
        findTarget();

        newCycle = true;
        placeCrystalProgress = .9;
        targetSurroundBlockPos = null;
        canPlaceBlock = true;

        if (breakMode.getValue() == BreakMode.Packet && ModuleManager.speedMine.isDisabled() && autoDisable.getValue()) {
            ThunderHack.notificationManager.publicity(getName(), isRu() ? "Для использования пакетного копания необходимо включить и настроить модуль SpeedMine" : "For using packet mine is necessary to enable and config SpeedMine", 5, Notification.Type.ERROR);
            disable(isRu() ? "Для использования пакетного копания необходимо включить и настроить модуль SpeedMine" : "For using packet mine is necessary to enable and config SpeedMine");
        }
    }

    @EventHandler
    public void onSync(@SuppressWarnings("unused") EventSync e) {
        if (fullNullCheck() || shouldPause()) return;

        // Find target
        if (target == null || target.isDead() || target.getHealth() + target.getAbsorptionAmount() <= 0 || target.distanceTo(((mc.player))) > range.getValue()) {
            target = null;
            findTarget();
            return;
        }
        if (delay < placeDelay.getValue()) {
            delay++;
            return;
        }

        // To prevent getting stuck we will mine necessary blocks
        if (!mc.world.getBlockState(target.getBlockPos().up(4)).isAir() && oldMode.getValue()) {
            startMine(target.getBlockPos().up(4));
            return;
        }
        if (!mc.world.getBlockState(target.getBlockPos().up(3)).isAir()) {
            startMine(target.getBlockPos().up(3));
            placeCrystalProgress = .1;
            return;
        }

        // Prevent SpeedMine stuck
        if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL && mc.world.getBlockState(target.getBlockPos().up(2)).getBlock().equals(Blocks.OBSIDIAN)) {
            placeCrystal();
        }

        delay = 0;
        int blocksPlaced = 0;

        // Checking if structure placed, start breaking. (Place crystal, if it is necessary)
        if (mc.world.getBlockState(target.getBlockPos().up(2)).getBlock().equals(Blocks.OBSIDIAN)) {
            if (SpeedMine.progress >= placeCrystalProgress && target.getBlockPos().up(2).equals(SpeedMine.minePosition)) {
                placeCrystal();
                return;
            } else {
                for (Map.Entry<Integer, BlockBreakingInfo> entry : ((IWorldRenderer) mc.worldRenderer).getBlockBreakingInfos().int2ObjectEntrySet()) {
                    BlockBreakingInfo destroyBlockProgress = entry.getValue();
                    if (target.getBlockPos().up(2).equals(destroyBlockProgress.getPos())) {

                        if (destroyBlockProgress.getStage() >= placeCrystalProgress * 10) {
                            placeCrystal();
                        }
                        return;
                    }
                }
            }

            startMine(target.getBlockPos().up(2));
        }

        BlockPos[] surroundBlocks = new BlockPos[]{target.getBlockPos().up().west(), target.getBlockPos().up().south(), target.getBlockPos().up().north(), target.getBlockPos().up().east()};

        // Generate structure
        if (targetSurroundBlockPos == null) {
            targetSurroundBlockPos = surroundBlocks[0];

            for (BlockPos pos : surroundBlocks) {
                if (!mc.world.getBlockState(pos.add(0, 2, 0)).isAir()) {
                    targetSurroundBlockPos = pos;
                    break;
                }
                if (!mc.world.getBlockState(pos.add(0, 1, 0)).isAir()) targetSurroundBlockPos = pos;
            }
        }

        // Trap
        if (trap.getValue()) {
            List<BlockPos> trapBlocks = new ArrayList<>();
            for (BlockPos surroundBlock : HoleUtility.getSurroundPoses(target.getPos()).stream().map(BlockPos::up).toList()) {
                if (InteractionUtility.canPlaceBlock(surroundBlock, interact.getValue(), false)) {
                    trapBlocks.add(surroundBlock);
                }
            }
            for (BlockPos headPos : HoleUtility.getHolePoses(target.getPos()).stream().map(pos -> pos.up(2)).filter(pos -> !pos.equals(currentMineBlockPos)).toList()) {
                if (InteractionUtility.canPlaceBlock(headPos, interact.getValue(), false)) {
                    trapBlocks.add(headPos);
                }
            }

            for (BlockPos trapBlock : trapBlocks) {
                if (blocksPlaced >= blocksPerTick.getValue()) return;
                if (!InteractionUtility.canPlaceBlock(trapBlock, interact.getValue(), false)) continue;
                if (placeObsidian(trapBlock)) renderTrapPoses.put(trapBlock, System.currentTimeMillis());
                blocksPlaced++;
            }
        }

        // Build structure
        while (blocksPlaced < blocksPerTick.getValue()) {
            if (InteractionUtility.canPlaceBlock(targetSurroundBlockPos.add(0, 2, 0), interact.getValue(), false)) {
                BlockPos targetedPos = target.getBlockPos().add(0, 2, 0);
                if (newCycle) {
                    // Place obsidian under target's head
                    if (placeObsidian(targetedPos)) {
                        renderStructurePoses.put(targetedPos, System.currentTimeMillis());
                        startMine(targetedPos);

                        newCycle = false;
                        break;
                    }
                }

                blocksPlaced++;
                continue;
            } else if (InteractionUtility.canPlaceBlock(targetSurroundBlockPos.add(0, 1, 0), interact.getValue(), false)) {
                // Second step place obsidian
                if (placeObsidian(targetSurroundBlockPos.add(0, 1, 0)))
                    renderStructurePoses.put(targetSurroundBlockPos.add(0, 1, 0), System.currentTimeMillis());
                blocksPlaced++;
                continue;
            }

            // First step place obsidian
            if (InteractionUtility.canPlaceBlock(targetSurroundBlockPos, interact.getValue(), false) && placeObsidian(targetSurroundBlockPos))
                renderStructurePoses.put(targetSurroundBlockPos, System.currentTimeMillis());

            blocksPlaced++;
        }

        // Rotations
        if (rotate.getValue() && rotations != null) {
            float[] yp = InteractionUtility.calculateAngle(rotations);
            final double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
            yp[0] = (float) (yp[0] - (yp[0] - ((IClientPlayerEntity) mc.player).getLastYaw()) % gcdFix);
            yp[1] = (float) (yp[1] - (yp[1] - ((IClientPlayerEntity) mc.player).getLastYaw()) % gcdFix);
            mc.player.setYaw(yp[0]);
            mc.player.setPitch(yp[1]);
            rotations = null;
        }
    }

    private boolean shouldPause() {
        return (onAura.getValue() && ModuleManager.aura.isEnabled()) || (onMine.getValue() && PlayerUtility.isMining()) || (onEat.getValue() && PlayerUtility.isEating());
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (renderTrap.getValue()) {
            renderTrapPoses.forEach((pos, time) -> {
                if (System.currentTimeMillis() - time > 500) {
                    renderTrapPoses.remove(pos);
                } else {
                    Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(new Box(pos), Render2DEngine.injectAlpha(trapFillColor.getValue().getColorObject(), (int) (100f * (trapFillColor.getValue().getColorObject().getAlpha() - ((System.currentTimeMillis() - time) / 500f))))));
                    Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(new Box(pos), trapLineColor.getValue().getColorObject(), trapLineWidth.getValue()));
                }
            });
        }
        if (renderStructure.getValue()) {
            renderStructurePoses.forEach((pos, time) -> {
                if (System.currentTimeMillis() - time > 500) {
                    renderTrapPoses.remove(pos);
                } else {
                    Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(new Box(pos), Render2DEngine.injectAlpha(structureFillColor.getValue().getColorObject(), (int) (structureFillColor.getValue().getColorObject().getAlpha() * (1f - ((System.currentTimeMillis() - time) / 500f))))));
                    Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(new Box(pos), structureLineColor.getValue().getColorObject(), structureLineWidth.getValue()));
                }
            });
        }
        if (renderTarget.getValue()) {
            Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(new Box(target.getBlockPos().up(3)), targetFillColor.getValue().getColorObject()));
            Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(new Box(target.getBlockPos().up(3)), targetLineColor.getValue().getColorObject(), targetLineWidth.getValue()));
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onEntityRemove(@NotNull EventEntityRemoved e) {
        if (e.entity == null || target == null) return;
        if (e.entity.getBlockPos().equals(target.getBlockPos().up(3)) && e.entity instanceof EndCrystalEntity) {
            newCycle = true;
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPacket(PacketEvent.Receive e) {
        if (target == null) return;
        if (e.getPacket() instanceof BlockUpdateS2CPacket pac) {
            if (pac.getPos().equals(target.getBlockPos().up(2)) && pac.getState().isAir()) {
                for (Entity entity : ThunderHack.asyncManager.getAsyncEntities()) {
                    if (entity instanceof EndCrystalEntity endCrystal && entity.getBlockPos().equals(target.getBlockPos().add(0, 3, 0))) {
                        canPlaceBlock = false;
                        startBreakCrystalThread(endCrystal);
                    }
                }
            }
        }
    }

    private void startBreakCrystalThread(EndCrystalEntity endCrystal) {
        if (mc.player == null || mc.interactionManager == null) return;

        ThunderHack.asyncManager.run(() -> {
            final int preSlot = mc.player.getInventory().selectedSlot;
            if (antiWeakness.getValue() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
                SearchInvResult swordResult = InventoryUtility.getAntiWeaknessItem();
                if (autoSwap.getValue()) swordResult.switchTo();
            }

            mc.interactionManager.attackEntity(mc.player, endCrystal);
            sendPacket(PlayerInteractEntityC2SPacket.attack(endCrystal, mc.player.isSneaking()));
            sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

            if (antiWeakness.getValue() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS))
                if (autoSwap.getValue()) InventoryUtility.switchTo(preSlot);

            try {
                Thread.sleep(breakCrystalDelay.getValue() / 2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            canPlaceBlock = true;
        }, breakCrystalDelay.getValue() / 2);
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPostSync(EventPostSync e) {
        if (shouldPause() || mc.world == null || mc.player == null || mc.interactionManager == null) return;

        // Normal breaking block
        if (mine && !mc.world.getBlockState(currentMineBlockPos).isReplaceable()) {
            if (breakMode.getValue() == BreakMode.Normal) {
                if (autoSwap.getValue()) {
                    SearchInvResult pickResult = InventoryUtility.findInInventory(stack -> stack.getItem() instanceof PickaxeItem);
                    InventoryUtility.switchTo(pickResult.slot());
                }

                InteractionUtility.BreakData bData = InteractionUtility.getBreakData(currentMineBlockPos, InteractionUtility.Interact.Strict);
                if (bData == null) return;
                mc.interactionManager.updateBlockBreakingProgress(currentMineBlockPos, bData.dir());
                if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
            }
        } else {
            mine = false;
        }
    }

    private void startMine(BlockPos pos) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;

        switch (breakMode.getValue()) {
            case Normal -> {
                mine = true;
                currentMineBlockPos = pos;
            }
            case Packet -> {
                if (ModuleManager.speedMine.isEnabled() && SpeedMine.progress != 0) return;
                if (SpeedMine.minePosition != null && !mc.world.getBlockState(SpeedMine.minePosition).isAir()) return;

                InteractionUtility.BreakData bData = InteractionUtility.getBreakData(pos, interact.getValue());
                if (bData == null) return;

                SpeedMine.minePosition = pos;
                mc.interactionManager.attackBlock(pos, bData.dir());
                if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    private void findTarget() {
        for (PlayerEntity player : ThunderHack.asyncManager.getAsyncPlayers()) {
            if (ThunderHack.friendManager.isFriend(player)) continue;
            if (player == mc.player) continue;
            if (player.distanceTo(((mc.player))) > range.getValue()) continue;
            if (player.isDead()) continue;
            if (player.getHealth() + player.getAbsorptionAmount() <= 0) continue;
            if (!HoleUtility.isHole(player.getBlockPos())) continue;

            target = player;
            break;
        }

        if (target == null && autoDisable.getValue()) {
            ThunderHack.notificationManager.publicity("CevBreaker", isRu() ? "Не удалось найти подходящую цель. Если игрок есть, он не в холке." : "There are no valid target. If player exists, maybe he not in hole.", 3, Notification.Type.ERROR);
            disable(isRu() ? "Не удалось найти подходящую цель. Если игрок есть, он не в холке." : "There are no valid target. If player exists, maybe he not in hole.");
        }
    }

    private void placeCrystal() {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;

        BlockPos pos = target.getBlockPos().up(2);
        for (Entity entity : ThunderHack.asyncManager.getAsyncEntities()) {
            if (entity instanceof EndCrystalEntity && entity.getBlockPos().equals(pos.up())) {
                return;
            }
        }

        BlockHitResult pData = getPlaceData(pos);

        // Place crystal
        if (pData != null) {
            if (mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL)) {
                mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, pData);
                sendPacket(new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, pData, PlayerUtility.getWorldActionId(mc.world)));

                if (swing.getValue()) mc.player.swingHand(Hand.OFF_HAND);
                return;
            }

            final int preSlot = mc.player.getInventory().selectedSlot;
            if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL) {
                SearchInvResult crystalResult = InventoryUtility.getCrystal();
                crystalResult.switchTo();
            }
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, pData);
            sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, pData, PlayerUtility.getWorldActionId(mc.world)));
            InventoryUtility.switchTo(preSlot);
            if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private @Nullable SearchInvResult getObsidian() {
        SearchInvResult result = InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN);

        if (!result.isHolding() && !autoSwap.getValue()) return null;
        if (!result.found() && autoDisable.getValue()) {
            ThunderHack.notificationManager.publicity("CevBreaker", isRu() ? "В хотбаре не найден обсидиан!" : "No obsidian found in hotbar!", 5, Notification.Type.ERROR);
            disable(isRu() ? "В хотбаре не найден обсидиан!" : "No obsidian found in hotbar!");
            return null;
        }

        return result;
    }

    private boolean placeObsidian(BlockPos pos) {
        if (!canPlaceBlock) return false;
        SearchInvResult result = getObsidian();
        if (result == null || !result.found()) return false;

        return InteractionUtility.placeBlock(pos, rotate.getValue(), interact.getValue(), placeMode.getValue(), result, autoSwap.getValue(), false);
    }

    public @Nullable BlockHitResult getPlaceData(BlockPos bp) {
        if (mc.world == null) return null;
        Block block = mc.world.getBlockState(bp).getBlock();
        Block freeSpace = mc.world.getBlockState(bp.up()).getBlock();
        Block legacyFreeSpace = mc.world.getBlockState(bp.up(2)).getBlock();

        if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) return null;
        if (!(freeSpace == Blocks.AIR && (!oldMode.getValue() || legacyFreeSpace == Blocks.AIR))) return null;

        Box posBoundingBox = new Box(bp.up());

        if (oldMode.getValue()) posBoundingBox.expand(0, 1f, 0);

        for (Entity ent : ThunderHack.asyncManager.getAsyncEntities()) {
            if (ent == null) continue;
            if (ent.getBoundingBox().intersects(posBoundingBox)) {
                if (ent instanceof ExperienceOrbEntity || ent instanceof EndCrystalEntity) continue;
                return null;
            }
        }

        Vec3d crystalVector = new Vec3d(0.5f + bp.getX(), 1f + bp.getY(), 0.5f + bp.getZ());
        if (interact.getValue() == InteractionUtility.Interact.Vanilla) {
            return new BlockHitResult(crystalVector, Direction.DOWN, bp, false);
        } else if (interact.getValue() == InteractionUtility.Interact.Strict && mc.player != null) {
            float bestDistance = 999f;
            Direction bestDirection = null;
            Vec3d bestVector = null;

            if (mc.player.getEyePos().getY() > bp.up().getY()) {
                bestDirection = Direction.UP;
                bestVector = new Vec3d(bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5);
            } else if (mc.player.getEyePos().getY() < bp.getY()) {
                bestDirection = Direction.DOWN;
                bestVector = new Vec3d(bp.getX() + 0.5, bp.getY(), bp.getZ() + 0.5);
            } else {
                for (Direction dir : Direction.values()) {
                    Vec3d directionVec = new Vec3d(bp.getX() + 0.5 + dir.getVector().getX() * 0.5, bp.getY() + 0.5 + dir.getVector().getY() * 0.5, bp.getZ() + 0.5 + dir.getVector().getZ() * 0.5);
                    float distance = InteractionUtility.squaredDistanceFromEyes(directionVec);
                    if (bestDistance > distance) {
                        bestDirection = dir;
                        bestVector = directionVec;
                        bestDistance = distance;
                    }
                }
            }
            if (bestVector == null) return null;
            return new BlockHitResult(bestVector, bestDirection, bp, false);
        } else {
            float bestDistance = 999f;
            BlockHitResult bestData = null;
            for (float x = 0f; x <= 1f; x += 0.05f) {
                for (float y = 0f; y <= 1; y += 0.05f) {
                    for (float z = 0f; z <= 1; z += 0.05f) {
                        Vec3d point = new Vec3d(bp.getX() + x, bp.getY() + y, bp.getZ() + z);
                        BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                        if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
                            continue;

                        BlockHitResult result = ExplosionUtility.rayCastBlock(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player), bp);

                        if (InteractionUtility.squaredDistanceFromEyes(point) < bestDistance)
                            if (result != null && result.getType() == HitResult.Type.BLOCK) bestData = result;
                    }
                }
            }
            return bestData;
        }
    }

    public static CevBreaker getInstance() {
        return instance;
    }

    public enum BreakMode {
        Packet,
        Normal
    }
}
