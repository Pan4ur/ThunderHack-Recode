package thunder.hack.utility.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.utility.render.shaders.satin.api.managed.ManagedCoreShader;
import thunder.hack.utility.render.shaders.satin.api.managed.ShaderEffectManager;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform1f;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform2f;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform4f;

import java.awt.*;

import static thunder.hack.features.modules.Module.mc;

public class HudShader {
    private Uniform2f uSize;
    private Uniform2f uLocation;
    private Uniform1f radius;
    private Uniform1f blend;
    private Uniform1f alpha;
    private Uniform1f outline;
    private Uniform1f glow;
    private Uniform4f color1;
    private Uniform4f color2;
    private Uniform4f color3;
    private Uniform4f color4;

    public static final ManagedCoreShader HUD_SHADER = ShaderEffectManager.getInstance()
            .manageCoreShader(Identifier.of("thunderhack", "hudshader"), VertexFormats.POSITION);

    public HudShader() {
        setup();
    }

    public void setParameters(float x, float y, float width, float height, float r, float externalAlpha, float internalAlpha) {
        float i = (float) mc.getWindow().getScaleFactor();
        radius.set(r * i);
        uLocation.set(x * i, -y * i + mc.getWindow().getScaledHeight() * i - height * i);
        uSize.set(width * i, height * i);

        Color c1 = HudEditor.getColor(270);
        Color c2 = HudEditor.getColor(0);
        Color c3 = HudEditor.getColor(180);
        Color c4 = HudEditor.getColor(90);

        color1.set(c1.getRed() / 255f, c1.getGreen() / 255f, c1.getBlue() / 255f, externalAlpha);
        color2.set(c2.getRed() / 255f, c2.getGreen() / 255f, c2.getBlue() / 255f, externalAlpha);
        color3.set(c3.getRed() / 255f, c3.getGreen() / 255f, c3.getBlue() / 255f, externalAlpha);
        color4.set(c4.getRed() / 255f, c4.getGreen() / 255f, c4.getBlue() / 255f, externalAlpha);
        blend.set(HudEditor.blend.getValue());
        outline.set(HudEditor.outline.getValue());
        glow.set(HudEditor.glow1.getValue());
        alpha.set(internalAlpha);
    }

    public void use() {
        RenderSystem.setShader(HUD_SHADER::getProgram);
    }

    public void setup() {
        uSize = HUD_SHADER.findUniform2f("uSize");
        uLocation = HUD_SHADER.findUniform2f("uLocation");
        radius = HUD_SHADER.findUniform1f("radius");
        blend = HUD_SHADER.findUniform1f("blend");
        alpha = HUD_SHADER.findUniform1f("alpha");
        color1 = HUD_SHADER.findUniform4f("color1");
        color2 = HUD_SHADER.findUniform4f("color2");
        color3 = HUD_SHADER.findUniform4f("color3");
        color4 = HUD_SHADER.findUniform4f("color4");
        outline = HUD_SHADER.findUniform1f("outline");
        glow = HUD_SHADER.findUniform1f("glow");
    }
}