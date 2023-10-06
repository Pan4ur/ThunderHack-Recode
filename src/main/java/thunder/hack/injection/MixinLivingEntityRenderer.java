package thunder.hack.injection;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.HorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.ThunderHack;
import thunder.hack.core.ModuleManager;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

import static thunder.hack.modules.Module.mc;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> {
    private LivingEntity lastEntity;

    private float originalYaw;
    private float originalHeadYaw;
    private float originalBodyYaw;
    private float originalPitch;

    private float originalPrevYaw;
    private float originalPrevHeadYaw;
    private float originalPrevBodyYaw;

    @Inject(method = "render", at = @At("HEAD"))
    public void onRenderPre(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player && MainSettings.renderRotations.getValue()) {
            originalYaw = livingEntity.getYaw();
            originalHeadYaw = livingEntity.headYaw;
            originalBodyYaw = livingEntity.bodyYaw;
            originalPitch = livingEntity.getPitch();
            originalPrevYaw = livingEntity.prevYaw;
            originalPrevHeadYaw = livingEntity.prevHeadYaw;
            originalPrevBodyYaw = livingEntity.prevBodyYaw;
            livingEntity.setYaw(((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw());
            livingEntity.headYaw = ((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw();
            livingEntity.bodyYaw = ((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw();
            livingEntity.setPitch(((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastPitch());
            livingEntity.prevYaw = ThunderHack.playerManager.lastYaw;
            livingEntity.prevHeadYaw = ThunderHack.playerManager.lastYaw;
            livingEntity.prevBodyYaw = ThunderHack.playerManager.lastYaw;
            livingEntity.prevPitch = ThunderHack.playerManager.lastPitch;
        }
        lastEntity = livingEntity;
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRenderPost(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player && MainSettings.renderRotations.getValue()) {
            livingEntity.setYaw(originalYaw);
            livingEntity.headYaw = originalHeadYaw;
            livingEntity.bodyYaw = originalBodyYaw;
            livingEntity.setPitch(originalPitch);
            livingEntity.prevYaw = originalPrevYaw;
            livingEntity.prevHeadYaw = originalPrevHeadYaw;
            livingEntity.prevBodyYaw = originalPrevBodyYaw;
            livingEntity.prevPitch = originalPitch;
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void onRenderModel(EntityModel entityModel, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        Color newColor = new Color(red, green, blue, alpha);
        if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.antiPlayerCollision.getValue() && lastEntity != mc.player) {
            float overrideAlpha = (float) (mc.player.squaredDistanceTo(lastEntity.getPos()) / 3f) + 0.2f;
            newColor = Render2DEngine.injectAlpha(newColor, (int) (255f * MathUtility.clamp(overrideAlpha, 0f, 1f)));
        }
        entityModel.render(matrices, vertices, light, overlay, newColor.getRed() / 255F, newColor.getGreen() / 255F, newColor.getBlue() / 255F, newColor.getAlpha() / 255F);
    }
}