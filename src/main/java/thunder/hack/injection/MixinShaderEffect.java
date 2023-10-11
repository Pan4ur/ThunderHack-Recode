package thunder.hack.injection;

import org.spongepowered.asm.mixin.Unique;
import thunder.hack.injection.accesors.IPostProcessShader;
import thunder.hack.utility.interfaces.IShaderEffect;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(PostEffectProcessor.class)
public class MixinShaderEffect implements IShaderEffect {
    @Unique private final List<String> fakedBufferNames = new ArrayList<>();
    @Shadow @Final private Map<String, Framebuffer> targetsByName;
    @Shadow @Final private List<PostEffectPass> passes;


    @Override
    public void addFakeTargetHook(String name, Framebuffer buffer) {
        Framebuffer previousFramebuffer = this.targetsByName.get(name);
        if (previousFramebuffer == buffer) {
            return;
        }
        if (previousFramebuffer != null) {
            for (PostEffectPass pass : this.passes) {
                if (pass.input == previousFramebuffer) ((IPostProcessShader) pass).setInput(buffer);
                if (pass.output == previousFramebuffer) ((IPostProcessShader) pass).setOutput(buffer);
            }
            this.targetsByName.remove(name);
            this.fakedBufferNames.remove(name);
        }

        this.targetsByName.put(name, buffer);
        this.fakedBufferNames.add(name);
    }

    @Inject(method = "close", at = @At("HEAD"))
    void deleteFakeBuffersHook(CallbackInfo ci) {
        for (String fakedBufferName : fakedBufferNames)
            targetsByName.remove(fakedBufferName);
    }
}
