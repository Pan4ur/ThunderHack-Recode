package thunder.hack.utility.render.shaders;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.render.WindowResizeCallback;

import java.awt.*;

import static thunder.hack.modules.Module.mc;

public class BlurProgram extends GlProgram {
    private GlUniform uSize;
    private GlUniform uLocation;
    private GlUniform radius;
    private GlUniform inputResolution;
    private GlUniform brightness;
    private GlUniform quality;
    private Framebuffer input;
    private GlUniform color1;

    public BlurProgram() {
        super(new Identifier("thunderhack", "blur"), VertexFormats.POSITION);

        WindowResizeCallback.EVENT.register((client, window) -> {
            if (this.input == null) return;
            this.input.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        });
    }

    public void setParameters(float x, float y, float width, float height, float r, Color c1, float blurStrenth, float blurOpacity) {
        float i = (float) mc.getWindow().getScaleFactor();
        radius.set(r * i);
        uLocation.set(x * i, -y * i + mc.getWindow().getScaledHeight() * i - height * i);
        uSize.set(width * i, height * i);
        this.brightness.set(blurOpacity);
        this.quality.set(blurStrenth);
        color1.set(c1.getRed() / 255f, c1.getGreen() / 255f, c1.getBlue() / 255f, 1f);
    }

    @Override
    public void use() {
        var buffer = MinecraftClient.getInstance().getFramebuffer();

        this.input.beginWrite(false);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer.fbo);
        GL30.glBlitFramebuffer(0, 0, buffer.textureWidth, buffer.textureHeight, 0, 0, buffer.textureWidth, buffer.textureHeight, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_LINEAR);
        buffer.beginWrite(false);

        this.inputResolution.set((float) buffer.textureWidth, (float) buffer.textureHeight);
        this.backingProgram.addSampler("InputSampler", this.input.getColorAttachment());

        super.use();
    }

    @Override
    protected void setup() {
        this.inputResolution = this.findUniform("InputResolution");
        this.brightness = this.findUniform("Brightness");
        this.quality = this.findUniform("Quality");
        this.color1 = findUniform("color1");
        this.uSize = findUniform("uSize");
        this.uLocation = findUniform("uLocation");
        this.radius = findUniform("radius");
        var window = MinecraftClient.getInstance().getWindow();
        this.input = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
    }
}