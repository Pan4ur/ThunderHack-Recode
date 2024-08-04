package thunder.hack.injection;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.manager.client.ModuleManager;

@Mixin(EndCrystalEntityRenderer.class)
public class MixinEndCrystalEntityRenderer {


    @Shadow @Final private static float SINE_45_DEGREES;

    @Shadow
    @Final
    private ModelPart core;


    @Shadow
    @Final
    private ModelPart frame;

    @Inject(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = {@At("HEAD")}, cancellable = true)
    public void render(EndCrystalEntity endCrystalEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if(ModuleManager.chams.isEnabled() && ModuleManager.chams.crystals.getValue()) {
            ci.cancel();
            ModuleManager.chams.renderCrystal(endCrystalEntity, f, g, matrixStack, i, core, frame);
        }
    }
}
