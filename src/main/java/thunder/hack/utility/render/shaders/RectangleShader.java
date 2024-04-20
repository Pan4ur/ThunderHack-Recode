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

public class RectangleShader extends GlProgram {

    private GlUniform uSize;
    private GlUniform uLocation;
    private GlUniform radius;
    private GlUniform color1;
    private GlUniform color2;
    private GlUniform color3;
    private GlUniform color4;

    private Framebuffer input;

    public RectangleShader() {
        super(new Identifier("thunderhack", "rectangle"), VertexFormats.POSITION);
        WindowResizeCallback.EVENT.register((client, window) -> {
            if (input != null) input.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        });
    }

    public void setParameters(float x, float y, float width, float height, float r, float alpha) {
        float i = (float) mc.getWindow().getScaleFactor();
        radius.set(r * i);
        uLocation.set(x * i, -y * i + mc.getWindow().getScaledHeight() * i - height * i);
        uSize.set(width * i, height * i);

        Color c1 =  HudEditor.getColor(270);
        Color c2 =  HudEditor.getColor(0);
        Color c3 =  HudEditor.getColor(180);
        Color c4 =  HudEditor.getColor(90);

        color1.set(c1.getRed() / 255f, c1.getGreen() / 255f, c1.getBlue() / 255f, alpha);
        color2.set(c2.getRed() / 255f, c2.getGreen() / 255f, c2.getBlue() / 255f, alpha);
        color3.set(c3.getRed() / 255f, c3.getGreen() / 255f, c3.getBlue() / 255f, alpha);
        color4.set(c4.getRed() / 255f, c4.getGreen() / 255f, c4.getBlue() / 255f, alpha);
    }

    public void setParameters(float x, float y, float width, float height, float r, float alpha, Color c1, Color c2, Color c3, Color c4) {
        int i = mc.options.getGuiScale().getValue();
        radius.set(r * i);
        uLocation.set(x * i, -y * i + mc.getWindow().getScaledHeight() * i - height * i);
        uSize.set(width * i, height * i);
        color1.set(c1.getRed() / 255f, c1.getGreen() / 255f, c1.getBlue() / 255f, alpha);
        color2.set(c2.getRed() / 255f, c2.getGreen() / 255f, c2.getBlue() / 255f, alpha);
        color3.set(c3.getRed() / 255f, c3.getGreen() / 255f, c3.getBlue() / 255f, alpha);
        color4.set(c4.getRed() / 255f, c4.getGreen() / 255f, c4.getBlue() / 255f, alpha);
    }

    @Override
    public void use() {
        var buffer = MinecraftClient.getInstance().getFramebuffer();
        input.beginWrite(false);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer.fbo);
        GL30.glBlitFramebuffer(0, 0, buffer.textureWidth, buffer.textureHeight, 0, 0, buffer.textureWidth, buffer.textureHeight, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_LINEAR);
        buffer.beginWrite(false);
        super.use();
    }

    @Override
    protected void setup() {
        uSize = findUniform("uSize");
        uLocation = findUniform("uLocation");
        radius = findUniform("radius");
        color1 = findUniform("color1");
        color2 = findUniform("color2");
        color3 = findUniform("color3");
        color4 = findUniform("color4");
        var window = MinecraftClient.getInstance().getWindow();
        input = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
    }
}