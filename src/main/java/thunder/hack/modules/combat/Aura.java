package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
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
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.Core;
import thunder.hack.core.ModuleManager;
import thunder.hack.core.PlayerManager;
import thunder.hack.events.impl.*;
import thunder.hack.injection.accesors.ILivingEntity;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.interfaces.IOtherClientPlayerEntity;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.render.Render3DEngine;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.minecraft.util.UseAction.BLOCK;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class Aura extends Module {
    public static final Setting<Float> attackRange = new Setting<>("Attack Range", 3.1f, 1f, 7.0f);
    public static final Setting<Mode> mode = new Setting<>("Rotation", Mode.Universal);
    public static final Setting<RayTrace> rayTrace = new Setting<>("RayTrace", RayTrace.OnlyTarget);
    private final Setting<Boolean> onlyWeapon = new Setting<>("OnlyWeapon", false);
    public final Setting<Boolean> smartCrit = new Setting<>("SmartCrit", true);
    public final Setting<Boolean> ignoreWalls = new Setting<>("IgnoreWalls", true);
    public final Setting<Boolean> wallsBypass = new Setting<>("WallsBypass", false, v -> ignoreWalls.getValue());
    public final Setting<Boolean> shieldBreaker = new Setting<>("ShieldBreaker", true);
    public final Setting<Boolean> unpressShield = new Setting<>("UnpressShield", true);
    public final Setting<Boolean> dropSprint = new Setting<>("DropSprint", true);
    public final Setting<Boolean> pauseInInventory = new Setting<>("PauseInInventory", true);
    public static final Setting<Boolean> oldDelay = new Setting<>("OldDelay", false);
    public static final Setting<Integer> minCPS = new Setting<>("MinCPS", 7, 1, 15, v -> oldDelay.getValue());
    public static final Setting<Integer> maxCPS = new Setting<>("MaxCPS", 12, 1, 15, v -> oldDelay.getValue());
    public final Setting<Grim> grimAC = new Setting<>("GrimAC", Grim.None);
    public final Setting<Boolean> esp = new Setting<>("ESP", true);
    public static final Setting<Sort> sort = new Setting<>("Sort", Sort.Distance);
    public final Setting<Parent> targets = new Setting<>("Targets", new Parent(false, 0));
    public final Setting<Boolean> Players = new Setting<>("Players", true).withParent(targets);
    public final Setting<Boolean> Mobs = new Setting<>("Mobs", true).withParent(targets);
    public final Setting<Boolean> Animals = new Setting<>("Animals", true).withParent(targets);
    public final Setting<Boolean> Villagers = new Setting<>("Villagers", true).withParent(targets);
    public final Setting<Boolean> Slimes = new Setting<>("Slimes", true).withParent(targets);
    public final Setting<Boolean> Projectiles = new Setting<>("Projectiles", true).withParent(targets);
    public final Setting<Boolean> ignoreInvisible = new Setting<>("IgnoreInvis", false).withParent(targets);
    public final Setting<Boolean> ignoreCreativ = new Setting<>("IgnoreCreative", true).withParent(targets);
    public final Setting<Boolean> ignoreShield = new Setting<>("IgnoreShield", true).withParent(targets);

    public enum Mode {
        Universal, None
    }

    public enum RayTrace {
        OFF, OnlyTarget, AllEntities
    }

    public enum Sort {
        Distance, Health, FOV
    }

    public enum Grim {
        None, MoveFix, SilentTest
    }

    public static Entity target;
    private float rotationYaw, rotationPitch, prevClientYaw;
    private float pitchAcceleration = 1f;

    private Vec3d rotationPoint = Vec3d.ZERO;
    private Vec3d rotationMotion = Vec3d.ZERO;

    private int hitTicks;
    public static boolean lookingAtHitbox;
    public static boolean attackAllowed;

    public Aura() {
        super("Aura", "Запомните блядь-киллка тх не мисает-а дает шанс убежать", Category.COMBAT);
    }

    @EventHandler
    public void modifyVelocity(EventPlayerTravel e) {
        if (target != null && grimAC.getValue() == Grim.MoveFix) {
            if (e.isPre()) {
                prevClientYaw = mc.player.getYaw();
                mc.player.setYaw(rotationYaw);
            } else {
                mc.player.setYaw(prevClientYaw);
            }
        }
    }

    @EventHandler
    public void modifyJump(EventPlayerJump e) {
        if (target != null && grimAC.getValue() == Grim.MoveFix) {
            if (e.isPre()) {
                prevClientYaw = mc.player.getYaw();
                mc.player.setYaw(rotationYaw);
            } else {
                mc.player.setYaw(prevClientYaw);
            }
        }
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent e) {
        if (grimAC.getValue() != Grim.SilentTest) {
            auraLogic();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPostSync(EventPostSync e) {
        if (grimAC.getValue() == Grim.SilentTest) {
            auraLogic();
        }
    }

    public void auraLogic() {
        if (target != null && target instanceof LivingEntity && (((LivingEntity) target).getHealth() <= 0 || ((LivingEntity) target).isDead())) {
            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                ThunderHack.notificationManager.publicity("Aura", "Цель успешно нейтрализована!", 3, Notification.Type.SUCCESS);
            } else {
                ThunderHack.notificationManager.publicity("Aura", "Target successfully neutralized!", 3, Notification.Type.SUCCESS);
            }
        }

        for (PlayerEntity player : mc.world.getPlayers())
            if (player instanceof OtherClientPlayerEntity) ((IOtherClientPlayerEntity) player).resolve();
        
        calcThread();

        for (PlayerEntity player : mc.world.getPlayers())
            if (player instanceof OtherClientPlayerEntity) ((IOtherClientPlayerEntity) player).releaseResolver();


        boolean readyForAttack = autoCrit() && (lookingAtHitbox || mode.getValue() != Mode.Universal || rayTrace.getValue() == RayTrace.OFF);

        if (target != null && (readyForAttack || attackAllowed)) {
            if (shieldBreaker(false)) {
                hitTicks = 10;
                return;
            }
            attackAllowed = false;
            final Item selectedItem = mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot).getItem();
            if (onlyWeapon.getValue() && !(selectedItem instanceof SwordItem || selectedItem instanceof AxeItem))
                return;

            boolean blocking = mc.player.isUsingItem() && mc.player.getActiveItem().getItem().getUseAction(mc.player.getActiveItem()) == BLOCK;
            if (blocking && unpressShield.getValue())
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));

            boolean sprint = Core.serversprint;
            if (sprint && dropSprint.getValue())
                sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));

            if (!(target instanceof PlayerEntity) || !(((PlayerEntity) target).isUsingItem() && ((PlayerEntity) target).getOffHandStack().getItem() == Items.SHIELD) || ignoreShield.getValue()) {
                Criticals.cancelCrit = true;
                ModuleManager.criticals.doCrit();
                mc.interactionManager.attackEntity(mc.player, target);
                Criticals.cancelCrit = false;
                mc.player.swingHand(Hand.MAIN_HAND);
                hitTicks = oldDelay.getValue() ? 1 + (int) (20f / MathUtility.random(minCPS.getValue(), maxCPS.getValue())) : 11;
            }

            if (sprint && dropSprint.getValue())
                sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            if (blocking && unpressShield.getValue())
                sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, PlayerUtility.getWorldActionId(mc.world)));
        }
        hitTicks--;
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (target != null) {
            if (mode.getValue() != Mode.None) {
                if (grimAC.getValue() == Grim.SilentTest && target != null && autoCrit()) {
                    mc.player.setYaw(rotationYaw);
                    mc.player.setPitch(rotationPitch);
                    attackAllowed = true;
                } else if (grimAC.getValue() != Grim.SilentTest) {
                    mc.player.setYaw(rotationYaw);
                    mc.player.setPitch(rotationPitch);
                }
            }
        } else {
            rotationYaw = mc.player.getYaw();
            rotationPitch = mc.player.getPitch();
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.@NotNull Receive e) {
        if (e.getPacket() instanceof EntityStatusS2CPacket status) {
            if (status.getStatus() == 30 && status.getEntity(mc.world) != null && target != null && status.getEntity(mc.world) == target)
                ThunderHack.notificationManager.publicity("Aura", MainSettings.isRu() ? ("Успешно сломали щит игроку " + target.getName().getString()) : ("Succesfully destroyed " + target.getName().getString() + "'s shield"), 2, Notification.Type.SUCCESS);
        }
        /*
        if(e.getPacket() instanceof EntityTrackerUpdateS2CPacket attrib){
            for(DataTracker.SerializedEntry<?> a : attrib.trackedValues()){
                if(a.id() == 8 && a.value().equals((byte) 3)){
                    PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
                    attrib.write(packetBuf);
                    Entity ent = mc.world.getEntityById(packetBuf.readVarInt());
                    if(target != null && ent != null && ent.getId() == target.getId()) {
                        shieldBreaker(true);
                        hitTicks = 10;
                    }
                }
            }
        }
         */
    }

    @Override
    public void onEnable() {
        target = null;
        lookingAtHitbox = false;
        rotationPoint = Vec3d.ZERO;
        rotationMotion = Vec3d.ZERO;
        rotationYaw = mc.player.getYaw();
        rotationPitch = mc.player.getPitch();
        attackAllowed = false;
    }

    private boolean autoCrit() {
        boolean reasonForSkipCrit =
                !smartCrit.getValue()
                        || mc.player.getAbilities().flying
                        || mc.player.isFallFlying()
                        || mc.player.hasStatusEffect(StatusEffects.SLOWNESS)
                        || mc.player.isHoldingOntoLadder()
                        || (mc.world.getBlockState(new BlockPos((int) Math.floor(mc.player.getX()), (int) (Math.floor(mc.player.getY())), (int) Math.floor(mc.player.getZ()))).getBlock() == Blocks.COBWEB);

        if (hitTicks > 0) return false;

        // я хз почему оно не критует когда фд больше 1.14
        if (mc.player.fallDistance > 1 && mc.player.fallDistance < 1.14) return false;

        if (pauseInInventory.getValue() && ThunderHack.playerManager.inInventory) return false;

        if (oldDelay.getValue())
            if (minCPS.getValue() > maxCPS.getValue()) minCPS.setValue(maxCPS.getValue());
            else if (!(MathHelper.clamp(((float) ((ILivingEntity) mc.player).getLastAttackedTicks() + 0.5f) / getAttackCooldownProgressPerTick(), 0.0F, 1.0F) >= 0.93f))
                return false;


        boolean mergeWithTargetStrafe = !ModuleManager.targetStrafe.isEnabled() || !ModuleManager.targetStrafe.jump.getValue();
        boolean mergeWithSpeed = !ModuleManager.speed.isEnabled() || mc.player.isOnGround();

        if (!mc.options.jumpKey.isPressed() && mergeWithTargetStrafe && mergeWithSpeed)
            return true;

        if (mc.player.isInLava()) return true;

        if (!mc.options.jumpKey.isPressed() && isAboveWater()) return true;

        double fallDelta = (double) ((int) mc.player.getY()) - mc.player.getY();
        if ((fallDelta == -0.01250004768371582 || fallDelta == -0.1875) && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, mc.player.getEyeHeight(mc.player.getPose()), 0.0)).iterator().hasNext() && !mc.player.isSneaking())
            return true;
        if (!reasonForSkipCrit) return !mc.player.isOnGround() && mc.player.fallDistance > 0.0f;
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
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, axeSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        } else {
            sendPacket(new UpdateSelectedSlotC2SPacket(axeSlot));
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        }
        return true;
    }

    static boolean isAboveWater() {
        return mc.player.isSubmergedInWater() || mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos().add(0, -0.4, 0))).getBlock() == Blocks.WATER;
    }

    static float getAttackCooldownProgressPerTick() {
        return (float) (1.0 / mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * (20.0 * ThunderHack.TICK_TIMER));
    }

    private void calcThread() {

        Entity candidat = findTarget();

        if (target == null) {
            target = candidat;
            return;
        }

        if (sort.getValue() == Sort.FOV)
            target = candidat;

        if (candidat instanceof ProjectileEntity)
            target = candidat;


        if (skipEntity(target)) {
            target = null;
            return;
        }

        if (mode.getValue() == Mode.Universal) {
            Vec3d targetVec = getLegitLook(target);

            if (targetVec == null)
                return;

            float delta_yaw = wrapDegrees((float) wrapDegrees(Math.toDegrees(Math.atan2(targetVec.z - mc.player.getZ(), (targetVec.x - mc.player.getX()))) - 90) - rotationYaw);
            float delta_pitch = ((float) (-Math.toDegrees(Math.atan2(targetVec.y - (mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose())), Math.sqrt(Math.pow((targetVec.x - mc.player.getX()), 2) + Math.pow(targetVec.z - mc.player.getZ(), 2))))) - rotationPitch);

            pitchAcceleration = lookingAtHitbox ? 1f : pitchAcceleration < 8f ? pitchAcceleration * 1.65f : 1f;

            float yawStep = grimAC.getValue() == Grim.SilentTest ? 360f : MathUtility.random(65f, 75f);
            float pitchStep = grimAC.getValue() == Grim.SilentTest ? 180f : pitchAcceleration + MathUtility.random(-1f, 1f);

            if (delta_yaw > 180) {
                delta_yaw = delta_yaw - 180;
            }

            float deltaYaw = MathHelper.clamp(MathHelper.abs(delta_yaw), -yawStep, yawStep);
            float deltaPitch = MathHelper.clamp(delta_pitch, -pitchStep, pitchStep);

            float newYaw = rotationYaw + (delta_yaw > 0 ? deltaYaw : -deltaYaw);
            float newPitch = MathHelper.clamp(rotationPitch + deltaPitch, -90.0F, 90.0F);

            double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;

            rotationYaw = (float) (newYaw - (newYaw - rotationYaw) % gcdFix);
            rotationPitch = (float) (newPitch - (newPitch - rotationPitch) % gcdFix);
            lookingAtHitbox = ThunderHack.playerManager.checkRtx(rotationYaw, rotationPitch, attackRange.getValue(), ignoreWalls.getValue(), rayTrace.getValue());
        }
    }


    public void onRender3D(MatrixStack stack) {
        if (target != null && esp.getValue()) {
            Render3DEngine.drawTargetEsp(stack, target);
        }
    }

    @Override
    public void onDisable() {
        target = null;
    }

    private float getRotateDistance() {
        float dst = attackRange.getValue();
        dst += 2f;
        if (mc.player.isFallFlying() && target != null) dst += 15f;
        if (grimAC.getValue() == Grim.SilentTest) dst = attackRange.getValue();
        return dst;
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


        // Задаем начальную скорость точки
        if (rotationMotion.equals(Vec3d.ZERO))
            rotationMotion = new Vec3d(MathUtility.random(-0.05f, 0.05f), MathUtility.random(-0.05f, 0.05f), MathUtility.random(-0.05f, 0.05f));


        rotationPoint = rotationPoint.add(rotationMotion);

        // Сталкиваемся с хитбоксом по X
        if (rotationPoint.x >= (target.getBoundingBox().getXLength() - 0.05) / 2f)
            rotationMotion = new Vec3d(-MathUtility.random(minMotionXZ, maxMotionXZ), rotationMotion.getY(), rotationMotion.getZ());


        // Сталкиваемся с хитбоксом по Y
        if (rotationPoint.y >= target.getBoundingBox().getYLength())
            rotationMotion = new Vec3d(rotationMotion.getX(), -MathUtility.random(minMotionY, maxMotionY), rotationMotion.getZ());


        // Сталкиваемся с хитбоксом по Z
        if (rotationPoint.z >= (target.getBoundingBox().getZLength() - 0.05) / 2f)
            rotationMotion = new Vec3d(rotationMotion.getX(), rotationMotion.getY(), -MathUtility.random(minMotionXZ, maxMotionXZ));


        // Сталкиваемся с хитбоксом по -X
        if (rotationPoint.x <= -(target.getBoundingBox().getXLength() - 0.05) / 2f)
            rotationMotion = new Vec3d(MathUtility.random(minMotionXZ, 0.03f), rotationMotion.getY(), rotationMotion.getZ());


        // Сталкиваемся с хитбоксом по -Y
        if (rotationPoint.y <= 0.05)
            rotationMotion = new Vec3d(rotationMotion.getX(), MathUtility.random(minMotionY, maxMotionY), rotationMotion.getZ());


        // Сталкиваемся с хитбоксом по -Z
        if (rotationPoint.z <= -(target.getBoundingBox().getZLength() - 0.05) / 2f)
            rotationMotion = new Vec3d(rotationMotion.getX(), rotationMotion.getY(), MathUtility.random(minMotionXZ, maxMotionXZ));


        // Добавляем джиттер
        rotationPoint.add(MathUtility.random(-0.03f, 0.03f), 0f, MathUtility.random(-0.03f, 0.03f));

        // Если мы используем обход ударов через стену и наша цель за стеной, то целимся в верхушку хитбокса т.к. матриксу поебать
        if (!mc.player.canSee(target) && wallsBypass.getValue())
            return target.getPos().add(MathUtility.random(-0.15, 0.15), target.getBoundingBox().getYLength(), MathUtility.random(-0.15, 0.15));

        // Если мы перестали смотреть на цель
        if (!lookingAtHitbox) {

            float[] rotation1 = PlayerManager.calcAngle(target.getPos().add(0, target.getEyeHeight(target.getPose()) / 2f, 0));

            // Проверяем видимость центра игрока
            if (distanceFromHead(target.getPos().add(0, target.getEyeHeight(target.getPose()) / 2f, 0)) <= attackRange.getPow2Value()
                    && ThunderHack.playerManager.checkRtx(rotation1[0], rotation1[1], attackRange.getValue(), false, rayTrace.getValue())) {
                // наводим на центр
                rotationPoint = new Vec3d(MathUtility.random(-0.1f, 0.1f), target.getEyeHeight(target.getPose()) / (MathUtility.random(1.8f, 2.5f)), MathUtility.random(-0.1f, 0.1f));
            } else {
                // Сканим хитбокс на видимую точку
                float halfBox = (float) (target.getBoundingBox().getXLength() / 2.3f);

                for (float x1 = -halfBox; x1 < halfBox; x1 += 0.05f) {
                    for (float z1 = -halfBox; z1 < halfBox; z1 += 0.05f) {
                        for (float y1 = 0.05f; y1 < target.getEyeHeight(target.getPose()); y1 += 0.1f) {

                            Vec3d v1 = new Vec3d(target.getPos().getX() + x1, target.getPos().getY() + y1, target.getPos().getZ() + z1);

                            // Скипаем, если вне досягаемости
                            if (distanceFromHead(v1) > attackRange.getPow2Value()) continue;

                            float[] rotation = PlayerManager.calcAngle(v1);
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

    public Entity findTarget() {
        List<LivingEntity> first_stage = new CopyOnWriteArrayList<>();
        for (Entity ent : mc.world.getEntities()) {
            if ((ent instanceof ShulkerBulletEntity || ent instanceof FireballEntity)
                    && ent.isAlive()
                    && distanceFromHead(ent.getPos()) < getRotateDistance() * getRotateDistance()
                    && Projectiles.getValue()) {
                return ent;
            }
            if (skipEntity(ent)) continue;
            if(!(ent instanceof LivingEntity)) continue;
            first_stage.add((LivingEntity) ent);
        }

        switch (sort.getValue()) {
            case Distance -> {
                return first_stage.stream().min(Comparator.comparing(e -> (mc.player.squaredDistanceTo(e.getPos())))).orElse(null);
            }
            case FOV -> {
                return first_stage.stream().min(Comparator.comparing(e -> (getFOVAngle(e)))).orElse(null);
            }
            case Health -> {
                return first_stage.stream().min(Comparator.comparing(e -> (e.getHealth() + e.getAbsorptionAmount()))).orElse(null);
            }
        }
        return null;
    }

    private boolean skipEntity(Entity entity) {
        if ((entity instanceof ShulkerBulletEntity || entity instanceof FireballEntity)
                && entity.isAlive()
                && distanceFromHead(entity.getPos()) < getRotateDistance() * getRotateDistance()
                && Projectiles.getValue())
            return false;
        if (!(entity instanceof LivingEntity ent)) return true;
        if (ent.isDead()) return true;
        if (!entity.isAlive()) return true;
        if (entity instanceof ArmorStandEntity) return true;
        if (ModuleManager.antiBot.isEnabled() && AntiBot.bots.contains(entity)) return true;
        if ((entity instanceof SlimeEntity) && !Slimes.getValue()) return true;
        if ((entity instanceof PlayerEntity) && !Players.getValue()) return true;
        if ((entity instanceof VillagerEntity) && !Villagers.getValue()) return true;
        if ((entity instanceof MobEntity) && !Mobs.getValue()) return true;
        if ((entity instanceof AnimalEntity) && !Animals.getValue()) return true;
        if ((entity instanceof PlayerEntity) && entity == mc.player) return true;
        if ((entity instanceof PlayerEntity) && ((PlayerEntity) entity).isCreative() && ignoreCreativ.getValue())
            return true;
        if ((entity instanceof PlayerEntity) && entity.isInvisible() && ignoreInvisible.getValue()) return true;
        if ((entity instanceof PlayerEntity) && ThunderHack.friendManager.isFriend((PlayerEntity) entity)) return true;
        return distanceFromHead(entity.getPos()) > getRotateDistance() * getRotateDistance();
    }

    private float getFOVAngle(@NotNull LivingEntity e) {
        double difX = e.getX() - mc.player.getPos().x;
        double difZ = e.getZ() - mc.player.getPos().z;
        float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
        double plYaw = MathHelper.wrapDegrees(mc.player.getYaw());
        return (float) Math.abs(yaw - plYaw);
    }

    private float distanceFromHead(@NotNull Vec3d vec) {
        double d0 = vec.x - mc.player.getX();
        double d1 = vec.z - mc.player.getZ();
        double d2 = vec.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        return (float) (d0 * d0 + d1 * d1 + d2 * d2);
    }
}
