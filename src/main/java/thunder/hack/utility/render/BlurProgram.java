package thunder.hack.utility.render;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;



public class BlurProgram extends GlProgram {

    private GlUniform inputResolution;
    private GlUniform directions;
    private GlUniform quality;
    private GlUniform size;
    private Framebuffer input;

    public BlurProgram() {
        super(new Identifier("thunderhack", "blur"), VertexFormats.POSITION);
        WindowResizeCallback.EVENT.register((client, window) -> {
            if (this.input == null) return;
            this.input.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        });
    }

    public void setParameters(int directions, float quality, float size) {
        this.directions.set((float) directions);
        this.size.set(size);
        this.quality.set(quality);
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
        this.directions = this.findUniform("Directions");
        this.quality = this.findUniform("Quality");
        this.size = this.findUniform("Size");
        var window = MinecraftClient.getInstance().getWindow();
        this.input = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
    }
}