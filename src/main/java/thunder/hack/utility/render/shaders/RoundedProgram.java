package thunder.hack.utility.render.shaders;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;
import thunder.hack.utility.Util;
import thunder.hack.utility.render.WindowResizeCallback;

import java.awt.*;

public class RoundedProgram extends GlProgram {

    private GlUniform uSize;
    private GlUniform uLocation;
    private GlUniform size;
    private GlUniform color;

    private Framebuffer input;

    public RoundedProgram() {
        super(new Identifier("thunderhack", "round"), VertexFormats.POSITION);
        WindowResizeCallback.EVENT.register((client, window) -> {
            if (this.input == null) return;
            this.input.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        });
    }

    public void setParameters(float x, float y, float width, float height, float radius, Color color) {
        this.size.set(radius * 2);
        this.uSize.set(width * 2,height * 2);
        this.uLocation.set(x * 2,-y * 2 + Util.getScaledResolution().getScaledHeight() * 2 - height * 2);
        this.color.set(color.getRed() / 255f,color.getGreen() / 255f,color.getBlue() / 255f,color.getAlpha() / 255f);
    }

    /*
     public int normaliseY() {
        ScaledResolution sr = new ScaledResolution(mc);
        return (((-Mouse.getY() + sr.getScaledHeight()) + sr.getScaledHeight()) / 2);
    }
     */
    @Override
    public void use() {
        var buffer = MinecraftClient.getInstance().getFramebuffer();
        this.input.beginWrite(false);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer.fbo);
        GL30.glBlitFramebuffer(0, 0, buffer.textureWidth, buffer.textureHeight, 0, 0, buffer.textureWidth, buffer.textureHeight, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_LINEAR);
        buffer.beginWrite(false);
        super.use();
    }

    @Override
    protected void setup() {
        this.uSize = this.findUniform("uSize");
        this.uLocation = this.findUniform("uLocation");
        this.size = this.findUniform("Size");
        this.color = this.findUniform("color");
        var window = MinecraftClient.getInstance().getWindow();
        this.input = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
    }
}
