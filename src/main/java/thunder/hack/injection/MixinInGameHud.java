package thunder.hack.injection;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Unique;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.hud.impl.Hotbar;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.ClickGui;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    @Inject(at = @At(value = "HEAD"), method = "render")
    public void renderHook(DrawContext context, float tickDelta, CallbackInfo ci) {
        if(Module.fullNullCheck()) return;
        ThunderHack.moduleManager.onRender2D(context);
        ThunderHack.notificationManager.onRender2D(context);
    }

    @Inject(at = @At(value = "HEAD"), method = "renderStatusBars", cancellable = true)
    private void renderStatusBarsHook(DrawContext context, CallbackInfo ci) {
        if (ModuleManager.hotbar.isEnabled()) {
   //         ci.cancel();
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "renderHotbar", cancellable = true)
    public void renderHotbarCustom(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (ModuleManager.hotbar.isEnabled()) {
            ci.cancel();
            Hotbar.renderHotBarItems(tickDelta, context);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "renderStatusEffectOverlay", cancellable = true)
    public void renderStatusEffectOverlayHook(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (ModuleManager.potionHud.isEnabled() || (ModuleManager.legacyHud.isEnabled() && ModuleManager.legacyHud.potions.getValue())) {
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

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At(value = "HEAD"), cancellable = true)
    private void renderScoreboardSidebarHook(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        if(ModuleManager.noRender.noScoreBoard.getValue() && ModuleManager.noRender.isEnabled()){
            ci.cancel();
        }
    }

    @Inject(method = "renderVignetteOverlay", at = @At(value = "HEAD"), cancellable = true)
    private void renderVignetteOverlayHook(DrawContext context, Entity entity, CallbackInfo ci) {
        if(ModuleManager.noRender.vignette.getValue())
            ci.cancel();
    }

    @Inject(method = "renderPortalOverlay", at = @At(value = "HEAD"), cancellable = true)
    private void renderPortalOverlayHook(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        if(ModuleManager.noRender.portal.getValue())
            ci.cancel();
    }

    @Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
    public void renderCrosshair(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (ModuleManager.crosshair.isEnabled()) {
            ci.cancel();
        }
    }
}
