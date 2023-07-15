package thunder.hack.injection;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.PushEvent;
import thunder.hack.modules.combat.HitBox;
import thunder.hack.modules.player.NoPitchLimit;
import thunder.hack.utility.interfaces.IEntity;
import thunder.hack.modules.render.Trails;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;

import java.util.ArrayList;
import java.util.List;

import static thunder.hack.utility.Util.mc;

@Mixin(Entity.class)
public abstract class MixinEntity implements IEntity {

    @Shadow public abstract Box getBoundingBox();

    @Override
    public List<Trails.Trail> getTrails(){
        return trails;
    }

    @Override
    public List<Vec3d> getPrevPositions(){
        return backPositions;
    }

    @ModifyArgs(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    private void pushAwayFromHook(Args args) {
        if ((Object) this == MinecraftClient.getInstance().player) {
            PushEvent event = new PushEvent(args.get(0), args.get(1), args.get(2));
            Thunderhack.EVENT_BUS.post(event);
            args.set(0, event.getPushX());
            args.set(1, event.getPushY());
            args.set(2, event.getPushZ());
        }
    }

    @Unique
    public List<Trails.Trail> trails = new ArrayList<>();

    @Unique
    public List<Vec3d> backPositions = new ArrayList<>();

    @Inject(method = {"changeLookDirection"}, at = {@At("HEAD")}, cancellable = true)
    public void changeLookDirectionHook(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
            if(Thunderhack.moduleManager.get(NoPitchLimit.class).isEnabled()){
                ci.cancel();
                changeLookDirectionCustom(cursorDeltaX,cursorDeltaY);
            }
    }



    @Inject(method = {"getBoundingBox"}, at = {@At("HEAD")}, cancellable = true)
    public final void getBoundingBox(CallbackInfoReturnable<Box> cir) {
       if(Thunderhack.moduleManager.get(HitBox.class).isEnabled() && ((Entity)(Object)this) != mc.player){
           cir.setReturnValue(new Box(getBoundingBox().minX - HitBox.XZExpand.getValue() / 2f,getBoundingBox().minY - HitBox.YExpand.getValue() / 2f,getBoundingBox().minZ - HitBox.XZExpand.getValue() / 2f,getBoundingBox().maxX + HitBox.XZExpand.getValue() / 2f,getBoundingBox().maxY + HitBox.YExpand.getValue() / 2f,getBoundingBox().maxZ + HitBox.XZExpand.getValue() / 2f));
       }
    }

    @Unique
    public void changeLookDirectionCustom(double cursorDeltaX, double cursorDeltaY) {
        float f = (float)cursorDeltaY * 0.15F;
        float g = (float)cursorDeltaX * 0.15F;
        ((Entity)(Object)this).setPitch(((Entity)(Object)this).getPitch() + f);
        ((Entity)(Object)this).setYaw(((Entity)(Object)this).getYaw() + g);
        ((Entity)(Object)this).prevPitch += f;
        ((Entity)(Object)this).prevYaw += g;
        if (((Entity)(Object)this).getVehicle() != null) {
            ((Entity)(Object)this).getVehicle().onPassengerLookAround(((Entity)(Object)this));
        }
    }
}