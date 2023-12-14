package thunder.hack.injection;

import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.EventEntityMoving;
import thunder.hack.modules.combat.HitBox;
import thunder.hack.modules.render.NoRender;
import thunder.hack.modules.render.Shaders;
import thunder.hack.utility.interfaces.IEntity;
import thunder.hack.modules.render.Trails;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;

import java.util.ArrayList;
import java.util.List;

import static thunder.hack.modules.Module.mc;

@Mixin(Entity.class)
public abstract class MixinEntity implements IEntity {

    @Shadow
    protected abstract BlockPos getVelocityAffectingPos();

    @Shadow
    private Box boundingBox;

    @Override
    public List<Trails.Trail> thunderHack_Recode$getTrails() {
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
        if ((Object) this == MinecraftClient.getInstance().player && ModuleManager.velocity.isEnabled() && ModuleManager.velocity.players.getValue()) {
            args.set(0, 0.);
            args.set(1, 0.);
            args.set(2, 0.);
        }
    }

    @Inject(method = "getBoundingBox", at = {@At("HEAD")}, cancellable = true)
    public final void getBoundingBox(CallbackInfoReturnable<Box> cir) {
        if (ModuleManager.hitBox.isEnabled() && mc != null && mc.player != null && ((Entity) (Object) this).getId() != mc.player.getId() && (ModuleManager.aura.isDisabled() || HitBox.affectToAura.getValue())) {
            cir.setReturnValue(new Box(this.boundingBox.minX - HitBox.XZExpand.getValue() / 2f, this.boundingBox.minY - HitBox.YExpand.getValue() / 2f, this.boundingBox.minZ - HitBox.XZExpand.getValue() / 2f, this.boundingBox.maxX + HitBox.XZExpand.getValue() / 2f, this.boundingBox.maxY + HitBox.YExpand.getValue() / 2f, this.boundingBox.maxZ + HitBox.XZExpand.getValue() / 2f));
        }
    }

    @Inject(method = "move", at = @At("HEAD"))
    public void onMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        ThunderHack.EVENT_BUS.post(new EventEntityMoving((Entity) (Object) this, movementType, movement));
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    public void isGlowingHook(CallbackInfoReturnable<Boolean> cir) {
        Shaders shaders = ModuleManager.shaders;
        if (shaders.isEnabled()) {
          //  cir.setReturnValue(shaders.shouldRender((Entity) (Object) this));
        }
    }

    @Inject(method = "isOnFire", at = @At("HEAD"), cancellable = true)
    public void isOnFireHook(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.noRender.isEnabled() && NoRender.fireEntity.getValue()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    public void isInvisibleToHook(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.fTHelper.isEnabled() && ModuleManager.fTHelper.trueSight.getValue()) {
            cir.setReturnValue(false);
        }
    }
}