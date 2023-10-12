package dev.thunderhack.mixins;

import dev.thunderhack.event.events.EventEntityMoving;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.core.ModuleManager;
import dev.thunderhack.event.events.PushEvent;
import dev.thunderhack.modules.combat.HitBox;
import dev.thunderhack.modules.render.NoRender;
import dev.thunderhack.modules.render.Shaders;
import dev.thunderhack.utils.interfaces.IEntity;
import dev.thunderhack.modules.render.Trails;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;

import java.util.ArrayList;
import java.util.List;

import static dev.thunderhack.modules.Module.mc;

@Mixin(Entity.class)
public abstract class MixinEntity implements IEntity {
    @Shadow private Box boundingBox;

    @Override public List<Trails.Trail> getTrails() {
        return trails;
    }

    @Unique public List<Trails.Trail> trails = new ArrayList<>();


    @ModifyArgs(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    private void pushAwayFromHook(Args args) {
        if ((Object) this == MinecraftClient.getInstance().player) {
            PushEvent event = new PushEvent(args.get(0), args.get(1), args.get(2));
            ThunderHack.EVENT_BUS.post(event);
            args.set(0, event.getPushX());
            args.set(1, event.getPushY());
            args.set(2, event.getPushZ());
        }
    }

    @Inject(method = "changeLookDirection", at = {@At("HEAD")}, cancellable = true)
    public void changeLookDirectionHook(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (ModuleManager.noPitchLimit.isEnabled()) {
            ci.cancel();
            changeLookDirectionCustom(cursorDeltaX, cursorDeltaY);
        }
    }

    @Inject(method = "getBoundingBox", at = {@At("HEAD")}, cancellable = true)
    public final void getBoundingBox(CallbackInfoReturnable<Box> cir) {
        if (ModuleManager.hitBox.isEnabled() && (Object) this != mc.player) {
            cir.setReturnValue(new Box(this.boundingBox.minX - HitBox.XZExpand.getValue() / 2f, this.boundingBox.minY - HitBox.YExpand.getValue() / 2f, this.boundingBox.minZ - HitBox.XZExpand.getValue() / 2f, this.boundingBox.maxX + HitBox.XZExpand.getValue() / 2f, this.boundingBox.maxY + HitBox.YExpand.getValue() / 2f, this.boundingBox.maxZ + HitBox.XZExpand.getValue() / 2f));
        }
    }

    @Inject(method = "move", at = @At("HEAD"))
    private void onMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        ThunderHack.EVENT_BUS.post(new EventEntityMoving((Entity) (Object) this, movementType, movement));
    }

    @Unique
    public void changeLookDirectionCustom(double cursorDeltaX, double cursorDeltaY) {
        float f = (float) cursorDeltaY * 0.15F;
        float g = (float) cursorDeltaX * 0.15F;
        ((Entity) (Object) this).setPitch(((Entity) (Object) this).getPitch() + f);
        ((Entity) (Object) this).setYaw(((Entity) (Object) this).getYaw() + g);
        ((Entity) (Object) this).prevPitch += f;
        ((Entity) (Object) this).prevYaw += g;
        if (((Entity) (Object) this).getVehicle() != null) {
            ((Entity) (Object) this).getVehicle().onPassengerLookAround(((Entity) (Object) this));
        }
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    void isGlowingHook(CallbackInfoReturnable<Boolean> cir) {
        Shaders shaders = ModuleManager.shaders;
        if (shaders.isEnabled()) {
            cir.setReturnValue(shaders.shouldRender((Entity) (Object) this));
        }
    }

    @Inject(method = "isOnFire", at = @At("HEAD"), cancellable = true)
    void isOnFireHook(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.noRender.isEnabled() && NoRender.fireEntity.getValue()) {
            cir.setReturnValue(false);
        }
    }
}