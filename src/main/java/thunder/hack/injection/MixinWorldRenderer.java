package thunder.hack.injection;

import thunder.hack.Thunderhack;
import thunder.hack.modules.render.Fullbright;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @ModifyVariable(method = "getLightmapCoordinates(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "STORE"), ordinal = 0)
    private static int getLightmapCoordinatesModifySkyLight(int sky) {
        if(Thunderhack.moduleManager.get(Fullbright.class).isOn())
            return  (Thunderhack.moduleManager.get(Fullbright.class).brightness.getValue());
        return sky;
    }
}
