package thunder.hack.utility.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import thunder.hack.utility.render.animation.AnimationUtility;
import thunder.hack.utility.render.shaders.satin.api.managed.ManagedCoreShader;
import thunder.hack.utility.render.shaders.satin.api.managed.ShaderEffectManager;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform1f;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform2f;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform4f;

import static thunder.hack.features.modules.Module.mc;

public class MainMenuProgram {
    private Uniform1f Time;
    private Uniform2f uSize;
    private Uniform4f color;
    public static float time_ = 10000f;

    public static final ManagedCoreShader MAIN_MENU = ShaderEffectManager.getInstance()
            .manageCoreShader(Identifier.of("thunderhack", "mainmenu"), VertexFormats.POSITION);

    public MainMenuProgram() {
        setup();
    }

    public void setParameters(float x, float y, float width, float height) {
        float i = (float) mc.getWindow().getScaleFactor();
        this.uSize.set(width * i, height * i);
        time_ += (float) (0.55 * AnimationUtility.deltaTime());
        this.Time.set((float) time_);
        //     this.color.set(HudEditor.getColor(0).getRed() / 255f, HudEditor.getColor(0).getGreen() / 255f, HudEditor.getColor(0).getBlue() / 255f, HudEditor.getColor(0).getAlpha() / 255f);
    }

    public void use() {
        RenderSystem.setShader(MAIN_MENU::getProgram);
    }

    protected void setup() {
        uSize = MAIN_MENU.findUniform2f("uSize");
        Time = MAIN_MENU.findUniform1f("Time");
        color = MAIN_MENU.findUniform4f("color");
    }
}