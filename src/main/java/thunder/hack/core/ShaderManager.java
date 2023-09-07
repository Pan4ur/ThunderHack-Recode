package thunder.hack.core;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL30C;
import thunder.hack.modules.render.Shaders;
import thunder.hack.utility.interfaces.IShaderEffect;

import java.util.LinkedList;

import static thunder.hack.modules.Module.mc;

public class ShaderManager {
    private final static LinkedList<RenderTask> tasks = new LinkedList<>();
    private ThunderHackFramebuffer shaderBuffer;

    public float time = 0;

    public static ManagedShaderEffect DEFAULT_OUTLINE;
    public static ManagedShaderEffect SMOKE_OUTLINE;
    public static ManagedShaderEffect GRADIENT_OUTLINE;

    public static ManagedShaderEffect DEFAULT;
    public static ManagedShaderEffect SMOKE;
    public static ManagedShaderEffect GRADIENT;

    public void renderShader(Runnable runnable, Shader mode) {
        tasks.add(new RenderTask(runnable, mode));
    }

    public void renderShaders() {
        if (DEFAULT == null) {
            shaderBuffer = new ThunderHackFramebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight);
            reloadShaders();
        }

        while (!tasks.isEmpty()) {
            RenderTask task = tasks.pop();
            applyShader(task.task(), task.shader);
        }
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
        if (effect != null) {
            ((IShaderEffect) effect).addFakeTargetHook("bufIn", shaderBuffer);
        }

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
        switch (mode) {
            case Smoke -> {
                return SMOKE;
            }
            case Default -> {
                return DEFAULT;
            }
            case Gradient -> {
                return GRADIENT;
            }
        }
        return DEFAULT;
    }

    public ManagedShaderEffect getShaderOutline(@NotNull Shader mode) {
        switch (mode) {
            case Smoke -> {
                return SMOKE_OUTLINE;
            }
            case Default -> {
                return DEFAULT_OUTLINE;
            }
            case Gradient -> {
                return GRADIENT_OUTLINE;
            }
        }
        return DEFAULT_OUTLINE;
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
            effect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", time);
            effect.render(mc.getTickDelta());
            time += 0.008f;
        } else if (shader == Shader.Smoke) {
            effect.setUniformValue("alpha0", shaders.glow.getValue() ? -1.0f : shaders.outlineColor.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("alpha1", shaders.fillAlpha.getValue() / 255f);
            effect.setUniformValue("lineWidth", shaders.lineWidth.getValue());
            effect.setUniformValue("quality", shaders.quality.getValue());
            effect.setUniformValue("first", shaders.outlineColor.getValue().getRed() / 255f, shaders.outlineColor.getValue().getGreen() / 255f, shaders.outlineColor.getValue().getBlue() / 255f, shaders.outlineColor.getValue().getAlpha() / 255f);
            effect.setUniformValue("second", shaders.outlineColor1.getValue().getRed() / 255f, shaders.outlineColor1.getValue().getGreen() / 255f, shaders.outlineColor1.getValue().getBlue() / 255f);
            effect.setUniformValue("third", shaders.outlineColor2.getValue().getRed() / 255f, shaders.outlineColor2.getValue().getGreen() / 255f, shaders.outlineColor2.getValue().getBlue() / 255f);
            effect.setUniformValue("ffirst", shaders.fillColor1.getValue().getRed() / 255f, shaders.fillColor1.getValue().getGreen() / 255f, shaders.fillColor1.getValue().getBlue() / 255f, shaders.fillColor1.getValue().getAlpha() / 255f);
            effect.setUniformValue("fsecond", shaders.fillColor2.getValue().getRed() / 255f, shaders.fillColor2.getValue().getGreen() / 255f, shaders.fillColor2.getValue().getBlue() / 255f);
            effect.setUniformValue("fthird", shaders.fillColor3.getValue().getRed() / 255f, shaders.fillColor3.getValue().getGreen() / 255f, shaders.fillColor3.getValue().getBlue() / 255f);
            effect.setUniformValue("oct", shaders.octaves.getValue());
            effect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", time);
            effect.render(mc.getTickDelta());
            time += 0.008f;
        } else if (shader == Shader.Default) {
            effect.setUniformValue("alpha0", shaders.glow.getValue() ? -1.0f : shaders.outlineColor.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("lineWidth", shaders.lineWidth.getValue());
            effect.setUniformValue("quality", shaders.quality.getValue());
            effect.setUniformValue("color", shaders.fillColor1.getValue().getRed() / 255f, shaders.fillColor1.getValue().getGreen() / 255f, shaders.fillColor1.getValue().getBlue() / 255f, shaders.fillColor1.getValue().getAlpha() / 255f);
            effect.setUniformValue("outlinecolor", shaders.outlineColor.getValue().getRed() / 255f, shaders.outlineColor.getValue().getGreen() / 255f, shaders.outlineColor.getValue().getBlue() / 255f, shaders.outlineColor.getValue().getAlpha() / 255f);
            effect.render(mc.getTickDelta());
        }
    }

    public void reloadShaders() {
        DEFAULT = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/outline.json"));
        SMOKE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/smoke.json"));
        GRADIENT = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/gradient.json"));

        DEFAULT_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/outline.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        });

        SMOKE_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/smoke.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        });

        GRADIENT_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/gradient.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
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
            shaderBuffer = new ThunderHackFramebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight);
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
        Gradient
    }
}
