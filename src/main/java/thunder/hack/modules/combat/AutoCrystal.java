package thunder.hack.modules.combat;

import com.google.common.collect.Lists;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.CombatManager;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.core.impl.ServerManager;
import thunder.hack.events.impl.*;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.player.SpeedMine;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.TickTimer;
import thunder.hack.utility.Timer;
import thunder.hack.utility.autoCrystal.DeadManager;
import thunder.hack.utility.math.ExplosionUtility;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.math.PredictUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class AutoCrystal extends Module {
    /*   MAIN   */
    private static final Setting<Pages> page = new Setting<>("Page", Pages.Main);
    private final Setting<Timing> timing = new Setting<>("Timing", Timing.NORMAL, v -> page.getValue() == Pages.Main);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", true, v -> page.getValue() == Pages.Main);
    private final Setting<BooleanSettingGroup> yawStep = new Setting<>("YawStep", new BooleanSettingGroup(false), v -> rotate.getValue() && page.getValue() == Pages.Main);
    private final Setting<Float> yawAngle = new Setting<>("YawAngle", 180.0f, 1.0f, 180.0f, v -> rotate.getValue() && page.getValue() == Pages.Main).addToGroup(yawStep);
    private final Setting<CombatManager.TargetBy> targetLogic = new Setting<>("TargetLogic", CombatManager.TargetBy.Distance, v -> page.getValue() == Pages.Main);
    private final Setting<Float> targetRange = new Setting<>("TargetRange", 10.0f, 1.0f, 15f, v -> page.getValue() == Pages.Main);
    private final Setting<OnBreakBlock> onBreakBlock = new Setting<>("OnBreakBlock", OnBreakBlock.Smart, v -> page.getValue() == Pages.Main);
    public final Setting<Boolean> useOptimizedCalc = new Setting<>("UseOptimizedCalc", true, v -> page.getValue() == Pages.Main);

    /*   PLACE   */
    private final Setting<Interact> interact = new Setting<>("Interact", Interact.Default, v -> page.getValue() == Pages.Place);
    private final Setting<Boolean> rayTraceBypass = new Setting<>("RayTraceBypass", false, v -> page.getValue() == Pages.Place);
    private final Setting<Boolean> strictCenter = new Setting<>("StrictCenter", true, v -> page.getValue() == Pages.Place && interact.getValue() == Interact.Strict);
    private final Setting<Boolean> oldVer = new Setting<>("1.12", false, v -> page.getValue() == Pages.Place);
    private final Setting<Boolean> ccPlace = new Setting<>("CC", true, v -> page.getValue() == Pages.Place);
    private final Setting<BooleanSettingGroup> instantPlace = new Setting<>("InstantPlace", new BooleanSettingGroup(true), v -> page.getValue() == Pages.Place);
    private final Setting<Recalc> recalculate = new Setting<>("Recalc", Recalc.FAST, v -> page.getValue() == Pages.Place).addToGroup(instantPlace);
    private final Setting<Integer> placeDelay = new Setting<>("PlaceDelay", 0, 0, 20, v -> page.getValue() == Pages.Place);
    private final Setting<Integer> lowPlaceDelay = new Setting<>("LowPlaceDelay", 11, 0, 20, v -> page.getValue() == Pages.Place);
    private final Setting<Float> placeRange = new Setting<>("PlaceRange", 5f, 1.0f, 6f, v -> page.getValue() == Pages.Place);
    private final Setting<Float> placeWallRange = new Setting<>("PlaceWallRange", 3.5f, 1.0f, 6f, v -> page.getValue() == Pages.Place);

    /*   BREAK   */
    private final Setting<Integer> breakDelay = new Setting<>("BreakDelay", 0, 0, 20, v -> page.getValue() == Pages.Break);
    private final Setting<Integer> lowBreakDelay = new Setting<>("LowBreakDelay", 11, 0, 20, v -> page.getValue() == Pages.Break);
    private final Setting<Float> explodeRange = new Setting<>("BreakRange", 5.0f, 1.0f, 6f, v -> page.getValue() == Pages.Break);
    private final Setting<Float> explodeWallRange = new Setting<>("BreakWallRange", 3.5f, 1.0f, 6f, v -> page.getValue() == Pages.Break);
    private final Setting<Integer> crystalAge = new Setting<>("CrystalAge", 0, 0, 20, v -> page.getValue() == Pages.Break);

    /*   PAUSE   */
    private final Setting<Boolean> mining = new Setting<>("Mining", true, v -> page.getValue() == Pages.Pause);
    private final Setting<Boolean> eating = new Setting<>("Eating", true, v -> page.getValue() == Pages.Pause);
    private final Setting<Boolean> aura = new Setting<>("Aura", false, v -> page.getValue() == Pages.Pause);
    private final Setting<Boolean> pistonAura = new Setting<>("PistonAura", true, v -> page.getValue() == Pages.Pause);
    private final Setting<Boolean> surround = new Setting<>("Surround", true, v -> page.getValue() == Pages.Pause);
    private final Setting<Boolean> middleClick = new Setting<>("MiddleClick", true, v -> page.getValue() == Pages.Pause);
    private final Setting<Float> pauseHP = new Setting<>("HP", 8.0f, 2.0f, 10f, v -> page.getValue() == Pages.Pause);
    private final Setting<BooleanSettingGroup> switchPause = new Setting<>("SwitchPause", new BooleanSettingGroup(true), v -> page.getValue() == Pages.Pause);
    private final Setting<Integer> switchDelay = new Setting<>("SwitchDelay", 100, 0, 1000, v -> page.getValue() == Pages.Pause).addToGroup(switchPause);

    /*   DAMAGES   */
    public final Setting<Sort> sort = new Setting<>("Sort", Sort.DAMAGE, v -> page.getValue() == Pages.Damages);
    public final Setting<Boolean> assumeBestArmor = new Setting<>("AssumeBestArmor", false, v -> page.getValue() == Pages.Damages);
    public final Setting<Float> minDamage = new Setting<>("MinDamage", 6.0f, 2.0f, 20f, v -> page.getValue() == Pages.Damages);
    public final Setting<Float> maxSelfDamage = new Setting<>("MaxSelfDamage", 10.0f, 2.0f, 20f, v -> page.getValue() == Pages.Damages);
    private final Setting<Safety> safety = new Setting<>("Safety", Safety.NONE, v -> page.getValue() == Pages.Damages);
    private final Setting<Float> safetyBalance = new Setting<>("SafetyBalance", 1.1f, 0.1f, 3f, v -> page.getValue() == Pages.Damages && safety.getValue() == Safety.BALANCE);
    public final Setting<Boolean> protectFriends = new Setting<>("ProtectFriends", true, v -> page.getValue() == Pages.Damages);
    private final Setting<Boolean> overrideSelfDamage = new Setting<>("OverrideSelfDamage", true, v -> page.getValue() == Pages.Damages);
    private final Setting<Float> lethalMultiplier = new Setting<>("LethalMultiplier", 1.0f, 0.0f, 5f, v -> page.getValue() == Pages.Damages);
    private final Setting<BooleanSettingGroup> armorBreaker = new Setting<>("ArmorBreaker", new BooleanSettingGroup(true), v -> page.getValue() == Pages.Damages);
    private final Setting<Float> armorScale = new Setting<>("Armor %", 5.0f, 0.0f, 40f, v -> page.getValue() == Pages.Damages).addToGroup(armorBreaker);
    private final Setting<Float> facePlaceHp = new Setting<>("FacePlaceHp", 5.0f, 0.0f, 20f, v -> page.getValue() == Pages.Damages);
    private final Setting<Bind> facePlaceButton = new Setting<>("FacePlaceBtn", new Bind(GLFW.GLFW_KEY_LEFT_SHIFT, false, false), v -> page.getValue() == Pages.Damages);
    public final Setting<Boolean> ignoreTerrain = new Setting<>("IgnoreTerrain", true, v -> page.getValue() == Pages.Damages);

    /*   SWITCH   */
    private final Setting<Boolean> autoGapple = new Setting<>("AutoGapple", true, v -> page.getValue() == Pages.Switch);
    private final Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL, v -> page.getValue() == Pages.Switch);
    private final Setting<Switch> antiWeakness = new Setting<>("AntiWeakness", Switch.SILENT, v -> page.getValue() == Pages.Switch);

    /*   RENDER   */
    private final Setting<Swing> swingMode = new Setting<>("Swing", Swing.Place, v -> page.getValue() == Pages.Render);
    private final Setting<Boolean> render = new Setting<>("Render", true, v -> page.getValue() == Pages.Render);
    private final Setting<Render> renderMode = new Setting<>("RenderMode", Render.Fade, v -> page.getValue() == Pages.Render);
    private final Setting<Boolean> rselfDamage = new Setting<>("SelfDamage", true, v -> page.getValue() == Pages.Render);
    private final Setting<Boolean> drawDamage = new Setting<>("RenderDamage", true, v -> page.getValue() == Pages.Render);
    private final Setting<ColorSetting> fillColor = new Setting<>("Block Fill Color", new ColorSetting(HudEditor.getColor(0)), v -> page.getValue() == Pages.Render);
    private final Setting<ColorSetting> lineColor = new Setting<>("Block Line Color", new ColorSetting(HudEditor.getColor(0)), v -> page.getValue() == Pages.Render);
    private final Setting<Integer> lineWidth = new Setting<>("Block Line Width", 2, 1, 10, v -> page.getValue() == Pages.Render);
    private final Setting<Integer> slideDelay = new Setting<>("Slide Delay", 200, 1, 1000, v -> page.getValue() == Pages.Render);
    private final Setting<ColorSetting> textColor = new Setting<>("Text Color", new ColorSetting(Color.WHITE), v -> page.getValue() == Pages.Render);

    /*   INFO   */
    private final Setting<Boolean> targetName = new Setting<>("TargetName", false, v -> page.getValue() == Pages.Info);
    private final Setting<Boolean> currentSide = new Setting<>("CurrentSide", true, v -> page.getValue() == Pages.Info);
    private final Setting<Boolean> speed = new Setting<>("Speed", true, v -> page.getValue() == Pages.Info);
    private final Setting<Boolean> confirmInfo = new Setting<>("ConfirmTime", true, v -> page.getValue() == Pages.Info);
    private final Setting<Boolean> calcInfo = new Setting<>("CalcInfo", false, v -> page.getValue() == Pages.Info);

    public static PlayerEntity target;
    private BlockHitResult bestPosition;
    private EndCrystalEntity bestCrystal;

    private final TickTimer placeTimer = new TickTimer();
    private final TickTimer breakTimer = new TickTimer();

    private final Timer blockRecalcTimer = new Timer();
    private final Timer pauseTimer = new Timer();

    private final Map<BlockPos, Long> placedCrystals = new HashMap<>();

    private final DeadManager deadManager = new DeadManager();

    public float renderDamage, renderSelfDamage, rotationYaw, rotationPitch;

    private int prevCrystalsAmount, crystalSpeed, invTimer;

    private boolean rotated, facePlacing, placedOnSpawn;

    private long confirmTime, calcTime;

    private BlockPos renderPos, prevRenderPos;
    private long renderMultiplier;
    private final Map<BlockPos, Long> renderPositions = new ConcurrentHashMap<>();

    public AutoCrystal() {
        super("AutoCrystal", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        resetVars();
    }

    @Override
    public void onDisable() {
        resetVars();
    }

    private void resetVars() {
        facePlacing = false;
        rotated = false;
        renderDamage = 0;
        renderSelfDamage = 0;
        renderMultiplier = 0;
        confirmTime = 0;
        renderPositions.clear();
        placedCrystals.clear();
        deadManager.reset();
        breakTimer.reset();
        placeTimer.reset();
        bestCrystal = null;
        bestPosition = null;
        renderPos = null;
        prevRenderPos = null;
        target = null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;

        ThunderHack.asyncManager.run(() -> {
            calcPosition(placeRange.getValue(), mc.player.getPos());
            getCrystalToExplode();
        });

        if (timing.is(Timing.NORMAL)) {
            if (bestCrystal != null && breakTimer.passedTicks(facePlacing ? lowBreakDelay.getValue() : breakDelay.getValue()))
                attackCrystal(bestCrystal);
            else if (bestPosition != null && placeTimer.passedTicks(facePlacing ? lowPlaceDelay.getValue() : (placeDelay.getValue())) && !placedOnSpawn)
                placeCrystal(bestPosition, false, false);
            placedOnSpawn = false;
        }
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (rotate.getValue())
            calcRotations();
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (mc.player == null || mc.world == null) return;

        target = ThunderHack.combatManager.getTarget(targetRange.getValue(), targetLogic.getValue());

        if (target != null && (target.isDead() || target.getHealth() < 0)) {
            target = null;
            return;
        }

        // Right click gapple
        if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL && autoGapple.getValue()
                && mc.options.useKey.isPressed() && mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
            InventoryUtility.findItemInHotBar(Items.ENCHANTED_GOLDEN_APPLE).switchTo();
        }

        // CA Speed counter
        if (invTimer++ >= 20) {
            crystalSpeed = MathUtility.clamp(prevCrystalsAmount - InventoryUtility.getItemCount(Items.END_CRYSTAL), 0, 255);
            prevCrystalsAmount = InventoryUtility.getItemCount(Items.END_CRYSTAL);
            invTimer = 0;
        }

        // Rotate
        if (rotate.getValue() && mc.player != null && rotationYaw != mc.player.getYaw() && rotationPitch != mc.player.getPitch()) {

            boolean hitVisible = bestCrystal == null || PlayerUtility.canSee(bestCrystal.getPos());
            boolean placeVisible = bestPosition == null || PlayerUtility.canSee(bestPosition.getPos());

            if (mc.player.age % 5 == 0 && rayTraceBypass.getValue() && (!hitVisible || !placeVisible))
                mc.player.setPitch(-90);
            else mc.player.setPitch(rotationPitch);
            mc.player.setYaw(rotationYaw);
        }
    }

    @EventHandler
    public void onPostSync(EventPostSync e) {
        if (timing.is(Timing.SEQUENTIAL)) {

            if (bestCrystal != null && breakTimer.passedTicks(facePlacing ? lowBreakDelay.getValue() : breakDelay.getValue()))
                attackCrystal(bestCrystal);
            else if (bestPosition != null && placeTimer.passedTicks(facePlacing ? lowPlaceDelay.getValue() : placeDelay.getValue()) && !placedOnSpawn)
                placeCrystal(bestPosition, false, false);

            placedOnSpawn = false;
        }
    }

    @Override
    public String getDisplayInfo() {
        StringBuilder info = new StringBuilder();

        Direction side = bestPosition == null ? null : bestPosition.getSide();
        if (side != null) {
            if (targetName.getValue() && target != null) info.append(target.getName().getString()).append(" | ");
            if (speed.getValue()) info.append(crystalSpeed).append(" c/s").append(" | ");
            if (currentSide.getValue()) info.append(side.toString().toUpperCase()).append(" | ");
            if (confirmInfo.getValue()) info.append("c: ").append(confirmTime).append(" | ");
            if (calcInfo.getValue()) info.append("calc: ").append(calcTime).append(" | ");
        }
        return info.length() < 4 ? info.toString() : info.substring(0, info.length() - 3);
    }

    @EventHandler
    public void onCrystalSpawn(@NotNull EventEntitySpawn e) {
        if (e.getEntity() instanceof EndCrystalEntity crystal) {
            HashMap<BlockPos, Long> cache = new HashMap<>(placedCrystals);
            for (BlockPos bp : cache.keySet())
                if (crystal.squaredDistanceTo(bp.toCenterPos()) < 0.3 && breakTimer.passedTicks(facePlacing ? lowBreakDelay.getValue() : breakDelay.getValue())) {
                    confirmTime = System.currentTimeMillis() - cache.get(bp);
                    placedCrystals.remove(bp);
                    ThunderHack.asyncManager.run(() -> handleSpawn(crystal));
                }
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (mc.player == null || mc.world == null) return;

        if (e.getPacket() instanceof ExplosionS2CPacket explosion) {
            for (Entity ent : Lists.newArrayList(mc.world.getEntities())) {
                if (ent instanceof EndCrystalEntity crystal
                        && crystal.squaredDistanceTo(explosion.getX(), explosion.getY(), explosion.getZ()) <= 144
                        && !deadManager.isDead(crystal.getId())) {
                    deadManager.setDead(crystal.getId(), System.currentTimeMillis());
                }
            }
        }
    }

    @EventHandler
    public void onBlockDestruct(EventSetBlockState e) {
        if (mc.player == null || mc.world == null) return;

        if (e.getPrevState() == null || e.getState() == null)
            return;
        if (target != null && target.squaredDistanceTo(e.getPos().toCenterPos()) <= 4 && e.getState().isAir() && !e.getPrevState().isAir() && blockRecalcTimer.every(200)) {
            ThunderHack.asyncManager.run(() -> calcPosition(recalculate.getValue() == Recalc.FAST ? 2f : placeRange.getValue(), recalculate.getValue() == Recalc.FAST ? e.getPos().toCenterPos() : mc.player.getPos()));
        }
    }

    private void handleSpawn(EndCrystalEntity crystal) {
        if (mc.player == null || mc.world == null) return;

        getCrystalToExplode();
        if (bestCrystal == crystal) {
            attackCrystal(crystal);
            if (instantPlace.getValue().isEnabled() && placeTimer.passedTicks(facePlacing ? lowPlaceDelay.getValue() : placeDelay.getValue())) {
                if (recalculate.getValue() != Recalc.OFF)
                    calcPosition(recalculate.getValue() == Recalc.FAST ? 2f : placeRange.getValue(), recalculate.getValue() == Recalc.FAST ? crystal.getPos() : mc.player.getPos());

                if (bestPosition != null)
                    placeCrystal(bestPosition, false, true);
            }
        }
    }

    public void calcRotations() {
        if (rotate.getValue() && !shouldPause() && (bestPosition != null || bestCrystal != null) && mc.player != null) {
            Vec3d vec = bestPosition == null ? bestCrystal.getPos() : bestPosition.getPos();

            float yawDelta = wrapDegrees((float) wrapDegrees(Math.toDegrees(Math.atan2(vec.z - mc.player.getZ(), (vec.x - mc.player.getX()))) - 90) - rotationYaw);
            float pitchDelta = ((float) (-Math.toDegrees(Math.atan2(vec.y - (mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose())), Math.sqrt(Math.pow((vec.x - mc.player.getX()), 2) + Math.pow(vec.z - mc.player.getZ(), 2))))) - rotationPitch);

            yawDelta = (float) (yawDelta + Render2DEngine.interpolate(-1.2f, 1.2f, Math.sin(mc.player.age % 80)) + MathUtility.random(-1.2f, 1.2f));
            pitchDelta = pitchDelta + MathUtility.random(-2.8f, 0.1f);

            if (yawDelta > 180)
                yawDelta = yawDelta - 180;

            float yawStepVal = 180f;

            if (yawStep.getValue().isEnabled())
                yawStepVal = yawAngle.getValue();

            float clampedYawDelta = MathHelper.clamp(MathHelper.abs(yawDelta), -yawStepVal, yawStepVal);
            float clampedPitchDelta = MathHelper.clamp(pitchDelta, -45, 45);

            rotated = MathHelper.abs(yawDelta) <= yawStepVal || !yawStep.getValue().isEnabled();

            float newYaw = rotationYaw + (yawDelta > 0 ? clampedYawDelta : -clampedYawDelta);
            float newPitch = MathHelper.clamp(rotationPitch + clampedPitchDelta, -90.0F, 90.0F);

            double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;

            rotationYaw = (float) (newYaw - (newYaw - rotationYaw) % gcdFix);
            rotationPitch = (float) (newPitch - (newPitch - rotationPitch) % gcdFix);

            ModuleManager.rotations.fixRotation = rotationYaw;
        } else {
            rotationYaw = mc.player.getYaw();
            rotationPitch = mc.player.getPitch();
        }
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        deadManager.update();

        if (render.getValue()) {
            Map<BlockPos, Long> cache = new ConcurrentHashMap<>(renderPositions);

            cache.forEach((pos, time) -> {
                if (System.currentTimeMillis() - time > 500)
                    renderPositions.remove(pos);
            });

            String dmg = MathUtility.round2(renderDamage) + (rselfDamage.getValue() ? " / " + MathUtility.round2(renderSelfDamage) : "");

            if (renderMode.getValue() == Render.Fade) {
                cache.forEach((pos, time) -> {
                    if (System.currentTimeMillis() - time < 500) {
                        int alpha = (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)));

                        Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(new Box(pos), Render2DEngine.injectAlpha(fillColor.getValue().getColorObject(), alpha)));
                        Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(new Box(pos), Render2DEngine.injectAlpha(lineColor.getValue().getColorObject(), alpha), lineWidth.getValue()));

                        if (drawDamage.getValue())
                            Render3DEngine.drawTextIn3D(dmg, pos.toCenterPos(), 0, 0.1, 0, Render2DEngine.applyOpacity(textColor.getValue().getColorObject(), alpha / 100f));
                    }
                });
            } else if (renderMode.getValue() == Render.Slide && renderPos != null) {
                if (prevRenderPos == null) prevRenderPos = renderPos;
                if (renderPositions.isEmpty()) return;
                float mult = MathUtility.clamp((System.currentTimeMillis() - renderMultiplier) / (float) slideDelay.getValue(), 0f, 1f);
                Box interpolatedBox = Render3DEngine.interpolateBox(new Box(prevRenderPos), new Box(renderPos), mult);

                renderBox(dmg, interpolatedBox);
            } else if (renderPos != null) {
                Box box = new Box(renderPos);
                if (renderPositions.isEmpty())
                    return;

                renderBox(dmg, box);
            }
        }
    }

    private void renderBox(String dmg, Box box) {
        Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(box, fillColor.getValue().getColorObject()));
        Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(box, lineColor.getValue().getColorObject(), lineWidth.getValue()));

        if (drawDamage.getValue())
            Render3DEngine.drawTextIn3D(dmg, box.getCenter(), 0, 0.1, 0, textColor.getValue().getColorObject());
    }

    public boolean shouldPause() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return true;

        boolean offhand = mc.player.getOffHandStack().getItem() instanceof EndCrystalItem;
        boolean mainHand = mc.player.getMainHandStack().getItem() instanceof EndCrystalItem;

        if (!pauseTimer.passedMs(1000))
            return true;

        if (mc.interactionManager.isBreakingBlock() && !offhand && mining.getValue())
            return true;

        if (autoSwitch.is(Switch.NONE) && !offhand && !mainHand)
            return true;

        if ((autoSwitch.is(Switch.SILENT) || autoSwitch.is(Switch.NORMAL)) && !InventoryUtility.findItemInHotBar(Items.END_CRYSTAL).found() && !offhand)
            return true;

        if (autoSwitch.is(Switch.INVENTORY) && !InventoryUtility.findItemInInventory(Items.END_CRYSTAL).found() && !offhand)
            return true;

        if (mc.player.isUsingItem() && eating.getValue())
            return true;

        if (rotationMarkedDirty())
            return true;

        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() < pauseHP.getValue())
            return true;

        if (!offhand && autoGapple.getValue() && mc.options.useKey.isPressed() && mc.player.getMainHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE)
            return true;

        boolean silentWeakness = antiWeakness.getValue() == Switch.SILENT || antiWeakness.getValue() == Switch.INVENTORY;

        boolean silent = autoSwitch.getValue() == Switch.SILENT || autoSwitch.getValue() == Switch.INVENTORY;

        return switchPause.getValue().isEnabled() && !ThunderHack.playerManager.switchTimer.passedMs(switchDelay.getValue()) && !silent && !silentWeakness;
    }

    public boolean rotationMarkedDirty() {
        if (ModuleManager.surround.isEnabled() && !ModuleManager.surround.inactivityTimer.passedMs(500) && surround.getValue())
            return true;

        if (ModuleManager.middleClick.isEnabled() && mc.options.pickItemKey.isPressed() && middleClick.getValue())
            return true;

        if (ModuleManager.autoTrap.isEnabled() && !ModuleManager.surround.inactivityTimer.passedMs(500))
            return true;

        if (ModuleManager.blocker.isEnabled() && !ModuleManager.surround.inactivityTimer.passedMs(500))
            return true;

        if (ModuleManager.holeFill.isEnabled() && !HoleFill.inactivityTimer.passedMs(500))
            return true;

        if (ModuleManager.aura.isEnabled() && aura.getValue())
            return true;

        return ModuleManager.pistonAura.isEnabled() && pistonAura.getValue();
    }

    public void attackCrystal(EndCrystalEntity crystal) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || crystal == null) return;

        if (shouldPause() || target == null)
            return;

        StatusEffectInstance weaknessEffect = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
        StatusEffectInstance strengthEffect = mc.player.getStatusEffect(StatusEffects.STRENGTH);

        int prevSlot = -1;
        SearchInvResult antiWeaknessResult = InventoryUtility.getAntiWeaknessItem();
        SearchInvResult antiWeaknessResultInv = InventoryUtility.findInInventory(itemStack ->
                itemStack.getItem() instanceof SwordItem
                        || itemStack.getItem() instanceof PickaxeItem
                        || itemStack.getItem() instanceof AxeItem
                        || itemStack.getItem() instanceof ShovelItem);

        if (antiWeakness.getValue() != Switch.NONE)
            if (weaknessEffect != null && (strengthEffect == null || strengthEffect.getAmplifier() < weaknessEffect.getAmplifier()))
                prevSlot = switchTo(antiWeaknessResult, antiWeaknessResultInv, antiWeakness);

        sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
        swingHand(false, true);

        breakTimer.reset();
        deadManager.setDead(crystal.getId(), System.currentTimeMillis());

        if (prevSlot != -1) {
            if (antiWeakness.getValue() == Switch.SILENT) {
                mc.player.getInventory().selectedSlot = prevSlot;
                sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            }
            if (antiWeakness.getValue() == Switch.INVENTORY) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, prevSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            }
        }
    }

    private boolean canAttackCrystal(EndCrystalEntity cr, boolean deadCheck) {
        if (crystalAge.getValue() != 0 && cr.age < crystalAge.getValue())
            return false;

        if (deadManager.isDead(cr.getId()) && deadCheck)
            return false;

        if (PlayerUtility.squaredDistanceFromEyes(cr.getBoundingBox().getCenter()) > (InteractionUtility.canSee(cr) ? explodeRange.getPow2Value() : explodeWallRange.getPow2Value()))
            return false;

        if (!cr.isAlive())
            return false;

        float damage = ExplosionUtility.getAutoCrystalDamage(cr.getPos(), target, getPredictTicks(), useOptimizedCalc.getValue());
        float selfDamage = ExplosionUtility.getSelfExplosionDamage(cr.getPos(), getPredictTicks(), useOptimizedCalc.getValue());

        boolean overrideDamage = shouldOverrideDamage(damage, selfDamage);

        if (protectFriends.getValue()) {
            List<PlayerEntity> players = Lists.newArrayList(mc.world.getPlayers());
            for (PlayerEntity pl : players) {
                if (!ThunderHack.friendManager.isFriend(pl)) continue;
                float fdamage = ExplosionUtility.getAutoCrystalDamage(cr.getPos(), pl, getPredictTicks(), useOptimizedCalc.getValue());
                if (fdamage > selfDamage) {
                    selfDamage = fdamage;
                }
            }
        }

        return !(selfDamage > maxSelfDamage.getValue()) || overrideDamage;
    }

    private int switchTo(SearchInvResult result, SearchInvResult resultInv, @NotNull Setting<Switch> switchMode) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return -1;

        int prevSlot = mc.player.getInventory().selectedSlot;

        switch (switchMode.getValue()) {
            case INVENTORY -> {
                if (resultInv.found()) {
                    prevSlot = resultInv.slot();
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, prevSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                }
            }
            case NORMAL -> result.switchTo();
            case SILENT -> result.switchToSilent();
            case NONE -> {
            }
        }
        return prevSlot;
    }

    public void placeCrystal(BlockHitResult bhr, boolean instant, boolean onSpawn) {
        if (shouldPause() || mc.player == null) return;
        int prevSlot = -1;

        SearchInvResult crystalResult = InventoryUtility.findItemInHotBar(Items.END_CRYSTAL);
        SearchInvResult crystalResultInv = InventoryUtility.findItemInInventory(Items.END_CRYSTAL);

        boolean offhand = mc.player.getOffHandStack().getItem() instanceof EndCrystalItem;
        boolean holdingCrystal = mc.player.getMainHandStack().getItem() instanceof EndCrystalItem || offhand;

        if (rotate.getValue()) {
            if (instant) {
                float[] angle = InteractionUtility.calculateAngle(bhr.getPos());
                sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), angle[0], angle[1], mc.player.isOnGround()));
            } else if (!rotated)
                return;
        }

        Box posBB = new Box(bhr.getBlockPos().up());

        if (!ccPlace.getValue())
            posBB = posBB.expand(0, 1f, 0);

        if (checkOtherEntities(posBB))
            return;

        if (autoSwitch.getValue() != Switch.NONE && !holdingCrystal)
            prevSlot = switchTo(crystalResult, crystalResultInv, autoSwitch);

        if (!(mc.player.getMainHandStack().getItem() instanceof EndCrystalItem || offhand || autoSwitch.getValue() == Switch.SILENT))
            return;

        sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, bhr, id));
        swingHand(offhand, false);

        placeTimer.reset();

        if (!bhr.getBlockPos().equals(renderPos)) {
            renderMultiplier = System.currentTimeMillis();
            prevRenderPos = renderPos;
            renderPos = bhr.getBlockPos();
        }

        if (!placedCrystals.containsKey(bhr.getBlockPos()))
            placedCrystals.put(bhr.getBlockPos(), System.currentTimeMillis());
        renderPositions.put(bhr.getBlockPos(), System.currentTimeMillis());
        postPlaceSwitch(prevSlot);

        if (onSpawn)
            placedOnSpawn = true;
    }

    private void postPlaceSwitch(int slot) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (autoSwitch.getValue() == Switch.SILENT && slot != -1) {
            mc.player.getInventory().selectedSlot = slot;
            sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        }

        if (autoSwitch.getValue() == Switch.INVENTORY && slot != -1) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        }
    }

    public void calcPosition(float range, Vec3d center) {
        if (mc.player == null || mc.world == null) return;
        long currentTime = System.currentTimeMillis();

        if (ModuleManager.speedMine.isWorth()) {
            if (onBreakBlock.getValue() == OnBreakBlock.Smart) {
                // если цивбрикаем - то ставим над
                if (mc.world.isAir(SpeedMine.minePosition.down())) {
                    PlaceData autoMineData = getPlaceData(SpeedMine.minePosition, null);
                    if (autoMineData != null) {
                        bestPosition = autoMineData.bhr;
                        return;
                    }
                }

                // иначе ставим рядом, чтоб трахнуть сурраунд
                for (Direction dir : Direction.values()) {
                    if (dir == Direction.UP || dir == Direction.DOWN) continue;
                    PlaceData autoMineData = getPlaceData(SpeedMine.minePosition.down().offset(dir), null);
                    if (autoMineData != null) {
                        bestPosition = autoMineData.bhr;
                        return;
                    }
                }

                // если ставить некуда, то ставим все-таки над
                PlaceData autoMineData = getPlaceData(SpeedMine.minePosition, null);
                if (autoMineData != null) {
                    bestPosition = autoMineData.bhr;
                    return;
                }
            } else if (onBreakBlock.getValue() == OnBreakBlock.PlaceOn) {
                PlaceData autoMineData = getPlaceData(SpeedMine.minePosition, null);
                if (autoMineData != null) {
                    bestPosition = autoMineData.bhr;
                    return;
                }
            }
        }

        if (target == null) {
            renderPos = null;
            prevRenderPos = null;
            bestPosition = null;
            return;
        }

        List<PlaceData> list = getPossibleBlocks(target, center, range).stream().filter(data -> isSafe(data.damage, data.selfDamage, data.overrideDamage)).toList();
        bestPosition = list.isEmpty() ? null : filterPositions(list);
        calcTime = System.currentTimeMillis() - currentTime;
    }

    private @NotNull List<PlaceData> getPossibleBlocks(PlayerEntity target, Vec3d center, float range) {
        List<PlaceData> blocks = new ArrayList<>();
        BlockPos playerPos = BlockPos.ofFloored(center);
        int r = (int) Math.ceil(range);

        for (int x = playerPos.getX() - r; x <= playerPos.getX() + r; x++) {
            for (int y = playerPos.getY() - r; y <= playerPos.getY() + r; y++) {
                for (int z = playerPos.getZ() - r; z <= playerPos.getZ() + r; z++) {
                    PlaceData data = getPlaceData(new BlockPos(x, y, z), target);
                    if (data != null)
                        blocks.add(data);
                }
            }
        }

        return blocks;
    }

    private @NotNull List<CrystalData> getPossibleCrystals(PlayerEntity target) {
        List<CrystalData> crystals = new ArrayList<>();

        if (mc.player == null || mc.world == null) return crystals;

        Iterable<Entity> entities = Lists.newArrayList(mc.world.getEntities());
        for (Entity ent : entities) {
            if (!(ent instanceof EndCrystalEntity cr))
                continue;

            if (crystalAge.getValue() != 0 && cr.age < crystalAge.getValue())
                continue;

            if (deadManager.isDead(cr.getId()))
                continue;

            if (PlayerUtility.squaredDistanceFromEyes(ent.getBoundingBox().getCenter()) > (InteractionUtility.canSee(cr) ? explodeRange.getPow2Value() : explodeWallRange.getPow2Value()))
                continue;

            if (!ent.isAlive())
                continue;

            float damage = ExplosionUtility.getAutoCrystalDamage(ent.getPos(), target, getPredictTicks(), useOptimizedCalc.getValue());
            float selfDamage = ExplosionUtility.getSelfExplosionDamage(ent.getPos(), getPredictTicks(), useOptimizedCalc.getValue());

            boolean overrideDamage = shouldOverrideDamage(damage, selfDamage);

            if (protectFriends.getValue()) {
                List<PlayerEntity> players = Lists.newArrayList(mc.world.getPlayers());
                for (PlayerEntity pl : players) {
                    if (!ThunderHack.friendManager.isFriend(pl)) continue;
                    float fdamage = ExplosionUtility.getAutoCrystalDamage(ent.getPos(), pl, getPredictTicks(), useOptimizedCalc.getValue());
                    if (fdamage > selfDamage) {
                        selfDamage = fdamage;
                    }
                }
            }

            if (damage < 1.5f) continue;
            if (selfDamage > maxSelfDamage.getValue() && !overrideDamage) continue;
            crystals.add(new CrystalData((EndCrystalEntity) ent, damage, selfDamage, overrideDamage));
        }
        return crystals;
    }

    public void getCrystalToExplode() {
        if (target == null) {
            bestCrystal = null;
            return;
        }

        List<CrystalData> list = getPossibleCrystals(target).stream().filter(data -> isSafe(data.damage, data.selfDamage, data.overrideDamage)).toList();
        bestCrystal = list.isEmpty() ? null : filterCrystals(list);
    }

    public boolean isSafe(float damage, float selfDamage, boolean overrideDamage) {
        if (mc.player == null || mc.world == null) return false;

        if (overrideDamage)
            return true;

        if (selfDamage + 0.5 > mc.player.getHealth() + mc.player.getAbsorptionAmount()) return false;
        else if (safety.getValue() == Safety.STABLE) return damage - selfDamage > 0;
        else if (safety.getValue() == Safety.BALANCE) return (damage * safetyBalance.getValue()) - selfDamage > 0;
        return true;
    }

    private @Nullable BlockHitResult filterPositions(@NotNull List<PlaceData> clearedList) {
        PlaceData bestData = null;
        float bestVal = 0f;
        for (PlaceData data : clearedList) {
            if ((shouldOverride(data.damage) || data.damage > minDamage.getValue())) {
                if (sort.getValue() == Sort.DAMAGE) {
                    if (bestVal < data.damage) {
                        bestData = data;
                        bestVal = data.damage;
                    }
                } else {
                    if (bestVal < data.damage / data.selfDamage) {
                        bestData = data;
                        bestVal = data.damage / data.selfDamage;
                    }
                }
            }
        }

        if (bestData == null) return null;
        facePlacing = bestData.damage < minDamage.getValue();
        renderDamage = bestData.damage;
        renderSelfDamage = bestData.selfDamage;
        return bestData.bhr;
    }

    public boolean shouldOverride(float damage) {
        if (target == null) return false;

        boolean override = target.getHealth() + target.getAbsorptionAmount() <= facePlaceHp.getValue();

        if (armorBreaker.getValue().isEnabled())
            for (ItemStack armor : target.getArmorItems())
                if (armor != null && !armor.getItem().equals(Items.AIR) && ((armor.getMaxDamage() - armor.getDamage()) / (float) armor.getMaxDamage()) * 100 < armorScale.getValue()) {
                    override = true;
                    break;
                }

        if (facePlaceButton.getValue().getKey() != -1 && InputUtil.isKeyPressed(mc.getWindow().getHandle(), facePlaceButton.getValue().getKey()))
            override = true;

        if ((target.getHealth() + target.getAbsorptionAmount()) - (damage * lethalMultiplier.getValue()) < 0.5)
            override = true;

        return override;
    }

    private @Nullable EndCrystalEntity filterCrystals(@NotNull List<CrystalData> clearedList) {
        CrystalData bestData = null;
        float bestVal = 0f;
        for (CrystalData data : clearedList) {
            if ((shouldOverride(data.damage) || data.damage > minDamage.getValue())) {
                if (sort.getValue() == Sort.DAMAGE) {
                    if (bestVal < data.damage) {
                        bestData = data;
                        bestVal = data.damage;
                    }
                } else {
                    if (bestVal < data.damage / data.selfDamage) {
                        bestData = data;
                        bestVal = data.damage / data.selfDamage;
                    }
                }
            }
        }

        if (bestData == null) return null;
        renderDamage = bestData.damage;
        renderSelfDamage = bestData.selfDamage;
        return bestData.crystal;
    }

    public @Nullable PlaceData getPlaceData(BlockPos bp, PlayerEntity target) {
        if (mc.player == null || mc.world == null)
            return null;

        if (!predictCrystalSpawn(bp))
            return null;

        if (target != null && target.getPos().squaredDistanceTo(bp.toCenterPos().add(0, 0.5, 0)) > 144)
            return null;

        Block base = mc.world.getBlockState(bp).getBlock();

        if (base != Blocks.OBSIDIAN && base != Blocks.BEDROCK)
            return null;

        boolean freeSpace = mc.world.isAir(bp.up());

        if (!(freeSpace && (!oldVer.getValue() || mc.world.isAir(bp.up().up()))))
            return null;

        if (checkEntities(bp))
            return null;

        Vec3d crystalVec = new Vec3d(0.5f + bp.getX(), 1f + bp.getY(), 0.5f + bp.getZ());

        float damage = target == null ? 10f : ExplosionUtility.getAutoCrystalDamage(crystalVec, target, getPredictTicks(), useOptimizedCalc.getValue());
        if (damage < 1.5f) return null;
        float selfDamage = ExplosionUtility.getSelfExplosionDamage(crystalVec, getPredictTicks(), useOptimizedCalc.getValue());
        boolean overrideDamage = shouldOverrideDamage(damage, selfDamage);

        if (protectFriends.getValue()) {
            List<PlayerEntity> players = Lists.newArrayList(mc.world.getPlayers());
            for (PlayerEntity pl : players) {
                if (!ThunderHack.friendManager.isFriend(pl)) continue;
                float fdamage = ExplosionUtility.getAutoCrystalDamage(crystalVec, pl, getPredictTicks(), useOptimizedCalc.getValue());
                if (fdamage > selfDamage) {
                    selfDamage = fdamage;
                }
            }
        }

        if (selfDamage > maxSelfDamage.getValue() && !overrideDamage) return null;

        BlockHitResult interactResult = getInteractResult(bp, crystalVec);
        if (interactResult == null) return null;

        return new PlaceData(interactResult, damage, selfDamage, overrideDamage);
    }

    public boolean predictCrystalSpawn(BlockPos bp) {
        Vec3d predictedPos = bp.toCenterPos().add(0, 1.5f, 0);
        PlayerEntity predictedPlayer = PredictUtility.predictPlayer(mc.player, getPredictTicks());
        float distance = (float) predictedPlayer.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0).squaredDistanceTo(predictedPos);

        if (InteractionUtility.canSee(predictedPos))
            return distance <= explodeRange.getPow2Value();
        else
            return distance <= explodeWallRange.getPow2Value();
    }

    public BlockHitResult getInteractResult(BlockPos bp, Vec3d crystalVec) {
        BlockHitResult interactResult = null;
        switch (interact.getValue()) {
            case Default -> interactResult = getDefaultInteract(crystalVec, bp);
            case Strict -> interactResult = getStrictInteract(bp);
        }
        return interactResult;
    }

    public boolean checkEntities(@NotNull BlockPos base) {
        if (mc.player == null || mc.world == null) return false;


        Box posBoundingBox = new Box(base.up());

        if (!ccPlace.getValue())
            posBoundingBox = posBoundingBox.expand(0, 1f, 0);

        Iterable<Entity> entities = Lists.newArrayList(mc.world.getEntities());
        for (Entity ent : entities) {
            if (ent == null) continue;
            if (ent.getBoundingBox().intersects(posBoundingBox)) {
                if (ent instanceof ExperienceOrbEntity)
                    continue;
                if (ent instanceof EndCrystalEntity cr && canAttackCrystal(cr, false)) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private boolean checkOtherEntities(Box posBoundingBox) {
        if (mc.player == null || mc.world == null) return false;

        Iterable<Entity> entities = Lists.newArrayList(mc.world.getEntities());

        for (Entity ent : entities) {
            if (ent == null) continue;
            if (ent.getBoundingBox().intersects(posBoundingBox)) {
                if (ent instanceof ExperienceOrbEntity)
                    continue;

                if (ent instanceof EndCrystalEntity cr && deadManager.isDead(cr.getId()))
                    continue;

                if (breakTimer.passedTicks(facePlacing ? lowBreakDelay.getValue() : breakDelay.getValue()) && ent instanceof EndCrystalEntity cr
                        && canAttackCrystal(cr, true)
                        && cr.getPos().squaredDistanceTo(posBoundingBox.getCenter()) > 0.3) {
                    attackCrystal(cr);
                    debug("attack stuck crystal");
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public boolean shouldOverrideDamage(float damage, float selfDamage) {
        if (overrideSelfDamage.getValue() && target != null) {
            if (mc.player == null || mc.world == null) return false;

            boolean targetSafe = (target.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING
                    || target.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING);

            boolean playerSafe = (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING
                    || mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING);

            float targetHp = target.getHealth() + target.getAbsorptionAmount() - 1f;

            float playerHp = mc.player.getHealth() + mc.player.getAbsorptionAmount() - 1f;

            boolean canPop = damage > targetHp && targetSafe;

            boolean canKill = damage > targetHp && !targetSafe;

            boolean canPopSelf = selfDamage > playerHp && playerSafe;

            boolean canKillSelf = selfDamage > playerHp && !playerSafe;

            if (canPopSelf && canKill)
                return true;

            return selfDamage > maxSelfDamage.getValue() && (canPop || canKill) && !canKillSelf && !canPopSelf;
        }
        return false;
    }

    private @Nullable BlockHitResult getDefaultInteract(Vec3d crystalVector, BlockPos bp) {
        if (mc.player == null || mc.world == null) return null;

        if (PlayerUtility.squaredDistanceFromEyes(crystalVector) > placeRange.getPow2Value())
            return null;

        BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), crystalVector, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));

        if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
            if (PlayerUtility.squaredDistanceFromEyes(crystalVector) > placeWallRange.getPow2Value())
                return null;

        return new BlockHitResult(crystalVector, mc.world.isInBuildLimit(bp.up()) ? Direction.UP : Direction.DOWN, bp, false);
    }

    public @Nullable BlockHitResult getStrictInteract(@NotNull BlockPos bp) {
        if (mc.player == null || mc.world == null) return null;

        float bestDistance = Float.MAX_VALUE;
        Direction bestDirection = null;

        float upPoint = strictCenter.getValue() ? (float) bp.toCenterPos().getY() : bp.up().getY();

        if (mc.player.getEyePos().getY() > upPoint) {
            bestDirection = Direction.UP;
        } else if (mc.player.getEyePos().getY() < bp.getY() && mc.world.isAir(bp.down())) {
            bestDirection = Direction.DOWN;
        } else {
            for (Direction dir : InteractionUtility.getStrictBlockDirections(bp)) {
                if (dir == Direction.UP || dir == Direction.DOWN)
                    continue;

                Vec3d directionVec = new Vec3d(bp.getX() + 0.5 + dir.getVector().getX() * 0.5, bp.getY() + 0.99, bp.getZ() + 0.5 + dir.getVector().getZ() * 0.5);

                if (!mc.world.getBlockState(bp.offset(dir)).isReplaceable())
                    continue;

                float distance = PlayerUtility.squaredDistanceFromEyes(directionVec);
                if (bestDistance > distance) {
                    bestDirection = dir;
                    bestDistance = distance;
                }
            }
        }

        if (bestDirection == null)
            return null;

        Vec3d vec = InteractionUtility.getVisibleDirectionPoint(bestDirection, bp, placeWallRange.getValue(), placeRange.getValue());

        if (vec == null)
            return null;

        return new BlockHitResult(vec, bestDirection, bp, false);
    }

    private void swingHand(boolean offHand, boolean attack) {
        switch (swingMode.getValue()) {
            case Both -> mc.player.swingHand(offHand ? Hand.OFF_HAND : Hand.MAIN_HAND);
            case Break -> {
                if (attack)
                    mc.player.swingHand(Hand.MAIN_HAND);
                else
                    sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

            }
            case Place -> {
                if (!attack)
                    mc.player.swingHand(offHand ? Hand.OFF_HAND : Hand.MAIN_HAND);
                else
                    sendPacket(new HandSwingC2SPacket(offHand ? Hand.OFF_HAND : Hand.MAIN_HAND));
            }
        }
    }

    public void pause() {
        pauseTimer.reset();
    }

    private int getPredictTicks() {
        // TODO
        return 0;
    }

    public record PlaceData(BlockHitResult bhr, float damage, float selfDamage, boolean overrideDamage) {
    }

    private record CrystalData(EndCrystalEntity crystal, float damage, float selfDamage, boolean overrideDamage) {
    }

    private enum Pages {
        Place, Break, Pause, Render, Damages, Main, Switch, Remove, Info
    }

    private enum Switch {
        NONE, NORMAL, SILENT, INVENTORY
    }

    private enum Timing {
        NORMAL, SEQUENTIAL
    }

    private enum Interact {
        Default, Strict
    }

    public enum Safety {
        BALANCE, STABLE, NONE
    }

    public enum Sort {
        SAFE, DAMAGE
    }

    public enum Render {
        Fade, Slide, Default
    }

    public enum OnBreakBlock {
        PlaceOn, Smart, None
    }

    public enum Recalc {
        OFF, FAST, SLOW
    }

    public enum Swing {
        Both, Place, Break, ServerSide
    }
}