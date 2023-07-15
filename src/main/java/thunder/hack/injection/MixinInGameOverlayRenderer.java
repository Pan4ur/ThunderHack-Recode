package thunder.hack.injection;

import thunder.hack.Thunderhack;
import thunder.hack.modules.render.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class MixinInGameOverlayRenderer {

    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void onRenderFireOverlay(MinecraftClient minecraftClient, MatrixStack matrixStack, CallbackInfo ci) {
        if (Thunderhack.moduleManager.get(NoRender.class).isEnabled() && Thunderhack.moduleManager.get(NoRender.class).fireOverlay.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true)
    private static void onRenderUnderwaterOverlay(MinecraftClient minecraftClient, MatrixStack matrixStack, CallbackInfo ci) {
        if (Thunderhack.moduleManager.get(NoRender.class).isEnabled() && Thunderhack.moduleManager.get(NoRender.class).waterOverlay.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
    private static void onrenderInWallOverlay(Sprite sprite, MatrixStack matrices, CallbackInfo ci) {
        if (Thunderhack.moduleManager.get(NoRender.class).isEnabled() && Thunderhack.moduleManager.get(NoRender.class).blockOverlay.getValue()) {
            ci.cancel();
        }
    }

}