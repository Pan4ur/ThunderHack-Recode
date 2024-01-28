package thunder.hack.injection;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

import static net.minecraft.util.math.MathHelper.clamp;
import static thunder.hack.modules.Module.mc;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> {
    private LivingEntity lastEntity;

    private float originalHeadYaw, originalPrevHeadYaw, originalPrevHeadPitch, originalHeadPitch;




    @Inject(method = "render", at = @At("HEAD"))
    public void onRenderPre(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player && MainSettings.renderRotations.getValue() && !ThunderHack.isFuturePresent()) {
            originalHeadYaw = livingEntity.headYaw;
            originalPrevHeadYaw = livingEntity.prevHeadYaw;
            originalPrevHeadPitch = livingEntity.prevPitch;
            originalHeadPitch = livingEntity.getPitch();

            livingEntity.setPitch(((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastPitch());
            livingEntity.prevPitch =ThunderHack.playerManager.lastPitch;
            livingEntity.headYaw = ((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw();
            livingEntity.bodyYaw = Render2DEngine.interpolateFloat(ThunderHack.playerManager.prevBodyYaw, ThunderHack.playerManager.bodyYaw, mc.getTickDelta());
            livingEntity.prevHeadYaw = ThunderHack.playerManager.lastYaw;
            livingEntity.prevBodyYaw = Render2DEngine.interpolateFloat(ThunderHack.playerManager.prevBodyYaw, ThunderHack.playerManager.bodyYaw, mc.getTickDelta());
        }
        lastEntity = livingEntity;
    }


    @Inject(method = "render", at = @At("TAIL"))
    public void onRenderPost(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player && MainSettings.renderRotations.getValue() && !ThunderHack.isFuturePresent()) {
            livingEntity.prevPitch = originalPrevHeadPitch;
            livingEntity.setPitch(originalHeadPitch);
            livingEntity.headYaw = originalHeadYaw;
            livingEntity.prevHeadYaw = originalPrevHeadYaw;
        }
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void renderHook(Args args) {
        if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.antiPlayerCollision.getValue() && lastEntity != mc.player && lastEntity instanceof PlayerEntity pl && !pl.isInvisible())
            args.set(7, MathUtility.clamp((float) (mc.player.squaredDistanceTo(lastEntity.getPos()) / 3f) + 0.2f, 0f, 1f));
        if(lastEntity != mc.player && lastEntity instanceof PlayerEntity pl && pl.isInvisible() && ModuleManager.fTHelper.isEnabled() && ModuleManager.fTHelper.trueSight.getValue())
            args.set(7, 0.3f);
    }
}