package dev.thunderhack.core;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.thunderhack.modules.Module;
import dev.thunderhack.modules.render.Shaders;
import dev.thunderhack.utils.interfaces.IShaderEffect;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.List;

public class ShaderManager {
    private final static List<RenderTask> tasks = new ArrayList<>();
    private ThunderHackFramebuffer shaderBuffer;

    public float time = 0;

    public static ManagedShaderEffect DEFAULT_OUTLINE;
    public static ManagedShaderEffect SMOKE_OUTLINE;
    public static ManagedShaderEffect GRADIENT_OUTLINE;
    public static ManagedShaderEffect SNOW_OUTLINE;

    public static ManagedShaderEffect DEFAULT;
    public static ManagedShaderEffect SMOKE;
    public static ManagedShaderEffect GRADIENT;
    public static ManagedShaderEffect SNOW;

    public void renderShader(Runnable runnable, Shader mode) {
        tasks.add(new RenderTask(runnable, mode));
    }

    public void renderShaders() {
        if (DEFAULT == null) {
            shaderBuffer = new ThunderHackFramebuffer(Module.mc.getFramebuffer().textureWidth, Module.mc.getFramebuffer().textureHeight);
            reloadShaders();
        }

        tasks.forEach(t -> applyShader(t.task(), t.shader()));
        tasks.clear();
    }

    public void applyShader(Runnable runnable, Shader mode) {
        Framebuffer mcBuffer = MinecraftClient.getInstance().getFramebuffer();
        RenderSystem.assertOnRenderThreadOrInit();
        if (shaderBuffer.textureWidth != mcBuffer.textureWidth || shaderBuffer.textureHeight != mcBuffer.textureHeight)
            shaderBuffer.resize(mcBuffer.textureWidth, mcBuffer.textureHeight, false);
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, shaderBuffer.fbo);
        shaderBuffer.beginWrite(true);
        runnable.run();
        shaderBuffer.endWrite();
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, mcBuffer.fbo);
        mcBuffer.beginWrite(false);
        ManagedShaderEffect shader = getShader(mode);
        Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
        PostEffectProcessor effect = shader.getShaderEffect();

        if (effect != null)
            ((IShaderEffect) effect).addFakeTargetHook("bufIn", shaderBuffer);

        Framebuffer outBuffer = shader.getShaderEffect().getSecondaryTarget("bufOut");
        setupShader(mode, shader);
        shaderBuffer.clear(false);
        mainBuffer.beginWrite(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        RenderSystem.backupProjectionMatrix();
        outBuffer.draw(outBuffer.textureWidth, outBuffer.textureHeight, false);
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public ManagedShaderEffect getShader(@NotNull Shader mode) {
        return switch (mode) {
            case Gradient -> GRADIENT;
            case Smoke -> SMOKE;
            case Snow -> SNOW;
            default -> DEFAULT;
        };
    }

    public ManagedShaderEffect getShaderOutline(@NotNull Shader mode) {
        return switch (mode) {
            case Gradient -> GRADIENT_OUTLINE;
            case Smoke -> SMOKE_OUTLINE;
            case Snow -> SNOW_OUTLINE;
            default -> DEFAULT_OUTLINE;
        };
    }

    public void setupShader(Shader shader, ManagedShaderEffect effect) {
        Shaders shaders = ModuleManager.shaders;
        if (shader == Shader.Gradient) {
            effect.setUniformValue("alpha0", shaders.glow.getValue() ? -1.0f : shaders.outlineColor.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("alpha1", shaders.fillAlpha.getValue() / 255f);
            effect.setUniformValue("alpha2", shaders.alpha2.getValue() / 255f);
            effect.setUniformValue("lineWidth", shaders.lineWidth.getValue());
            effect.setUniformValue("oct", shaders.octaves.getValue());
            effect.setUniformValue("quality", shaders.quality.getValue());
            effect.setUniformValue("factor", shaders.factor.getValue());
            effect.setUniformValue("moreGradient", shaders.gradient.getValue());
            effect.setUniformValue("resolution", (float) Module.mc.getWindow().getScaledWidth(), (float) Module.mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", time);
            effect.render(Module.mc.getTickDelta());
            time += 0.008f;
        } else if (shader == Shader.Smoke) {
            effect.setUniformValue("alpha0", shaders.glow.getValue() ? -1.0f : shaders.outlineColor.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("alpha1", shaders.fillAlpha.getValue() / 255f);
            effect.setUniformValue("lineWidth", shaders.lineWidth.getValue());
            effect.setUniformValue("quality", shaders.quality.getValue());
            effect.setUniformValue("first", shaders.outlineColor.getValue().getGlRed(), shaders.outlineColor.getValue().getGlGreen(), shaders.outlineColor.getValue().getGlBlue(), shaders.outlineColor.getValue().getGlAlpha());
            effect.setUniformValue("second", shaders.outlineColor1.getValue().getGlRed(), shaders.outlineColor1.getValue().getGlGreen(), shaders.outlineColor1.getValue().getGlBlue());
            effect.setUniformValue("third", shaders.outlineColor2.getValue().getGlRed(), shaders.outlineColor2.getValue().getGlGreen(), shaders.outlineColor2.getValue().getGlBlue());
            effect.setUniformValue("ffirst", shaders.fillColor1.getValue().getGlRed(), shaders.fillColor1.getValue().getGlGreen(), shaders.fillColor1.getValue().getGlBlue(), shaders.fillColor1.getValue().getGlAlpha());
            effect.setUniformValue("fsecond", shaders.fillColor2.getValue().getGlRed(), shaders.fillColor2.getValue().getGlGreen(), shaders.fillColor2.getValue().getGlBlue());
            effect.setUniformValue("fthird", shaders.fillColor3.getValue().getGlRed(), shaders.fillColor3.getValue().getGlGreen(), shaders.fillColor3.getValue().getGlBlue());
            effect.setUniformValue("oct", shaders.octaves.getValue());
            effect.setUniformValue("resolution", (float) Module.mc.getWindow().getScaledWidth(), (float) Module.mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", time);
            effect.render(Module.mc.getTickDelta());
            time += 0.008f;
        } else if (shader == Shader.Default) {
            effect.setUniformValue("alpha0", shaders.glow.getValue() ? -1.0f : shaders.outlineColor.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("lineWidth", shaders.lineWidth.getValue());
            effect.setUniformValue("quality", shaders.quality.getValue());
            effect.setUniformValue("color", shaders.fillColor1.getValue().getGlRed(), shaders.fillColor1.getValue().getGlGreen(), shaders.fillColor1.getValue().getGlBlue(), shaders.fillColor1.getValue().getGlAlpha());
            effect.setUniformValue("outlinecolor", shaders.outlineColor.getValue().getGlRed(), shaders.outlineColor.getValue().getGlGreen(), shaders.outlineColor.getValue().getGlBlue(), shaders.outlineColor.getValue().getGlAlpha());
            effect.render(Module.mc.getTickDelta());
        } else if (shader == Shader.Snow) {
            effect.setUniformValue("color", shaders.fillColor1.getValue().getGlRed(), shaders.fillColor1.getValue().getGlGreen(), shaders.fillColor1.getValue().getGlBlue(), shaders.fillColor1.getValue().getGlAlpha());
            effect.setUniformValue("quality", shaders.quality.getValue());
            effect.setUniformValue("resolution", (float) Module.mc.getWindow().getScaledWidth(), (float) Module.mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", time);
            effect.render(Module.mc.getTickDelta());
            time += 0.008f;
        }
    }

    public void reloadShaders() {
        DEFAULT = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/outline.json"));
        SMOKE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/smoke.json"));
        GRADIENT = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/gradient.json"));
        SNOW = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/snow.json"));

        DEFAULT_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/outline.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).addFakeTargetHook("bufIn", Module.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).addFakeTargetHook("bufOut", Module.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });

        SMOKE_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/smoke.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).addFakeTargetHook("bufIn", Module.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).addFakeTargetHook("bufOut", Module.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });

        GRADIENT_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/gradient.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).addFakeTargetHook("bufIn", Module.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).addFakeTargetHook("bufOut", Module.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });

        SNOW_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/snow.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).addFakeTargetHook("bufIn", Module.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).addFakeTargetHook("bufOut", Module.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
    }

    public static class ThunderHackFramebuffer extends Framebuffer {
        public ThunderHackFramebuffer(int width, int height) {
            super(false);
            RenderSystem.assertOnRenderThreadOrInit();
            resize(width, height, true);
            setClearColor(0f, 0f, 0f, 0f);
        }
    }

    public boolean fullNullCheck() {
        if (GRADIENT == null || SMOKE == null || DEFAULT == null) {
            shaderBuffer = new ThunderHackFramebuffer(Module.mc.getFramebuffer().textureWidth, Module.mc.getFramebuffer().textureHeight);
            reloadShaders();
            return true;
        }

        return false;
    }

    public record RenderTask(Runnable task, Shader shader) {
    }

    public enum Shader {
        Default,
        Smoke,
        Gradient,
        Snow
    }
}
