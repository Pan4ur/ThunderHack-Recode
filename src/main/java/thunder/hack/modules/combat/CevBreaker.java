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
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import thunder.hack.Thunderhack;
import thunder.hack.core.ModuleManager;
import thunder.hack.events.impl.EventEntityRemoved;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.injection.accesors.IWorldRenderer;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.modules.player.SpeedMine;
import thunder.hack.modules.render.HoleESP;
import thunder.hack.notification.Notification;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CevBreaker extends Module {
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

    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.All);
    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Strict);

    private final Setting<Parent> render = new Setting<>("Render", new Parent(false, 1));

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

    public CevBreaker() {
        super("CevBreaker", "Банит гринхолфагов", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        findTarget();

        newCycle = true;
        placeCrystalProgress = .9;
        targetSurroundBlockPos = null;
        canPlaceBlock = true;

        if (breakMode.getValue() == BreakMode.Packet && Thunderhack.moduleManager.get(SpeedMine.class).isDisabled()) {
            Thunderhack.notificationManager.publicity(getName(), MainSettings.isRu() ? "Для использования пакетного копания необходимо включить и настроить модуль SpeedMine" : "For using packet mine is necessary to enable and config SpeedMine", 5, Notification.Type.ERROR);
            disable("MainSettings.isRu() ? \"Для использования пакетного копания необходимо включить и настроить модуль SpeedMine\" : \"For using packet mine is necessary to enable and config SpeedMine\"");
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (fullNullCheck()) return;

        // Find target
        if (target == null || target.isDead()
                || target.getHealth() + target.getAbsorptionAmount() <= 0
                || target.distanceTo(((mc.player))) > range.getValue()) {
            findTarget();
            return;
        }
        if (delay < placeDelay.getValue()) {
            delay++;
            return;
        }

        // To prevent stuck we will mine necessary blocks
        if (!mc.world.getBlockState(target.getBlockPos().add(0, 4, 0)).getBlock().equals(Blocks.AIR) && oldMode.getValue()) {
            startMine(target.getBlockPos().add(0, 4, 0));
            return;
        }
        if (!mc.world.getBlockState(target.getBlockPos().add(0, 3, 0)).getBlock().equals(Blocks.AIR)) {
            startMine(target.getBlockPos().add(0, 3, 0));
            placeCrystalProgress = .1;
            return;
        }

        // Prevent SpeedMine stuck
        if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL
                && mc.world.getBlockState(target.getBlockPos().add(0, 2, 0)).getBlock().equals(Blocks.OBSIDIAN)) {
            placeCrystal();
        }

        delay = 0;
        int blocksPlaced = 0;

        // Checking if structure placed, start breaking, place crystal, if it is necessary
        if (mc.world.getBlockState(target.getBlockPos().add(0, 2, 0)).getBlock().equals(Blocks.OBSIDIAN)) {
            if (SpeedMine.progress >= placeCrystalProgress && SpeedMine.minePosition.equals(target.getBlockPos().add(0, 2, 0))) {
                placeCrystal();
                return;
            } else {
                for (Map.Entry<Integer, BlockBreakingInfo> entry : ((IWorldRenderer) mc.worldRenderer).getBlockBreakingInfos().entrySet()) {
                    BlockBreakingInfo destroyBlockProgress = entry.getValue();
                    if (target.getBlockPos().add(0, 2, 0).equals(destroyBlockProgress.getPos())) {

                        if (destroyBlockProgress.getStage() >= placeCrystalProgress * 10) {
                            placeCrystal();
                        }
                        return;
                    }
                }
            }

            startMine(target.getBlockPos().add(0, 2, 0));
        }

        BlockPos[] surroundBlocks = new BlockPos[]{
                target.getBlockPos().add(0, 1, 0).west(),
                target.getBlockPos().add(0, 1, 0).south(),
                target.getBlockPos().add(0, 1, 0).north(),
                target.getBlockPos().add(0, 1, 0).east()
        };

        // Generate structure
        if (targetSurroundBlockPos == null) {
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

        // Trap
        if (trap.getValue()) {
            List<BlockPos> trapBlocks = new ArrayList<>();
            for (BlockPos surroundBlock : surroundBlocks) {
                if (mc.world.getBlockState(surroundBlock).getBlock().equals(Blocks.AIR)) {
                    trapBlocks.add(surroundBlock);
                }
            }
            InventoryUtility.saveSlot();
            for (BlockPos trapBlock : trapBlocks) {
                if (blocksPlaced >= blocksPerTick.getValue()) return;
                if (placeObsidian(trapBlock)) renderTrapPoses.put(trapBlock, System.currentTimeMillis());
                blocksPlaced++;
            }
            InventoryUtility.returnSlot();
        }

        // Build structure
        InventoryUtility.saveSlot();
        while (blocksPlaced < blocksPerTick.getValue()) {
            if (!mc.world.getBlockState(targetSurroundBlockPos.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) {
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
            } else if (!mc.world.getBlockState(targetSurroundBlockPos.add(0, 0, 0)).getBlock().equals(Blocks.AIR)) {
                // Second step place obsidian
                if (placeObsidian(targetSurroundBlockPos.add(0, 1, 0)))
                    renderStructurePoses.put(targetSurroundBlockPos.add(0, 1, 0), System.currentTimeMillis());
                blocksPlaced++;
                continue;
            }

            // First step place obsidian
            if (placeObsidian(targetSurroundBlockPos))
                renderStructurePoses.put(targetSurroundBlockPos, System.currentTimeMillis());

            blocksPlaced++;
        }
        InventoryUtility.returnSlot();


        // Rotations
        if (rotate.getValue()) {
            float[] yp = InteractionUtility.calculateAngle(rotations);
            final double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
            yp[0] = (float) (yp[0] - (yp[0] - ((IClientPlayerEntity) ((mc.player))).getLastYaw()) % gcdFix);
            yp[1] = (float) (yp[1] - (yp[1] - ((IClientPlayerEntity) ((mc.player))).getLastYaw()) % gcdFix);
            mc.player.setYaw(yp[0]);
            mc.player.setPitch(yp[1]);
            rotations = null;
        }
    }

    public void onRender3D(MatrixStack stack) {
        if (renderTrap.getValue()) {
            renderTrapPoses.forEach((pos, time) -> {
                if (System.currentTimeMillis() - time > 500) {
                    renderTrapPoses.remove(pos);
                } else {
                    Render3DEngine.drawFilledBox(stack, new Box(pos), Render2DEngine.injectAlpha(trapFillColor.getValue().getColorObject(), (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)))));
                    Render3DEngine.drawBoxOutline(new Box(pos), trapLineColor.getValue().getColorObject(), trapLineWidth.getValue());
                }
            });
        }
        if (renderStructure.getValue()) {
            renderStructurePoses.forEach((pos, time) -> {
                if (System.currentTimeMillis() - time > 500) {
                    renderTrapPoses.remove(pos);
                } else {
                    Render3DEngine.drawFilledBox(stack, new Box(pos), Render2DEngine.injectAlpha(structureFillColor.getValue().getColorObject(), (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)))));
                    Render3DEngine.drawBoxOutline(new Box(pos), structureLineColor.getValue().getColorObject(), structureLineWidth.getValue());
                }
            });
        }
        if (renderTarget.getValue()) {
            Render3DEngine.drawFilledBox(stack, new Box(target.getBlockPos().add(0, 3, 0)), targetFillColor.getValue().getColorObject());
            Render3DEngine.drawBoxOutline(new Box(target.getBlockPos().add(0, 3, 0)), targetLineColor.getValue().getColorObject(), targetLineWidth.getValue());
        }
    }

    @EventHandler
    private void onEntityRemove(EventEntityRemoved e) {
        if (e.entity == null) return;
        if(target == null) return;
        if (e.entity.getBlockPos().equals(target.getBlockPos().add(0, 3, 0))
                && e.entity instanceof EndCrystalEntity) {
            newCycle = true;
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive e) {
        if(target == null) return;
        if (e.getPacket() instanceof BlockUpdateS2CPacket pac) {
            if (pac.getPos().equals(target.getBlockPos().add(0, 2, 0)) && pac.getState().getBlock().equals(Blocks.AIR)) {
                for (Entity entity : Thunderhack.asyncManager.getAsyncEntities()) {
                    if (entity instanceof EndCrystalEntity endCrystal && entity.getBlockPos().equals(target.getBlockPos().add(0, 3, 0))) {
                        canPlaceBlock = false;
                        breakCrystalThread(endCrystal);
                    }
                }
            }
        }
    }

    public void breakCrystalThread(EndCrystalEntity endCrystal) {
        Thunderhack.asyncManager.run(() -> {
                    if (antiWeakness.getValue() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS) && !(mc.player.getMainHandStack().getItem() instanceof SwordItem)) {

                        SearchInvResult swordResult = InventoryUtility.getSword();
                        if (autoSwap.getValue()) swordResult.switchTo();
                    }

                    Criticals.cancelCrit = true;
                    mc.interactionManager.attackEntity(mc.player, endCrystal);
                    mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    Criticals.cancelCrit = false;
                    try {
                        Thread.sleep(breakCrystalDelay.getValue() / 2);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    canPlaceBlock = true;
                },
                breakCrystalDelay.getValue() / 2);
    }

    @EventHandler
    private void onPostSync(EventPostSync e) {
        // Normal breaking block
        if (mine && !mc.world.getBlockState(currentMineBlockPos).getBlock().equals(Blocks.AIR)) {
            if (breakMode.getValue() == BreakMode.Normal) {
                if (autoSwap.getValue()) {
                    SearchInvResult pickResult = InventoryUtility.findInInventory(stack -> stack.getItem() instanceof PickaxeItem);
                    mc.player.getInventory().selectedSlot = pickResult.found() ? pickResult.slot() : mc.player.getInventory().selectedSlot;
                }

                InteractionUtility.BreakData bData = InteractionUtility.getBreakData(currentMineBlockPos, InteractionUtility.Interact.Strict);
                if(bData == null) return;
                mc.interactionManager.updateBlockBreakingProgress(currentMineBlockPos, bData.dir());
                if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
            }
        } else {
            mine = false;
        }
    }

    private void startMine(BlockPos pos) {
        switch (breakMode.getValue()) {
            case Normal -> {
                mine = true;
                currentMineBlockPos = pos;
            }
            case Packet -> {
                if (ModuleManager.speedMine.isEnabled()
                        && SpeedMine.progress != 0
                        && !mc.world.getBlockState(SpeedMine.minePosition).getBlock().equals(Blocks.AIR)) {
                    return;
                }

                InteractionUtility.BreakData bData = InteractionUtility.getBreakData(pos, interact.getValue());
                if(bData == null) return;

                mc.interactionManager.attackBlock(pos, bData.dir());
                if (swing.getValue())
                    mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    private void findTarget() {
       // if ((!HoleESP.validBedrock(AutoCrystal.CAtarget.getBlockPos()) && !HoleESP.validIndestructible(AutoCrystal.CAtarget.getBlockPos())) || AutoCrystal.CAtarget.distanceTo(((mc.player))) > range.getValue()) {

            for (PlayerEntity player : Thunderhack.asyncManager.getAsyncPlayers()) {
                if (Thunderhack.friendManager.isFriend(player)) continue;
                if (player == mc.player) continue;
                if (player.distanceTo(((mc.player))) > range.getValue()) continue;
                if (player.isDead()) continue;
                if (player.getHealth() + player.getAbsorptionAmount() <= 0) continue;
                if (!HoleESP.validBedrock(player.getBlockPos()) && !HoleESP.validIndestructible(player.getBlockPos()))
                    continue;

                target = player;
                break;
            }

            if (target == null) {
                Thunderhack.notificationManager.publicity("CevBreaker", MainSettings.isRu() ? "Не удалось найти подходящую цель. Если игрок есть, он не в холке." : "There are no valid target. If player exists, maybe he not in hole.", 5, Notification.Type.ERROR);
                disable(MainSettings.isRu() ? "Не удалось найти подходящую цель. Если игрок есть, он не в холке." : "There are no valid target. If player exists, maybe he not in hole.");
            }
      //  } else target = (PlayerEntity) AutoCrystal.CAtarget;
    }

    private void placeCrystal() {
        BlockPos pos = target.getBlockPos().add(0, 2, 0);
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity && entity.getBlockPos().equals(pos.add(0, 1, 0))) {
                return;
            }
        }

        BlockHitResult pData = getPlaceData(pos);

        if (mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL)) {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, pData, PlayerUtility.getWorldActionId(mc.world)));

            if (swing.getValue()) mc.player.swingHand(Hand.OFF_HAND);
            return;
        }

        int preSlot = mc.player.getInventory().selectedSlot;

        if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL) {
            SearchInvResult crystalResult = InventoryUtility.getCrystal();
            crystalResult.switchTo();
        }

        // Place crystal
        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, pData, PlayerUtility.getWorldActionId(mc.world)));

        if (preSlot != mc.player.getInventory().selectedSlot) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(preSlot));
        }

    }

    private SearchInvResult getObby(){
        SearchInvResult result = InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN);

        if (!result.isHolding() && !autoSwap.getValue())
            return null;

        if (!result.found()) {
            Thunderhack.notificationManager.publicity("CevBreaker", MainSettings.isRu() ? "В хотбаре не найден обсидиан!" : "No obsidian in hotbar", 5, Notification.Type.ERROR);
            disable(MainSettings.isRu() ? "В хотбаре не найден обсидиан!" : "No obsidian in hotbar");
            return null;
        }
        return result;
    }

    private boolean placeObsidian(BlockPos pos) {
        if (!canPlaceBlock) return false;
        SearchInvResult result = getObby();
        if(result == null) return  false;
        if(!result.found()) return false;
        return InteractionUtility.placeBlock(pos, rotate.getValue(), interact.getValue(),placeMode.getValue(), result, false);
    }

    public BlockHitResult getPlaceData(BlockPos bp) {
        Block block = mc.world.getBlockState(bp).getBlock();
        Block freeSpace = mc.world.getBlockState(bp.up()).getBlock();
        Block legacyFreeSpace = mc.world.getBlockState(bp.up().up()).getBlock();

        if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK)
            return null;

        if (!(freeSpace == Blocks.AIR && (!oldMode.getValue() || legacyFreeSpace == Blocks.AIR)))
            return null;

        Box posBoundingBox = new Box(bp.up());

        if (oldMode.getValue())
            posBoundingBox.expand(0, 1f, 0);

        for (Entity ent : Thunderhack.asyncManager.getAsyncEntities()) {
            if (ent == null) continue;
            if (ent.getBoundingBox().intersects(posBoundingBox)) {
                if (ent instanceof ExperienceOrbEntity)
                    continue;
                if (ent instanceof EndCrystalEntity)
                    continue;
                return null;
            }
        }

        Vec3d crystalvector = new Vec3d(0.5f + bp.getX(), 1f + bp.getY(), 0.5f + bp.getZ());
        if (interact.getValue() == InteractionUtility.Interact.Vanilla) {
            return new BlockHitResult(crystalvector, Direction.DOWN, bp, false);
        } else if (interact.getValue() == InteractionUtility.Interact.Strict) {
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
                            if (result != null && result.getType() == HitResult.Type.BLOCK)
                                bestData = result;
                    }
                }
            }
            return bestData;
        }
    }

    public enum BreakMode {
        Packet,
        Normal
    }
}
