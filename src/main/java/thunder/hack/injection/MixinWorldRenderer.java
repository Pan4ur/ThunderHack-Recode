package thunder.hack.injection;

import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.core.impl.ShaderManager;
import thunder.hack.modules.render.Fullbright;
import thunder.hack.modules.render.Shaders;

import static thunder.hack.modules.Module.mc;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    @Shadow
    public abstract void render(float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2);

    @ModifyVariable(method = "getLightmapCoordinates(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "STORE"), ordinal = 0)
    private static int getLightmapCoordinatesModifySkyLight(int sky) {
        if (ModuleManager.fullbright.isEnabled())
            return (Fullbright.brightness.getValue());
        return sky;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"), index = 3)
    private boolean renderSetupTerrainModifyArg(boolean spectator) {
        return ModuleManager.freeCam.isEnabled() || spectator;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal = 0))
    private void replaceShaderHook(PostEffectProcessor instance, float tickDelta) {
        ShaderManager.Shader shaders = ModuleManager.shaders.mode.getValue();
        if (ModuleManager.shaders.isEnabled() && mc.world != null) {
            if (ThunderHack.shaderManager.fullNullCheck()) return;
            ThunderHack.shaderManager.setupShader(shaders, ThunderHack.shaderManager.getShaderOutline(shaders));
        } else {
            instance.render(tickDelta);
        }
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void renderWeatherHook(LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.noWeather.getValue()) {
            ci.cancel();
        }
    }
}
