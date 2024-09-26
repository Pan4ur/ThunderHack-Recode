package thunder.hack.utility.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import thunder.hack.utility.render.shaders.satin.api.managed.ManagedCoreShader;
import thunder.hack.utility.render.shaders.satin.api.managed.ShaderEffectManager;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform1f;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform2f;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform4f;
import thunder.hack.features.modules.client.HudEditor;

import java.awt.*;

import static thunder.hack.features.modules.Module.mc;

public class RectangleShader {
    private Uniform2f uSize;
    private Uniform2f uLocation;
    private Uniform1f radius;
    private Uniform4f color1;
    private Uniform4f color2;
    private Uniform4f color3;
    private Uniform4f color4;

    public static final ManagedCoreShader RECTANGLE_SHADER = ShaderEffectManager.getInstance()
            .manageCoreShader(Identifier.of("thunderhack", "rectangle"), VertexFormats.POSITION);

    public RectangleShader() {
        setup();
    }

    public void setParameters(float x, float y, float width, float height, float r, float alpha) {
        float i = (float) mc.getWindow().getScaleFactor();
        radius.set(r * i);
        uLocation.set(x * i, -y * i + mc.getWindow().getScaledHeight() * i - height * i);
        uSize.set(width * i, height * i);

        Color c1 = HudEditor.getColor(270);
        Color c2 = HudEditor.getColor(0);
        Color c3 = HudEditor.getColor(180);
        Color c4 = HudEditor.getColor(90);

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

    public void use() {
        RenderSystem.setShader(RECTANGLE_SHADER::getProgram);
    }

    protected void setup() {
        uSize = RECTANGLE_SHADER.findUniform2f("uSize");
        uLocation = RECTANGLE_SHADER.findUniform2f("uLocation");
        radius = RECTANGLE_SHADER.findUniform1f("radius");
        color1 = RECTANGLE_SHADER.findUniform4f("color1");
        color2 = RECTANGLE_SHADER.findUniform4f("color2");
        color3 = RECTANGLE_SHADER.findUniform4f("color3");
        color4 = RECTANGLE_SHADER.findUniform4f("color4");
    }
}