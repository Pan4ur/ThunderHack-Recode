package thunder.hack.injection;

import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Redirect;
import thunder.hack.core.ModuleManager;
import thunder.hack.modules.render.Fullbright;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import thunder.hack.modules.render.Shaders;
import thunder.hack.utility.render.shaders.ShaderManager;

import static thunder.hack.modules.Module.mc;


@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    @Shadow public abstract void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix);

    @Unique
    public float stime = 0;

    @ModifyVariable(method = "getLightmapCoordinates(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "STORE"), ordinal = 0)
    private static int getLightmapCoordinatesModifySkyLight(int sky) {
        if(ModuleManager.fullbright.isEnabled())
            return  (Fullbright.brightness.getValue());
        return sky;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal = 0))
    void replaceShaderHook(PostEffectProcessor instance, float tickDelta) {
        Shaders shaders = ModuleManager.shaders;
        if (shaders.isEnabled() && mc.world != null) {
            if(ShaderManager.fullNullCheck()) return;
            if(shaders.mode.getValue() == Shaders.Mode.Default) {
                ShaderManager.OUTLINE.setSamplerUniform("outline", mc.worldRenderer.getEntityOutlinesFramebuffer());
                ShaderManager.OUTLINE.setUniformValue("alpha0", shaders.glow.getValue() ? -1.0f : shaders.outlineColor.getValue().getAlpha() / 255.0f);
                ShaderManager.OUTLINE.setUniformValue("lineWidth", shaders.lineWidth.getValue());
                ShaderManager.OUTLINE.setUniformValue("quality", shaders.quality.getValue());
                ShaderManager.OUTLINE.setUniformValue("color", shaders.fillColor1.getValue().getRed() / 255f,shaders.fillColor1.getValue().getGreen() / 255f,shaders.fillColor1.getValue().getBlue() / 255f,shaders.fillColor1.getValue().getAlpha() / 255f);
                ShaderManager.OUTLINE.setUniformValue("outlinecolor", shaders.outlineColor.getValue().getRed() / 255f,shaders.outlineColor.getValue().getGreen() / 255f,shaders.outlineColor.getValue().getBlue() / 255f,shaders.outlineColor.getValue().getAlpha() / 255f);
                ShaderManager.OUTLINE.render(tickDelta);
            } else if (shaders.mode.getValue() == Shaders.Mode.Smoke) {
                ShaderManager.SMOKE.setSamplerUniform("smoke", mc.worldRenderer.getEntityOutlinesFramebuffer());
                ShaderManager.SMOKE.setUniformValue("alpha0", shaders.glow.getValue() ? -1.0f : shaders.outlineColor.getValue().getAlpha() / 255.0f);
                ShaderManager.SMOKE.setUniformValue("alpha1",shaders.fillAlpha.getValue() / 255f);
                ShaderManager.SMOKE.setUniformValue("lineWidth", shaders.lineWidth.getValue());
                ShaderManager.SMOKE.setUniformValue("quality", shaders.quality.getValue());
                ShaderManager.SMOKE.setUniformValue("first", shaders.outlineColor.getValue().getRed() / 255f,shaders.outlineColor.getValue().getGreen() / 255f,shaders.outlineColor.getValue().getBlue() / 255f,shaders.outlineColor.getValue().getAlpha() / 255f);
                ShaderManager.SMOKE.setUniformValue("second", shaders.outlineColor1.getValue().getRed() / 255f,shaders.outlineColor1.getValue().getGreen() / 255f,shaders.outlineColor1.getValue().getBlue() / 255f);
                ShaderManager.SMOKE.setUniformValue("third", shaders.outlineColor2.getValue().getRed() / 255f,shaders.outlineColor2.getValue().getGreen() / 255f,shaders.outlineColor2.getValue().getBlue() / 255f);
                ShaderManager.SMOKE.setUniformValue("ffirst", shaders.fillColor1.getValue().getRed() / 255f,shaders.fillColor1.getValue().getGreen() / 255f,shaders.fillColor1.getValue().getBlue() / 255f,shaders.fillColor1.getValue().getAlpha() / 255f);
                ShaderManager.SMOKE.setUniformValue("fsecond", shaders.fillColor2.getValue().getRed() / 255f,shaders.fillColor2.getValue().getGreen() / 255f,shaders.fillColor2.getValue().getBlue() / 255f);
                ShaderManager.SMOKE.setUniformValue("fthird", shaders.fillColor3.getValue().getRed() / 255f,shaders.fillColor3.getValue().getGreen() / 255f,shaders.fillColor3.getValue().getBlue() / 255f);
                ShaderManager.SMOKE.setUniformValue("oct", shaders.octaves.getValue());
                ShaderManager.SMOKE.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
                ShaderManager.SMOKE.setUniformValue("time", stime);
                ShaderManager.SMOKE.render(tickDelta);
                stime += 0.008f;
            } else if (shaders.mode.getValue() == Shaders.Mode.Gradient) {
                ShaderManager.GRADIENT.setSamplerUniform("gradient", mc.worldRenderer.getEntityOutlinesFramebuffer());
                ShaderManager.GRADIENT.setUniformValue("alpha0", shaders.glow.getValue() ? -1.0f : shaders.outlineColor.getValue().getAlpha() / 255.0f);
                ShaderManager.GRADIENT.setUniformValue("alpha1",shaders.fillAlpha.getValue() / 255f);
                ShaderManager.GRADIENT.setUniformValue("alpha2",shaders.alpha2.getValue() / 255f);
                ShaderManager.GRADIENT.setUniformValue("lineWidth", shaders.lineWidth.getValue());
                ShaderManager.GRADIENT.setUniformValue("oct", shaders.octaves.getValue());
                ShaderManager.GRADIENT.setUniformValue("quality", shaders.quality.getValue());
                ShaderManager.GRADIENT.setUniformValue("factor", shaders.factor.getValue());
                ShaderManager.GRADIENT.setUniformValue("moreGradient", shaders.gradient.getValue());
                ShaderManager.GRADIENT.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
                ShaderManager.GRADIENT.setUniformValue("time", stime);
                ShaderManager.GRADIENT.render(tickDelta);
                stime += 0.008f;
            }
        } else {
            instance.render(tickDelta);
        }
    }
}
