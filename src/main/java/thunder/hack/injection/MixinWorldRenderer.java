package thunder.hack.injection;

import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import thunder.hack.ThunderHack;
import thunder.hack.core.ModuleManager;
import thunder.hack.core.ShaderManager;
import thunder.hack.modules.render.Fullbright;

import static thunder.hack.modules.Module.mc;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    @Shadow
    public abstract void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix);

    @ModifyVariable(method = "getLightmapCoordinates(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "STORE"), ordinal = 0)
    private static int getLightmapCoordinatesModifySkyLight(int sky) {
        if (ModuleManager.fullbright.isEnabled())
            return (Fullbright.brightness.getValue());
        return sky;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal = 0))
    void replaceShaderHook(PostEffectProcessor instance, float tickDelta) {
        ShaderManager.Shader shaders = ModuleManager.shaders.mode.getValue();
        if (ModuleManager.shaders.isEnabled() && mc.world != null) {
            if (ThunderHack.shaderManager.fullNullCheck()) return;
            ThunderHack.shaderManager.setupShader(shaders, ThunderHack.shaderManager.getShaderOutline(shaders));
        } else {
            instance.render(tickDelta);
        }
    }
}
