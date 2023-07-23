package thunder.hack.injection;

import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = JsonEffectShaderProgram.class, priority = 900)
public abstract class MixinJsonEffectShaderProgram {


    @Redirect(at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"), method = "loadEffect", require = 0)
    private static Identifier loadEffectHook(String arg, ResourceManager unused, ShaderStage.Type shaderType, String id) {
        if (!arg.contains(":")) {
            return new Identifier(arg);
        }
        Identifier split = new Identifier(id);
        return new Identifier(split.getNamespace(), "shaders/program/" + split.getPath() + shaderType.getFileExtension());
    }


    @Redirect(at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/Identifier;", ordinal = 0), method = "<init>", require = 0)
    Identifier initHook(String arg, ResourceManager unused, String id) {
        if (!id.contains(":")) {
            return new Identifier(arg);
        }
        Identifier split = new Identifier(id);
        return new Identifier(split.getNamespace(), "shaders/program/" + split.getPath() + ".json");
    }
}