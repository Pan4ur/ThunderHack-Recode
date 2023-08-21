package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
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
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;
import thunder.hack.Thunderhack;
import thunder.hack.core.ModuleManager;
import thunder.hack.events.impl.*;
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

import static net.minecraft.util.math.MathHelper.wrapDegrees;

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
    public Setting<Boolean> aura = new Setting<>("Aura", true, v -> page.getValue() == Pages.Pause);
    public Setting<Boolean> pistonAura = new Setting<>("PistonAura", true, v -> page.getValue() == Pages.Pause);
    public Setting<Boolean> surround = new Setting<>("Surround", true, v -> page.getValue() == Pages.Pause);
    public Setting<Boolean> burrow = new Setting<>("Burrow", true, v -> page.getValue() == Pages.Pause);
    public Setting<Boolean> cevBreaker = new Setting<>("CevBreaker", true, v -> page.getValue() == Pages.Pause);
    public Setting<Boolean> blockPlacing = new Setting<>("BlockPlacing", true, v -> page.getValue() == Pages.Pause);
    public Setting<Float> pauseHP = new Setting<>("HP", 8.0f, 2.0f, 10f, v -> page.getValue() == Pages.Pause);
    public Setting<Boolean> switchPause = new Setting<>("SwitchPause", true, v -> page.getValue() == Pages.Pause);
    public Setting<Integer> switchDelay = new Setting<>("SwitchDelay", 100, 0, 1000, v -> page.getValue() == Pages.Pause && switchPause.getValue());


    /*   DAMAGES   */
    public Setting<Float> minDamage = new Setting<>("MinDamage", 6.0f, 2.0f, 20f, v -> page.getValue() == Pages.Damages);
    public Setting<Float> maxSelfDamage = new Setting<>("MaxSelfDamage", 10.0f, 2.0f, 20f, v -> page.getValue() == Pages.Damages);
    private final Setting<Safety> safety = new Setting<>("Safety", Safety.NONE, v -> page.getValue() == Pages.Damages);
    public Setting<Float> safetyBalance = new Setting<>("SafetyBalance", 1.1f, 0.1f, 3f, v -> page.getValue() == Pages.Damages);
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
    public Setting<ColorSetting> fillColor = new Setting<>("Block Fill Color", new ColorSetting(HudEditor.getColor(0)), v -> page.getValue() == Pages.Render);
    public Setting<ColorSetting> lineColor = new Setting<>("Block Line Color", new ColorSetting(HudEditor.getColor(0)), v -> page.getValue() == Pages.Render);
    public Setting<Integer> lineWidth = new Setting<>("Block Line Width", 2, 1, 10, v -> page.getValue() == Pages.Render);
    public Setting<Integer> slideDelay = new Setting<>("Slide Delay", 200, 1, 1000, v -> page.getValue() == Pages.Render);
    public Setting<ColorSetting> textColor = new Setting<>("Text Color", new ColorSetting(Color.WHITE), v -> page.getValue() == Pages.Render);

    private enum Pages {Place, Break, Pause, Render, Damages, Main, Switch}

    private enum Switch {NONE, NORMAL, SILENT}

    private enum Timing {NORMAL, SEQUENTIAL}

    private enum Interact {Default, Strict, BrutForce}

    private enum TargetLogic {Distance, HP}

    public enum Safety {BALANCE, STABLE, NONE}

    public enum Render {Fade, Slide}

    private PlayerEntity target;
    private BlockHitResult bestPosition;
    private EndCrystalEntity bestCrystal;

    private Timer placeTimer = new Timer();
    private Timer breakTimer = new Timer();
    private Timer switchTimer = new Timer();

    // позиция и время постановки
    private final Map<BlockPos, Long> placedCrystals = new HashMap<>();

    // id кристаллa и кол-во ударов
    private final Map<Integer, Integer> attackedCrystals = new HashMap<>();


    private float renderDamage = 0;

    private float[] rotation = new float[]{0f, 0f};

    private int prev_crystals_ammount, crys_speed, inv_timer;

    private BlockPos renderPos, prevRenderPos;
    long renderMultiplier;
    private final Map<BlockPos, Long> renderPositions = new HashMap<>();

    @Override
    public void onEnable() {
        renderDamage = 0;
        rotation = new float[]{0f, 0f};
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


    @EventHandler
    public void onSync(EventSync e) {
        if (targetLogic.getValue() == TargetLogic.Distance) {
            target = Thunderhack.combatManager.getNearestTarget(targetRange.getValue());
        } else {
            target = Thunderhack.combatManager.getTargetByHP(targetRange.getValue());
        }

        if (target != null && (target.isDead() || target.getHealth() < 0)) {
            target = null;
            return;
        }

        if (renderPositions.isEmpty()) attackedCrystals.clear();

        if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL && autoGapple.getValue()
                && mc.options.useKey.isPressed() && mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
            SearchInvResult result = InventoryUtility.findItemInHotBar(Items.ENCHANTED_GOLDEN_APPLE);
            result.switchIfFound();
        }

        if (inv_timer++ >= 20) {
            crys_speed = prev_crystals_ammount - InventoryUtility.getItemCount(Items.END_CRYSTAL);
            prev_crystals_ammount = InventoryUtility.getItemCount(Items.END_CRYSTAL);
            inv_timer = 0;
        }

        HashMap<Integer, Integer> cache = new HashMap<>(attackedCrystals);
        cache.forEach((crystal, attacks) -> {
            if (mc.world.getEntityById(crystal) == null)
                attackedCrystals.remove(crystal);
        });

        if (rotate.getValue()) {
            if (bestPosition != null) {
                float delta_yaw = wrapDegrees((float) wrapDegrees(Math.toDegrees(Math.atan2(bestPosition.getPos().z - mc.player.getZ(), (bestPosition.getPos().x - mc.player.getX()))) - 90) - mc.player.getYaw());
                float delta_pitch = ((float) (-Math.toDegrees(Math.atan2(bestPosition.getPos().y - (mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose())), Math.sqrt(Math.pow((bestPosition.getPos().x - mc.player.getX()), 2) + Math.pow(bestPosition.getPos().z - mc.player.getZ(), 2))))) - mc.player.getPitch());

                if (delta_yaw > 180)
                    delta_yaw = delta_yaw - 180;

                float step = MathUtility.random(-2f, 2f) + yawAngle.getValue();
                if (!yawStep.getValue()) step = 360f;

                float deltaYaw = MathHelper.clamp(MathHelper.abs(delta_yaw), -step, step);
                float newYaw = mc.player.getYaw() + (delta_yaw > 0 ? deltaYaw : -deltaYaw);
                float newPitch = MathHelper.clamp(mc.player.getPitch() + delta_pitch, -90.0F, 90.0F);
                double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;

                rotation = new float[]{(float) (newYaw - (newYaw - mc.player.getYaw()) % gcdFix), (float) (newPitch - (newPitch - mc.player.getPitch()) % gcdFix)};

                mc.player.setYaw(rotation[0]);
                mc.player.setPitch(rotation[1]);
            }
        }
    }

    @Override
    public String getDisplayInfo() {
        String info = crys_speed + " c/s";
        if (bestPosition != null)
            info = crys_speed + " c/s | " + bestPosition.getSide().toString().toUpperCase();
        return info;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (target == null) return;
        if (e.getPacket() instanceof PlaySoundS2CPacket sound && sound.getCategory().equals(SoundCategory.BLOCKS) && sound.getSound().value().equals(SoundEvents.ENTITY_GENERIC_EXPLODE)) {
            for (Entity crystal : Thunderhack.asyncManager.getAsyncEntities()) {
                if (!(crystal instanceof EndCrystalEntity))
                    continue;
                if (crystal == null || !crystal.isAlive())
                    continue;
                if (crystal.squaredDistanceTo(sound.getX() + 0.5, sound.getY() + 0.5, sound.getZ() + 0.5) > 121)
                    continue;

                HashMap<Integer, Integer> cache = new HashMap<>(attackedCrystals);
                if (cache.containsKey(crystal.getId())) {
                    attackedCrystals.remove(crystal.getId());
                    if (timing.getValue() == Timing.NORMAL && (placeDelay.getValue() == 0 || placeTimer.passedMs(placeDelay.getValue())))
                        if (bestPosition != null) {
                            placeCrystal(bestPosition);
                        }
                }
            }
        }

        if (e.getPacket() instanceof ExplosionS2CPacket expl) {
            for (Entity crystal : Thunderhack.asyncManager.getAsyncEntities()) {
                if (crystal == null || !crystal.isAlive())
                    continue;
                if (!(crystal instanceof EndCrystalEntity))
                    continue;
                if (crystal.squaredDistanceTo(expl.getX() + 0.5, expl.getY() + 0.5, expl.getZ() + 0.5) > expl.getRadius() * expl.getRadius())
                    continue;

                HashMap<Integer, Integer> cache = new HashMap<>(attackedCrystals);
                if (cache.containsKey(crystal.getId())) {
                    attackedCrystals.remove(crystal.getId());
                    if (timing.getValue() == Timing.NORMAL && (placeDelay.getValue() == 0 || placeTimer.passedMs(placeDelay.getValue())))
                        if (bestPosition != null) {
                            placeCrystal(bestPosition);
                        }
                }
            }
        }

        if (e.getPacket() instanceof EntitiesDestroyS2CPacket destroyEntities) {
            for (int entityId : destroyEntities.getEntityIds()) {
                Entity crystal = mc.world.getEntityById(entityId);
                if (crystal instanceof EndCrystalEntity) {

                    HashMap<Integer, Integer> cache = new HashMap<>(attackedCrystals);
                    if (cache.containsKey(crystal.getId())) {
                        attackedCrystals.remove(crystal.getId());

                        if (timing.getValue() == Timing.NORMAL && (placeDelay.getValue() == 0 || placeTimer.passedMs(placeDelay.getValue())))
                            if (bestPosition != null) {
                                placeCrystal(bestPosition);
                            }
                    }
                }
            }
        }

        if(e.getPacket() instanceof EntitySpawnS2CPacket spawn){
            if (!placedCrystals.isEmpty()) {
                Map<BlockPos, Long> cachedList = new HashMap<>(placedCrystals);
                for (BlockPos bp : cachedList.keySet())
                    if (spawn.getX() == bp.getX() + 0.5 && spawn.getZ() == bp.getZ() + 0.5 && spawn.getY() == bp.getY() + 1f) {
                        if (timing.getValue() == Timing.NORMAL && (breakDelay.getValue() == 0 || breakTimer.passedMs(breakDelay.getValue()))) {
                            EndCrystalEntity fakeCrystal = new EndCrystalEntity(mc.world,spawn.getX(),spawn.getY(),spawn.getZ());
                            fakeCrystal.setId(spawn.getId());
                            sendMessage("pac");
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
        if (bestPosition != null)
            if (placeTimer.passedMs(placeDelay.getValue()) || placeDelay.getValue() == 0)
                if (placeCrystal(bestPosition)) {
                    placeTimer.reset();
                }

        if (bestCrystal != null)
            if (breakTimer.passedMs(breakDelay.getValue()) || breakDelay.getValue() == 0)
                attackCrystal(bestCrystal);
    }


    @EventHandler
    public void onCrystalSpawn(EventEntitySpawn e) {
        if (e.getEntity() != null && e.getEntity() instanceof EndCrystalEntity && !placedCrystals.isEmpty()) {
            Map<BlockPos, Long> cachedList = new HashMap<>(placedCrystals);
            for (BlockPos bp : cachedList.keySet())
                if (e.getEntity().squaredDistanceTo(bp.toCenterPos().add(0, 0.5f, 0)) < 1) {
                    if (timing.getValue() == Timing.NORMAL && (breakDelay.getValue() == 0 || breakTimer.passedMs(breakDelay.getValue())))
                        attackCrystal((EndCrystalEntity) e.getEntity());
                    placedCrystals.remove(bp);
                }
        }
    }

    @EventHandler
    public void onCrystalRemove(EventEntityRemoved e) {
        if (e.entity != null && e.entity instanceof EndCrystalEntity) {
            HashMap<Integer, Integer> cache = new HashMap<>(attackedCrystals);
            if (cache.containsKey(e.entity.getId())) {
                attackedCrystals.remove(e.entity.getId());

                if (timing.getValue() == Timing.NORMAL && (placeDelay.getValue() == 0 || placeTimer.passedMs(placeDelay.getValue())))
                    if (bestPosition != null) {
                        placeCrystal(bestPosition);
                    }
            }
        }
    }


    @Override
    public void onThread() {
        if (ModuleManager.speedMine.isWorth()) {
            PlaceData autoMineData = getPlaceData(SpeedMine.minePosition, null);
            if(autoMineData != null) {
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
            if (data.selfDamage + 0.5 > mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
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

        if (!clearedList.isEmpty()) {
            bestPosition = filterPositions(clearedList);
        } else {
            bestPosition = null;
        }
    }

    public void onRender3D(MatrixStack stack) {
        if (render.getValue()) {
            Map<BlockPos, Long> cache = new HashMap<>(renderPositions);
            cache.forEach((pos, time) -> {
                if (System.currentTimeMillis() - time > 500) {
                    renderPositions.remove(pos);
                }
            });

            if (renderMode.getValue() == Render.Fade) {
                cache.forEach((pos, time) -> {
                    if (System.currentTimeMillis() - time < 500) {
                        int alpha = (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)));
                        Render3DEngine.drawFilledBox(stack, new Box(pos), Render2DEngine.injectAlpha(fillColor.getValue().getColorObject(), alpha));
                        Render3DEngine.drawBoxOutline(new Box(pos), Render2DEngine.injectAlpha(lineColor.getValue().getColorObject(), alpha), lineWidth.getValue());
                        Render3DEngine.drawTextIn3D(String.valueOf(MathUtility.round2(renderDamage)), pos.toCenterPos(), 0, 0.1, 0, Render2DEngine.injectAlpha(textColor.getValue().getColorObject(), alpha));
                    }
                });
            } else if (renderPos != null) {
                if (prevRenderPos == null) prevRenderPos = renderPos;
                if (renderPositions.isEmpty()) return;
                float mult = MathUtility.clamp((System.currentTimeMillis() - renderMultiplier) / (float) slideDelay.getValue(), 0f, 1f);
                Box interpolatedBox = Render3DEngine.interpolateBox(new Box(prevRenderPos), new Box(renderPos), mult);
                Render3DEngine.drawFilledBox(stack, interpolatedBox, fillColor.getValue().getColorObject());
                Render3DEngine.drawBoxOutline(interpolatedBox, lineColor.getValue().getColorObject(), lineWidth.getValue());
                Render3DEngine.drawTextIn3D(String.valueOf(MathUtility.round2(renderDamage)), interpolatedBox.getCenter(), 0, 0.1, 0, textColor.getValue().getColorObject());
            }
        }
    }

    public boolean canDoAC() {
        boolean offhand = mc.player.getOffHandStack().getItem() instanceof EndCrystalItem;
        if (mc.interactionManager.isBreakingBlock() && !offhand && mining.getValue())
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

        if (switchPause.getValue() && !switchTimer.passedMs(switchDelay.getValue()) && autoSwitch.getValue() != Switch.SILENT && antiWeakness.getValue() != Switch.SILENT) {
            return false;
        }

        return true;
    }

    public void attackCrystal(EndCrystalEntity crystal) {
        if (!canDoAC()) return;

        StatusEffectInstance weaknessEffect = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
        StatusEffectInstance strengthEffect = mc.player.getStatusEffect(StatusEffects.STRENGTH);

        if (crystalAge.getValue() != 0 && crystal.age < crystalAge.getValue())
            return;

        if(target == null || !checkCrystal(crystal)){
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
                sendMessage("return value of map is null");
            }
        } else {
            attackedCrystals.put(crystal.getId(), 1);
        }


        int prevSlot = -1;
        SearchInvResult swordResult = InventoryUtility.getSwordHotbar();
        if (antiWeakness.getValue() != Switch.NONE) {
            if (weaknessEffect != null && (strengthEffect == null || strengthEffect.getAmplifier() < weaknessEffect.getAmplifier())) {
                if (mc.player.getMainHandStack().getItem() instanceof SwordItem) {
                    prevSlot = mc.player.getInventory().selectedSlot;
                    swordResult.switchIfFound();
                }
            }
        }

        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        breakTimer.reset();

        HashMap<Integer, Integer> cache = new HashMap<>(attackedCrystals);
        if (cache.containsKey(crystal.getId())) {
            int newId = cache.get(crystal.getId()) + 1;
            attackedCrystals.remove(crystal.getId());
            attackedCrystals.put(crystal.getId(), newId);
        } else {
            attackedCrystals.put(crystal.getId(), 1);
        }

        if (prevSlot != -1) {
            if (antiWeakness.getValue() == Switch.SILENT) {
                mc.player.getInventory().selectedSlot = prevSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            }
        }
    }

    public boolean placeCrystal(BlockHitResult bhr) {
        if (!canDoAC()) return false;

        for (Entity ent : Thunderhack.asyncManager.getAsyncEntities()) {
            if (ent == null) continue;
            if (ent.getBoundingBox().intersects(new Box(bhr.getBlockPos().up()))) {
                if (ent instanceof ExperienceOrbEntity)
                    continue;
                if (ent instanceof EndCrystalEntity && !attackedCrystals.isEmpty() && attackedCrystals.containsKey(ent.getId()))
                    continue;
                return false;
            }
        }

        int prevSlot = -1;
        SearchInvResult crystalResult = InventoryUtility.findItemInHotBar(Items.END_CRYSTAL);
        if (autoSwitch.getValue() != Switch.NONE) {
            if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
                prevSlot = mc.player.getInventory().selectedSlot;
                crystalResult.switchIfFound();
            }
        }

        if (rotate.getValue() && mc.player.getYaw() != rotation[0] && mc.player.getPitch() != rotation[1])
            return false;

        Box posBoundingBox = new Box(bhr.getBlockPos().up());

        if (oldVer.getValue())
            posBoundingBox.expand(0, 1f, 0);

        for (Entity ent : Thunderhack.asyncManager.getAsyncEntities()) {
            if (ent.getBoundingBox().intersects(posBoundingBox)) {
                if (ent instanceof ExperienceOrbEntity)
                    continue;

                if (ent instanceof EndCrystalEntity) {
                    if (timing.getValue() == Timing.NORMAL && (breakDelay.getValue() == 0 || breakTimer.passedMs(breakDelay.getValue()))) {
                        attackCrystal((EndCrystalEntity) ent);
                    }
                    continue;
                }
                return false;
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

        if (autoSwitch.getValue() == Switch.SILENT) {
            if (prevSlot != -1) {
                mc.player.getInventory().selectedSlot = prevSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            }
        }
        return true;
    }

    public EndCrystalEntity getCrystalToExplode(PlayerEntity target) {
        List<PlaceData> clearedList = new ArrayList<>();
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

            double safetyIndex = 1;
            double health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
            if (selfDamage + 0.5 > health) {
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
            if (safetyIndex < 0) continue;
            if (damage < 1.5f) continue;
            clearedList.add(new PlaceData(new BlockHitResult(ent.getPos(), null, null, false), damage, selfDamage));
        }

        if (!clearedList.isEmpty()) {
            BlockHitResult bestCrystal = filterPositions(clearedList);
            if (bestCrystal == null) return null;
            for (Entity ent : Thunderhack.asyncManager.getAsyncEntities()) {
                if (!(ent instanceof EndCrystalEntity))
                    continue;

                if (ent.getPos() == bestCrystal.getPos()) {
                    return (EndCrystalEntity) ent;
                }
            }
        }
        return null;
    }

    public boolean checkCrystal(EndCrystalEntity crystal){
        if (squaredDistanceFromEyes(crystal.getPos()) > explodeRange.getPow2Value())
            return false;

        if (!InteractionUtility.canSee(crystal) && squaredDistanceFromEyes(crystal.getPos()) > explodeWallRange.getPow2Value())
            return false;

        if (!crystal.isAlive())
            return false;

        float damage = ExplosionUtility.getExplosionDamage2(crystal.getPos(), target);
        float selfDamage = ExplosionUtility.getSelfExplosionDamage(crystal.getPos());

        double safetyIndex = 1;
        double health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (selfDamage + 0.5 > health) {
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
        if (safetyIndex < 0) return false;
        if (damage < 1.5f) return false;
        boolean override = target.getHealth() + target.getAbsorptionAmount() <= facePlaceHp.getValue();

        if (armorBreaker.getValue())
            for (ItemStack armor : target.getArmorItems())
                if (armor != null && !armor.getItem().equals(Items.AIR) && ((armor.getMaxDamage() - armor.getDamage()) / (float) armor.getMaxDamage()) * 100 < armorScale.getValue()) {
                    override = true;
                    break;
                }

        if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), facePlaceButton.getValue().getKey()))
            override = true;

        if ((target.getHealth() + target.getAbsorptionAmount()) - (damage * lethalMultiplier.getValue()) < 0.5)
            override = true;

        return override || damage > minDamage.getValue();
    }

    public BlockHitResult filterPositions(List<PlaceData> clearedList) {
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
                bestData = data;
                bestDmg = data.damage;
            }
        }

        if (bestData == null) return null;

        return bestData.bhr;
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

    public PlaceData getPlaceData(BlockPos bp, PlayerEntity target) {
        Block block = mc.world.getBlockState(bp).getBlock();
        Block freeSpace = mc.world.getBlockState(bp.up()).getBlock();
        Block legacyFreeSpace = mc.world.getBlockState(bp.up().up()).getBlock();

        if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK)
            return null;

        if (!(freeSpace == Blocks.AIR && (!oldVer.getValue() || legacyFreeSpace == Blocks.AIR)))
            return null;

        Box posBoundingBox = new Box(bp.up());

        if (oldVer.getValue())
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

        float damage;

        if(target == null) {
            damage = 10;
        } else {
            damage = ExplosionUtility.getExplosionDamage2(crystalvector, target);
        }

        float selfDamage = ExplosionUtility.getSelfExplosionDamage(crystalvector);

        if (damage < 1.5f) return null;
        if (selfDamage > maxSelfDamage.getValue()) return null;

        if (interact.getValue() == Interact.Default) {
            if (squaredDistanceFromEyes(crystalvector) > placeRange.getPow2Value())
                return null;

            BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), crystalvector, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));

            if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
                if (squaredDistanceFromEyes(crystalvector) > placeWallRange.getPow2Value())
                    return null;

            return new PlaceData(new BlockHitResult(crystalvector, Direction.DOWN, bp, false), damage, selfDamage);
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

            return new PlaceData(new BlockHitResult(bestVector, bestDirection, bp, false), damage, selfDamage);
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
                                bestData = new PlaceData(result, damage, selfDamage);
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

    private record PlaceData(BlockHitResult bhr, float damage, float selfDamage) {
    }
}
