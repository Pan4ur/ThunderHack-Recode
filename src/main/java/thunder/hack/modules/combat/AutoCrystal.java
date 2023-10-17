package thunder.hack.modules.combat;

import com.google.common.collect.Lists;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
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
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.*;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.player.SpeedMine;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.ExplosionUtility;
import thunder.hack.utility.math.MathUtility;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static thunder.hack.modules.client.MainSettings.isRu;

public final class AutoCrystal extends Module {

    /*   MAIN   */
    private static final Setting<Pages> page = new Setting<>("Page", Pages.Main);
    private final Setting<Timing> timing = new Setting<>("Timing", Timing.NORMAL, v -> page.getValue() == Pages.Main);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", true, v -> page.getValue() == Pages.Main);
    private final Setting<Boolean> yawStep = new Setting<>("YawStep", false, v -> rotate.getValue() && page.getValue() == Pages.Main);
    private final Setting<Float> yawAngle = new Setting<>("YawAngle", 180.0f, 1.0f, 180.0f, v -> rotate.getValue() && yawStep.getValue() && page.getValue() == Pages.Main);
    private final Setting<TargetLogic> targetLogic = new Setting<>("TargetLogic", TargetLogic.Distance, v -> page.getValue() == Pages.Main);
    private final Setting<Float> targetRange = new Setting<>("TargetRange", 10.0f, 1.0f, 15f, v -> page.getValue() == Pages.Main);
    public static final Setting<Integer> selfPredictTicks = new Setting<>("SelfPredictTicks", 3, 0, 20, v -> page.getValue() == Pages.Main);
    private final Setting<OnBreakBlock> onBreakBlock = new Setting<>("OnBreakBlock", OnBreakBlock.Smart, v -> page.getValue() == Pages.Main);

    /* MULTITHREADING */
    private final Setting<Boolean> multiThread = new Setting<>("MultiThread", false, v -> page.getValue() == Pages.MultiThread);

    /*   PLACE   */
    private final Setting<Interact> interact = new Setting<>("Interact", Interact.Default, v -> page.getValue() == Pages.Place);
    private final Setting<Boolean> oldVer = new Setting<>("1.12", false, v -> page.getValue() == Pages.Place);
    private final Setting<Boolean> ccPlace = new Setting<>("CC", true, v -> page.getValue() == Pages.Place);
    private final Setting<Boolean> instantPlace = new Setting<>("InstantPlace", true, v -> page.getValue() == Pages.Place);
    private final Setting<Integer> placeDelay = new Setting<>("PlaceDelay", 0, 0, 1000, v -> page.getValue() == Pages.Place);
    private final Setting<Float> placeRange = new Setting<>("PlaceRange", 5f, 1.0f, 6f, v -> page.getValue() == Pages.Place);
    private final Setting<Float> placeWallRange = new Setting<>("PlaceWallRange", 3.5f, 1.0f, 6f, v -> page.getValue() == Pages.Place);
    public static final Setting<Integer> predictTicks = new Setting<>("PredictTicks", 3, 0, 20, v -> page.getValue() == Pages.Place);

    /*   BREAK   */
    private final Setting<Integer> breakDelay = new Setting<>("BreakDelay", 0, 0, 1000, v -> page.getValue() == Pages.Break);
    private final Setting<Boolean> instantBreak = new Setting<>("InstantBreak", false, v -> page.getValue() == Pages.Break && multiThread.getValue());
    private final Setting<Float> explodeRange = new Setting<>("BreakRange", 5.0f, 1.0f, 6f, v -> page.getValue() == Pages.Break);
    private final Setting<Float> explodeWallRange = new Setting<>("BreakWallRange", 3.5f, 1.0f, 6f, v -> page.getValue() == Pages.Break);
    private final Setting<Integer> crystalAge = new Setting<>("CrystalAge", 0, 0, 20, v -> page.getValue() == Pages.Break);
    private final Setting<Integer> limitAttacks = new Setting<>("LimitAttacks", 2, 1, 10, v -> page.getValue() == Pages.Break);

    /*   PAUSE   */
    private final Setting<Boolean> mining = new Setting<>("Mining", true, v -> page.getValue() == Pages.Pause);
    private final Setting<Boolean> eating = new Setting<>("Eating", true, v -> page.getValue() == Pages.Pause);
    private final Setting<Boolean> aura = new Setting<>("Aura", false, v -> page.getValue() == Pages.Pause);
    private final Setting<Boolean> pistonAura = new Setting<>("PistonAura", true, v -> page.getValue() == Pages.Pause);
    private final Setting<Boolean> surround = new Setting<>("Surround", true, v -> page.getValue() == Pages.Pause);
    private final Setting<Float> pauseHP = new Setting<>("HP", 8.0f, 2.0f, 10f, v -> page.getValue() == Pages.Pause);
    private final Setting<Boolean> switchPause = new Setting<>("SwitchPause", true, v -> page.getValue() == Pages.Pause);
    private final Setting<Integer> switchDelay = new Setting<>("SwitchDelay", 100, 0, 1000, v -> page.getValue() == Pages.Pause && switchPause.getValue());

    /*   DAMAGES   */
    private final Setting<Sort> sort = new Setting<>("Sort", Sort.DAMAGE, v -> page.getValue() == Pages.Damages);
    private final Setting<Float> minDamage = new Setting<>("MinDamage", 6.0f, 2.0f, 20f, v -> page.getValue() == Pages.Damages);
    private final Setting<Float> maxSelfDamage = new Setting<>("MaxSelfDamage", 10.0f, 2.0f, 20f, v -> page.getValue() == Pages.Damages);
    private final Setting<Safety> safety = new Setting<>("Safety", Safety.NONE, v -> page.getValue() == Pages.Damages);
    private final Setting<Float> safetyBalance = new Setting<>("SafetyBalance", 1.1f, 0.1f, 3f, v -> page.getValue() == Pages.Damages && safety.getValue() == Safety.BALANCE);
    private final Setting<Boolean> protectFriends = new Setting<>("ProtectFriends", true, v -> page.getValue() == Pages.Damages);
    private final Setting<Boolean> overrideSelfDamage = new Setting<>("OverrideSelfDamage", true, v -> page.getValue() == Pages.Damages);
    private final Setting<Float> lethalMultiplier = new Setting<>("LethalMultiplier", 1.0f, 0.0f, 5f, v -> page.getValue() == Pages.Damages);
    private final Setting<Boolean> armorBreaker = new Setting<>("ArmorBreaker", true, v -> page.getValue() == Pages.Damages);
    private final Setting<Float> armorScale = new Setting<>("Armor %", 5.0f, 0.0f, 40f, v -> armorBreaker.getValue() && page.getValue() == Pages.Damages);
    private final Setting<Float> facePlaceHp = new Setting<>("FacePlaceHp", 5.0f, 2.0f, 20f, v -> page.getValue() == Pages.Damages);
    private final Setting<Bind> facePlaceButton = new Setting<>("FacePlaceButton", new Bind(GLFW.GLFW_KEY_LEFT_SHIFT, false, false), v -> page.getValue() == Pages.Damages);

    /*   SWITCH   */
    private final Setting<Boolean> autoGapple = new Setting<>("AutoGapple", true, v -> page.getValue() == Pages.Switch);
    private final Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL, v -> page.getValue() == Pages.Switch);
    private final Setting<Switch> antiWeakness = new Setting<>("AntiWeakness", Switch.SILENT, v -> page.getValue() == Pages.Switch);

    /*   Remove   */
    private final Setting<Remove> remove = new Setting<>("Remove", Remove.Fake, v -> page.getValue() == Pages.Remove);
    private final Setting<Integer> removeDelay = new Setting<>("RemoveDelay", 10, 0, 200, v -> page.getValue() == Pages.Remove);

    /*   RENDER   */
    private final Setting<Boolean> render = new Setting<>("Render", true, v -> page.getValue() == Pages.Render);
    private final Setting<Render> renderMode = new Setting<>("RenderMode", Render.Fade, v -> page.getValue() == Pages.Render);
    private final Setting<Boolean> rselfDamage = new Setting<>("SelfDamage", true, v -> page.getValue() == Pages.Render);
    private final Setting<Boolean> drawDamage = new Setting<>("RenderDamage", true, v -> page.getValue() == Pages.Render);
    private final Setting<ColorSetting> fillColor = new Setting<>("Block Fill Color", new ColorSetting(HudEditor.getColor(0)), v -> page.getValue() == Pages.Render);
    private final Setting<ColorSetting> lineColor = new Setting<>("Block Line Color", new ColorSetting(HudEditor.getColor(0)), v -> page.getValue() == Pages.Render);
    private final Setting<Integer> lineWidth = new Setting<>("Block Line Width", 2, 1, 10, v -> page.getValue() == Pages.Render);
    private final Setting<Integer> slideDelay = new Setting<>("Slide Delay", 200, 1, 1000, v -> page.getValue() == Pages.Render);
    private final Setting<ColorSetting> textColor = new Setting<>("Text Color", new ColorSetting(Color.WHITE), v -> page.getValue() == Pages.Render);

    public static PlayerEntity target;
    private BlockHitResult bestPosition;
    private EndCrystalEntity bestCrystal;

    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private final Timer switchTimer = new Timer();

    // позиция и время постановки
    private final Map<BlockPos, Long> placedCrystals = new HashMap<>();

    // id кристаллa и кол-во ударов
    private final Map<Integer, Integer> attackedCrystals = new HashMap<>();

    private final Map<EndCrystalEntity, Long> deadCrystals = new HashMap<>();

    private float renderDamage, renderSelfDamage;

    private int prevCrystalsAmount, crystalSpeed, invTimer;

    private boolean rotated, tickBusy, prevMthread;

    private BlockPos renderPos, prevRenderPos;
    private long renderMultiplier;
    private final Map<BlockPos, Long> renderPositions = new ConcurrentHashMap<>();

    // Threads
    private PlaceThread placeThread;
    private BreakThread breakThread;
    private final AtomicBoolean stopThreads = new AtomicBoolean(false);

    private static AutoCrystal instance;

    public AutoCrystal() {
        super("AutoCrystal", Category.COMBAT);
        instance = this;
    }

    @Override
    public void onEnable() {
        rotated = false;
        renderDamage = 0;
        renderSelfDamage = 0;
        attackedCrystals.clear();
        placedCrystals.clear();
        deadCrystals.clear();
        switchTimer.reset();
        breakTimer.reset();
        placeTimer.reset();
        bestCrystal = null;
        bestPosition = null;
        renderPos = null;
        prevRenderPos = null;
        target = null;
        renderMultiplier = 0;
        renderPositions.clear();

        prevMthread = multiThread.getValue();

        stopThreads.set(false);

        if (multiThread.getValue()) {
            placeThread = new PlaceThread();
            breakThread = new BreakThread();

            placeThread.start();
            breakThread.start();
        }
    }

    @Override
    public void onDisable() {
        target = null;
        if (placeThread != null)
            stopThreads.set(true);
        if (breakThread != null)
            stopThreads.set(true);
    }

    @EventHandler
    public void onSync(EventSync e) {
        switch (targetLogic.getValue()) {
            case HP -> target = ThunderHack.combatManager.getTargetByHP(targetRange.getValue());
            case Distance -> target = ThunderHack.combatManager.getNearestTarget(targetRange.getValue());
            case FOV -> target = ThunderHack.combatManager.getTargetByFOV(targetRange.getValue());
        }

        if (target != null && (target.isDead() || target.getHealth() < 0)) {
            target = null;
            return;
        }

        if (prevMthread != multiThread.getValue()) {
            disable(isRu() ? "Включи модуль заново!" : "Re-enable me!");
            return;
        }

        if (renderPositions.isEmpty()) {
            attackedCrystals.clear();
            deadCrystals.clear();
        }

        if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL && autoGapple.getValue()
                && mc.options.useKey.isPressed() && mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
            SearchInvResult result = InventoryUtility.findItemInHotBar(Items.ENCHANTED_GOLDEN_APPLE);
            result.switchTo();
        }

        if (invTimer++ >= 20) {
            crystalSpeed = prevCrystalsAmount - InventoryUtility.getItemCount(Items.END_CRYSTAL);
            prevCrystalsAmount = InventoryUtility.getItemCount(Items.END_CRYSTAL);
            invTimer = 0;
        }

        HashMap<Integer, Integer> cache = new HashMap<>(attackedCrystals);
        cache.forEach((crystal, attacks) -> {
            if (mc.world.getEntityById(crystal) == null)
                attackedCrystals.remove(crystal);
        });

        if (rotate.getValue()) rotateMethod();
    }

    @Override
    public String getDisplayInfo() {
        String info = crystalSpeed + " c/s";
        if (bestPosition != null)
            info = crystalSpeed + " c/s | " + bestPosition.getSide().toString().toUpperCase();
        return info;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (target == null)
            return;
        if (e.getPacket() instanceof EntitySpawnS2CPacket spawn) {
            onSpawnPacket(spawn);
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.@NotNull Send e) {
        if (e.getPacket() instanceof UpdateSelectedSlotC2SPacket) switchTimer.reset();
    }

    @EventHandler
    public void onPostSync(EventPostSync e) {
        if (!multiThread.getValue()) {
            if (bestPosition != null && placeTimer.passedMs(placeDelay.getValue()))
                placeCrystal(bestPosition);

            if (bestCrystal != null && breakTimer.passedMs(breakDelay.getValue()))
                attackCrystal(bestCrystal);
        }
        tickBusy = false;
    }

    public void rotateMethod() {
        if (bestPosition != null && mc.player != null) {
            float[] angle = InteractionUtility.calculateAngle(bestPosition.getPos());
            if (yawStep.getValue()) {
                float yaw_delta = MathHelper.wrapDegrees(angle[0] - ((IClientPlayerEntity) mc.player).getLastYaw());
                if (Math.abs(yaw_delta) > yawAngle.getValue()) {
                    angle[0] = ((IClientPlayerEntity) mc.player).getLastYaw() + (yaw_delta * (yawAngle.getValue() / Math.abs(yaw_delta)));
                    rotated = false;
                } else rotated = true;
            } else rotated = true;

            double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
            mc.player.setYaw((float) (angle[0] - (angle[0] - ((IClientPlayerEntity) mc.player).getLastYaw()) % gcdFix));
            mc.player.setPitch((float) (angle[1] - (angle[1] - ((IClientPlayerEntity) mc.player).getLastPitch()) % gcdFix));
        }
    }

    public void onRender3D(MatrixStack stack) {
        removeAttackedCrystals();
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

    public boolean canDoAC() {
        boolean offhand = mc.player.getOffHandStack().getItem() instanceof EndCrystalItem;
        if (mc.interactionManager.isBreakingBlock() && !offhand && mining.getValue())
            return false;

        if (mc.player.isUsingItem() && eating.getValue() && !offhand)
            return false;

        if (rotationMarkedDirty())
            return false;

        if (tickBusy && timing.getValue() == Timing.SEQUENTIAL)
            return false;

        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() < pauseHP.getValue())
            return false;

        if (!offhand && autoGapple.getValue() && mc.options.useKey.isPressed() && mc.player.getMainHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE)
            return false;

        boolean silentWeakness = antiWeakness.getValue() == Switch.SILENT || antiWeakness.getValue() == Switch.INVENTORY;

        boolean silent = autoSwitch.getValue() == Switch.SILENT || autoSwitch.getValue() == Switch.INVENTORY;

        return !switchPause.getValue() || switchTimer.passedMs(switchDelay.getValue()) || silent || silentWeakness;
    }

    public boolean rotationMarkedDirty() {
        if (ModuleManager.surround.isEnabled() && !Surround.inactivityTimer.passedMs(500) && surround.getValue())
            return true;

        if (ModuleManager.autoTrap.isEnabled() && !AutoTrap.inactivityTimer.passedMs(500))
            return true;

        if (ModuleManager.blocker.isEnabled() && !Blocker.inactivityTimer.passedMs(500))
            return true;

        if (ModuleManager.holeFill.isEnabled() && !HoleFill.inactivityTimer.passedMs(500))
            return true;

        if (ModuleManager.aura.isEnabled() && aura.getValue())
            return true;

        return ModuleManager.pistonAura.isEnabled() && pistonAura.getValue();
    }

    public void attackCrystal(EndCrystalEntity crystal) {
        if (!canDoAC() || mc.player == null || mc.interactionManager == null) return;

        StatusEffectInstance weaknessEffect = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
        StatusEffectInstance strengthEffect = mc.player.getStatusEffect(StatusEffects.STRENGTH);

        if (crystalAge.getValue() != 0 && crystal.age < crystalAge.getValue())
            return;

        if (target == null)
            return;

        if (checkAttackedBefore(crystal.getId()))
            return;

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
        sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        tickBusy = true;
        breakTimer.reset();

        if (remove.getValue() != Remove.OFF)
            deadCrystals.put(crystal, System.currentTimeMillis());

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

    private boolean checkAttackedBefore(int id) {
        if (attackedCrystals.containsKey(id)) {
            if (attackedCrystals.get(id) != null) {
                int attacks = attackedCrystals.get(id);
                if (attacks >= limitAttacks.getValue()) {
                    return true;
                }
                attackedCrystals.remove(id);
                attackedCrystals.put(id, attacks + 1);
            } else {
                attackedCrystals.put(id, 1);
            }
        } else {
            attackedCrystals.put(id, 1);
        }
        return false;
    }

    private int switchTo(SearchInvResult result, SearchInvResult resultInv, @NotNull Setting<Switch> antiWeakness) {
        int prevSlot = mc.player.getInventory().selectedSlot;

        if (antiWeakness.getValue() != Switch.INVENTORY) {
            result.switchTo();
        } else if (resultInv.found()) {
            prevSlot = resultInv.slot();
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, prevSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        }

        return prevSlot;
    }

    public void placeCrystal(BlockHitResult bhr) {
        if (!canDoAC() || mc.player == null) return;
        int prevSlot = -1;

        SearchInvResult crystalResult = InventoryUtility.findItemInHotBar(Items.END_CRYSTAL);
        SearchInvResult crystalResultInv = InventoryUtility.findItemInInventory(Items.END_CRYSTAL);

        boolean offhand = mc.player.getOffHandStack().getItem() instanceof EndCrystalItem;
        boolean holdingCrystal = mc.player.getMainHandStack().getItem() instanceof EndCrystalItem || offhand;

        if (rotate.getValue() && !rotated)
            return;

        Box posBB = new Box(bhr.getBlockPos().up());

        if (!ccPlace.getValue())
            posBB = posBB.expand(0, 1f, 0);

        if (checkOtherEntities(posBB))
            return;

        if (autoSwitch.getValue() != Switch.NONE && !holdingCrystal)
            prevSlot = switchTo(crystalResult, crystalResultInv, autoSwitch);

        if (!(mc.player.getMainHandStack().getItem() instanceof EndCrystalItem || offhand))
            return;

        sendPacket(new PlayerInteractBlockC2SPacket(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, bhr, PlayerUtility.getWorldActionId(mc.world)));
        mc.player.swingHand(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND);
        placeTimer.reset();

        if (!bhr.getBlockPos().equals(renderPos)) {
            renderMultiplier = System.currentTimeMillis();
            prevRenderPos = renderPos;
            renderPos = bhr.getBlockPos();
        }

        placedCrystals.put(bhr.getBlockPos(), System.currentTimeMillis());
        renderPositions.put(bhr.getBlockPos(), System.currentTimeMillis());
        tickBusy = true;

        postPlaceSwitch(prevSlot);
    }

    private boolean checkOtherEntities(Box posBoundingBox) {
        Iterable<Entity> entities = Lists.newArrayList(mc.world.getEntities());

        for (Entity ent : entities) {
            if (ent == null) continue;
            if (ent.getBoundingBox().intersects(posBoundingBox)) {
                if (ent instanceof ExperienceOrbEntity)
                    continue;
                if (ent instanceof EndCrystalEntity && deadCrystals.containsKey(ent))
                    continue;

                return true;
            }
        }
        return false;
    }

    private void postPlaceSwitch(int slot) {
        if (autoSwitch.getValue() == Switch.SILENT && slot != -1) {
            mc.player.getInventory().selectedSlot = slot;
            sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        }

        if (autoSwitch.getValue() == Switch.INVENTORY && slot != -1) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        }
    }

    public synchronized void calcPosition() {
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
                } else {
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

        List<PlaceData> list = getPossibleBlocks(target).stream().filter(data -> isSafe(data.damage, data.selfDamage, data.overrideDamage)).toList();
        bestPosition = list.isEmpty() ? null : filterPositions(list);
    }

    private @NotNull List<PlaceData> getPossibleBlocks(PlayerEntity target) {
        List<PlaceData> blocks = new ArrayList<>();
        BlockPos playerPos = BlockPos.ofFloored(mc.player.getPos());
        for (int x = (int) Math.floor(playerPos.getX() - placeRange.getValue()); x <= Math.ceil(playerPos.getX() + placeRange.getValue()); x++) {
            for (int y = (int) Math.floor(playerPos.getY() - placeRange.getValue()); y <= Math.ceil(playerPos.getY() + placeRange.getValue()); y++) {
                for (int z = (int) Math.floor(playerPos.getZ() - placeRange.getValue()); z <= Math.ceil(playerPos.getZ() + placeRange.getValue()); z++) {
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
        Iterable<Entity> entities = Lists.newArrayList(mc.world.getEntities());
        for (Entity ent : entities) {
            if (!(ent instanceof EndCrystalEntity))
                continue;

            if (deadCrystals.containsKey(ent))
                continue;

            if (PlayerUtility.squaredDistanceFromEyes(ent.getPos()) > explodeRange.getPow2Value())
                continue;

            if (!InteractionUtility.canSee(ent) && PlayerUtility.squaredDistanceFromEyes(ent.getPos()) > explodeWallRange.getPow2Value())
                continue;

            if (!ent.isAlive())
                continue;

            float damage = ExplosionUtility.getExplosionDamage2(ent.getPos(), target);
            float selfDamage = ExplosionUtility.getSelfExplosionDamage(ent.getPos(), selfPredictTicks.getValue());

            boolean overrideDamage = shouldOverrideDamage(damage, selfDamage);

            if (protectFriends.getValue()) {
                List<PlayerEntity> players = Lists.newArrayList(mc.world.getPlayers());
                for (PlayerEntity pl : players) {
                    if (!ThunderHack.friendManager.isFriend(pl)) continue;
                    float fdamage = ExplosionUtility.getExplosionDamage2(ent.getPos(), pl);
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
        if (target == null)
            bestCrystal = null;

        List<CrystalData> list = getPossibleCrystals(target).stream().filter(data -> isSafe(data.damage, data.selfDamage, data.overrideDamage)).toList();
        bestCrystal = list.isEmpty() ? null : filterCrystals(list);
    }

    public boolean isSafe(float damage, float selfDamage, boolean overrideDamage) {
        double safetyIndex = 1;
        if (selfDamage + 0.5 > mc.player.getHealth() + mc.player.getAbsorptionAmount() && !overrideDamage) {
            safetyIndex = -9999;
        } else if (safety.getValue() == Safety.STABLE) {
            double efficiency = damage - selfDamage;
            if (efficiency < 0 && Math.abs(efficiency) < 0.25)
                efficiency = 0;
            safetyIndex = efficiency;
        } else if (safety.getValue() == Safety.BALANCE) {
            double balance = damage * safetyBalance.getValue();
            safetyIndex = balance - selfDamage;
        }
        return safetyIndex >= 0;
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
        renderDamage = bestData.damage;
        renderSelfDamage = bestData.selfDamage;
        return bestData.bhr;
    }

    private boolean shouldOverride(float damage) {
        if (target == null) return false;

        boolean override = target.getHealth() + target.getAbsorptionAmount() <= facePlaceHp.getValue();

        if (armorBreaker.getValue())
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

    private @Nullable PlaceData getPlaceData(BlockPos bp, PlayerEntity target) {
        Block base = mc.world.getBlockState(bp).getBlock();
        boolean freeSpace = mc.world.isAir(bp.up());
        boolean legacyFreeSpace = mc.world.isAir(bp.up().up());

        if (base != Blocks.OBSIDIAN && base != Blocks.BEDROCK)
            return null;

        if (!(freeSpace && (!oldVer.getValue() || legacyFreeSpace)))
            return null;

        if (checkEntities(bp)) return null;

        Vec3d crystalVec = new Vec3d(0.5f + bp.getX(), 1f + bp.getY(), 0.5f + bp.getZ());

        float damage = target == null ? 10f : ExplosionUtility.getExplosionDamage2(crystalVec, target);
        float selfDamage = ExplosionUtility.getSelfExplosionDamage(crystalVec, selfPredictTicks.getValue());
        boolean overrideDamage = shouldOverrideDamage(damage, selfDamage);

        if (protectFriends.getValue()) {
            List<PlayerEntity> players = Lists.newArrayList(mc.world.getPlayers());
            for (PlayerEntity pl : players) {
                if (!ThunderHack.friendManager.isFriend(pl)) continue;
                float fdamage = ExplosionUtility.getExplosionDamage2(crystalVec, pl);
                if (fdamage > selfDamage) {
                    selfDamage = fdamage;
                }
            }
        }

        if (damage < 1.5f) return null;
        if (selfDamage > maxSelfDamage.getValue() && !overrideDamage) return null;

        BlockHitResult interactResult = null;

        switch (interact.getValue()) {
            case Default -> interactResult = getDefaultInteract(crystalVec, bp);
            case Strict -> interactResult = getStrictInteract(bp);
            case Legit -> interactResult = getLegitInteract(bp);
        }

        if (interactResult == null) return null;

        return new PlaceData(interactResult, damage, selfDamage, overrideDamage);
    }

    private boolean checkEntities(@NotNull BlockPos base) {
        Box posBoundingBox = new Box(base.up());

        if (!ccPlace.getValue())
            posBoundingBox = posBoundingBox.expand(0, 1f, 0);

        Iterable<Entity> entities = Lists.newArrayList(mc.world.getEntities());
        for (Entity ent : entities) {
            if (ent == null) continue;
            if (ent.getBoundingBox().intersects(posBoundingBox)) {
                if (ent instanceof ExperienceOrbEntity)
                    continue;
                if (ent instanceof EndCrystalEntity) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    public boolean shouldOverrideDamage(float damage, float selfDamage) {
        if (overrideSelfDamage.getValue() && target != null) {
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
        if (PlayerUtility.squaredDistanceFromEyes(crystalVector) > placeRange.getPow2Value())
            return null;

        BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), crystalVector, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));

        if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
            if (PlayerUtility.squaredDistanceFromEyes(crystalVector) > placeWallRange.getPow2Value())
                return null;

        return new BlockHitResult(crystalVector, mc.world.isInBuildLimit(bp.up()) ? Direction.UP : Direction.DOWN, bp, false);
    }

    public @Nullable BlockHitResult getStrictInteract(@NotNull BlockPos bp) {
        float bestDistance = 999f;
        Direction bestDirection = null;
        Vec3d bestVector = null;

        if (mc.player.getEyePos().getY() > bp.up().getY()) {
            bestDirection = Direction.UP;
            bestVector = new Vec3d(bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5);
        } else if (mc.player.getEyePos().getY() < bp.getY() && mc.world.isAir(bp.down())) {
            bestDirection = Direction.DOWN;
            bestVector = new Vec3d(bp.getX() + 0.5, bp.getY(), bp.getZ() + 0.5);
        } else {
            for (Direction dir : Direction.values()) {
                if(dir == Direction.UP || dir == Direction.DOWN)
                    continue;

                if(!mc.world.isAir(bp.offset(dir)))
                    continue;

                Vec3d directionVec = new Vec3d(bp.getX() + 0.5 + dir.getVector().getX() * 0.5, bp.getY() + 0.5 + dir.getVector().getY() * 0.5, bp.getZ() + 0.5 + dir.getVector().getZ() * 0.5);
                float distance = PlayerUtility.squaredDistanceFromEyes(directionVec);
                if (bestDistance > distance) {
                    bestDirection = dir;
                    bestVector = directionVec;
                    bestDistance = distance;
                }
            }
        }

        if (bestVector == null) return null;

        if (PlayerUtility.squaredDistanceFromEyes(bestVector) > placeRange.getPow2Value())
            return null;

        BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), bestVector, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));

        if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
            if (PlayerUtility.squaredDistanceFromEyes(bestVector) > placeWallRange.getPow2Value())
                return null;

        return new BlockHitResult(bestVector, bestDirection, bp, false);
    }

    public BlockHitResult getLegitInteract(BlockPos bp) {
        float bestDistance = 999f;
        BlockHitResult bestResult = null;
        for (float x = 0f; x <= 1f; x += 0.2f) {
            for (float y = 0f; y <= 1f; y += 0.2f) {
                for (float z = 0f; z <= 1f; z += 0.2f) {
                    Vec3d point = new Vec3d(bp.getX() + x, bp.getY() + y, bp.getZ() + z);
                    float distance = PlayerUtility.squaredDistanceFromEyes(point);

                    BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                    if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
                        if (distance > placeWallRange.getPow2Value())
                            continue;


                    BlockHitResult result = ExplosionUtility.rayCastBlock(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player), bp);
                    if (distance > placeRange.getPow2Value())
                        continue;

                    if (distance < bestDistance) {
                        if (result != null && result.getType() == HitResult.Type.BLOCK) {
                            bestResult = result;
                            bestDistance = distance;
                        }
                    }
                }
            }
        }
        return bestResult;
    }

    public void removeAttackedCrystals() {
        if (remove.getValue() == Remove.ON && !deadCrystals.isEmpty()) {
            Map<EndCrystalEntity, Long> cache = new HashMap<>(deadCrystals);
            cache.forEach((crystal, time) -> {
                if (System.currentTimeMillis() - time >= removeDelay.getValue()) {
                    crystal.kill();
                    crystal.setRemoved(Entity.RemovalReason.KILLED);
                    crystal.onRemoved();
                    deadCrystals.remove(crystal);
                }
            });
        }
    }

    private record PlaceData(BlockHitResult bhr, float damage, float selfDamage, boolean overrideDamage) {
    }

    private record CrystalData(EndCrystalEntity crystal, float damage, float selfDamage, boolean overrideDamage) {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTick(EventTick e) {
        if (multiThread.getValue())
            return;

        new Thread(() -> {
            calcPosition();
            getCrystalToExplode();
        }).start();
    }

    @EventHandler
    public void onCrystalSpawn(@NotNull EventEntitySpawn e) {
        if (e.getEntity() != null && e.getEntity() instanceof EndCrystalEntity && !placedCrystals.isEmpty()) {
            Map<BlockPos, Long> cachedList = new HashMap<>(placedCrystals);
            for (BlockPos bp : cachedList.keySet())
                if (e.getEntity().squaredDistanceTo(bp.toCenterPos().add(0, 0.5f, 0)) < 1) {
                    if ((!multiThread.getValue() || instantBreak.getValue()) && timing.getValue() == Timing.NORMAL && (breakDelay.getValue() == 0 || breakTimer.passedMs(breakDelay.getValue()))) {
                        new Thread(() -> {
                            getCrystalToExplode();
                            if (bestCrystal == e.getEntity())
                                attackCrystal((EndCrystalEntity) e.getEntity());

                            if (target != null && instantPlace.getValue()) {
                                getPossibleCrystals(target);
                                if (bestPosition != null) {
                                    placeCrystal(bestPosition);
                                }
                            }
                        }).start();
                    }
                    placedCrystals.remove(bp);
                }
        }
    }

    private void onSpawnPacket(@NotNull EntitySpawnS2CPacket spawn) {
        if (spawn.getEntityType() == EntityType.END_CRYSTAL) {
            if (!placedCrystals.isEmpty()) {
                Map<BlockPos, Long> cachedList = new HashMap<>(placedCrystals);
                for (BlockPos bp : cachedList.keySet())
                    if (spawn.getX() == bp.getX() + 0.5 && spawn.getZ() == bp.getZ() + 0.5 && spawn.getY() == bp.getY() + 1f) {
                        placedCrystals.remove(bp);
                        if (!multiThread.getValue()) {
                            new Thread(() -> {
                                EndCrystalEntity fakeCrystal = new EndCrystalEntity(mc.world, spawn.getX(), spawn.getY(), spawn.getZ());
                                fakeCrystal.setId(spawn.getId());
                                getCrystalToExplode();

                                if (bestCrystal == fakeCrystal)
                                    attackCrystal(fakeCrystal);

                                if (target != null && instantPlace.getValue()) {
                                    getPossibleCrystals(target);
                                    if (bestPosition != null) {
                                        placeCrystal(bestPosition);
                                    }
                                }
                            }).start();
                        }
                    }
            }
        }
    }

    @EventHandler
    public void onCrystalRemove(@NotNull EventEntityRemoved e) {
        if (e.entity instanceof EndCrystalEntity) {
            HashMap<Integer, Integer> cache = new HashMap<>(attackedCrystals);
            if (cache.containsKey(e.entity.getId())) {
                attackedCrystals.remove(e.entity.getId());

                if (!multiThread.getValue() && timing.getValue() == Timing.NORMAL && (placeDelay.getValue() == 0 || placeTimer.passedMs(placeDelay.getValue())))
                    if (bestPosition != null) {
                        placeCrystal(bestPosition);
                    }
            }
        }
    }

    private class PlaceThread extends Thread {
        @Override
        public void run() {
            while (ModuleManager.autoCrystal.isEnabled()) {
                while (ThunderHack.asyncManager.ticking.get() || !placeTimer.passedMs(placeDelay.getValue())) {
                }

                calcPosition();

                if (bestPosition != null)
                    placeCrystal(bestPosition);

                if (stopThreads.get())
                    placeThread.interrupt();
            }
        }
    }

    private class BreakThread extends Thread {
        @Override
        public void run() {
            while (ModuleManager.autoCrystal.isEnabled()) {
                while (ThunderHack.asyncManager.ticking.get()) {
                }
                while (!breakTimer.passedMs(breakDelay.getValue())) {
                }

                getCrystalToExplode();
                if (bestCrystal != null)
                    attackCrystal(bestCrystal);

                if (stopThreads.get())
                    breakThread.interrupt();
            }
        }
    }

    public static AutoCrystal getInstance() {
        return instance;
    }

    private enum Pages {
        Place, Break, Pause, Render, Damages, Main, Switch, Remove, MultiThread
    }

    private enum Switch {
        NONE, NORMAL, SILENT, INVENTORY
    }

    private enum Timing {
        NORMAL, SEQUENTIAL
    }

    private enum Interact {
        Default, Strict, Legit
    }

    private enum TargetLogic {
        Distance, HP, FOV
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
        PlaceOn, Smart
    }

    public enum Remove {
        OFF, Fake, ON
    }
}