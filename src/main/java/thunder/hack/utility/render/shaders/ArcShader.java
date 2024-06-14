package thunder.hack.utility.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.ladysnake.satin.api.managed.ManagedCoreShader;
import org.ladysnake.satin.api.managed.ShaderEffectManager;
import org.ladysnake.satin.api.managed.uniform.Uniform1f;
import org.ladysnake.satin.api.managed.uniform.Uniform2f;
import org.ladysnake.satin.api.managed.uniform.Uniform4f;
import org.lwjgl.opengl.GL30;
import thunder.hack.modules.client.HudEditor;

import java.awt.*;

import static thunder.hack.modules.Module.mc;

public class ArcShader {

    private Uniform2f uLocation;
    private Uniform2f uSize;
    private Uniform1f radius;
    private Uniform1f thickness;
    private Uniform1f time;
    private Uniform4f color1;
    private Uniform4f color2;
    private Uniform1f start;
    private Uniform1f end;

    public static final ManagedCoreShader ARC = ShaderEffectManager.getInstance()
            .manageCoreShader(Identifier.of("thunderhack", "arc"), VertexFormats.POSITION);

    public ArcShader() {
        setup();
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

    public void use() {
        RenderSystem.setShader(ARC::getProgram);
    }

    protected void setup() {
        uSize = ARC.findUniform2f("uSize");
        uLocation = ARC.findUniform2f("uLocation");
        radius = ARC.findUniform1f("radius");
        thickness = ARC.findUniform1f("thickness");
        start = ARC.findUniform1f("start");
        end = ARC.findUniform1f("end");
        time = ARC.findUniform1f("time");
        color1 = ARC.findUniform4f("color1");
        color2 = ARC.findUniform4f("color2");
    }
}
