package thunder.hack.injection;

import net.minecraft.entity.decoration.ArmorStandEntity;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.modules.render.Fullbright;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity> {
    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void renderLabelIfPresent(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo info) {
        if(entity instanceof ArmorStandEntity && ModuleManager.noRender.isEnabled() && ModuleManager.noRender.noArmorStands.getValue())
            info.cancel();

        if (entity instanceof PlayerEntity && ModuleManager.nameTags.isEnabled())
            info.cancel();
    }

    @Inject(method = "getSkyLight", at = @At("RETURN"), cancellable = true)
    private void onGetSkyLight(CallbackInfoReturnable<Integer> cir) {
        if(ModuleManager.fullbright.isEnabled())
            cir.setReturnValue(Fullbright.brightness.getValue());
    }
}