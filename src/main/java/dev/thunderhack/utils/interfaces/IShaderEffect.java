package dev.thunderhack.utils.interfaces;

import net.minecraft.client.gl.Framebuffer;

public interface IShaderEffect {
    void addFakeTargetHook(String name, Framebuffer buffer);
}
