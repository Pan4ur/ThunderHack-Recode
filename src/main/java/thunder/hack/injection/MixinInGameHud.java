package thunder.hack.injection;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.hud.impl.Hotbar;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.modules.render.NoRender;
import thunder.hack.utility.render.MSAAFramebuffer;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static thunder.hack.modules.Module.mc;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    @Inject(at = @At(value = "HEAD"), method = "render")
    public void render(DrawContext context, float tickDelta, CallbackInfo ci) {
        ThunderHack.moduleManager.onRenderShaders(context);
        ThunderHack.notificationManager.onRenderShader(context);

        if (ClickGui.getInstance().msaa.getValue()) {
            MSAAFramebuffer.use(false, () -> {
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

    @Inject(method = "renderScoreboardSidebar", at = @At(value = "HEAD"), cancellable = true)
    private void renderScoreboardSidebarHook(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        if(ModuleManager.noRender.noScoreBoard.getValue() != NoRender.NoScoreBoard.None){
            ci.cancel();
            if(ModuleManager.noRender.noScoreBoard.getValue() == NoRender.NoScoreBoard.Position)
                renderScoreboardSidebarCustom(context, objective);
        }
    }

    @Unique
    private void renderScoreboardSidebarCustom(DrawContext context, ScoreboardObjective objective) {
        /*

        !!!! FIND ANOTHER WAY !!!!

        int i;
        Scoreboard scoreboard = objective.getScoreboard();
        Collection<ScoreboardPlayerScore> collection = scoreboard.getScore(objective);
        List list = collection.stream().filter(score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")).collect(Collectors.toList());
        collection = list.size() > 15 ? Lists.newArrayList(Iterables.skip(list, collection.size() - 15)) : list;
        ArrayList<Pair<ScoreboardPlayerScore, MutableText>> list2 = Lists.newArrayListWithCapacity(collection.size());
        Text text = objective.getDisplayName();
        int j = i = mc.textRenderer.getWidth(text);
        int k = mc.textRenderer.getWidth(": ");
        for (ScoreboardPlayerScore scoreboardPlayerScore : collection) {
            Team team = scoreboard.getPlayerTeam(scoreboardPlayerScore.getPlayerName());
            MutableText text2 = Team.decorateName(team, Text.literal(scoreboardPlayerScore.getPlayerName()));
            list2.add(Pair.of(scoreboardPlayerScore, text2));
            j = Math.max(j, mc.textRenderer.getWidth(text2) + k + mc.textRenderer.getWidth(Integer.toString(scoreboardPlayerScore.getScore())));
        }
        int l = collection.size() * mc.textRenderer.fontHeight;

        int m = (int) (mc.getWindow().getScaledHeight() * (ModuleManager.noRender.sbY.getValue() / 10f)) + l / 3;
        int o = (int) (mc.getWindow().getWidth() * (ModuleManager.noRender.sbX.getValue() / 10f)) - j - 3;

        int p = 0;
        int q = mc.options.getTextBackgroundColor(0.3f);
        int r = mc.options.getTextBackgroundColor(0.4f);
        for (Pair pair : list2) {
            ScoreboardPlayerScore scoreboardPlayerScore2 = (ScoreboardPlayerScore)pair.getFirst();
            Text text3 = (Text)pair.getSecond();
            String string = "" + Formatting.RED + scoreboardPlayerScore2.getScore();
            int s = o;
            int t = m - ++p * mc.textRenderer.fontHeight;
            int u = (int) (mc.getWindow().getWidth() * (ModuleManager.noRender.sbX.getValue() / 10f)) - 3 + 2;
            context.fill(s - 2, t, u, t + mc.textRenderer.fontHeight, q);
            context.drawText(mc.textRenderer, text3, s, t, -1, false);
            context.drawText(mc.textRenderer, string, u - mc.textRenderer.getWidth(string), t, -1, false);
            if (p != collection.size()) continue;
            context.fill(s - 2, t - mc.textRenderer.fontHeight - 1, u, t - 1, r);
            context.fill(s - 2, t - 1, u, t, q);
            context.drawText(mc.textRenderer, text, s + j / 2 - i / 2, t - mc.textRenderer.fontHeight, -1, false);
        }

         */
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
    public void renderCrosshair(DrawContext context, CallbackInfo ci) {
        if (ModuleManager.crosshair.isEnabled()) {
            ci.cancel();
        }
    }
}
