package thunder.hack.injection;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import thunder.hack.Thunderhack;
import thunder.hack.modules.render.Chams;
import thunder.hack.setting.impl.ColorSetting;

@Mixin(EndCrystalEntityRenderer.class)
public class MixinEndCrystalEntityRenderer {
    private EndCrystalEntity lastEntity;

    @Inject(method = "render", at = @At("HEAD"))
    public void onRender(EndCrystalEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        lastEntity = livingEntity;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
    public void onRenderPart(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        if(Thunderhack.moduleManager.get(Chams.class).isEnabled()){
            ColorSetting clr = Thunderhack.moduleManager.get(Chams.class).getEntityColor(lastEntity);
            modelPart.render(matrices, vertices, light, overlay, clr.getRed() / 255F, clr.getGreen() / 255F, clr.getBlue() / 255F, clr.getAlpha() / 255F);
            return;
        }
        modelPart.render(matrices, vertices, light, overlay, 1f, 1f, 1f, 1f);
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V", ordinal = 0))
    private void onScale(Args args) {
        if(Thunderhack.moduleManager.get(Chams.class).isEnabled()) {
            float scale = Thunderhack.moduleManager.get(Chams.class).getEntityScale(lastEntity);
            args.set(0, scale);
            args.set(1, scale);
            args.set(2, scale);
        }
    }
}
