package thunder.hack.injection;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreboardObjective;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.hud.impl.Hotbar;
import thunder.hack.gui.windows.WindowsScreen;
import thunder.hack.features.modules.Module;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static thunder.hack.core.manager.IManager.mc;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    @Inject(at = @At(value = "HEAD"), method = "render")
    public void renderHook(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if(Module.fullNullCheck()) return;
        Managers.MODULE.onRender2D(context);
        Managers.NOTIFICATION.onRender2D(context);
    }

    @Inject(at = @At(value = "HEAD"), method = "renderStatusBars", cancellable = true)
    private void renderStatusBarsHook(DrawContext context, CallbackInfo ci) {
        if (mc != null && mc.currentScreen instanceof WindowsScreen) {
            ci.cancel();
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "renderHotbar", cancellable = true)
    public void renderHotbarCustom(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (mc != null && mc.currentScreen instanceof WindowsScreen)
            ci.cancel();

        if (ModuleManager.hotbar.isEnabled()) {
            ci.cancel();
            Hotbar.renderHotBarItems(tickCounter.getTickDelta(true), context);
        }
    }


    @Inject(at = @At(value = "HEAD"), method = "renderHeldItemTooltip", cancellable = true)
    public void renderHeldItemTooltipHook(DrawContext context, CallbackInfo ci) {
        if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.hotbarItemName.getValue())
            ci.cancel();
    }

    @Inject(at = @At(value = "HEAD"), method = "renderStatusEffectOverlay", cancellable = true)
    public void renderStatusEffectOverlayHook(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ModuleManager.potionHud.isEnabled() || (ModuleManager.legacyHud.isEnabled() && ModuleManager.legacyHud.potions.getValue())) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At(value = "HEAD"), cancellable = true)
    public void renderXpBarCustom(DrawContext context, int x, CallbackInfo ci) {
        if (mc != null && mc.currentScreen instanceof WindowsScreen)
            ci.cancel();

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
    public void renderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ModuleManager.crosshair.isEnabled())
            ci.cancel();
    }
}
