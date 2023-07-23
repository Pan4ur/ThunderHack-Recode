package thunder.hack.utility.interfaces;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;

import java.util.List;
public interface IShaderEffect {
    void addFakeTargetHook(String name, Framebuffer buffer);
    List<PostEffectPass> getPassesHook();
}
