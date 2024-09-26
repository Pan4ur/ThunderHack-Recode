package thunder.hack.utility.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;
import thunder.hack.utility.render.WindowResizeCallback;
import thunder.hack.utility.render.shaders.satin.api.managed.ManagedCoreShader;
import thunder.hack.utility.render.shaders.satin.api.managed.ShaderEffectManager;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.SamplerUniform;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform1f;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform2f;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform4f;

import java.awt.*;

import static thunder.hack.features.modules.Module.mc;

public class BlurProgram {
    private Uniform2f uSize;
    private Uniform2f uLocation;
    private Uniform1f radius;
    private Uniform2f inputResolution;
    private Uniform1f brightness;
    private Uniform1f quality;
    private Uniform4f color1;
    private SamplerUniform sampler;

    private Framebuffer input;

    public static final ManagedCoreShader BLUR = ShaderEffectManager.getInstance()
            .manageCoreShader(Identifier.of("thunderhack", "blur"), VertexFormats.POSITION);

    public BlurProgram() {
        setup();
    }

    public void setParameters(float x, float y, float width, float height, float r, Color c1, float blurStrenth, float blurOpacity) {
        if (input == null)
            input = new SimpleFramebuffer(mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), false, MinecraftClient.IS_SYSTEM_MAC);

        float i = (float) mc.getWindow().getScaleFactor();
        radius.set(r * i);
        uLocation.set(x * i, -y * i + mc.getWindow().getScaledHeight() * i - height * i);
        uSize.set(width * i, height * i);
        brightness.set(blurOpacity);
        quality.set(blurStrenth);
        color1.set(c1.getRed() / 255f, c1.getGreen() / 255f, c1.getBlue() / 255f, 1f);
        sampler.set(input.getColorAttachment());
    }

    public void use() {
        var buffer = MinecraftClient.getInstance().getFramebuffer();
        input.beginWrite(false);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer.fbo);
        GL30.glBlitFramebuffer(0, 0, buffer.textureWidth, buffer.textureHeight, 0, 0, buffer.textureWidth, buffer.textureHeight, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_LINEAR);
        buffer.beginWrite(false);

        if (input != null && (input.textureWidth != mc.getWindow().getFramebufferWidth() || input.textureHeight != mc.getWindow().getFramebufferHeight()))
            input.resize(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);

        inputResolution.set((float) buffer.textureWidth, (float) buffer.textureHeight);
        sampler.set(input.getColorAttachment());

        RenderSystem.setShader(BLUR::getProgram);
    }

    protected void setup() {
        this.inputResolution = BLUR.findUniform2f("InputResolution");
        this.brightness = BLUR.findUniform1f("Brightness");
        this.quality = BLUR.findUniform1f("Quality");
        this.color1 = BLUR.findUniform4f("color1");
        this.uSize = BLUR.findUniform2f("uSize");
        this.uLocation = BLUR.findUniform2f("uLocation");
        this.radius = BLUR.findUniform1f("radius");
        sampler = BLUR.findSampler("InputSampler");
        WindowResizeCallback.EVENT.register((client, window) -> {
            if (input != null)
                input.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        });
    }
}