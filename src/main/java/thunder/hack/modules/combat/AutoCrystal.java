package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
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
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import thunder.hack.Thunderhack;
import thunder.hack.core.ModuleManager;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
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

public class AutoCrystal extends Module {
    public AutoCrystal() {
        super("AutoCrystal", Category.COMBAT);
    }

    // я ебал (не спастил btw)

    /*   MAIN   */
    private static final Setting<Pages> page = new Setting<>("Page", Pages.Main);
    private final Setting<Timing> timing = new Setting<>("Timing", Timing.NORMAL, v -> page.getValue() == Pages.Main);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", true, v -> page.getValue() == Pages.Main);
    private final Setting<Boolean> yawStep = new Setting<>("YawStep", false, v -> rotate.getValue() && page.getValue() == Pages.Main);
    public Setting<Float> yawAngle = new Setting<>("YawAngle", 180.0f, 1.0f, 180.0f, v -> rotate.getValue() && yawStep.getValue() && page.getValue() == Pages.Main);
    private final Setting<TargetLogic> targetLogic = new Setting<>("TargetLogic", TargetLogic.Distance, v -> page.getValue() == Pages.Main);
    public Setting<Float> targetRange = new Setting<>("TargetRange", 10.0f, 1.0f, 15f, v -> page.getValue() == Pages.Main);

    /*   PLACE   */
    private final Setting<Interact> interact = new Setting<>("Interact", Interact.Default, v -> page.getValue() == Pages.Place);
    public Setting<Boolean> oldVer = new Setting<>("1.12", false, v -> page.getValue() == Pages.Place);
    public Setting<Boolean> ccPlace = new Setting<>("CC", true, v -> page.getValue() == Pages.Place);
    public Setting<Integer> placeDelay = new Setting<>("PlaceDelay", 0, 0, 1000, v -> page.getValue() == Pages.Place);
    public Setting<Float> placeRange = new Setting<>("PlaceRange", 5f, 1.0f, 6f, v -> page.getValue() == Pages.Place);
    public Setting<Float> placeWallRange = new Setting<>("PlaceWallRange", 3.5f, 1.0f, 6f, v -> page.getValue() == Pages.Place);
    public static Setting<Integer> predictTicks = new Setting<>("PredictTicks", 3, 0, 10, v -> page.getValue() == Pages.Place);

    /*   BREAK   */
    public Setting<Integer> breakDelay = new Setting<>("BreakDelay", 0, 0, 1000, v -> page.getValue() == Pages.Break);
    public Setting<Float> explodeRange = new Setting<>("BreakRange", 5.0f, 1.0f, 6f, v -> page.getValue() == Pages.Break);
    public Setting<Float> explodeWallRange = new Setting<>("BreakWallRange", 3.5f, 1.0f, 6f, v -> page.getValue() == Pages.Break);
    public Setting<Integer> crystalAge = new Setting<>("CrystalAge", 0, 0, 20, v -> page.getValue() == Pages.Break);
    public Setting<Integer> limitAttacks = new Setting<>("LimitAttacks", 2, 0, 10, v -> page.getValue() == Pages.Break);

    /*   PAUSE   */
    public Setting<Boolean> mining = new Setting<>("Mining", true, v -> page.getValue() == Pages.Pause);
    public Setting<Boolean> eating = new Setting<>("Eating", true, v -> page.getValue() == Pages.Pause);
    public Setting<Boolean> aura = new Setting<>("Aura", false, v -> page.getValue() == Pages.Pause);
    public Setting<Boolean> pistonAura = new Setting<>("PistonAura", true, v -> page.getValue() == Pages.Pause);
    public Setting<Boolean> surround = new Setting<>("Surround", true, v -> page.getValue() == Pages.Pause);
    public Setting<Float> pauseHP = new Setting<>("HP", 8.0f, 2.0f, 10f, v -> page.getValue() == Pages.Pause);
    public Setting<Boolean> switchPause = new Setting<>("SwitchPause", true, v -> page.getValue() == Pages.Pause);
    public Setting<Integer> switchDelay = new Setting<>("SwitchDelay", 100, 0, 1000, v -> page.getValue() == Pages.Pause && switchPause.getValue());

    /*   DAMAGES   */
    public Setting<Float> minDamage = new Setting<>("MinDamage", 6.0f, 2.0f, 20f, v -> page.getValue() == Pages.Damages);
    public Setting<Float> maxSelfDamage = new Setting<>("MaxSelfDamage", 10.0f, 2.0f, 20f, v -> page.getValue() == Pages.Damages);
    private final Setting<Safety> safety = new Setting<>("Safety", Safety.NONE, v -> page.getValue() == Pages.Damages);
    public Setting<Float> safetyBalance = new Setting<>("SafetyBalance", 1.1f, 0.1f, 3f, v -> page.getValue() == Pages.Damages && safety.getValue() == Safety.BALANCE);
    public Setting<Boolean> protectFriends = new Setting<>("ProtectFriends", true, v -> page.getValue() == Pages.Damages);
    public Setting<Boolean> overrideSelfDamage = new Setting<>("OverrideSelfDamage", true, v -> page.getValue() == Pages.Damages);
    public Setting<Float> lethalMultiplier = new Setting<>("LethalMultiplier", 1.0f, 0.0f, 5f, v -> page.getValue() == Pages.Damages);
    public Setting<Boolean> armorBreaker = new Setting<>("ArmorBreaker", true, v -> page.getValue() == Pages.Damages);
    public Setting<Float> armorScale = new Setting<>("ArmorScale", 5.0f, 0.0f, 40f, v -> armorBreaker.getValue() && page.getValue() == Pages.Damages);
    public Setting<Float> facePlaceHp = new Setting<>("FacePlaceHp", 5.0f, 2.0f, 20f, v -> page.getValue() == Pages.Damages);
    private final Setting<Bind> facePlaceButton = new Setting<>("FacePlaceButton", new Bind(GLFW.GLFW_KEY_LEFT_SHIFT, false, false), v -> page.getValue() == Pages.Damages);

    /*   SWITCH   */
    public Setting<Boolean> autoGapple = new Setting<>("AutoGapple", true, v -> page.getValue() == Pages.Switch);
    private final Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL, v -> page.getValue() == Pages.Switch);
    private final Setting<Switch> antiWeakness = new Setting<>("AntiWeakness", Switch.SILENT, v -> page.getValue() == Pages.Switch);

    /*   RENDER   */
    public Setting<Boolean> render = new Setting<>("Render", true, v -> page.getValue() == Pages.Render);
    private final Setting<Render> renderMode = new Setting<>("RenderMode", Render.Fade, v -> page.getValue() == Pages.Render);
    public Setting<Boolean> rselfDamage = new Setting<>("SelfDamage", true, v -> page.getValue() == Pages.Render);
    public Setting<ColorSetting> fillColor = new Setting<>("Block Fill Color", new ColorSetting(HudEditor.getColor(0)), v -> page.getValue() == Pages.Render);
    public Setting<ColorSetting> lineColor = new Setting<>("Block Line Color", new ColorSetting(HudEditor.getColor(0)), v -> page.getValue() == Pages.Render);
    public Setting<Integer> lineWidth = new Setting<>("Block Line Width", 2, 1, 10, v -> page.getValue() == Pages.Render);
    public Setting<Integer> slideDelay = new Setting<>("Slide Delay", 200, 1, 1000, v -> page.getValue() == Pages.Render);
    public Setting<ColorSetting> textColor = new Setting<>("Text Color", new ColorSetting(Color.WHITE), v -> page.getValue() == Pages.Render);

    private enum Pages {Place, Break, Pause, Render, Damages, Main, Switch}

    private enum Switch {NONE, NORMAL, SILENT, INVENTORY}

    private enum Timing {NORMAL, SEQUENTIAL}

    private enum Interact {Default, Strict, BrutForce}

    private enum TargetLogic {Distance, HP, FOV}

    public enum Safety {BALANCE, STABLE, NONE}

    public enum Render {Fade, Slide}

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


    private float renderDamage = 0;
    private float renderSelfDamage = 0;

    private int prevCrystalsAmmount, crysSpeed, invTimer;

    private boolean rotated, tickBusy;

    private BlockPos renderPos, prevRenderPos;
    private long renderMultiplier;
    private final Map<BlockPos, Long> renderPositions = new HashMap<>();

    @Override
    public void onEnable() {
        rotated = false;
        renderDamage = 0;
        renderSelfDamage = 0;
        attackedCrystals.clear();
        placedCrystals.clear();
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
    }

    @Override
    public void onDisable() {
        target = null;
    }

    @EventHandler
    public void onSync(EventSync e) {
        switch (targetLogic.getValue()) {
            case HP -> target = Thunderhack.combatManager.getTargetByHP(targetRange.getValue());
            case Distance -> target = Thunderhack.combatManager.getNearestTarget(targetRange.getValue());
            case FOV -> Thunderhack.combatManager.getTargetByFOV(targetRange.getValue());
        }

        if (target != null && (target.isDead() || target.getHealth() < 0)) {
            target = null;
            return;
        }

        new Thread(this::calcPosition).start();

        if (renderPositions.isEmpty()) attackedCrystals.clear();

        if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL && autoGapple.getValue()
                && mc.options.useKey.isPressed() && mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
            SearchInvResult result = InventoryUtility.findItemInHotBar(Items.ENCHANTED_GOLDEN_APPLE);
            result.switchIfFound();
        }

        if (invTimer++ >= 20) {
            crysSpeed = prevCrystalsAmmount - InventoryUtility.getItemCount(Items.END_CRYSTAL);
            prevCrystalsAmmount = InventoryUtility.getItemCount(Items.END_CRYSTAL);
            invTimer = 0;
        }

        HashMap<Integer, Integer> cache = new HashMap<>(attackedCrystals);
        cache.forEach((crystal, attacks) -> {
            if (mc.world.getEntityById(crystal) == null)
                attackedCrystals.remove(crystal);
        });

        if (rotate.getValue()) rotateMethod();
    }

    public void rotateMethod() {
        if (bestPosition != null) {
            float[] angle = InteractionUtility.calculateAngle(bestPosition.getPos());
            if (yawStep.getValue()) {
                float yaw_delta = MathHelper.wrapDegrees(angle[0] - ((IClientPlayerEntity) ((mc.player))).getLastYaw());
                if (Math.abs(yaw_delta) > yawAngle.getValue()) {
                    angle[0] = ((IClientPlayerEntity) ((mc.player))).getLastYaw() + (yaw_delta * (yawAngle.getValue() / Math.abs(yaw_delta)));
                    rotated = false;
                } else rotated = true;
            } else rotated = true;
            double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
            mc.player.setYaw((float) (angle[0] - (angle[0] - ((IClientPlayerEntity) ((mc.player))).getLastYaw()) % gcdFix));
            mc.player.setPitch((float) (angle[1] - (angle[1] - ((IClientPlayerEntity) ((mc.player))).getLastPitch()) % gcdFix));
        }
    }

    @Override
    public String getDisplayInfo() {
        String info = crysSpeed + " c/s";
        if (bestPosition != null)
            info = crysSpeed + " c/s | " + bestPosition.getSide().toString().toUpperCase();
        return info;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (target == null) return;
        if (e.getPacket() instanceof PlaySoundS2CPacket sound && sound.getCategory().equals(SoundCategory.BLOCKS) && sound.getSound().value().equals(SoundEvents.ENTITY_GENERIC_EXPLODE)) {
            for (Entity crystal : Thunderhack.asyncManager.getAsyncEntities()) {
                if (!(crystal instanceof EndCrystalEntity))
                    continue;
                if (crystal == null || !crystal.isAlive() || crystal.squaredDistanceTo(sound.getX() + 0.5, sound.getY() + 0.5, sound.getZ() + 0.5) > 121)
                    continue;
                onDestroyPacket((EndCrystalEntity) crystal);
            }
        }

        if (e.getPacket() instanceof ExplosionS2CPacket expl) {
            for (Entity crystal : Thunderhack.asyncManager.getAsyncEntities()) {
                if (crystal == null || !crystal.isAlive() || !(crystal instanceof EndCrystalEntity))
                    continue;
                if (crystal.squaredDistanceTo(expl.getX() + 0.5, expl.getY() + 0.5, expl.getZ() + 0.5) > expl.getRadius() * expl.getRadius())
                    continue;
                onDestroyPacket((EndCrystalEntity) crystal);
            }
        }

        if (e.getPacket() instanceof EntitiesDestroyS2CPacket destroyEntities) {
            for (int entityId : destroyEntities.getEntityIds()) {
                Entity crystal = mc.world.getEntityById(entityId);
                if (crystal instanceof EndCrystalEntity) {
                    onDestroyPacket((EndCrystalEntity) crystal);
                }
            }
        }

        if (e.getPacket() instanceof EntitySpawnS2CPacket spawn) {
            onSpawnPacket(spawn);
        }
    }

    private void onDestroyPacket(EndCrystalEntity ent) {
        HashMap<Integer, Integer> cache = new HashMap<>(attackedCrystals);
        if (cache.containsKey(ent.getId())) {
            attackedCrystals.remove(ent.getId());
            if (timing.getValue() == Timing.NORMAL && (placeDelay.getValue() == 0 || placeTimer.passedMs(placeDelay.getValue())))
                if (bestPosition != null)
                    placeCrystal(bestPosition);
        }
    }

    private void onSpawnPacket(EntitySpawnS2CPacket spawn) {
        if (spawn.getEntityType() == EntityType.END_CRYSTAL) {
            if (!placedCrystals.isEmpty()) {
                Map<BlockPos, Long> cachedList = new HashMap<>(placedCrystals);
                for (BlockPos bp : cachedList.keySet())
                    if (spawn.getX() == bp.getX() + 0.5 && spawn.getZ() == bp.getZ() + 0.5 && spawn.getY() == bp.getY() + 1f) {
                        if (timing.getValue() == Timing.NORMAL && (breakDelay.getValue() == 0 || breakTimer.passedMs(breakDelay.getValue()))) {
                            EndCrystalEntity fakeCrystal = new EndCrystalEntity(mc.world, spawn.getX(), spawn.getY(), spawn.getZ());
                            fakeCrystal.setId(spawn.getId());
                            sendMessage("spawn");
                            attackCrystal(fakeCrystal);
                        }
                        placedCrystals.remove(bp);
                    }
            }
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (e.getPacket() instanceof UpdateSelectedSlotC2SPacket) switchTimer.reset();
    }


    @EventHandler
    public void onPostSync(EventPostSync e) {
        tickBusy = false;
        handleAutoCrystal();
    }

    public void handleAutoCrystal() {
        if (bestPosition != null)
            if (placeTimer.passedMs(placeDelay.getValue()) || placeDelay.getValue() == 0)
                if (placeCrystal(bestPosition))
                    placeTimer.reset();

        if (bestCrystal != null)
            if (breakTimer.passedMs(breakDelay.getValue()) || breakDelay.getValue() == 0)
                attackCrystal(bestCrystal);
    }

    public void onRender3D(MatrixStack stack) {
        if (render.getValue()) {
            Map<BlockPos, Long> cache = new HashMap<>(renderPositions);
            cache.forEach((pos, time) -> {
                if (System.currentTimeMillis() - time > 500) {
                    renderPositions.remove(pos);
                }
            });

            String dmg = MathUtility.round2(renderDamage) + (rselfDamage.getValue() ? " / " + MathUtility.round2(renderSelfDamage) : "");

            if (renderMode.getValue() == Render.Fade) {
                cache.forEach((pos, time) -> {
                    if (System.currentTimeMillis() - time < 500) {
                        int alpha = (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)));
                        Render3DEngine.drawFilledBox(stack, new Box(pos), Render2DEngine.injectAlpha(fillColor.getValue().getColorObject(), alpha));
                        Render3DEngine.drawBoxOutline(new Box(pos), Render2DEngine.injectAlpha(lineColor.getValue().getColorObject(), alpha), lineWidth.getValue());
                        Render3DEngine.drawTextIn3D(dmg, pos.toCenterPos(), 0, 0.1, 0, Render2DEngine.injectAlpha(textColor.getValue().getColorObject(), alpha));
                    }
                });
            } else if (renderPos != null) {
                if (prevRenderPos == null) prevRenderPos = renderPos;
                if (renderPositions.isEmpty()) return;
                float mult = MathUtility.clamp((System.currentTimeMillis() - renderMultiplier) / (float) slideDelay.getValue(), 0f, 1f);
                Box interpolatedBox = Render3DEngine.interpolateBox(new Box(prevRenderPos), new Box(renderPos), mult);
                Render3DEngine.drawFilledBox(stack, interpolatedBox, fillColor.getValue().getColorObject());
                Render3DEngine.drawBoxOutline(interpolatedBox, lineColor.getValue().getColorObject(), lineWidth.getValue());
                Render3DEngine.drawTextIn3D(dmg, interpolatedBox.getCenter(), 0, 0.1, 0, textColor.getValue().getColorObject());
            }
        }
        if(timing.getValue() == Timing.NORMAL) handleAutoCrystal();
    }

    public boolean canDoAC() {
        boolean offhand = mc.player.getOffHandStack().getItem() instanceof EndCrystalItem;
        if (mc.interactionManager.isBreakingBlock() && !offhand && mining.getValue())
            return false;

        if (tickBusy && timing.getValue() == Timing.SEQUENTIAL)
            return false;

        if (mc.player.isUsingItem() && eating.getValue() && !offhand)
            return false;

        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() < pauseHP.getValue())
            return false;

        if (ModuleManager.surround.isEnabled() && !Surround.inactivityTimer.passedMs(500) && surround.getValue())
            return false;

        if (ModuleManager.autoTrap.isEnabled() && !AutoTrap.inactivityTimer.passedMs(500))
            return false;

        if (ModuleManager.blocker.isEnabled() && !Blocker.inactivityTimer.passedMs(500))
            return false;

        if (ModuleManager.holeFill.isEnabled() && !HoleFill.inactivityTimer.passedMs(500))
            return false;

        if (ModuleManager.aura.isEnabled() && aura.getValue())
            return false;

        if (ModuleManager.pistonAura.isEnabled() && pistonAura.getValue())
            return false;

        if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL && autoGapple.getValue() && mc.options.useKey.isPressed() && mc.player.getMainHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
            return false;
        }

        return !switchPause.getValue() || switchTimer.passedMs(switchDelay.getValue()) || autoSwitch.getValue() == Switch.SILENT || antiWeakness.getValue() == Switch.SILENT || autoSwitch.getValue() == Switch.INVENTORY || antiWeakness.getValue() == Switch.INVENTORY;
    }

    public void attackCrystal(EndCrystalEntity crystal) {
        if (!canDoAC()) return;

        StatusEffectInstance weaknessEffect = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
        StatusEffectInstance strengthEffect = mc.player.getStatusEffect(StatusEffects.STRENGTH);

        if (crystalAge.getValue() != 0 && crystal.age < crystalAge.getValue())
            return;

        if (target == null) {
            return;
        }

        if (attackedCrystals.containsKey(crystal.getId())) {
            if (attackedCrystals.get(crystal.getId()) != null) {
                int attacks = attackedCrystals.get(crystal.getId());
                if (attacks >= limitAttacks.getValue()) {
                    return;
                }
                attackedCrystals.remove(crystal.getId());
                attackedCrystals.put(crystal.getId(), attacks + 1);
            } else {
                attackedCrystals.put(crystal.getId(), 1);
            }
        } else {
            attackedCrystals.put(crystal.getId(), 1);
        }

        int prevSlot = -1;
        SearchInvResult swordResult = InventoryUtility.getSwordHotbar();
        SearchInvResult swordResultInv = InventoryUtility.getSword();
        if (antiWeakness.getValue() != Switch.NONE) {
            if (weaknessEffect != null && (strengthEffect == null || strengthEffect.getAmplifier() < weaknessEffect.getAmplifier())) {
                if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem)) {
                    prevSlot = mc.player.getInventory().selectedSlot;
                    if (antiWeakness.getValue() != Switch.INVENTORY) {
                        swordResult.switchIfFound();
                    } else if (swordResultInv.found()) {
                        prevSlot = swordResultInv.slot();
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, prevSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                    }
                }
            }
        }

        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        tickBusy = true;
        breakTimer.reset();

        if (prevSlot != -1) {
            if (antiWeakness.getValue() == Switch.SILENT) {
                mc.player.getInventory().selectedSlot = prevSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            }
            if (antiWeakness.getValue() == Switch.INVENTORY) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, prevSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            }
        }
    }

    public boolean placeCrystal(BlockHitResult bhr) {
        if (!canDoAC()) return false;
        int prevSlot = -1;
        SearchInvResult crystalResult = InventoryUtility.findItemInHotBar(Items.END_CRYSTAL);
        SearchInvResult crystalResultInv = InventoryUtility.findItemInInventory(Items.END_CRYSTAL);

        if (rotate.getValue() && !rotated)
            return false;

        Box posBoundingBox = new Box(bhr.getBlockPos().up());

        if (!ccPlace.getValue())
            posBoundingBox = posBoundingBox.expand(0, 1f, 0);

        for (Entity ent : Thunderhack.asyncManager.getAsyncEntities()) {
            if (ent == null) continue;
            if (ent.getBoundingBox().intersects(posBoundingBox)) {
                if (ent instanceof ExperienceOrbEntity)
                    continue;
                if (ent instanceof EndCrystalEntity && attackedCrystals.containsKey(ent.getId()))
                    continue;

                return false;
            }
        }

        if (autoSwitch.getValue() != Switch.NONE) {
            if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
                prevSlot = mc.player.getInventory().selectedSlot;
                if (autoSwitch.getValue() != Switch.INVENTORY) {
                    crystalResult.switchIfFound();
                } else if (crystalResultInv.found()) {
                    prevSlot = crystalResultInv.slot();
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, prevSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                }
            }
        }

        if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL)
            return false;

        boolean offhand = mc.player.getOffHandStack().getItem() instanceof EndCrystalItem;

        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, bhr, PlayerUtility.getWorldActionId(mc.world)));
        mc.player.swingHand(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND);

        if (!bhr.getBlockPos().equals(renderPos)) {
            renderMultiplier = System.currentTimeMillis();
            prevRenderPos = renderPos;
            renderPos = bhr.getBlockPos();
        }

        placedCrystals.put(bhr.getBlockPos(), System.currentTimeMillis());
        renderPositions.put(bhr.getBlockPos(), System.currentTimeMillis());
        tickBusy = true;

        if (autoSwitch.getValue() == Switch.SILENT && prevSlot != -1) {
            mc.player.getInventory().selectedSlot = prevSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
        }

        if (autoSwitch.getValue() == Switch.INVENTORY && prevSlot != -1) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, prevSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        }
        return true;
    }

    public void calcPosition() {
        if (ModuleManager.speedMine.isWorth()) {
            PlaceData autoMineData = getPlaceData(SpeedMine.minePosition, null);
            if (autoMineData != null) {
                bestPosition = autoMineData.bhr;
                return;
            }
        }

        if (target == null) {
            bestPosition = null;
            renderPos = null;
            prevRenderPos = null;
            return;
        }
        List<PlaceData> rawList = getPossibleBlocks(target);
        List<PlaceData> clearedList = new ArrayList<>();
        bestCrystal = getCrystalToExplode(target);

        for (PlaceData data : rawList) {
            double safetyIndex = 1;
            if (data.selfDamage + 0.5 > mc.player.getHealth() + mc.player.getAbsorptionAmount() && !data.overrideDamage) {
                safetyIndex = -9999;
            } else if (safety.getValue() == Safety.STABLE) {
                double efficiency = data.damage - data.selfDamage;
                if (efficiency < 0 && Math.abs(efficiency) < 0.25)
                    efficiency = 0;
                safetyIndex = efficiency;
            } else if (safety.getValue() == Safety.BALANCE) {
                double balance = data.damage * safetyBalance.getValue();
                safetyIndex = balance - data.selfDamage;
            }
            if (safetyIndex < 0) continue;
            clearedList.add(data);
        }

        bestPosition = clearedList.isEmpty() ? null : filterPositions(clearedList);
    }

    public List<PlaceData> getPossibleBlocks(PlayerEntity target) {
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

    public List<CrystalData> getPossibleCrystals(PlayerEntity target) {
        List<CrystalData> crystals = new ArrayList<>();
        for (Entity ent : Thunderhack.asyncManager.getAsyncEntities()) {
            if (!(ent instanceof EndCrystalEntity))
                continue;

            if (squaredDistanceFromEyes(ent.getPos()) > explodeRange.getPow2Value())
                continue;

            if (!InteractionUtility.canSee(ent) && squaredDistanceFromEyes(ent.getPos()) > explodeWallRange.getPow2Value())
                continue;

            if (!ent.isAlive())
                continue;

            float damage = ExplosionUtility.getExplosionDamage2(ent.getPos(), target);
            float selfDamage = ExplosionUtility.getSelfExplosionDamage(ent.getPos());

            boolean overrideDamage = false;


            if (overrideSelfDamage.getValue()) {
                boolean canPop = target != null
                        && damage + 0.5 > target.getHealth() + target.getAbsorptionAmount()
                        && (target.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING
                        || target.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING);

                boolean canKill = target != null
                        && damage + 0.5 > target.getHealth() + target.getAbsorptionAmount()
                        && target.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING
                        && target.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING;

                boolean canPopSelf = selfDamage + 0.5 > mc.player.getHealth() + mc.player.getAbsorptionAmount()
                        && (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING
                        || mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING);

                boolean canKillSelf = selfDamage + 0.5 > mc.player.getHealth() + mc.player.getAbsorptionAmount()
                        && mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING
                        && mc.player.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING;

                if (canPopSelf && canKill && !canKillSelf)
                    overrideDamage = true;


                if (selfDamage > maxSelfDamage.getValue() && canPop && !canKillSelf && !canPopSelf)
                    overrideDamage = true;
            }

            if (protectFriends.getValue()) {
                for (PlayerEntity pl : Thunderhack.asyncManager.getAsyncPlayers()) {
                    if (!Thunderhack.friendManager.isFriend(pl)) continue;
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

    public EndCrystalEntity getCrystalToExplode(PlayerEntity target) {
        List<CrystalData> rawList = getPossibleCrystals(target);
        List<CrystalData> clearedList = new ArrayList<>();

        for (CrystalData data : rawList) {
            double safetyIndex = 1;
            if (data.selfDamage + 0.5 > mc.player.getHealth() + mc.player.getAbsorptionAmount() && !data.overrideDamage) {
                safetyIndex = -9999;
            } else if (safety.getValue() == Safety.STABLE) {
                double efficiency = data.damage - data.selfDamage;
                if (efficiency < 0 && Math.abs(efficiency) < 0.25)
                    efficiency = 0;
                safetyIndex = efficiency;
            } else if (safety.getValue() == Safety.BALANCE) {
                double balance = data.damage * safetyBalance.getValue();
                safetyIndex = balance - data.selfDamage;
            }
            if (safetyIndex < 0) continue;
            clearedList.add(data);
        }

        return clearedList.isEmpty() ? null : filterCrystals(clearedList);
    }

    public BlockHitResult filterPositions(@NotNull List<PlaceData> clearedList) {
        PlaceData bestData = null;
        float bestDmg = 0f;
        for (PlaceData data : clearedList) {
            boolean override = target.getHealth() + target.getAbsorptionAmount() <= facePlaceHp.getValue();

            if (armorBreaker.getValue())
                for (ItemStack armor : target.getArmorItems())
                    if (armor != null && !armor.getItem().equals(Items.AIR) && ((armor.getMaxDamage() - armor.getDamage()) / (float) armor.getMaxDamage()) * 100 < armorScale.getValue()) {
                        override = true;
                        break;
                    }

            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), facePlaceButton.getValue().getKey()))
                override = true;

            if ((target.getHealth() + target.getAbsorptionAmount()) - (data.damage * lethalMultiplier.getValue()) < 0.5)
                override = true;

            if ((override || data.damage > minDamage.getValue()) && bestDmg < data.damage) {
                renderDamage = data.damage;
                renderSelfDamage = data.selfDamage;
                bestData = data;
                bestDmg = data.damage;
            }
        }

        if (bestData == null) return null;

        return bestData.bhr;
    }

    public EndCrystalEntity filterCrystals(@NotNull List<CrystalData> clearedList) {
        CrystalData bestData = null;
        float bestDmg = 0f;
        for (CrystalData data : clearedList) {
            boolean override = target.getHealth() + target.getAbsorptionAmount() <= facePlaceHp.getValue();

            if (armorBreaker.getValue())
                for (ItemStack armor : target.getArmorItems())
                    if (armor != null && !armor.getItem().equals(Items.AIR) && ((armor.getMaxDamage() - armor.getDamage()) / (float) armor.getMaxDamage()) * 100 < armorScale.getValue()) {
                        override = true;
                        break;
                    }

            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), facePlaceButton.getValue().getKey()))
                override = true;

            if ((target.getHealth() + target.getAbsorptionAmount()) - (data.damage * lethalMultiplier.getValue()) < 0.5)
                override = true;

            if ((override || data.damage > minDamage.getValue()) && bestDmg < data.damage) {
                renderDamage = data.damage;
                renderSelfDamage = data.selfDamage;
                bestData = data;
                bestDmg = data.damage;
            }
        }

        if (bestData == null) return null;
        return bestData.crystal;
    }

    public PlaceData getPlaceData(BlockPos bp, PlayerEntity target) {
        Block block = mc.world.getBlockState(bp).getBlock();
        Block freeSpace = mc.world.getBlockState(bp.up()).getBlock();
        Block legacyFreeSpace = mc.world.getBlockState(bp.up().up()).getBlock();

        if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK)
            return null;

        if (!(freeSpace == Blocks.AIR && (!oldVer.getValue() || legacyFreeSpace == Blocks.AIR)))
            return null;

        Box posBoundingBox = new Box(bp.up());

        if (!ccPlace.getValue())
            posBoundingBox = posBoundingBox.expand(0, 1f, 0);

        for (Entity ent : Thunderhack.asyncManager.getAsyncEntities()) {
            if (ent == null) continue;
            if (ent.getBoundingBox().intersects(posBoundingBox)) {
                if (ent instanceof ExperienceOrbEntity)
                    continue;
                if (ent instanceof EndCrystalEntity){
                    continue;
                }
                return null;
            }
        }

        Vec3d crystalvector = new Vec3d(0.5f + bp.getX(), 1f + bp.getY(), 0.5f + bp.getZ());

        float damage;

        boolean overrideDamage = false;

        if (target == null) {
            damage = 10;
        } else {
            damage = ExplosionUtility.getExplosionDamage2(crystalvector, target);
        }

        float selfDamage = ExplosionUtility.getSelfExplosionDamage(crystalvector);

        if (overrideSelfDamage.getValue()) {
            boolean canPop = target != null
                    && damage + 0.5 > target.getHealth() + target.getAbsorptionAmount()
                    && (target.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING
                    || target.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING);

            boolean canKill = target != null
                    && damage + 0.5 > target.getHealth() + target.getAbsorptionAmount()
                    && target.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING
                    && target.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING;

            boolean canPopSelf = selfDamage + 0.5 > mc.player.getHealth() + mc.player.getAbsorptionAmount()
                    && (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING
                    || mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING);

            boolean canKillSelf = selfDamage + 0.5 > mc.player.getHealth() + mc.player.getAbsorptionAmount()
                    && mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING
                    && mc.player.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING;

            if (canPopSelf && canKill && !canKillSelf)
                overrideDamage = true;


            if (selfDamage > maxSelfDamage.getValue() && canPop && !canKillSelf && !canPopSelf)
                overrideDamage = true;
        }

        if (protectFriends.getValue()) {
            for (PlayerEntity pl : Thunderhack.asyncManager.getAsyncPlayers()) {
                if (!Thunderhack.friendManager.isFriend(pl)) continue;
                float fdamage = ExplosionUtility.getExplosionDamage2(crystalvector, pl);
                if (fdamage > selfDamage) {
                    selfDamage = fdamage;
                }
            }
        }

        if (damage < 1.5f) return null;
        if (selfDamage > maxSelfDamage.getValue() && !overrideDamage) return null;

        if (interact.getValue() == Interact.Default) {
            if (squaredDistanceFromEyes(crystalvector) > placeRange.getPow2Value())
                return null;

            BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), crystalvector, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));

            if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
                if (squaredDistanceFromEyes(crystalvector) > placeWallRange.getPow2Value())
                    return null;

            return new PlaceData(new BlockHitResult(crystalvector, Direction.DOWN, bp, false), damage, selfDamage, overrideDamage);
        } else if (interact.getValue() == Interact.Strict) {
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
                    float distance = squaredDistanceFromEyes(directionVec);
                    if (bestDistance > distance) {
                        bestDirection = dir;
                        bestVector = directionVec;
                        bestDistance = distance;
                    }
                }
            }

            if (bestVector == null) return null;

            if (squaredDistanceFromEyes(bestVector) > placeRange.getPow2Value())
                return null;

            BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), bestVector, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));

            if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
                if (squaredDistanceFromEyes(bestVector) > placeWallRange.getPow2Value())
                    return null;

            return new PlaceData(new BlockHitResult(bestVector, bestDirection, bp, false), damage, selfDamage, overrideDamage);
        } else {
            float bestDistance = 999f;
            PlaceData bestData = null;
            for (float x = 0f; x <= 1f; x += 0.05f) {
                for (float y = 0f; y <= 1; y += 0.05f) {
                    for (float z = 0f; z <= 1; z += 0.05f) {
                        Vec3d point = new Vec3d(bp.getX() + x, bp.getY() + y, bp.getZ() + z);
                        BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                        if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
                            if (squaredDistanceFromEyes(point) > placeWallRange.getPow2Value())
                                continue;

                        BlockHitResult result = ExplosionUtility.rayCastBlock(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player), bp);
                        if (squaredDistanceFromEyes(point) > placeRange.getPow2Value())
                            continue;

                        if (squaredDistanceFromEyes(point) < bestDistance)
                            if (result != null && result.getType() == HitResult.Type.BLOCK)
                                bestData = new PlaceData(result, damage, selfDamage, overrideDamage);
                    }
                }
            }
            return bestData;
        }
    }

    public float squaredDistanceFromEyes(Vec3d vec) {
        double d0 = vec.x - mc.player.getX();
        double d1 = vec.z - mc.player.getZ();
        double d2 = vec.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        return (float) (d0 * d0 + d1 * d1 + d2 * d2);
    }

    private record PlaceData(BlockHitResult bhr, float damage, float selfDamage, boolean overrideDamage) {
    }

    private record CrystalData(EndCrystalEntity crystal, float damage, float selfDamage, boolean overrideDamage) {
    }
}
