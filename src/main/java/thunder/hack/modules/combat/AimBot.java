package thunder.hack.modules.combat;

import com.google.common.eventbus.Subscribe;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.events.impl.Render3DEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Util;
import thunder.hack.utility.math.MathUtil;
import thunder.hack.utility.render.Render3DEngine;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class AimBot extends Module {

    public AimBot() {
        super("AimBot", "RustMeAim", Category.COMBAT);
    }

    public final Setting<Float> aimRange = new Setting("Range", 20f, 1f, 30f);
    private final Setting<Mode> mode = new Setting("Rotation", Mode.Silent);
    public final Setting<Boolean> ignoreWalls = new Setting<>("Ignore Walls", true);
    public final Setting<Boolean> ignoreInvisible = new Setting<>("IgnoreInvis", false);
    public Setting<Float> rotYawRandom = new Setting<>("Yaw Random", 0f, 0f, 3f);
    public Setting<Float> rotPitchRandom = new Setting<>("Pitch Random", 0f, 0f, 3f);
    public Setting<Float> predict = new Setting<>("Aim Predict", 0.5f, 0.5f, 2f);
    public Setting<Integer> delay = new Setting<>("Shoot delay", 5, 0, 10);
    public Setting<Integer> fov = new Setting<>("FOV", 65, 10, 360);

    private final Setting<Part> part = new Setting<>("Part Mode", Part.Chest);

    private enum Part {
        Chest, Head,  Neck, Leggings, Boots
    }

    private enum Mode {
        Client, Silent
    }


    private final AtomicInteger threadCounter = new AtomicInteger(0);
    private final ExecutorService asyncRunning = Executors.newCachedThreadPool(task -> new Thread(task, "Aura Thread " + threadCounter.getAndIncrement()){});

    public static Entity target;
    private float rotationYaw,rotationPitch;
    public static float ppx,ppy,ppz,pmx,pmy,pmz;
    private Box debug_box;


    @Subscribe
    public void onCalc(PlayerUpdateEvent e){
        if (fullNullCheck())
            return;
        SyncedEntityList = mc.world.getEntities();
        asyncRunning.execute(this::calcThread);
        if(target != null && mc.player.canSee(target) ){
            if(mc.player.age % delay.getValue() == 0)
                mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, Util.getWorldActionId(Util.mc.world)));
        }
    }

    @Subscribe
    public void onSync(EventSync e){
        if (target != null) {
            if(mode.getValue() == Mode.Silent) {
                mc.player.setYaw(rotationYaw);
                mc.player.setPitch(rotationPitch);
            }
        } else {
            rotationYaw = mc.player.getYaw();
            rotationPitch = mc.player.getPitch();
        }
    }



    @Override
    public void onEnable(){
        target = null;
        debug_box = null;
        ppx = ppy = ppz = pmx = pmz = pmy = 0;
        rotationYaw = mc.player.getYaw();
        rotationPitch = mc.player.getPitch();
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

        Vec3d targetVec = getMatrix4Vec(target, aimRange.getValue());
        if (targetVec == null) return;

        float delta_yaw = wrapDegrees((float) wrapDegrees(Math.toDegrees(Math.atan2(targetVec.z - mc.player.getZ(), (targetVec.x - mc.player.getX()))) - 90) - rotationYaw);
        float delta_pitch = ((float) (-Math.toDegrees(Math.atan2(targetVec.y - (mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose())), Math.sqrt(Math.pow((targetVec.x - mc.player.getX()), 2) + Math.pow(targetVec.z - mc.player.getZ(), 2))))) - rotationPitch);

        if (delta_yaw > 180) {
            delta_yaw = delta_yaw - 180;
        }

        float deltaYaw = MathHelper.clamp(MathHelper.abs(delta_yaw), MathUtil.random(-40.0F, -60.0F), MathUtil.random(40.0F, 60.0F));

        float newYaw = rotationYaw + (delta_yaw > 0 ? deltaYaw : -deltaYaw) + MathUtil.random(-rotYawRandom.getValue(), rotYawRandom.getValue());
        float newPitch = MathHelper.clamp(rotationPitch + MathHelper.clamp(delta_pitch, MathUtil.random(-10.0F, -20.0F), MathUtil.random(10,20)), -90.0F, 90.0F) + MathUtil.random(-rotPitchRandom.getValue(), rotPitchRandom.getValue());

        double gcdFix1 = mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
        double gcdFix2 = Math.pow(gcdFix1, 3.0) * 8.0;
        double gcdFix = gcdFix2 * 0.15000000596046448;

        rotationYaw = (float) (newYaw - (newYaw - rotationYaw) % gcdFix);
        rotationPitch = (float) (newPitch - (newPitch - rotationPitch) % gcdFix);
    }

    @Subscribe
    public void onRender3D(Render3DEvent e){
        if(debug_box != null){
            Render3DEngine.drawFilledBox(e.getMatrixStack(),debug_box, new Color(0x3400FF41, true));
        }
        if (target != null && mc.player.canSee(target)) {
            if(mode.getValue() == Mode.Client) {
                mc.player.setYaw(rotationYaw);
                mc.player.setPitch(rotationPitch);
            }
        } else {
            rotationYaw = mc.player.getYaw();
            rotationPitch = mc.player.getPitch();
        }
    }

    public Vec3d getMatrix4Vec(Entity target, double distance) {
        Vec3d cuteTargetPos = getResolvedPos(target);
        float aimPoint = switch (part.getValue()) {
            case Head -> 0.05f;
            case Neck -> 0.3f;
            case Chest -> 0.5f;
            case Leggings -> 0.9f;
            case Boots -> 1.3f;
        };

        aimPoint = 1.6f - aimPoint;

        Vec3d v1 = new Vec3d(cuteTargetPos.getX(),cuteTargetPos.getY() + aimPoint,cuteTargetPos.getZ());
        float[] rotation = calcAngle(v1);

        if (checkRtx(target,rotation[0],rotation[1],distance,ignoreWalls.getValue())) {
            return v1;
        }
        return null;
    }

    public void findTarget(){
        List<Entity> first_stage = new CopyOnWriteArrayList<>();
        for(Entity entity : getEntities()){
            if(skipEntity(entity)) continue;
            first_stage.add(entity);
        }

        float best_distance = 144;
        Entity best_entity = null;

        for(Entity ent : first_stage){
            float temp_dst = (float) Math.sqrt(mc.player.squaredDistanceTo(getResolvedPos(ent)));
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
        if(!(entity instanceof PlayerEntity)) return true;
        if(entity == mc.player) return true;
        if(entity.isInvisible() && ignoreInvisible.getValue()) return true;
        if(Thunderhack.friendManager.isFriend((PlayerEntity) entity)) return true;
       // if(Math.abs(getYawToEntityNew(entity)) > fov.getValue()) return true;
        return mc.player.squaredDistanceTo(getResolvedPos(entity)) > aimRange.getPow2Value();
    }

    public float getYawToEntityNew(Entity entity) {
        return getYawBetween(rotationYaw, mc.player.getX(), mc.player.getZ(), entity.getX(), entity.getZ());
    }

    public  float getYawBetween(float yaw, double srcX, double srcZ, double destX, double destZ) {
        double xDist = destX - srcX;
        double zDist = destZ - srcZ;
        float yaw1 = (float) (StrictMath.atan2(zDist, xDist) * 180.0 / 3.141592653589793) - 90.0f;
        return yaw + MathHelper.wrapDegrees(yaw1 - yaw);
    }

    private Vec3d getResolvedPos(Entity pl){
        return new Vec3d(pl.getX() + pl.getVelocity().x * predict.getValue(), pl.getY(),pl.getZ() +  pl.getVelocity().z * predict.getValue());
    }

    public boolean checkRtx(Entity target, float yaw, float pitch, double distance, boolean ignoreWalls) {
        return mc.player.canSee(target);
    }
    public static float[] calcAngle(Vec3d to) {
        if (to == null) return null;
        double difX = to.x - mc.player.getEyePos().x;
        double difY = (to.y - mc.player.getEyePos().y) * -1.0;
        double difZ = to.z - mc.player.getEyePos().z;
        double dist = MathHelper.sqrt((float) (difX * difX + difZ * difZ));
        return new float[]{(float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)))};
    }

    private Vec3d getRotationVector(float yaw, float pitch){
        return new Vec3d(MathHelper.sin(-pitch * 0.017453292F) * MathHelper.cos(yaw * 0.017453292F), -MathHelper.sin(yaw * 0.017453292F), MathHelper.cos(-pitch * 0.017453292F) * MathHelper.cos(yaw * 0.017453292F));
    }


    private volatile Iterable<Entity> SyncedEntityList = Collections.emptyList();

    public Iterable<Entity> getEntities() {
        return SyncedEntityList;
    }
}
