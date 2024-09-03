package thunder.hack.injection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventFixVelocity;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.HitBox;
import thunder.hack.features.modules.render.Shaders;
import thunder.hack.features.modules.render.Trails;
import thunder.hack.utility.interfaces.IEntity;

import java.util.ArrayList;
import java.util.List;

import static thunder.hack.features.modules.Module.mc;

@Mixin(Entity.class)
public abstract class MixinEntity implements IEntity {

    @Shadow
    protected abstract BlockPos getVelocityAffectingPos();

    @Shadow
    private Box boundingBox;

    @Override
    public List<Trails.Trail> getTrails() {
        return trails;
    }

    @Override
    public BlockPos thunderHack_Recode$getVelocityBP() {
        return getVelocityAffectingPos();
    }

    @Unique
    public List<Trails.Trail> trails = new ArrayList<>();

    @ModifyArgs(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void pushAwayFromHook(Args args) {

        //Condition '...' is always 'false' is a lie!!! do not delete
        if ((Object) this == mc.player && ModuleManager.noPush.isEnabled() && ModuleManager.noPush.players.getValue()) {
            args.set(0, 0.);
            args.set(1, 0.);
            args.set(2, 0.);
        }
    }

    @Inject(method = "updateVelocity", at = {@At("HEAD")}, cancellable = true)
    public void updateVelocityHook(float speed, Vec3d movementInput, CallbackInfo ci) {
        if(Module.fullNullCheck()) return;
        if ((Object) this == mc.player) {
            ci.cancel();
            EventFixVelocity event = new EventFixVelocity(movementInput, speed, mc.player.getYaw(), movementInputToVelocityC(movementInput, speed, mc.player.getYaw()));
            ThunderHack.EVENT_BUS.post(event);
            mc.player.setVelocity(mc.player.getVelocity().add(event.getVelocity()));
        }
    }

    @Unique
    private static Vec3d movementInputToVelocityC(Vec3d movementInput, float speed, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3d.ZERO;
        }
        Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
        float f = MathHelper.sin(yaw * ((float) Math.PI / 180));
        float g = MathHelper.cos(yaw * ((float) Math.PI / 180));
        return new Vec3d(vec3d.x * (double) g - vec3d.z * (double) f, vec3d.y, vec3d.z * (double) g + vec3d.x * (double) f);
    }

    @Inject(method = "getBoundingBox", at = {@At("HEAD")}, cancellable = true)
    public final void getBoundingBox(CallbackInfoReturnable<Box> cir) {
        if (ModuleManager.hitBox.isEnabled() && mc != null && mc.player != null && ((Entity) (Object) this).getId() != mc.player.getId() && (ModuleManager.aura.isDisabled() || HitBox.affectToAura.getValue())) {
            cir.setReturnValue(new Box(this.boundingBox.minX - HitBox.XZExpand.getValue() / 2f, this.boundingBox.minY - HitBox.YExpand.getValue() / 2f, this.boundingBox.minZ - HitBox.XZExpand.getValue() / 2f, this.boundingBox.maxX + HitBox.XZExpand.getValue() / 2f, this.boundingBox.maxY + HitBox.YExpand.getValue() / 2f, this.boundingBox.maxZ + HitBox.XZExpand.getValue() / 2f));
        }
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    public void isGlowingHook(CallbackInfoReturnable<Boolean> cir) {
        Shaders shaders = ModuleManager.shaders;
        if (shaders.isEnabled()) {
            cir.setReturnValue(shaders.shouldRender((Entity) (Object) this));
        }
    }

    @Inject(method = "isOnFire", at = @At("HEAD"), cancellable = true)
    public void isOnFireHook(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.fireEntity.getValue()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    public void isInvisibleToHook(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.serverHelper.isEnabled() && ModuleManager.serverHelper.trueSight.getValue()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isInLava", at = @At("HEAD"), cancellable = true)
    public void isInLavaHook(CallbackInfoReturnable<Boolean> cir) {
        if((ModuleManager.jesus.isEnabled() || ModuleManager.noWaterCollision.isEnabled()) && mc.player != null && ((Entity) (Object) this).getId() == mc.player.getId())
            cir.setReturnValue(false);
    }

    @Inject(method = "isTouchingWater", at = @At("HEAD"), cancellable = true)
    public void isTouchingWaterHook(CallbackInfoReturnable<Boolean> cir) {
        if((ModuleManager.jesus.isEnabled() || ModuleManager.noWaterCollision.isEnabled()) && mc.player != null && ((Entity) (Object) this).getId() == mc.player.getId())
            cir.setReturnValue(false);
    }

    @Inject(method = "setSwimming", at = @At("HEAD"), cancellable = true)
    public void setSwimmingHook(boolean swimming, CallbackInfo ci) {
        if((ModuleManager.jesus.isEnabled() || ModuleManager.noWaterCollision.isEnabled()) && swimming && mc.player != null && ((Entity) (Object) this).getId() == mc.player.getId())
            ci.cancel();
    }

    @ModifyVariable(method = "changeLookDirection", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double changeLookDirectionHook0(double value) {
        if(ModuleManager.viewLock.isEnabled() && ModuleManager.viewLock.yaw.getValue())
            return 0d;
        return value;
    }

    @ModifyVariable(method = "changeLookDirection", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double changeLookDirectionHook1(double value) {
        if(ModuleManager.viewLock.isEnabled() && ModuleManager.viewLock.pitch.getValue())
            return 0d;
        return value;
    }
}