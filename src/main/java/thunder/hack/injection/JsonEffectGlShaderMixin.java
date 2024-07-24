package thunder.hack.injection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import thunder.hack.utility.render.shaders.satin.impl.SamplerAccess;

@Mixin(JsonEffectShaderProgram.class)
public abstract class JsonEffectGlShaderMixin implements SamplerAccess {

    @WrapOperation(at = @At(value = "INVOKE", target = "net/minecraft/util/Identifier.ofVanilla (Ljava/lang/String;)Lnet/minecraft/util/Identifier;", ordinal = 0), method = "<init>")
    Identifier constructProgramIdentifier(String arg, Operation<Identifier> original, ResourceFactory unused, String id) {
        if (!id.contains(":")) {
            return original.call(arg);
        }
        Identifier split = Identifier.of(id);
        return Identifier.of(split.getNamespace(), "shaders/program/" + split.getPath() + ".json");
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "net/minecraft/util/Identifier.ofVanilla (Ljava/lang/String;)Lnet/minecraft/util/Identifier;", ordinal = 0), method = "loadEffect")
    private static Identifier constructProgramIdentifier(String arg, Operation<Identifier> original, ResourceFactory unused, ShaderStage.Type shaderType, String id) {
        if (!arg.contains(":")) {
            return original.call(arg);
        }
        Identifier split = Identifier.of(id);
        return Identifier.of(split.getNamespace(), "shaders/program/" + split.getPath() + shaderType.getFileExtension());
    }
}
