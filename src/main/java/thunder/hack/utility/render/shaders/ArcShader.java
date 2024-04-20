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

public class ArcShader extends GlProgram {

    private GlUniform uLocation;
    private GlUniform uSize;
    private GlUniform radius;
    private GlUniform thickness;
    private GlUniform time;
    private GlUniform color1;
    private GlUniform color2;
    private GlUniform start;
    private GlUniform end;

    private Framebuffer input;

    public ArcShader() {
        super(new Identifier("thunderhack", "arc"), VertexFormats.POSITION);
        WindowResizeCallback.EVENT.register((client, window) -> {
            if (input != null)
                input.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        });
    }

    public void setParameters(float x, float y, float width, float height, float r, float thickness, float start, float end) {
        if(mc.player == null)
            return;
        float i = (float) mc.getWindow().getScaleFactor();
        radius.set(r * i);
        uLocation.set(x * i, -y * i + mc.getWindow().getScaledHeight() * i - height * i);
        uSize.set(width * i, height * i);

        Color c1 = HudEditor.hcolor1.getValue().getColorObject();
        Color c2 = HudEditor.acolor.getValue().getColorObject();
        color1.set(c1.getRed() / 255f, c1.getGreen() / 255f, c1.getBlue() / 255f, 1f);
        color2.set(c2.getRed() / 255f, c2.getGreen() / 255f, c2.getBlue() / 255f, 1f);
        time.set((float)mc.player.age * 4);
        this.thickness.set(thickness);
        this.start.set(start);
        this.end.set(end);
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
        thickness = findUniform("thickness");
        start = findUniform("start");
        end = findUniform("end");
        time = findUniform("time");
        color1 = findUniform("color1");
        color2 = findUniform("color2");
        var window = MinecraftClient.getInstance().getWindow();
        input = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
    }
}
