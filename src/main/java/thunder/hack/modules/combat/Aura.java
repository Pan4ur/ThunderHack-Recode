package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.Core;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.core.impl.PlayerManager;
import thunder.hack.events.impl.*;
import thunder.hack.gui.notification.Notification;
import thunder.hack.injection.accesors.ILivingEntity;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanParent;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.Timer;
import thunder.hack.utility.interfaces.IOtherClientPlayerEntity;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.MovementUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.animation.CaptureMark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.minecraft.util.UseAction.BLOCK;
import static net.minecraft.util.math.MathHelper.wrapDegrees;
import static thunder.hack.modules.client.MainSettings.isRu;
import static thunder.hack.utility.math.MathUtility.random;

public final class Aura extends Module {
    public final Setting<Float> attackRange = new Setting<>("Range", 3.1f, 2f, 6.0f);
    public final Setting<Rotation> rotationMode = new Setting<>("Rotation", Rotation.Universal);
    public final Setting<Switch> switchMode = new Setting<>("Switch", Switch.None);
    public final Setting<Boolean> onlyWeapon = new Setting<>("OnlyWeapon", false, v -> switchMode.getValue() != Switch.Silent);
    public final Setting<BooleanParent> smartCrit = new Setting<>("SmartCrit", new BooleanParent(true));
    public final Setting<Boolean> onlySpace = new Setting<>("OnlySpace", false).withParent(smartCrit);
    public final Setting<Boolean> autoJump = new Setting<>("AutoJump", false).withParent(smartCrit);
    public final Setting<BooleanParent> ignoreWalls = new Setting<>("IgnoreWalls", new BooleanParent(true));
    public final Setting<Boolean> wallsBypass = new Setting<>("Bypass", false).withParent(ignoreWalls);
    public final Setting<Boolean> shieldBreaker = new Setting<>("ShieldBreaker", true);
    public final Setting<Boolean> moveFix = new Setting<>("MoveFix", false);
    public final Setting<Boolean> clientLook = new Setting<>("ClientLook", false);
    public final Setting<BooleanParent> oldDelay = new Setting<>("OldDelay", new BooleanParent(false));
    public final Setting<Integer> minCPS = new Setting<>("MinCPS", 7, 1, 15).withParent(oldDelay);
    public final Setting<Integer> maxCPS = new Setting<>("MaxCPS", 12, 1, 15).withParent(oldDelay);
    public final Setting<ESP> esp = new Setting<>("ESP", ESP.ThunderHack);
    public final Setting<Sort> sort = new Setting<>("Sort", Sort.Distance);

    /*   ADVANCED   */
    public final Setting<Parent> advanced = new Setting<>("Advanced", new Parent(false, 0));
    public final Setting<Boolean> pauseWhileEating = new Setting<>("PauseWhileEating", false).withParent(advanced);
    public final Setting<Boolean> dropSprint = new Setting<>("DropSprint", true).withParent(advanced);
    public final Setting<Boolean> pauseInInventory = new Setting<>("PauseInInventory", true).withParent(advanced);
    public final Setting<RayTrace> rayTrace = new Setting<>("RayTrace", RayTrace.OnlyTarget).withParent(advanced);
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Track).withParent(advanced);
    public final Setting<Integer> interactTicks = new Setting<>("InteractTicks", 3, 1, 10, v -> mode.getValue() == Mode.Interact).withParent(advanced);
    public final Setting<Boolean> unpressShield = new Setting<>("UnpressShield", true).withParent(advanced);
    public final Setting<Boolean> deathDisable = new Setting<>("DeathDisable", true).withParent(advanced);
    public final Setting<Boolean> tpDisable = new Setting<>("TPDisable", false).withParent(advanced);
    public final Setting<Boolean> pullDown = new Setting<>("PullDown", false).withParent(advanced);
    public final Setting<Float> pullValue = new Setting<>("PullValue", 3f, 0f, 20f, v -> pullDown.getValue()).withParent(advanced);
    public final Setting<AttackHand> attackHand = new Setting<>("AttackHand", AttackHand.MainHand).withParent(advanced);
    public final Setting<RayTraceAngle> rayTraceAngle = new Setting<>("TraceAngle", RayTraceAngle.Calculated).withParent(advanced);
    public final Setting<Resolver> resolver = new Setting<>("Resolver", Resolver.Advantage).withParent(advanced);
    public final Setting<Integer> minYawStep = new Setting<>("MinYawStep", 65, 1, 180).withParent(advanced);
    public final Setting<Integer> maxYawStep = new Setting<>("MaxYawStep", 75, 1, 180).withParent(advanced);
    public final Setting<Float> aimedPitchStep = new Setting<>("AimedPitchStep", 1f, 0f, 90f).withParent(advanced);
    public final Setting<Float> maxPitchStep = new Setting<>("MaxPitchStep", 8f, 1f, 90f).withParent(advanced);
    public final Setting<Float> pitchAccelerate = new Setting<>("PitchAccelerate", 1.65f, 1f, 10f).withParent(advanced);

    /*   TARGETS   */
    public final Setting<Parent> targets = new Setting<>("Targets", new Parent(false, 0));
    public final Setting<Boolean> Players = new Setting<>("Players", true).withParent(targets);
    public final Setting<Boolean> Mobs = new Setting<>("Mobs", true).withParent(targets);
    public final Setting<Boolean> Animals = new Setting<>("Animals", true).withParent(targets);
    public final Setting<Boolean> Villagers = new Setting<>("Villagers", true).withParent(targets);
    public final Setting<Boolean> Slimes = new Setting<>("Slimes", true).withParent(targets);
    public final Setting<Boolean> Projectiles = new Setting<>("Projectiles", true).withParent(targets);
    public final Setting<Boolean> ignoreInvisible = new Setting<>("IgnoreInvis", false).withParent(targets);
    public final Setting<Boolean> ignoreCreative = new Setting<>("IgnoreCreative", true).withParent(targets);
    public final Setting<Boolean> ignoreShield = new Setting<>("IgnoreShield", true).withParent(targets);
    public final Setting<Boolean> onlyAngry = new Setting<>("OnlyAngryEntities", true).withParent(targets);

    public static Entity target;

    private float rotationYaw, rotationPitch, prevClientYaw, pitchAcceleration = 1f, prevYaw;

    private Vec3d rotationPoint = Vec3d.ZERO;
    private Vec3d rotationMotion = Vec3d.ZERO;

    private int hitTicks;
    private int trackticks;
    private boolean lookingAtHitbox;

    private static Aura instance;
    private final Timer delayTimer = new Timer();

    public Aura() {
        super("Aura", Category.COMBAT);
        instance = this;
    }

    @EventHandler
    public void modifyVelocity(EventPlayerTravel e) {
        Item handItem = mc.player.getMainHandStack().getItem();
        if (target == null || !moveFix.getValue() || (switchMode.getValue() != Switch.Silent && onlyWeapon.getValue() && !(handItem instanceof SwordItem || handItem instanceof AxeItem)))
            return;

        if (e.isPre()) {
            prevClientYaw = mc.player.getYaw();
            mc.player.setYaw(rotationYaw);
        } else {
            mc.player.setYaw(prevClientYaw);
        }
    }

    @EventHandler
    public void modifyJump(EventPlayerJump e) {
        Item handItem = mc.player.getMainHandStack().getItem();
        if (target == null || !moveFix.getValue() || (switchMode.getValue() != Switch.Silent && onlyWeapon.getValue() && !(handItem instanceof SwordItem || handItem instanceof AxeItem)))
            return;

        if (e.isPre()) {
            prevClientYaw = mc.player.getYaw();
            mc.player.setYaw(rotationYaw);
        } else mc.player.setYaw(prevClientYaw);
    }

    public void auraLogic() {
        Item handItem = mc.player.getMainHandStack().getItem();

        if((switchMode.getValue() != Switch.Silent && onlyWeapon.getValue() && !(handItem instanceof SwordItem || handItem instanceof AxeItem))) {
            target = null;
            return;
        }

        handleKill();
        updateTarget();

        if (target == null) {
            return;
        }

        if (!mc.options.jumpKey.isPressed() && mc.player.isOnGround() && autoJump.getValue())
            mc.player.jump();

        calcRotations(autoCrit());
        
        boolean readyForAttack = autoCrit() && (lookingAtHitbox || skipRayTraceCheck());

        if (readyForAttack) {
            if (shieldBreaker(false))
                return;

            if (switchMode.getValue() == Switch.None && onlyWeapon.getValue() && !(handItem instanceof SwordItem || handItem instanceof AxeItem))
                return;

            boolean[] playerState = preAttack();
            if (!(target instanceof PlayerEntity pl) || !(pl.isUsingItem() && pl.getOffHandStack().getItem() == Items.SHIELD) || ignoreShield.getValue())
                attack();

            postAttack(playerState[0], playerState[1]);
        }
    }

    private boolean skipRayTraceCheck() {
        return rotationMode.getValue() == Rotation.None || rayTrace.getValue() == RayTrace.OFF || (mode.getValue() == Mode.Interact && interactTicks.getValue() <= 1);
    }

    public void attack() {
        Criticals.cancelCrit = true;
        ModuleManager.criticals.doCrit();
        int prevSlot = switchMethod();
        mc.interactionManager.attackEntity(mc.player, target);
        Criticals.cancelCrit = false;
        swingHand();
        hitTicks = getHitTicks();
        if (prevSlot != -1)
            InventoryUtility.switchTo(prevSlot);
    }

    private boolean @NotNull [] preAttack() {
        boolean blocking = mc.player.isUsingItem() && mc.player.getActiveItem().getItem().getUseAction(mc.player.getActiveItem()) == BLOCK;
        if (blocking && unpressShield.getValue())
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));

        boolean sprint = Core.serverSprint;
        if (sprint && dropSprint.getValue())
            disableSprint();
        return new boolean[]{blocking, sprint};
    }

    public void postAttack(boolean block, boolean sprint) {
        if (sprint && dropSprint.getValue())
            enableSprint();
        if (block && unpressShield.getValue())
            sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, PlayerUtility.getWorldActionId(mc.world)));
    }

    private void disableSprint() {
        mc.player.setSprinting(false);
        mc.options.sprintKey.setPressed(false);
        sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
    }

    private void enableSprint() {
        mc.player.setSprinting(true);
        mc.options.sprintKey.setPressed(true);
        sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
    }

    public void resolvePlayers() {
        if (resolver.getValue() != Resolver.Off)
            for (PlayerEntity player : mc.world.getPlayers())
                if (player instanceof OtherClientPlayerEntity)
                    ((IOtherClientPlayerEntity) player).resolve(resolver.getValue());
    }

    public void restorePlayers() {
        if (resolver.getValue() != Resolver.Off)
            for (PlayerEntity player : mc.world.getPlayers())
                if (player instanceof OtherClientPlayerEntity)
                    ((IOtherClientPlayerEntity) player).releaseResolver();
    }

    public void handleKill() {
        if (target instanceof LivingEntity && (((LivingEntity) target).getHealth() <= 0 || ((LivingEntity) target).isDead()))
            ThunderHack.notificationManager.publicity("Aura", isRu() ? "Цель успешно нейтрализована!" : "Target successfully neutralized!", 3, Notification.Type.SUCCESS);
    }

    private int switchMethod() {
        int prevSlot = -1;
        SearchInvResult swordResult = InventoryUtility.getSwordHotBar();
        if (swordResult.found() && switchMode.getValue() != Switch.None) {
            if (switchMode.getValue() == Switch.Silent)
                prevSlot = mc.player.getInventory().selectedSlot;
            swordResult.switchTo();
        }

        return prevSlot;
    }

    private int getHitTicks() {
        // Обоссаный плагин поставили чтоб нубики с читами в крит не попадали
        if (mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().address.equals("ngrief.me") && mc.player.getMainHandStack().getItem() instanceof AxeItem) {
            return 21;
        }
        return oldDelay.getValue().isEnabled() ? 1 + (int) (20f / random(minCPS.getValue(), maxCPS.getValue())) : 11;
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent e) {
        resolvePlayers();
        auraLogic();
        restorePlayers();
        hitTicks--;
    }

    @EventHandler
    public void onSync(EventSync e) {
        Item handItem = mc.player.getMainHandStack().getItem();
        if ((onlyWeapon.getValue() && !(handItem instanceof SwordItem || handItem instanceof AxeItem)) && switchMode.getValue() != Switch.Silent)
            return;

        if (target != null && rotationMode.getValue() != Rotation.None) {
            mc.player.setYaw(rotationYaw);
            mc.player.setPitch(rotationPitch);
        } else {
            rotationYaw = mc.player.getYaw();
            rotationPitch = mc.player.getPitch();
        }

        if (oldDelay.getValue().isEnabled())
            if (minCPS.getValue() > maxCPS.getValue())
                minCPS.setValue(maxCPS.getValue());

        if (target != null && pullDown.getValue())
            mc.player.addVelocity(0f, -pullValue.getValue() / 1000f, 0f);

        CaptureMark.tick();
        Render3DEngine.updateTargetESP();
    }

    @EventHandler
    public void onPacketSend(PacketEvent.@NotNull Send e) {
        if(e.getPacket() instanceof  PlayerInteractEntityC2SPacket pie && Criticals.getInteractType(pie) != Criticals.InteractType.ATTACK)
           e.cancel();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.@NotNull Receive e) {
        if (e.getPacket() instanceof EntityStatusS2CPacket status)
            if (status.getStatus() == 30 && status.getEntity(mc.world) != null && target != null && status.getEntity(mc.world) == target)
                ThunderHack.notificationManager.publicity("Aura", isRu() ? ("Успешно сломали щит игроку " + target.getName().getString()) : ("Succesfully destroyed " + target.getName().getString() + "'s shield"), 2, Notification.Type.SUCCESS);

        if (e.getPacket() instanceof PlayerPositionLookS2CPacket && tpDisable.getValue())
            disable(isRu() ? "Отключаю из-за телепортации!" : "Disabling due to tp!");

        if (e.getPacket() instanceof EntityStatusS2CPacket pac && pac.getStatus() == 3 && pac.getEntity(mc.world) == mc.player && deathDisable.getValue())
            disable(isRu() ? "Отключаю из-за смерти!" : "Disabling due to death!");
    }

    @Override
    public void onEnable() {
        target = null;
        lookingAtHitbox = false;
        rotationPoint = Vec3d.ZERO;
        rotationMotion = Vec3d.ZERO;
        rotationYaw = mc.player.getYaw();
        rotationPitch = mc.player.getPitch();
        delayTimer.reset();
    }

    private boolean autoCrit() {
        boolean reasonForSkipCrit =
                !smartCrit.getValue().isEnabled()
                        || mc.player.getAbilities().flying
                        || (mc.player.isFallFlying() || ModuleManager.elytraPlus.isEnabled())
                        || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                        || mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos())).getBlock() == Blocks.COBWEB;

        if (hitTicks > 0)
            return false;

        if (mc.player.isUsingItem() && pauseWhileEating.getValue()
                && (mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE || mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE))
            return true;

        // я хз почему оно не критует когда фд больше 1.14
        if (mc.player.fallDistance > 1 && mc.player.fallDistance < 1.14)
            return false;

        if (pauseInInventory.getValue() && ThunderHack.playerManager.inInventory)
            return false;

        if (getAttackCooldown() < 0.9f && !oldDelay.getValue().isEnabled())
            return false;

        boolean mergeWithTargetStrafe = !ModuleManager.targetStrafe.isEnabled() || !ModuleManager.targetStrafe.jump.getValue();
        boolean mergeWithSpeed = !ModuleManager.speed.isEnabled() || mc.player.isOnGround();

        if (!mc.options.jumpKey.isPressed() && mergeWithTargetStrafe && mergeWithSpeed && !onlySpace.getValue() && !autoJump.getValue())
            return true;

        if (mc.player.isInLava())
            return true;

        if (!mc.options.jumpKey.isPressed() && isAboveWater())
            return true;

        if (!reasonForSkipCrit)
            return !mc.player.isOnGround() && mc.player.fallDistance > 0.0f;
        return true;
    }

    private boolean shieldBreaker(boolean instant) {
        int axeSlot = InventoryUtility.getAxe().slot();
        if (axeSlot == -1) return false;
        if (!shieldBreaker.getValue()) return false;
        if (!(target instanceof PlayerEntity)) return false;
        if (!((PlayerEntity) target).isUsingItem() && !instant) return false;
        if (((PlayerEntity) target).getOffHandStack().getItem() != Items.SHIELD && ((PlayerEntity) target).getMainHandStack().getItem() != Items.SHIELD)
            return false;

        if (axeSlot >= 9) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, axeSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            mc.interactionManager.attackEntity(mc.player, target);
            swingHand();
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, axeSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        } else {
            sendPacket(new UpdateSelectedSlotC2SPacket(axeSlot));
            mc.interactionManager.attackEntity(mc.player, target);
            swingHand();
            sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        }
        hitTicks = 10;
        return true;
    }

    private void swingHand() {
        switch (attackHand.getValue()) {
            case OffHand -> mc.player.swingHand(Hand.OFF_HAND);
            case MainHand -> mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    public static boolean isAboveWater() {
        return mc.player.isSubmergedInWater() || mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos().add(0, -0.4, 0))).getBlock() == Blocks.WATER;
    }

    public static float getAttackCooldownProgressPerTick() {
        return (float) (1.0 / mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * (20.0 * ThunderHack.TICK_TIMER));
    }

    public static float getAttackCooldown() {
        return MathHelper.clamp(((float) ((ILivingEntity) mc.player).getLastAttackedTicks() + 0.5f) / getAttackCooldownProgressPerTick(), 0.0F, 1.0F);
    }

    private void updateTarget() {
        Entity candidat = findTarget();

        if (target == null) {
            target = candidat;
            return;
        }

        if (sort.getValue() == Sort.FOV)
            target = candidat;

        if (candidat instanceof ProjectileEntity)
            target = candidat;

        if (skipEntity(target))
            target = null;
    }

    private void calcRotations(boolean ready) {
        if (ready) {
            trackticks = interactTicks.getValue();
        } else if (trackticks > 0) {
            trackticks--;
        }

        if (target == null)
            return;

        switch (rotationMode.getValue()) {
            case Universal -> {
                Vec3d targetVec;

                if (mc.player.isFallFlying() || ModuleManager.elytraPlus.isEnabled()) targetVec = target.getEyePos();
                else targetVec = getLegitLook(target);

                if (targetVec == null)
                    return;

                pitchAcceleration = lookingAtHitbox ? aimedPitchStep.getValue() : pitchAcceleration < maxPitchStep.getValue() ? pitchAcceleration * pitchAccelerate.getValue() : maxPitchStep.getValue();

                float prevYaw = rotationYaw;
                float prevPitch = rotationPitch;

                float delta_yaw = wrapDegrees((float) wrapDegrees(Math.toDegrees(Math.atan2(targetVec.z - mc.player.getZ(), (targetVec.x - mc.player.getX()))) - 90) - rotationYaw);
                float delta_pitch = ((float) (-Math.toDegrees(Math.atan2(targetVec.y - (mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose())), Math.sqrt(Math.pow((targetVec.x - mc.player.getX()), 2) + Math.pow(targetVec.z - mc.player.getZ(), 2))))) - rotationPitch);

                float yawStep = mode.getValue() == Mode.Interact ? 360f : random(minYawStep.getValue(), maxYawStep.getValue());
                float pitchStep = mode.getValue() == Mode.Interact ? 180f : pitchAcceleration + random(-1f, 1f);

                if (delta_yaw > 180)
                    delta_yaw = delta_yaw - 180;

                float deltaYaw = MathHelper.clamp(MathHelper.abs(delta_yaw), -yawStep, yawStep);

                float deltaPitch = MathHelper.clamp(delta_pitch, -pitchStep, pitchStep);

                float newYaw = rotationYaw + (delta_yaw > 0 ? deltaYaw : -deltaYaw);
                float newPitch = MathHelper.clamp(rotationPitch + deltaPitch, -90.0F, 90.0F);

                double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;

                if (trackticks > 0 || mode.getValue() == Mode.Track) {
                    rotationYaw = (float) (newYaw - (newYaw - rotationYaw) % gcdFix);
                    rotationPitch = (float) (newPitch - (newPitch - rotationPitch) % gcdFix);
                    if (MovementUtility.sprintIsLegit(rotationYaw) && hitTicks > 1) {
                        //   mc.player.setVelocity(mc.player.getVelocity().getX() * 1.3, mc.player.getVelocity().getY(), mc.player.getVelocity().getZ() * 1.3);
                    }
                } else {
                    rotationYaw = mc.player.getYaw();
                    rotationPitch = mc.player.getPitch();
                }

                lookingAtHitbox = ThunderHack.playerManager.checkRtx(
                        rayTraceAngle.getValue() == RayTraceAngle.Calculated ? rotationYaw : prevYaw,
                        rayTraceAngle.getValue() == RayTraceAngle.Calculated ? rotationPitch : prevPitch,
                        attackRange.getValue(), ignoreWalls.getValue().isEnabled(), rayTrace.getValue());
            }
            case SunRise -> {
                Vec3d ent = getSunriseVector(target);
                if (ent == null)
                    ent = target.getEyePos();
                double deltaX = ent.getX() - mc.player.getX();
                double deltaZ = ent.getZ() - mc.player.getZ();
                float yawDelta = MathHelper.wrapDegrees((float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0) - rotationYaw) / 1.0001f;
                float pitchDelta = ((float) -Math.toDegrees(Math.atan2(ent.getY() - mc.player.getEyePos().getY(), Math.hypot(deltaX, deltaZ))) - rotationPitch) / 1.0001f;
                float additionYaw = Math.min(Math.max((int) Math.abs(yawDelta), 1), 80);
                float additionPitch = Math.max(ready ? Math.abs(pitchDelta) : 1.0f, 2.0f);
                if (Math.abs(additionYaw - prevYaw) <= 3.0f)
                    additionYaw = prevYaw + 3.1f;

                if (trackticks > 0 || mode.getValue() == Mode.Track) {
                    rotationYaw = rotationYaw + (yawDelta > 0.0f ? additionYaw : -additionYaw) * 1.0001f;
                    rotationPitch = MathHelper.clamp(rotationPitch + (pitchDelta > 0.0f ? additionPitch : -additionPitch) * 1.0001f, -90.0f, 90.0f);
                } else {
                    rotationYaw = mc.player.getYaw();
                    rotationPitch = mc.player.getPitch();
                }

                prevYaw = additionYaw;
                lookingAtHitbox = ThunderHack.playerManager.checkRtx(rotationYaw, rotationPitch, attackRange.getValue(), ignoreWalls.getValue().isEnabled(), rayTrace.getValue());
            }
        }
    }

    private Vec3d getSunriseVector(Entity trgt) {
        if (!mc.player.canSee(trgt) && wallsBypass.getValue())
            return trgt.getPos().add(random(-0.15, 0.15), trgt.getBoundingBox().getLengthY(), random(-0.15, 0.15));

        return new ArrayList<>(Arrays.asList(trgt.getEyePos(), trgt.getPos().add(0, trgt.getEyeHeight(trgt.getPose()) / 2f, 0f), trgt.getPos().add(0, 0.05f, 0f)))
                .stream()
                .min(Comparator.comparing(p -> getPitchDelta(rotationPitch, p)))
                .orElse(null);
    }

    private double getPitchDelta(float currentY, Vec3d v) {
        return Math.abs(-Math.toDegrees(Math.atan2(v.y - mc.player.getEyePos().getY(), Math.hypot(v.x - mc.player.getX(), v.z - mc.player.getZ()))) - currentY);
    }

    public void onRender3D(MatrixStack stack) {
        Item handItem = mc.player.getMainHandStack().getItem();
        if (target == null
                || (switchMode.getValue() != Switch.Silent
                && onlyWeapon.getValue()
                && !(handItem instanceof SwordItem || handItem instanceof AxeItem)))
            return;

        switch (esp.getValue()) {
            case CelkaPasta -> Render3DEngine.drawOldTargetEsp(stack, target);
            case NurikZapen -> CaptureMark.render(target);
            case ThunderHack -> Render3DEngine.drawTargetEsp(stack, target);
        }

        if (clientLook.getValue() && rotationMode.getValue() != Rotation.None) {
            mc.player.setYaw((float) Render2DEngine.interpolate(mc.player.prevYaw, rotationYaw, mc.getTickDelta()));
            mc.player.setPitch((float) Render2DEngine.interpolate(mc.player.prevPitch, rotationPitch, mc.getTickDelta()));
        }
    }

    @Override
    public void onDisable() {
        target = null;
    }

    private float getSquaredRotateDistance() {
        float dst = attackRange.getValue();
        dst += 2f;
        if ((mc.player.isFallFlying() || ModuleManager.elytraPlus.isEnabled()) && target != null) dst += 4f;
        if (ModuleManager.strafe.isEnabled()) dst += 4f;
        if (mode.getValue() == Mode.Interact || rotationMode.getValue() == Rotation.None || rayTrace.getValue() == RayTrace.OFF)
            dst = attackRange.getValue();

        return dst * dst;
    }

    /*
     * Эта хуеверть основанна на приципе "DVD Logo"
     * У нас есть точка и "коробка" (хитбокс цели)
     * Точка летает внутри коробки и отталкивается от стенок с рандомной скоростью и легким джиттером
     * Также выбирает лучшую дистанцию для удара, то есть считает не от центра до центра, а от наших глаз до достигаемых точек хитбокса цели
     * Со стороны не сильно заметно что ты играешь с киллкой, в отличие от аур семейства Wexside
     */

    public Vec3d getLegitLook(Entity target) {

        float minMotionXZ = 0.003f;
        float maxMotionXZ = 0.03f;

        float minMotionY = 0.001f;
        float maxMotionY = 0.03f;

        double lenghtX = target.getBoundingBox().getLengthX();
        double lenghtY = target.getBoundingBox().getLengthY();
        double lenghtZ = target.getBoundingBox().getLengthZ();


        // Задаем начальную скорость точки
        if (rotationMotion.equals(Vec3d.ZERO))
            rotationMotion = new Vec3d(random(-0.05f, 0.05f), random(-0.05f, 0.05f), random(-0.05f, 0.05f));

        rotationPoint = rotationPoint.add(rotationMotion);

        // Сталкиваемся с хитбоксом по X
        if (rotationPoint.x >= (lenghtX - 0.05) / 2f)
            rotationMotion = new Vec3d(-random(minMotionXZ, maxMotionXZ), rotationMotion.getY(), rotationMotion.getZ());

        // Сталкиваемся с хитбоксом по Y
        if (rotationPoint.y >= lenghtY)
            rotationMotion = new Vec3d(rotationMotion.getX(), -random(minMotionY, maxMotionY), rotationMotion.getZ());

        // Сталкиваемся с хитбоксом по Z
        if (rotationPoint.z >= (lenghtZ - 0.05) / 2f)
            rotationMotion = new Vec3d(rotationMotion.getX(), rotationMotion.getY(), -random(minMotionXZ, maxMotionXZ));

        // Сталкиваемся с хитбоксом по -X
        if (rotationPoint.x <= -(lenghtX - 0.05) / 2f)
            rotationMotion = new Vec3d(random(minMotionXZ, 0.03f), rotationMotion.getY(), rotationMotion.getZ());

        // Сталкиваемся с хитбоксом по -Y
        if (rotationPoint.y <= 0.05)
            rotationMotion = new Vec3d(rotationMotion.getX(), random(minMotionY, maxMotionY), rotationMotion.getZ());

        // Сталкиваемся с хитбоксом по -Z
        if (rotationPoint.z <= -(lenghtZ - 0.05) / 2f)
            rotationMotion = new Vec3d(rotationMotion.getX(), rotationMotion.getY(), random(minMotionXZ, maxMotionXZ));

        // Добавляем джиттер
        rotationPoint.add(random(-0.03f, 0.03f), 0f, random(-0.03f, 0.03f));

        // Если мы используем обход ударов через стену и наша цель за стеной, то целимся в верхушку хитбокса т.к. матриксу поебать
        if (!mc.player.canSee(target) && wallsBypass.getValue())
            return target.getPos().add(random(-0.15, 0.15), lenghtY, random(-0.15, 0.15));

        float[] rotation;

        // Если мы перестали смотреть на цель
        if (!ThunderHack.playerManager.checkRtx(rotationYaw, rotationPitch, attackRange.getValue(), ignoreWalls.getValue().isEnabled(), rayTrace.getValue())) {
            float[] rotation1 = PlayerManager.calcAngle(target.getPos().add(0, target.getEyeHeight(target.getPose()) / 2f, 0));

            // Проверяем видимость центра игрока
            if (PlayerUtility.squaredDistanceFromEyes(target.getPos().add(0, target.getEyeHeight(target.getPose()) / 2f, 0)) <= attackRange.getPow2Value()
                    && ThunderHack.playerManager.checkRtx(rotation1[0], rotation1[1], attackRange.getValue(), false, rayTrace.getValue())) {
                // наводим на центр
                rotationPoint = new Vec3d(random(-0.1f, 0.1f), target.getEyeHeight(target.getPose()) / (random(1.8f, 2.5f)), random(-0.1f, 0.1f));
            } else {
                // Сканим хитбокс на видимую точку
                float halfBox = (float) (lenghtX / 2f);

                for (float x1 = -halfBox; x1 <= halfBox; x1 += 0.05f) {
                    for (float z1 = -halfBox; z1 <= halfBox; z1 += 0.05f) {
                        for (float y1 = 0.05f; y1 <= target.getBoundingBox().getLengthY(); y1 += 0.15f) {

                            Vec3d v1 = new Vec3d(target.getX() + x1, target.getY() + y1, target.getZ() + z1);

                            // Скипаем, если вне досягаемости
                            if (PlayerUtility.squaredDistanceFromEyes(v1) > attackRange.getPow2Value()) continue;

                            rotation = PlayerManager.calcAngle(v1);
                            if (ThunderHack.playerManager.checkRtx(rotation[0], rotation[1], attackRange.getValue(), false, rayTrace.getValue())) {
                                // Наводимся, если видим эту точку
                                rotationPoint = new Vec3d(x1, y1, z1);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return target.getPos().add(rotationPoint);
    }

    public boolean isInRange(Entity target) {

        if (PlayerUtility.squaredDistanceFromEyes(target.getPos().add(0, target.getEyeHeight(target.getPose()), 0)) > getSquaredRotateDistance() + 4) {
            return false;
        }

        float[] rotation;
        float halfBox = (float) (target.getBoundingBox().getLengthX() / 2f);

        // уменьшил частоту выборки
        for (float x1 = -halfBox; x1 <= halfBox; x1 += 0.15f) {
            for (float z1 = -halfBox; z1 <= halfBox; z1 += 0.15f) {
                for (float y1 = 0.05f; y1 <= target.getBoundingBox().getLengthY(); y1 += 0.25f) {
                    if (PlayerUtility.squaredDistanceFromEyes(new Vec3d(target.getX() + x1, target.getY() + y1, target.getZ() + z1)) > getSquaredRotateDistance())
                        continue;

                    rotation = PlayerManager.calcAngle(new Vec3d(target.getX() + x1, target.getY() + y1, target.getZ() + z1));
                    if (ThunderHack.playerManager.checkRtx(rotation[0], rotation[1], (float) Math.sqrt(getSquaredRotateDistance()), ignoreWalls.getValue().isEnabled(), rayTrace.getValue())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Entity findTarget() {
        List<LivingEntity> first_stage = new CopyOnWriteArrayList<>();
        for (Entity ent : mc.world.getEntities()) {
            if ((ent instanceof ShulkerBulletEntity || ent instanceof FireballEntity)
                    && ent.isAlive()
                    && isInRange(ent)
                    && Projectiles.getValue()) {
                return ent;
            }
            if (skipEntity(ent)) continue;
            if (!(ent instanceof LivingEntity)) continue;
            first_stage.add((LivingEntity) ent);
        }

        return switch (sort.getValue()) {
            case Distance ->
                    first_stage.stream().min(Comparator.comparing(e -> (mc.player.squaredDistanceTo(e.getPos())))).orElse(null);
            case FOV -> first_stage.stream().min(Comparator.comparing(this::getFOVAngle)).orElse(null);
            case Health ->
                    first_stage.stream().min(Comparator.comparing(e -> (e.getHealth() + e.getAbsorptionAmount()))).orElse(null);
        };
    }

    private boolean skipEntity(Entity entity) {
        if (isBullet(entity)) return false;
        if (!(entity instanceof LivingEntity ent)) return true;
        if (ent.isDead() || !entity.isAlive()) return true;
        if (entity instanceof ArmorStandEntity) return true;
        if (entity instanceof CatEntity) return true;
        if (skipNotSelected(entity)) return true;

        if (entity instanceof PlayerEntity player) {
            if (ModuleManager.antiBot.isEnabled() && AntiBot.bots.contains(entity))
                return true;
            if (player == mc.player || ThunderHack.friendManager.isFriend(player))
                return true;
            if (player.isCreative() && ignoreCreative.getValue())
                return true;
            if (player.isInvisible() && ignoreInvisible.getValue())
                return true;
        }

        return !isInRange(entity);
    }

    private boolean isBullet(Entity entity) {
        return (entity instanceof ShulkerBulletEntity || entity instanceof FireballEntity)
                && entity.isAlive()
                && PlayerUtility.squaredDistanceFromEyes(entity.getPos()) < getSquaredRotateDistance()
                && Projectiles.getValue();
    }

    private boolean skipNotSelected(Entity entity) {
        if (entity instanceof SlimeEntity && !Slimes.getValue()) return true;
        if (entity instanceof HostileEntity he && !he.isAngryAt(mc.player) && onlyAngry.getValue()) return true;
        if (entity instanceof PlayerEntity && !Players.getValue()) return true;
        if (entity instanceof VillagerEntity && !Villagers.getValue()) return true;
        if (entity instanceof MobEntity && !Mobs.getValue()) return true;
        return entity instanceof AnimalEntity && !Animals.getValue();
    }

    private float getFOVAngle(@NotNull LivingEntity e) {
        double difX = e.getX() - mc.player.getX();
        double difZ = e.getZ() - mc.player.getZ();
        float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
        return Math.abs(yaw - MathHelper.wrapDegrees(mc.player.getYaw()));
    }

    public static Aura getInstance() {
        return instance;
    }

    public enum Rotation {
        Universal, SunRise, None
    }

    public enum RayTrace {
        OFF, OnlyTarget, AllEntities
    }

    public enum Sort {
        Distance, Health, FOV
    }

    public enum Switch {
        Normal, None, Silent
    }

    public enum RayTraceAngle {
        Calculated, Real
    }

    public enum Resolver {
        Off, Advantage, Predictive
    }

    public enum Mode {
        Interact, Track
    }

    public enum AttackHand {
        MainHand, OffHand, None
    }

    public enum ESP {
        Off, ThunderHack, NurikZapen, CelkaPasta
    }
}
