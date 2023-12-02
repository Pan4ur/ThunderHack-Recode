package thunder.hack.injection;

import net.minecraft.client.gl.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import thunder.hack.utility.render.shaders.GlProgram;

@Mixin(ShaderProgram.class)
public class MixinShaderProgram {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;<init>(Ljava/lang/String;)V"), require = 0)
    private String fixIdentifier(String id) {
        if (!((Object) this instanceof GlProgram.THShaderProgram))
            return id;

        var splitName = id.split(":");
        if (splitName.length != 2 || !splitName[0].startsWith("shaders/core/"))
            return id;

        return splitName[0].replace("shaders/core/", "") + ":" + "shaders/core/" + splitName[1];
    }
}
