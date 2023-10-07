package thunder.hack.injection.accesors;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PostEffectPass.class)
public interface IPostProcessShader {
    @Mutable
    @Accessor("input")
    void setInput(Framebuffer framebuffer);

    @Mutable
    @Accessor("output")
    void setOutput(Framebuffer framebuffer);
}