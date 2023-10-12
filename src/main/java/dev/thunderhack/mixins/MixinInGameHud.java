package dev.thunderhack.mixins;

import net.minecraft.client.gui.DrawContext;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.core.ModuleManager;
import dev.thunderhack.gui.hud.impl.Hotbar;
import dev.thunderhack.modules.client.ClickGui;
import dev.thunderhack.utils.render.MSAAFramebuffer;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {
    @Inject(at = @At(value = "HEAD"), method = "render")
    public void render(DrawContext context, float tickDelta, CallbackInfo ci) {
        ThunderHack.moduleManager.onRenderShaders(context);
        ThunderHack.notificationManager.onRenderShader(context);

        if (ClickGui.getInstance().msaa.getValue()) {
            MSAAFramebuffer.use(() -> {
                ThunderHack.moduleManager.onRender2D(context);
                ThunderHack.notificationManager.onRender2D(context);
            });
        } else {
            ThunderHack.moduleManager.onRender2D(context);
            ThunderHack.notificationManager.onRender2D(context);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "renderHotbar", cancellable = true)
    public void renderHotbarCustom(float tickDelta, DrawContext context, CallbackInfo ci) {
        if (ModuleManager.hotbar.isEnabled()) {
            ci.cancel();
            Hotbar.renderCustomHotbar(tickDelta, context);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "renderStatusEffectOverlay", cancellable = true)
    public void renderStatusEffectOverlayHook(DrawContext context, CallbackInfo ci) {
        if (ModuleManager.potionHud.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At(value = "HEAD"), cancellable = true)
    public void renderXpBarCustom(DrawContext context, int x, CallbackInfo ci) {
        if (ModuleManager.hotbar.isEnabled()) {
            ci.cancel();
            Hotbar.renderXpBar(x, context.getMatrices());
        }
    }

    @Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
    public void renderCrosshair(DrawContext context, CallbackInfo ci) {
        if (ModuleManager.crosshair.isEnabled()) {
            ci.cancel();
        }
    }
}
