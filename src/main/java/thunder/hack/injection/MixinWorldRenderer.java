package thunder.hack.injection;

import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import thunder.hack.Thunderhack;
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
                ShaderManager.OUTLINE.setAlpha(shaders.outlineColor.getValue().getAlpha(), shaders.glow.getValue());
                ShaderManager.OUTLINE.setLineWidth(shaders.lineWidth.getValue());
                ShaderManager.OUTLINE.setQuality(shaders.quality.getValue());
                ShaderManager.OUTLINE.setColor(shaders.fillColor1.getValue().getColorObject());
                ShaderManager.OUTLINE.setOutlineColor(shaders.outlineColor.getValue().getColorObject());
                ShaderManager.OUTLINE.render(tickDelta);
            } else if (shaders.mode.getValue() == Shaders.Mode.Smoke) {
                ShaderManager.SMOKE.setAlpha(shaders.outlineColor.getValue().getAlpha(), shaders.glow.getValue());
                ShaderManager.SMOKE.setAlpha1(shaders.fillAlpha.getValue());
                ShaderManager.SMOKE.setLineWidth(shaders.lineWidth.getValue());
                ShaderManager.SMOKE.setQuality(shaders.quality.getValue());

                ShaderManager.SMOKE.setFirst(shaders.outlineColor.getValue().getColorObject());
                ShaderManager.SMOKE.setSecond(shaders.outlineColor1.getValue().getColorObject());
                ShaderManager.SMOKE.setThird(shaders.outlineColor2.getValue().getColorObject());

                ShaderManager.SMOKE.setFFirst(shaders.fillColor1.getValue().getColorObject());
                ShaderManager.SMOKE.setFSecond(shaders.fillColor2.getValue().getColorObject());
                ShaderManager.SMOKE.setFThird(shaders.fillColor3.getValue().getColorObject());

                ShaderManager.SMOKE.setOctaves(shaders.octaves.getValue());
                ShaderManager.SMOKE.setResolution(mc.getWindow().getScaledWidth(),mc.getWindow().getScaledHeight());
                ShaderManager.SMOKE.setTime();
                ShaderManager.SMOKE.render(tickDelta);
            } else if (shaders.mode.getValue() == Shaders.Mode.Gradient) {
                ShaderManager.GRADIENT.setAlpha(shaders.outlineColor.getValue().getAlpha(), shaders.glow.getValue());
                ShaderManager.GRADIENT.setAlpha1(shaders.fillAlpha.getValue());
                ShaderManager.GRADIENT.setAlpha2(shaders.alpha2.getValue());
                ShaderManager.GRADIENT.setLineWidth(shaders.lineWidth.getValue());
                ShaderManager.GRADIENT.setQuality(shaders.quality.getValue());
                ShaderManager.GRADIENT.setOctaves(shaders.octaves.getValue());
                ShaderManager.GRADIENT.setMoreGradient(shaders.gradient.getValue());
                ShaderManager.GRADIENT.setFactor(shaders.factor.getValue());
                ShaderManager.GRADIENT.setResolution(mc.getWindow().getScaledWidth(),mc.getWindow().getScaledHeight());
                ShaderManager.GRADIENT.setTime();
                ShaderManager.GRADIENT.render(tickDelta);
            }
        } else {
            instance.render(tickDelta);
        }
    }
}
