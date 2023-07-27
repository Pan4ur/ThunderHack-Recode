package thunder.hack.modules.combat;

import com.google.common.eventbus.Subscribe;
import thunder.hack.Thunderhack;
import thunder.hack.core.Core;
import thunder.hack.events.impl.*;
import thunder.hack.injection.accesors.ILivingEntity;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.modules.movement.Speed;
import thunder.hack.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.player.InventoryUtil;
import thunder.hack.utility.Util;
import thunder.hack.utility.interfaces.IOtherClientPlayerEntity;
import thunder.hack.utility.math.MathUtil;
import thunder.hack.utility.render.Render3DEngine;
import net.minecraft.block.*;
import net.minecraft.client.network.OtherClientPlayerEntity;
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
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.util.List;
import java.util.concurrent.*;

import static net.minecraft.util.UseAction.BLOCK;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class Aura extends Module {

    public Aura() {
        super("Aura", "Запомните блядь-киллка тх не мисает-а дает шанс убежать", Category.COMBAT);
    }


    public static final Setting<Float> attackRange = new Setting("Attack Range", 3.1f, 1f, 7.0f);
    public static final Setting<Mode> mode = new Setting("Rotation", Mode.Universal);
    public final Setting<Boolean> smartCrit = new Setting<>("Smart Crit", true);
    public final Setting<Boolean> ignoreWalls = new Setting<>("Ignore Walls", true);
    public final Setting<Boolean> wallsBypass = new Setting("WallsBypass", false, v-> ignoreWalls.getValue());
    public final Setting<Boolean> shieldBreaker = new Setting("Shield Breaker", false);
    public static final Setting<Boolean> oldDelay = new Setting("OldDelay", false);
    public static final Setting<Integer> minCPS = new Setting("MinCPS", 7, 1, 15, v-> oldDelay.getValue());
    public static final Setting<Integer> maxCPS = new Setting("MaxCPS", 12, 1, 15, v-> oldDelay.getValue());
    public final Setting<Boolean> grimAC = new Setting("GrimAC", false);
    public final Setting<Boolean> esp = new Setting("ESP", true);
    public final Setting<Parent> targets = new Setting<>("Targets", new Parent(false,0));
    public final Setting<Boolean> Players = new Setting<>("Players", true).withParent(targets);
    public final Setting<Boolean> Mobs = new Setting<>("Mobs", true).withParent(targets);
    public final Setting<Boolean> Animals = new Setting<>("Animals", true).withParent(targets);
    public final Setting<Boolean> Villagers = new Setting<>("Villagers", true).withParent(targets);
    public final Setting<Boolean> Slimes = new Setting<>("Slimes", true).withParent(targets);
    public final Setting<Boolean> ignoreInvisible = new Setting<>("IgnoreInvis", false).withParent(targets);
    public final Setting<Boolean> ignoreCreativ = new Setting<>("IgnoreCreative", true).withParent(targets);
    public final Setting<Boolean> ignoreShield = new Setting<>("IgnoreShield", true).withParent(targets);

    public enum Mode {
        Universal, None
    }

    public static Entity target;
    private float rotationYaw, rotationPitch, prevClientYaw;
    private float pitchAcceleration = 1f;
    public static float ppx,ppy,ppz,pmx,pmy,pmz;
    private int hitTicks;
    public static boolean lookingAtHitbox;

    @Subscribe
    public void modifyVelocity(EventPlayerTravel e){
        if(target != null && grimAC.getValue()){
            if(e.isPre()){
                prevClientYaw = mc.player.getYaw();
                mc.player.setYaw(rotationYaw);
            } else {
                mc.player.setYaw(prevClientYaw);
            }
        }
    }

    @Subscribe
    public void modifyJump(EventPlayerJump e){
        if(target != null && grimAC.getValue()){
            if(e.isPre()){
                prevClientYaw = mc.player.getYaw();
                mc.player.setYaw(rotationYaw);
            } else {
                mc.player.setYaw(prevClientYaw);
            }
        }
    }


    @Subscribe
    public void onAttack(PlayerUpdateEvent e){
        if(target != null && (((LivingEntity)target).getHealth() <= 0 || ((LivingEntity)target).isDead())){
            if(MainSettings.language.getValue() == MainSettings.Language.RU){
                Thunderhack.notificationManager.publicity("Aura","Цель успешно нейтрализована!",3, Notification.Type.SUCCESS);
            } else {
                Thunderhack.notificationManager.publicity("Aura","Target successfully neutralized!",3, Notification.Type.SUCCESS);
            }
        }
        for (PlayerEntity player : mc.world.getPlayers()) {if (player instanceof OtherClientPlayerEntity) ((IOtherClientPlayerEntity) player).resolve();}
        calcThread();
        for (PlayerEntity player : mc.world.getPlayers()) {if (player instanceof OtherClientPlayerEntity) ((IOtherClientPlayerEntity) player).releaseResolver();}
        if(target != null && autoCrit() && (lookingAtHitbox || mode.getValue() != Mode.Universal)){
            boolean blocking = mc.player.isUsingItem() && mc.player.getActiveItem().getItem().getUseAction(mc.player.getActiveItem()) == BLOCK;
            if (blocking) mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));

            int axe_slot = InventoryUtil.getBestAxe();
            int hotbar_axe_slot = InventoryUtil.findItem(AxeItem.class);

            boolean sprint = Core.serversprint;
            if(sprint) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));

            if(shieldBreaker.getValue() && target instanceof PlayerEntity && (((PlayerEntity) target).isUsingItem() && (((PlayerEntity) target).getOffHandStack().getItem() == Items.SHIELD || ((PlayerEntity) target).getMainHandStack().getItem() == Items.SHIELD) && (axe_slot != -1 || hotbar_axe_slot != -1))) {
                if (axe_slot != -1) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, axe_slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                    mc.interactionManager.attackEntity(mc.player, target);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, axe_slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                    notifySBreaker();
                } else if (hotbar_axe_slot != -1) {
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hotbar_axe_slot));
                    mc.interactionManager.attackEntity(mc.player, target);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                    notifySBreaker();
                }
            } else if (!(target instanceof PlayerEntity) || !(((PlayerEntity) target).isUsingItem() && ((PlayerEntity) target).getOffHandStack().getItem() == Items.SHIELD) || ignoreShield.getValue()) {
                Criticals.cancelCrit = true;
                if(Thunderhack.moduleManager.get(Criticals.class).isEnabled()) Thunderhack.moduleManager.get(Criticals.class).doCrit();
                mc.interactionManager.attackEntity(mc.player, target);
                Criticals.cancelCrit = false;
                mc.player.swingHand(Hand.MAIN_HAND);
                hitTicks = oldDelay.getValue() ? 1 + (int) (20f / MathUtil.random(minCPS.getValue(), maxCPS.getValue())) : 11;
            }
            if(sprint) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            if(blocking) mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, Util.getWorldActionId(Util.mc.world)));
        }
        hitTicks--;
    }


    @Subscribe
    public void onSync(EventSync e){
        rotate();
    }


    private void rotate(){
        if (target != null) {
            if (mode.getValue() != Mode.None) {
                mc.player.setYaw(rotationYaw);
                mc.player.setPitch(rotationPitch);
            }
        } else {
            rotationYaw = mc.player.getYaw();
            rotationPitch = mc.player.getPitch();
        }
    }

    public void notifySBreaker(){
        if(MainSettings.language.getValue() == MainSettings.Language.ENG) {
            Thunderhack.notificationManager.publicity("Aura", "Breaking " + target.getName().getString() +"'s shield", 2, Notification.Type.SUCCESS);
        } else {
            Thunderhack.notificationManager.publicity("Aura", "Ломаем щит игроку " + target.getName().getString(), 2, Notification.Type.SUCCESS);
        }
    }

    @Override
    public void onEnable(){
        target = null;
        lookingAtHitbox = false;
        ppx = ppy = ppz = pmx = pmz = pmy = 0;
        rotationYaw = mc.player.getYaw();
        rotationPitch = mc.player.getPitch();
    }

    private boolean autoCrit() {
        boolean reasonForSkipCrit = !smartCrit.getValue() || mc.player.getAbilities().flying || mc.player.isFallFlying() || mc.player.hasStatusEffect(StatusEffects.SLOWNESS) || mc.player.isHoldingOntoLadder() || (mc.world.getBlockState(new BlockPos((int) Math.floor(mc.player.getX()), (int) (Math.floor(mc.player.getY())), (int) Math.floor(mc.player.getZ()))).getBlock() == Blocks.COBWEB);

        if(hitTicks > 0) return false;

        if(mc.player.fallDistance > 1 && mc.player.fallDistance < 1.14) return false;

        if(!oldDelay.getValue()) {
            if (!(MathHelper.clamp(((float) ((ILivingEntity) mc.player).getLastAttackedTicks() + 0.5f) / getAttackCooldownProgressPerTick(), 0.0F, 1.0F) >= (mc.options.jumpKey.isPressed() ? 0.93f : 0.93f)))
                return false;
        } else {
            if(minCPS.getValue() > maxCPS.getValue()) minCPS.setValue(maxCPS.getValue());
        }

        if (!mc.options.jumpKey.isPressed() && (!Thunderhack.moduleManager.get(TargetStrafe.class).isEnabled() && !Thunderhack.moduleManager.get(Speed.class).isEnabled())) return true;

        if (mc.player.isInLava()) return true;

        if (!mc.options.jumpKey.isPressed() && isAboveWater()) return true;

        double d2 = (double)((int) mc.player.getY()) - mc.player.getY();
        if ((d2 == -0.01250004768371582 || d2 == -0.1875) && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, mc.player.getEyeHeight(mc.player.getPose()), 0.0)).iterator().hasNext() && !mc.player.isSneaking()) return true;
        if (!reasonForSkipCrit) return !mc.player.isOnGround() && mc.player.fallDistance > 0.0f;
        return true;
    }

    static boolean isAboveWater() {
        return mc.player.isSubmergedInWater() || mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos().add(0,-0.4,0))).getBlock() == Blocks.WATER;
    }

    static float getAttackCooldownProgressPerTick() {
        return (float)(1.0 / mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * (20.0 * Thunderhack.TICK_TIMER));
    }

    private void calcThread(){
        if(target == null) {
            findTarget();
            return;
        }

        if(skipEntity(target)){
            target = null;
            return;
        }

        switch (mode.getValue()) {
            case Universal: {
                Vec3d targetVec = getLegitLook(target);
                if (targetVec == null) {
                    return;
                }
                float delta_yaw = wrapDegrees((float) wrapDegrees(Math.toDegrees(Math.atan2(targetVec.z - mc.player.getZ(), (targetVec.x - mc.player.getX()))) - 90) - rotationYaw);
                float delta_pitch = ((float) (-Math.toDegrees(Math.atan2(targetVec.y - (mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose())), Math.sqrt(Math.pow((targetVec.x - mc.player.getX()), 2) + Math.pow(targetVec.z - mc.player.getZ(), 2))))) - rotationPitch);
                if(!lookingAtHitbox){
                    if(pitchAcceleration < 8f){
                        pitchAcceleration *= 1.65;
                    } else {
                        pitchAcceleration = 1f;
                    }
                    if(pitchAcceleration <= 0) {
                        pitchAcceleration = 1f;
                    }
                } else {
                    pitchAcceleration = 1f;
                }


                if (delta_yaw > 180) {
                    delta_yaw = delta_yaw - 180;
                }


                float deltaYaw = MathHelper.clamp(MathHelper.abs(delta_yaw), MathUtil.random(-75.0F, -85.0F), MathUtil.random(75.0F, 85.0F));

                float newYaw = rotationYaw + (delta_yaw > 0 ? deltaYaw : -deltaYaw);
                float pitch_speed = pitchAcceleration + MathUtil.random(-1f,1f);

                float newPitch = MathHelper.clamp(rotationPitch + (autoCrit() ? delta_pitch : MathHelper.clamp(delta_pitch, -pitch_speed, pitch_speed)), -90.0F, 90.0F);

                double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;

                rotationYaw = (float) (newYaw - (newYaw - rotationYaw) % gcdFix);
                rotationPitch = (float) (newPitch - (newPitch - rotationPitch) % gcdFix);

                lookingAtHitbox = Thunderhack.playerManager.checkRtx(rotationYaw, rotationPitch, attackRange.getValue(), ignoreWalls.getValue());
                break;
            }
        }
    }

    @Subscribe
    public void onRender3D(Render3DEvent e){
        if(target != null && esp.getValue()){
            Render3DEngine.drawTargetEsp(e,target);
        }
    }

    @Override
    public void onDisable(){
        target = null;
    }

    private float getRotateDistance(){
        float dst = attackRange.getValue();
        dst += 2f;
        if(mc.player.isFallFlying() && target != null) dst += 15f;
        return dst;
    }


    public Vec3d getLegitLook(Entity target){
        if(pmx == 0f && pmy == 0f && pmz == 0f){
            pmx = MathUtil.random(-0.05f,0.05f);
            pmy = MathUtil.random(-0.05f,0.05f);
            pmz = MathUtil.random(-0.05f,0.05f);
        }

        ppx += pmx;
        ppz += pmz;
        ppy += pmy;

        if(ppx >= (target.getBoundingBox().getXLength() - 0.05) / 2f) pmx = MathUtil.random(-0.003f,-0.03f);
        if(ppy >= target.getBoundingBox().getYLength()) pmy = MathUtil.random(-0.001f,-0.03f);
        if(ppz >= (target.getBoundingBox().getZLength()- 0.05) / 2f) pmz = MathUtil.random(-0.003f,-0.03f);


        if(ppx <= -(target.getBoundingBox().getXLength() - 0.05) / 2f) pmx = MathUtil.random(0.003f,0.03f);
        if(ppy <= 0.05) pmy = MathUtil.random(0.001f,0.03f);
        if(ppz <= -(target.getBoundingBox().getZLength() - 0.05) / 2f) pmz = MathUtil.random(0.003f,0.03f);

        ppx += MathUtil.random(-0.03f,0.03f);
        ppz += MathUtil.random(-0.03f,0.03f);

        if((!mc.player.canSee(target) && wallsBypass.getValue())) return target.getPos().add(MathUtil.random(-0.15,0.15),target.getBoundingBox().getYLength(),MathUtil.random(-0.15,0.15));

        if(!lookingAtHitbox && target instanceof PlayerEntity){
            float[] rotation1 = Thunderhack.playerManager.calcAngle(target.getPos().add(0,target.getEyeHeight(target.getPose()) / 2f,0));
            if (distanceFromHead(target.getPos().add(0,target.getEyeHeight(target.getPose()) / 2f,0)) <= attackRange.getPow2Value() && Thunderhack.playerManager.checkRtx(rotation1[0], rotation1[1], attackRange.getValue(), false)) {
                ppx =  0;
                ppy = target.getEyeHeight(target.getPose()) / 2f;
                ppz = 0;
            } else {
                for (float x1 = -0.25f; x1 < 0.25f; x1 += 0.05f) {
                    for (float z1 = -0.25f; z1 < 0.25f; z1 += 0.05f) {
                        for (float y1 = 0.05f; y1 < target.getEyeHeight(target.getPose()); y1 += 0.1f) {
                            Vec3d v1 = new Vec3d(target.getPos().getX() + x1, target.getPos().getY() + y1, target.getPos().getZ() + z1);
                            if (distanceFromHead(v1) > attackRange.getPow2Value()) continue;
                            float[] rotation = Thunderhack.playerManager.calcAngle(v1);
                            if (Thunderhack.playerManager.checkRtx(rotation[0], rotation[1], attackRange.getValue(), false)) {
                                ppx = x1;
                                ppy = y1;
                                ppz = z1;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return target.getPos().add(ppx,ppy,ppz);
    }

    public void findTarget(){
        List<Entity> first_stage = new CopyOnWriteArrayList<>();
        for(Entity entity : mc.world.getEntities()){
            if(skipEntity(entity)) continue;
            first_stage.add(entity);
        }

        float best_distance = 144;
        Entity best_entity = null;

        for(Entity ent : first_stage){
            float temp_dst = distanceFromHead(ent.getPos());
            if(temp_dst < best_distance){
                best_entity = ent;
                best_distance = temp_dst;
            }
        }
        target  = best_entity;
    }

    private boolean skipEntity(Entity entity){
        if(!(entity instanceof LivingEntity ent)) return true;
        if(ent.isDead()) return true;
        if(!entity.isAlive()) return true;
        if(entity instanceof ArmorStandEntity) return true;
        if(Thunderhack.moduleManager.get(AntiBot.class).isEnabled() && AntiBot.bots.contains(entity)) return true;
        if((entity instanceof SlimeEntity) && !Slimes.getValue()) return true;
        if((entity instanceof PlayerEntity) && !Players.getValue()) return true;
        if((entity instanceof VillagerEntity) && !Villagers.getValue()) return true;
        if((entity instanceof MobEntity) && !Mobs.getValue()) return true;
        if((entity instanceof AnimalEntity) && !Animals.getValue()) return true;
        if((entity instanceof PlayerEntity) && entity == mc.player) return true;
        if((entity instanceof PlayerEntity) && ((PlayerEntity) entity).isCreative() && ignoreCreativ.getValue()) return true;
        if((entity instanceof PlayerEntity) && entity.isInvisible() && ignoreInvisible.getValue()) return true;
        if((entity instanceof PlayerEntity) && Thunderhack.friendManager.isFriend((PlayerEntity) entity)) return true;
        return distanceFromHead(entity.getPos()) > getRotateDistance() * getRotateDistance();
    }

    private float distanceFromHead(Vec3d vec) {
        double d0 = vec.x - mc.player.getX();
        double d1 = vec.z - mc.player.getZ();
        double d2 = vec.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        return (float) (d0*d0 + d1*d1 + d2*d2);
    }
}
