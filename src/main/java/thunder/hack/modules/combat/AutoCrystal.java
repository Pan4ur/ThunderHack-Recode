package thunder.hack.modules.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.*;
import thunder.hack.Thunderhack;
import thunder.hack.core.PlaceManager;
import thunder.hack.events.impl.*;
import thunder.hack.gui.hud.impl.PingHud;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.player.SpeedMine;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtil;
import thunder.hack.utility.player.PlaceUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.Util;
import thunder.hack.utility.math.ExplosionUtil;
import thunder.hack.utility.math.MathUtil;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AutoCrystal extends Module {
    public AutoCrystal() {
        super("AutoCrystal", "AutoCrystal", Category.COMBAT);
    }

    public Setting<TimingMode> timingMode = new Setting<>("Timing", TimingMode.Sequential);
    public Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public Setting<Boolean> inhibit = new Setting<>("Inhibit", true);
    public Setting<Boolean> limit = new Setting<>("Limit", false);
    public Setting<YawStepMode> yawStep = new Setting<>("YawStep", YawStepMode.OnlyBreak, v-> rotate.getValue());
    public Setting<Integer> yawAngle = new Setting<>("YawAngle", 54, 5, 180, v-> rotate.getValue() && yawStep.getValue() != YawStepMode.Off);
    public Setting<Boolean> strictDirection = new Setting<>("StrictDirection", true);
    public Setting<Boolean> oldPlace = new Setting<>("1.12 Place", false);
    public Setting<ConfirmMode> confirm = new Setting<>("Confirm", ConfirmMode.OFF);
    public Setting<Integer> ticksExisted = new Setting<>("TicksExisted", 0, 0, 20);
    public Setting<Integer> breakDelay = new Setting<>("BreakDelay", 0, 0, 1000);
    public Setting<Integer> placeDelay = new Setting<>("PlaceDelay", 0, 0, 1000);
    public Setting<SyncMode> sync = new Setting<>("Sync", SyncMode.Strict);
    public Setting<Float> breakRange = new Setting<>("BreakRange", 4.3F, 1F, 6F);
    public Setting<Float> breakWallsRange = new Setting<>("BreakWalls", 3F, 1F, 6F);
    public Setting<Float> placeRange = new Setting<>("PlaceRange", 4F, 1F, 6F);
    public Setting<Float> placeWallsRange = new Setting<>("PlaceWalls", 1.5F, 1F, 6F);
    public Setting<AutoSwapMode> autoSwap = new Setting<>("AutoSwap", AutoSwapMode.Normal);
    public Setting<Integer> afterSwapDelay = new Setting<>("AfterSwapDelay", 500, 0, 1000, v -> autoSwap.getValue() == AutoSwapMode.Normal);
    public Setting<Boolean> antiWeakness = new Setting<>("AntiWeakness", false, v -> autoSwap.getValue() != AutoSwapMode.None);
    public static Setting<Boolean> terrainIgnore = new Setting<>("TerrainIgnore", true);
    public Setting<PriorityMode> priorityMode = new Setting<>("PlacePriority", PriorityMode.MaxDamage);
    public Setting<Float> enemyRange = new Setting<>("TargetRange", 8F, 4F, 20F);
    public static Setting<Integer> predictTicks = new Setting<>("PredictTicks", 3, 0, 10);
    public Setting<Float> minDamage = new Setting<>("MinDamage", 6F, 0F, 20F);
    public Setting<Float> maxSelfDamage = new Setting<>("MaxSelfDmg", 12F, 0F, 20F);
    public Setting<Float> faceplaceHealth = new Setting<>("FaceplaceHP", 4F, 0F, 20F);
    public Setting<Boolean> pauseWhileMining = new Setting<>("PauseWhenMining", false);
    public Setting<Boolean> pauseWhileGapping = new Setting<>("PauseWhenGapping", false);
    public Setting<Boolean> rightClickGap = new Setting<>("RightClickGap", false,v-> pauseWhileGapping.getValue());
    public Setting<Boolean> pauseWhenAura = new Setting<>("PauseWhenAura", true);
    public Setting<Float> pauseHealth = new Setting<>("PauseHealth", 2f, 0f, 10f);
    public Setting<Boolean> render = new Setting<>("Render", true);

    private enum TimingMode {Sequential, Vanilla}

    private enum YawStepMode {Off, OnlyBreak, BreakPlace}

    private enum ConfirmMode {OFF, SEMI, FULL}

    private enum SyncMode {Strict, Merge, Adaptive}

    private enum PriorityMode {MaxDamage, Balance}

    private enum AutoSwapMode {None, Normal, Silent}


    public static ConcurrentHashMap<Integer, Long> silentMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<BlockPos, Long> placeLocations = new ConcurrentHashMap<>();

    private Vec3d rotations;
    private Vec3d lastExplosionVec = null;

    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private final Timer noGhostTimer = new Timer();
    private final Timer cacheTimer = new Timer();
    private final Timer scatterTimer = new Timer();
    private final Timer inhibitTimer = new Timer();

    private EndCrystalEntity inhibitEntity = null;

    private int prev_crystals_ammount;
    private int crys_speed,inv_timer;

    private BlockPos cachePos = null;
    private BlockPos threadedBp = null;

    private boolean lastBroken = false;
    public static Entity CAtarget;

    private float renderDmg;

    @Override
    public void onEnable() {
        lastBroken = false;
        silentMap.clear();
        rotations = null;
        cachePos = null;
        inhibitEntity = null;
        lastExplosionVec = null;
        crys_speed = 0;
        prev_crystals_ammount = InventoryUtil.getItemCount(Items.END_CRYSTAL);
        threadedBp = null;
    }

    @Override
    public void onDisable() {
        CAtarget = null;
    }

    @Subscribe
    public void onTick(EventTick event) {
        if (timingMode.getValue() == TimingMode.Vanilla && check()) {
            if (!generateBreak()) generatePlace(false);
        }
    }

    @Subscribe
    public void onPostTick(EventPostTick event) {
        if (timingMode.getValue() == TimingMode.Vanilla && check()) {
            if (!generateBreak()) generatePlace(false);
        }
    }

    @Subscribe
    public void onEntitySync(EventSync event) {
        if(mc.player == null || mc.world == null) return;
        if(inv_timer++ >= 20){
            crys_speed = prev_crystals_ammount - InventoryUtil.getItemCount(Items.END_CRYSTAL);
            prev_crystals_ammount = InventoryUtil.getItemCount(Items.END_CRYSTAL);
            inv_timer = 0;
        }

        placeLocations.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 1500) {
                placeLocations.remove(pos);
            }
        });

        if (timingMode.getValue() == TimingMode.Sequential) {
            if (lastExplosionVec != null) {
                for (Entity entity : mc.world.getEntities()) {
                    if (entity instanceof EndCrystalEntity && entity.squaredDistanceTo(lastExplosionVec.x, lastExplosionVec.y, lastExplosionVec.z) <= 36) {
                        silentMap.put(entity.getId(), System.currentTimeMillis());
                    }
                }
                lastExplosionVec = null;
            }

            if (check()) {
                if (!generateBreak()) {
                    generatePlace(false);
                }
            }
        }

        if (rotate.getValue() && rotations != null) {
            float[] yp = PlaceUtility.calculateAngle(rotations);
            if ((yawStep.getValue() == YawStepMode.OnlyBreak && PlaceManager.trailingBreakAction != null) || yawStep.getValue() == YawStepMode.BreakPlace) {
                float yawDiff = MathHelper.wrapDegrees(yp[0] - ((IClientPlayerEntity) ((mc.player))).getLastYaw());
                if (Math.abs(yawDiff) > yawAngle.getValue()) {
                    yp[0] = ((IClientPlayerEntity) ((mc.player))).getLastYaw() + (yawDiff * (yawAngle.getValue() / Math.abs(yawDiff)));
                    PlaceManager.trailingBreakAction = null;
                    PlaceManager.trailingPlaceAction = null;
                }
            }

            double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
            yp[0] = (float) (yp[0] - (yp[0] - ((IClientPlayerEntity) ((mc.player))).getLastYaw()) % gcdFix);
            yp[1] = (float) (yp[1] - (yp[1] - ((IClientPlayerEntity) ((mc.player))).getLastYaw()) % gcdFix);

            PlaceManager.setTrailingRotation(yp);
            rotations = null;
        }
    }

    @Subscribe
    public void onPacketSend(PacketEvent.SendPost event){
        if(event.getPacket() instanceof UpdateSelectedSlotC2SPacket) noGhostTimer.reset();
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
        if (pauseWhileGapping.getValue() && rightClickGap.getValue() && mc.options.useKey.isPressed() && mc.player.getInventory().getMainHandStack().getItem() instanceof EndCrystalItem) {
            int gappleSlot = InventoryUtil.getItemSlotHotbar(Items.ENCHANTED_GOLDEN_APPLE);
            if (gappleSlot != -1 && gappleSlot != mc.player.getInventory().selectedSlot) {
                mc.player.getInventory().selectedSlot = gappleSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(gappleSlot));
                return false;
            }
        }
        if (!isOffhand() && !(mc.player.getInventory().getMainHandStack().getItem() instanceof EndCrystalItem)) {
            if (autoSwap.getValue() == AutoSwapMode.None) return false;
            else return InventoryUtil.getCrystalSlot() != -1;
        }
        return true;
    }

    private void generatePlace(boolean adaptive) {
        if (confirm.getValue() != ConfirmMode.FULL || inhibitEntity == null || inhibitEntity.age >= ticksExisted.getValue()) {
            lastBroken = false;
            if ((sync.getValue() != SyncMode.Strict || breakTimer.passedMs(breakDelay.getValue())) && placeTimer.passedMs(placeDelay.getValue())) {
                if (confirm.getValue() != ConfirmMode.OFF) {
                    if (cachePos != null && !cacheTimer.passedMs((int) Math.max(100, ((PingHud.getPing() + 50) / (Thunderhack.serverManager.getTPS() / 20F))) + 250) && canPlaceCrystal(cachePos)) {
                        BlockHitResult result = handlePlaceRotation(cachePos);
                        if(result == null) return;
                        PlaceManager.trailingPlaceAction = () -> {
                            if (placeCrystal(result)) placeTimer.reset();
                        };
                        if (timingMode.getValue() == TimingMode.Vanilla) {
                            PlaceManager.trailingPlaceAction.run();
                            PlaceManager.trailingPlaceAction = null;
                        }
                        return;
                    }
                }
                BlockPos candidatePos;
                if(
                        Thunderhack.moduleManager.get(SpeedMine.class).isEnabled()
                        && SpeedMine.mode.getValue() == SpeedMine.Mode.Packet
                        && SpeedMine.progress > 0.95
                        && SpeedMine.minePosition != null
                        && mc.world.getBlockState(SpeedMine.minePosition).getBlock() == Blocks.OBSIDIAN
                        && !mc.world.getNonSpectatingEntities(PlayerEntity.class,
                                new Box(
                                        SpeedMine.minePosition.toCenterPos().getX() - 1.5f,
                                        SpeedMine.minePosition.toCenterPos().getY() - 1.5f,
                                        SpeedMine.minePosition.toCenterPos().getZ() - 1.5f,
                                        SpeedMine.minePosition.toCenterPos().getX() + 1.5f,
                                        SpeedMine.minePosition.toCenterPos().getY() + 1.5f,
                                        SpeedMine.minePosition.toCenterPos().getZ() + 1.5f
                                )
                        ).stream().filter(e -> e != mc.player && !Thunderhack.friendManager.isFriend(e)).collect(Collectors.toList()).isEmpty()
                        && canPlaceCrystal(SpeedMine.minePosition)
                ){
                    candidatePos = SpeedMine.minePosition;
                } else {
                    candidatePos = threadedBp;
                }

                if (candidatePos != null) {
                    BlockHitResult result = handlePlaceRotation(candidatePos);
                    if(result == null) return;
                    PlaceManager.trailingPlaceAction = () -> {
                        if (placeCrystal(result)) {
                            placeTimer.reset();
                        }
                    };
                }

            }
        }
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

    private boolean generateBreak() {
        List<PlayerEntity> targetsInRange = getTargetsInRange();

        int adjustedResponseTime = (int) Math.max(100, ((PingHud.getPing() + 50) / (Thunderhack.serverManager.getTPS() / 20F))) + 150;

        EndCrystalEntity crystal = findCrystalTarget(targetsInRange, adjustedResponseTime);

        if (crystal != null) {
            if (crystal.age >= ticksExisted.getValue()) {
                if (rotate.getValue()) {
                    rotations = crystal.getPos();
                }
                if (breakTimer.passedMs(breakDelay.getValue() + 20)) {
                    if (lastBroken) {
                        lastBroken = false;
                        if (sync.getValue() == SyncMode.Strict) return false;
                    }
                    PlaceManager.trailingBreakAction = () -> {
                        if (breakCrystal(crystal)) {
                            lastBroken = true;
                            breakTimer.reset();
                            silentMap.put(crystal.getId(), System.currentTimeMillis());
                            for (Entity entity : mc.world.getEntities()) {
                                if (entity instanceof EndCrystalEntity && entity.squaredDistanceTo(crystal) <= 36) {
                                    silentMap.put(entity.getId(), System.currentTimeMillis());
                                }
                            }
                        }
                        if (sync.getValue() != SyncMode.Strict && check()) generatePlace(false);
                    };
                    if (timingMode.getValue() == TimingMode.Vanilla) {
                        PlaceManager.trailingBreakAction.run();
                        PlaceManager.trailingBreakAction = null;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if(render.getValue())
            placeLocations.forEach((pos, time) -> {
                if (System.currentTimeMillis() - time < 500) {
                    int alpha = (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)));
                    Render3DEngine.drawFilledBox(event.getMatrixStack(),new Box(pos), Render2DEngine.injectAlpha(HudEditor.getColor(0), alpha));
                    Render3DEngine.drawBoxOutline(new Box(pos), Render2DEngine.injectAlpha(HudEditor.getColor(0), alpha), 2);
                    Render3DEngine.drawTextIn3D(String.valueOf(MathUtil.round2(renderDmg)),pos.toCenterPos(),0,0.1,0,Render2DEngine.injectAlpha(Color.WHITE, alpha));
                }
            });
    }


    public boolean placeCrystal(BlockHitResult result) {
        if (result != null) {
            int prevSlot = mc.player.getInventory().selectedSlot;
            if (autoSwap.getValue() != AutoSwapMode.None && !setCrystalSlot()) return false;
            if (autoSwap.getValue() != AutoSwapMode.Silent && (!isOffhand() && mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL)) return false;

            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(isOffhand() ? Hand.OFF_HAND : Hand.MAIN_HAND, result, Util.getWorldActionId(mc.world)));
          //  mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(isOffhand() ? Hand.OFF_HAND : Hand.MAIN_HAND));
            mc.player.swingHand(isOffhand() ? Hand.OFF_HAND : Hand.MAIN_HAND);
            placeLocations.put(result.getBlockPos(), System.currentTimeMillis());
            if(autoSwap.getValue() == AutoSwapMode.Silent){
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            }
            return true;
        }
        return false;
    }

    private boolean breakCrystal(EndCrystalEntity targetCrystal) {
        if (!noGhostTimer.passedMs(afterSwapDelay.getValue())) return false;
        if (targetCrystal != null) {
            if (SpeedMine.minePosition != null && targetCrystal.age < 2 && MathUtil.getSqrDistance(SpeedMine.minePosition.getX() + 0.5, SpeedMine.minePosition.getY(), SpeedMine.minePosition.getZ() + 0.5, targetCrystal.getX(),  targetCrystal.getY() - 1, targetCrystal.getZ()) < 1){
                return false;
            }

            if (antiWeakness.getValue() && ((mc.player)).hasStatusEffect(StatusEffects.WEAKNESS) && !(mc.player.getMainHandStack().getItem() instanceof SwordItem)) {
                int swordSlot = InventoryUtil.getBestSword();
                if (swordSlot != -1) {
                    mc.player.getInventory().selectedSlot = swordSlot;
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(swordSlot));
                }
            }

            Criticals.cancelCrit = true;
            mc.interactionManager.attackEntity(mc.player, targetCrystal);
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            Criticals.cancelCrit = false;

            if (inhibit.getValue()) {
                inhibitTimer.reset();
                inhibitEntity = targetCrystal;
            }

            return true;
        }
        return false;
    }

    @Override
    public void onThread() {
        threadedBp = findPlacePosition();
    }

    private BlockPos findPlacePosition() {
        if (!scatterTimer.passedMs(ticksExisted.getValue() * 50)) return null;

        List<PlayerEntity> targetss = getTargetsInRange();

        List<BlockPos> blocks = findCrystalBlocks();

        if(blocks == null) return null;

        BlockPos bestPos = null;

        PlayerEntity bestTarget = null;

        float bestDamage = 0.0f;

        float bestBalance = 0.0f;

        if (targetss.isEmpty()) return null;

        for (BlockPos block : blocks) {
            Vec3d blockVec = new Vec3d(block.getX() + 0.5, block.getY() + 1, block.getZ() + 0.5);
            float damage = 0.0F;
            PlayerEntity target = null;
            float damageToSelf = ExplosionUtil.getSelfExplosionDamage(blockVec);

            if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= damageToSelf + 2F) {
                continue;
            }

            if (damageToSelf > maxSelfDamage.getValue()) {
                continue;
            }

            for (PlayerEntity player : targetss) {
                boolean facePlace = false;

                float damageToTarget = ExplosionUtil.getExplosionDamage2(blockVec, player);

                if (damageToTarget >= 0.5D) {
                    if (player.getHealth() + player.getAbsorptionAmount() - damageToTarget <= 0 || player.getHealth() + player.getAbsorptionAmount() < faceplaceHealth.getValue()) {
                        facePlace = true;
                    }
                }

                if (mc.options.sneakKey.isPressed()) {
                    facePlace = true;
                }

                if (damageToTarget > damage && (damageToTarget >= minDamage.getValue() || facePlace)) {
                    damage = damageToTarget;
                    target = player;
                }
            }

            if (priorityMode.getValue() == PriorityMode.MaxDamage) {
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestPos = block;
                    bestTarget = target;
                }
            } else {
                if (damage / damageToSelf > bestBalance) {
                    bestDamage = damage;
                    bestBalance = damage / damageToSelf;
                    bestPos = block;
                    bestTarget = target;
                }
            }
        }

        if (bestTarget != null && bestPos != null) {
            CAtarget = bestTarget;
            renderDmg = bestDamage;
        } else {
            CAtarget = null;
        }

        cachePos = bestPos;
        cacheTimer.reset();
        return bestPos;
    }

    public void onSpawnCrystal(double x,double y, double z, int id){
        placeLocations.forEach((pos, time) -> {

            if (MathUtil.getSqrDistance(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, x,  y - 1, z) < 1) {
                placeLocations.remove(pos);
                cachePos = null;
                if (!limit.getValue() && inhibit.getValue()) {
                    scatterTimer.reset();
                }
                if (ticksExisted.getValue() != 0 || ((mc.player)).hasStatusEffect(StatusEffects.WEAKNESS) || fullNullCheck())
                    return;

                if (SpeedMine.minePosition != null && MathUtil.getSqrDistance(SpeedMine.minePosition.getX() + 0.5, SpeedMine.minePosition.getY(), SpeedMine.minePosition.getZ() + 0.5, x,  y - 1, z) < 1){
                    return;
                }

                if (!noGhostTimer.passedMs(afterSwapDelay.getValue())) return;

                if (silentMap.containsKey(id)) return;

                if (!check()) return;

                Vec3d spawnVec = new Vec3d(x, y, z);

                if (PlaceUtility.getEyesPos(((mc.player))).distanceTo(spawnVec) > breakRange.getValue()) return;

                if (!(breakTimer.passedMs(breakDelay.getValue()))) return;

                if (ExplosionUtil.getSelfExplosionDamage(spawnVec) + 2F >= mc.player.getHealth() + mc.player.getAbsorptionAmount())
                    return;

                silentMap.put(id, System.currentTimeMillis());
                lastExplosionVec = spawnVec;

                Criticals.cancelCrit = true;
                PlayerInteractEntityC2SPacket attackPacket = PlayerInteractEntityC2SPacket.attack(mc.player, ((mc.player)).isSneaking());
                changeId(attackPacket,id);
                mc.player.networkHandler.sendPacket(attackPacket);
                mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                Criticals.cancelCrit = false;

                breakTimer.reset();
                lastBroken = true;
                if (sync.getValue() == SyncMode.Adaptive) {
                    generatePlace(true);
                }
            }
        });
    }

    @Subscribe
    public void onEntitySpawn(EventEntitySpawn e){
        if(e.getEntity() instanceof EndCrystalEntity){
            onSpawnCrystal(e.getEntity().getX(), e.getEntity().getY(), e.getEntity().getZ(), e.getEntity().getId());
        }
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.Receive event) {
        if(event.getPacket() instanceof  EntitySpawnS2CPacket){
            EntitySpawnS2CPacket packet = event.getPacket();
            if (packet.getEntityType() == EntityType.END_CRYSTAL) {
                onSpawnCrystal(packet.getX(), packet.getY(), packet.getZ(), packet.getId());
            }
        }
    }

    public static void changeId(PlayerInteractEntityC2SPacket packet, int id)  {
        try {
            Field field = PlayerInteractEntityC2SPacket.class.getDeclaredField("field_12870");
        //     Field field = PlayerInteractEntityC2SPacket.class.getDeclaredField("entityId");

            field.setAccessible(true);
            field.setInt(packet, id);
        } catch (Exception ignored){}
    }

    private List<BlockPos> findCrystalBlocks() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos centerPos = ((mc.player)).getBlockPos();
        int r = (int) Math.ceil(placeRange.getValue()) + 1;
        int h = placeRange.getValue().intValue();
        for (int i = centerPos.getX() - r; i < centerPos.getX() + r; i++) {
            for (int j = centerPos.getY() - h; j < centerPos.getY() + h; j++) {
                for (int k = centerPos.getZ() - r; k < centerPos.getZ() + r; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (canPlaceCrystal(pos)) {
                        positions.add(pos);
                    }
                }
            }
        }
        return positions;
    }

    public boolean canPlaceCrystal(BlockPos blockPos) {
        if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) return false;

        if (!(mc.world.getBlockState(blockPos.up()).getBlock() == Blocks.AIR)) return false;

        if (oldPlace.getValue() && !(mc.world.getBlockState(blockPos.up().up()).getBlock() == Blocks.AIR)) return false;

        if (!PlaceUtility.canSee(new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1.7, blockPos.getZ() + 0.5), new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5))) {
            if (PlaceUtility.getEyesPos(((mc.player))).distanceTo(new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5)) > breakWallsRange.getValue()) {
                return false;
            }
        }

        Vec3d playerEyes = PlaceUtility.getEyesPos(((mc.player)));
        boolean canPlace = false;

        if (strictDirection.getValue()) {
            for (Vec3d point : PlaceUtility.fastMultiPoint) {
                Vec3d p = new Vec3d(blockPos.getX() + point.getX(), blockPos.getY() + point.getY(), blockPos.getZ() + point.getZ());
                double distanceTo = playerEyes.distanceTo(p);
                if (distanceTo > placeRange.getValue()) {
                    continue;
                }
                if (distanceTo > placeWallsRange.getValue()) {
                    BlockHitResult result = mc.world.raycast(new RaycastContext(playerEyes, p, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ((mc.player))));
                    if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(blockPos)) {
                        canPlace = true;
                        break;
                    }
                } else {
                    canPlace = true;
                    break;
                }
            }
        } else {
            for (Direction dir : Direction.values()) {
                Vec3d p = new Vec3d(blockPos.getX() + 0.5 + dir.getOffsetX() * 0.5, blockPos.getY() + 0.5 + dir.getOffsetY() * 0.5, blockPos.getZ() + 0.5 + dir.getOffsetZ() * 0.5);
                double distanceTo = playerEyes.distanceTo(p);
                if (distanceTo > placeRange.getValue()) {
                    continue;
                }
                if (distanceTo < placeWallsRange.getValue()) {
                    canPlace = true;
                    break;
                }
            }
        }
        if (!canPlace) return false;
        boolean final_result = true;

        for(Entity ent : Thunderhack.asyncManager.getAsyncEntities()){
            if(ent.getBoundingBox().intersects(new Box(blockPos).stretch(0, oldPlace.getValue() ? 2 : 1, 0)) && !silentMap.containsKey(ent.getId()) && (!(ent instanceof EndCrystalEntity) || ent.age > 20)){
                final_result = false;
                break;
            }
        }

        return final_result;
    }

    public boolean setCrystalSlot() {
        if (isOffhand()) {
            return true;
        }
        int crystalSlot = InventoryUtil.getCrystalSlot();
        if (crystalSlot == -1) {
            return false;
        } else if (mc.player.getInventory().selectedSlot != crystalSlot) {
            if(autoSwap.getValue() != AutoSwapMode.Silent) {
                mc.player.getInventory().selectedSlot = crystalSlot;
            }
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(crystalSlot));
        }
        return true;
    }

    private EndCrystalEntity findCrystalTarget(List<PlayerEntity> targetsInRange, int adjustedResponseTime) {
        silentMap.forEach((id, time) -> {
            if (System.currentTimeMillis() - time > 1000) {
                silentMap.remove(id);
            }
        });

        EndCrystalEntity bestCrystal = null;

        if (inhibit.getValue() && !limit.getValue() && !inhibitTimer.passedMs(adjustedResponseTime) && inhibitEntity != null) {
            if (mc.world.getEntityById(inhibitEntity.getId()) != null && isValidCrystalTarget(inhibitEntity)) {
                bestCrystal = inhibitEntity;
                return bestCrystal;
            }
        }

        List<EndCrystalEntity> crystalsInRange = getCrystalInRange();

        if (crystalsInRange.isEmpty()) {
            return null;
        }

        double bestDamage = 0.0D;

        for (EndCrystalEntity crystal : crystalsInRange) {
            if (crystal.getPos().distanceTo(PlaceUtility.getEyesPos(((mc.player)))) < breakWallsRange.getValue() || PlaceUtility.canSee(crystal)) {

                double selfDamage = ExplosionUtil.getSelfExplosionDamage(crystal.getPos());

                if (!placeLocations.containsKey(BlockPos.ofFloored(crystal.getPos()).down()) && selfDamage > maxSelfDamage.getValue()) {
                    continue;
                }

                double damage = 0.0D;

                for (PlayerEntity target : targetsInRange) {
                    double targetDamage = ExplosionUtil.getExplosionDamage2(crystal.getPos(), target);
                    damage += targetDamage;
                }

                if (!placeLocations.containsKey(BlockPos.ofFloored(crystal.getPos()).down()) && (damage < minDamage.getValue() || damage < selfDamage))
                    continue;

                if (damage > bestDamage || bestDamage == 0D) {
                    bestDamage = damage;
                    bestCrystal = crystal;
                }
            }
        }
        return bestCrystal;
    }


    private List<EndCrystalEntity> getCrystalInRange() {
        List<EndCrystalEntity> list = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity)) continue;
            if (!isValidCrystalTarget((EndCrystalEntity) entity)) continue;
            list.add((EndCrystalEntity) entity);
        }
        return list;
    }

    private boolean isValidCrystalTarget(EndCrystalEntity crystal) {
        if (PlaceUtility.getEyesPos(((mc.player))).distanceTo(crystal.getPos()) > breakRange.getValue()) return false;
        if (silentMap.containsKey(crystal.getId()) && limit.getValue()) return false;
        if (silentMap.containsKey(crystal.getId()) && crystal.age < ticksExisted.getValue()) return false;
        return !(ExplosionUtil.getSelfExplosionDamage(crystal.getPos()) + 2F >= mc.player.getHealth() + mc.player.getAbsorptionAmount());
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

    private boolean isOffhand() {
        return mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
    }

    @Override
    public String getDisplayInfo() {
        return crys_speed + " c/s";
    }
}
