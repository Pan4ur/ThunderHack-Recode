package thunder.hack.injection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import thunder.hack.utility.render.shaders.satin.impl.SamplerAccess;
import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

@Mixin(JsonEffectShaderProgram.class)
public abstract class JsonEffectGlShaderMixin implements SamplerAccess {
    @Shadow
    @Final
    private Map<String, IntSupplier> samplerBinds;

    @Override
    public boolean hasSampler(String name) {
        return this.samplerBinds.containsKey(name);
    }

    @Override
    @Accessor("samplerNames")
    public abstract List<String> getSamplerNames();

    @Override
    @Accessor("samplerLocations")
    public abstract List<Integer> getSamplerShaderLocs();

    @WrapOperation(at = @At(value = "NEW", target = "net/minecraft/util/Identifier", ordinal = 0), method = "<init>")
    Identifier constructProgramIdentifier(String arg, Operation<Identifier> original, ResourceFactory unused, String id) {
        if (!id.contains(":")) {
            return original.call(arg);
        }
        Identifier split = new Identifier(id);
        return new Identifier(split.getNamespace(), "shaders/program/" + split.getPath() + ".json");
    }

    @WrapOperation(at = @At(value = "NEW", target = "net/minecraft/util/Identifier", ordinal = 0), method = "loadEffect")
    private static Identifier constructProgramIdentifier(String arg, Operation<Identifier> original, ResourceFactory unused, ShaderStage.Type shaderType, String id) {
        if (!arg.contains(":")) {
            return original.call(arg);
        }
        Identifier split = new Identifier(id);
        return new Identifier(split.getNamespace(), "shaders/program/" + split.getPath() + shaderType.getFileExtension());
    }
}
