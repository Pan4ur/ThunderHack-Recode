package thunder.hack.injection;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.Thunderhack;
import thunder.hack.core.ModuleManager;
import thunder.hack.gui.hud.impl.Hotbar;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.utility.render.MSAAFramebuffer;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinIngameHud {
    @Inject(at = @At(value = "HEAD"), method = "render")
    public void render(DrawContext context, float tickDelta, CallbackInfo ci) {
        Thunderhack.moduleManager.onRenderShaders(context);
        Thunderhack.notificationManager.onRenderShader(context);

        if (ClickGui.getInstance().msaa.getValue()) {
            MSAAFramebuffer.use(() -> {
                Thunderhack.moduleManager.onRender2D(context);
                Thunderhack.notificationManager.onRender2D(context);
            });
        } else {
            Thunderhack.moduleManager.onRender2D(context);
            Thunderhack.notificationManager.onRender2D(context);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "renderHotbar",cancellable = true)
    public void renderHotbarCustom(float tickDelta, DrawContext context, CallbackInfo ci) {
        if(ModuleManager.hotbar.isEnabled()){
            ci.cancel();
            Hotbar.renderCustomHotbar(tickDelta,context);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "renderStatusEffectOverlay",cancellable = true)
    public void renderStatusEffectOverlayHook(DrawContext context, CallbackInfo ci) {
        if(ModuleManager.potionHud.isEnabled()){
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At(value = "HEAD"),cancellable = true)
    public void renderXpBarCustom(DrawContext context, int x, CallbackInfo ci) {
        if(ModuleManager.hotbar.isEnabled()){
            ci.cancel();
            Hotbar.renderXpBar(x,context.getMatrices());
        }
    }

    @Inject(method = "renderCrosshair",at = @At(value = "HEAD"),cancellable = true)
    public void renderCrosshair(DrawContext context, CallbackInfo ci) {
        if(ModuleManager.crosshair.isEnabled()){
            ci.cancel();
        }
    }
}
