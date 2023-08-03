package thunder.hack.modules.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import thunder.hack.events.impl.*;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;
import thunder.hack.utility.player.MovementUtil;
import thunder.hack.utility.player.PlaceUtility;

import java.util.UUID;

public class FreeCam extends Module {

    public Setting<Float> speed = new Setting<>("Speed", 1f, 0.0f, 5.0f);

    public FreeCam() {
        super("Freecam", "Freecam", Category.PLAYER);
    }

    public static PlayerCopyEntity dummy;
    private Vec3d playerPos;
    private Vec2f playerRot;
    private Entity riding;

    private boolean prevFlying;
    private float prevFlySpeed;

    public Vec3d prevPos;
    public Vec2f prevRotate;


    @Override
    public void onEnable() {
        mc.chunkCullingEnabled = false;

        playerPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        playerRot = new Vec2f(mc.player.getYaw(), mc.player.getPitch());

        dummy = new PlayerCopyEntity();

        dummy.spawn();

        if (mc.player.getVehicle() != null) {
            riding = mc.player.getVehicle();
            mc.player.getVehicle().removeAllPassengers();
        }

        if (mc.player.isSprinting()) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        }

        prevFlying = mc.player.getAbilities().flying;
        prevFlySpeed = mc.player.getAbilities().getFlySpeed();
    }


    @Override
    public void onDisable() {
        if(fullNullCheck()){
            return;
        }
        if(dummy == null){
            return;
        }
            mc.chunkCullingEnabled = true;

            dummy.despawn();
            mc.player.noClip = false;
            mc.player.getAbilities().flying = prevFlying;
            mc.player.getAbilities().setFlySpeed(prevFlySpeed);

            mc.player.refreshPositionAndAngles(playerPos.getX(), playerPos.getY(), playerPos.getZ(), playerRot.x, playerRot.y);
            mc.player.setVelocity(Vec3d.ZERO);

            if (riding != null && mc.world.getEntityById(riding.getId()) != null) {
                mc.player.startRiding(riding);
            }

    }

    public static OtherClientPlayerEntity getPlayer(){
        return dummy;
    }

    @Subscribe
    public void onSync(EventSync event) {
        if(fullNullCheck()){
            disable();
            return;
        }
        if(dummy == null){
            disable();
            return;
        }
        HitResult result = mc.crosshairTarget;
        if (result != null )
        {
            if (result instanceof BlockHitResult && !mc.world.getBlockState(((BlockHitResult) result).getBlockPos()).isAir()) {
                float[] rotations = PlaceUtility.calculateAngle(getPlayer().getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0), result.getPos());
                getPlayer().setYaw(rotations[0]);
                getPlayer().setPitch(rotations[1]);
            } else if(!(result instanceof BlockHitResult)) {
                float[] rotations = PlaceUtility.calculateAngle(getPlayer().getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0), result.getPos());
                getPlayer().setYaw(rotations[0]);
                getPlayer().setPitch(rotations[1]);
            }
        }

        getPlayer().setStackInHand(Hand.MAIN_HAND, mc.player.getMainHandStack());
        getPlayer().setStackInHand(Hand.OFF_HAND, mc.player.getOffHandStack());
        prevPos = mc.player.getPos();
        prevRotate = new Vec2f(mc.player.getYaw(),mc.player.getPitch());
        mc.player.setPosition(getPlayer().getPos());
        mc.player.setYaw(getPlayer().getYaw());
        mc.player.setPitch(getPlayer().getPitch());
        mc.player.setOnGround(getPlayer().isOnGround());
    }


    @Subscribe
    public void onPostSync(EventPostSync event) {
        if(fullNullCheck()){
            disable();
            return;
        }
        if(prevPos == null || prevRotate == null) return;
        mc.player.setPosition(prevPos);
        mc.player.setYaw(prevRotate.x);
        mc.player.setPitch(prevRotate.y);
    }

    @Subscribe
    public void onMove(EventMove event) {
        if(fullNullCheck()){
            disable();
            return;
        }
        mc.player.noClip = true;
    }

    @Subscribe
    public void onTick(PlayerUpdateEvent event) {
        if(fullNullCheck()){
            disable();
            return;
        }
        mc.player.setOnGround(false);
        if(!MovementUtil.isMoving()){
            mc.player.setVelocity(Vec3d.ZERO);
        }
        mc.player.getAbilities().setFlySpeed((float) (speed.getValue() / 5));
        mc.player.getAbilities().flying = true;
        mc.player.setPose(EntityPose.STANDING);
    }


    public class PlayerCopyEntity extends OtherClientPlayerEntity {
        public PlayerCopyEntity() {
            super(mc.world, mc.player.getGameProfile());
            copyFrom(mc.player);
            getPlayerListEntry();
            dataTracker.set(PLAYER_MODEL_PARTS, mc.player.getDataTracker().get(PLAYER_MODEL_PARTS));
            setUuid(UUID.randomUUID());
        }

        public void spawn() {
            unsetRemoved();
            mc.world.addEntity(this.getId(), this);
        }

        public void despawn() {
            mc.world.removeEntity(this.getId(), RemovalReason.DISCARDED);
        }
    }
}
