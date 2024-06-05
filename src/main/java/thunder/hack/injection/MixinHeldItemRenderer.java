package thunder.hack.injection;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.EventHeldItemRenderer;
import thunder.hack.modules.Module;


@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {

    private double changeRotate(double value, double speed) {
        return value - speed <= 180 && value - speed > -180 ? value - speed : 180;
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"), cancellable = true)
    private void onRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if(Module.fullNullCheck()) return;
        EventHeldItemRenderer event = new EventHeldItemRenderer(hand, item, equipProgress, matrices);
        ThunderHack.EVENT_BUS.post(event);
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "HEAD"), cancellable = true)
    private void onRenderItemHook(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (ThunderHack.moduleManager != null && ModuleManager.animations.isEnabled() && !(item.isEmpty()) && !(item.getItem() instanceof FilledMapItem)) {
            ci.cancel();
            ModuleManager.animations.renderFirstPersonItemCustom(player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);
        }
    }
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    public void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack m, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        float mainRotX = ModuleManager.viewModel.rotationMainX.getValue();
        float mainPosX = ModuleManager.viewModel.positionMainX.getValue();
        float mainRotZ = ModuleManager.viewModel.rotationMainZ.getValue();
        float mainPosZ = ModuleManager.viewModel.positionMainZ.getValue();
        float mainRotY = ModuleManager.viewModel.rotationMainY.getValue();
        float mainPosY = ModuleManager.viewModel.positionMainY.getValue();

        float offRotX = ModuleManager.viewModel.rotationOffX.getValue();
        float offPosX = ModuleManager.viewModel.positionOffX.getValue();
        float offRotZ = ModuleManager.viewModel.rotationOffZ.getValue();
        float offPosZ = ModuleManager.viewModel.positionOffZ.getValue();
        float offRotY = ModuleManager.viewModel.rotationOffY.getValue();
        float offPosY = ModuleManager.viewModel.positionOffY.getValue();

        if (hand == Hand.MAIN_HAND) {
            if (ModuleManager.viewModel.animateMainX.getValue()) {
                mainRotX = (float) changeRotate(mainRotX, ModuleManager.viewModel.speedAnimateMain.getValue());
                ModuleManager.viewModel.rotationMainX.setValue(mainRotX);
            }
            if (ModuleManager.viewModel.animateMainY.getValue()) {
                mainRotY = (float) changeRotate(mainRotY, ModuleManager.viewModel.speedAnimateMain.getValue());
                ModuleManager.viewModel.rotationMainY.setValue(mainRotY);
            }
            if (ModuleManager.viewModel.animateMainZ.getValue()) {
                mainRotZ = (float) changeRotate(mainRotZ, ModuleManager.viewModel.speedAnimateMain.getValue());
                ModuleManager.viewModel.rotationMainZ.setValue(mainRotZ);
            }

            m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mainRotX));
            m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(mainRotY));
            m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(mainRotZ));
            m.translate(mainPosX, mainPosY, mainPosZ);
        } else {
            if (ModuleManager.viewModel.animateOffX.getValue()) {
                offRotX = (float) changeRotate(offRotX, ModuleManager.viewModel.speedAnimateOff.getValue());
                ModuleManager.viewModel.rotationOffX.setValue(offRotX);
            }
            if (ModuleManager.viewModel.animateOffY.getValue()) {
                offRotY = (float) changeRotate(offRotY, ModuleManager.viewModel.speedAnimateOff.getValue());
                ModuleManager.viewModel.rotationOffY.setValue(offRotY);
            }
            if (ModuleManager.viewModel.animateOffZ.getValue()) {
                offRotZ = (float) changeRotate(offRotZ, ModuleManager.viewModel.speedAnimateOff.getValue());
                ModuleManager.viewModel.rotationOffZ.setValue(offRotZ);
            }

            m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(offRotX));
            m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(offRotY));
            m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(offRotZ));
            m.translate(offPosX, offPosY, offPosZ);
        }
    }
}