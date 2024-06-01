package thunder.hack.utility.render.shaders;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.render.WindowResizeCallback;
import thunder.hack.utility.render.animation.AnimationUtility;

import static thunder.hack.modules.Module.mc;

public class MainMenuProgram extends GlProgram {

    private GlUniform Time;
    private GlUniform uSize;
    private GlUniform color;
    private Framebuffer input;
    public static float time_ = 10000f;

    public MainMenuProgram() {
        super(new Identifier("thunderhack", "mainmenu"), VertexFormats.POSITION);
        WindowResizeCallback.EVENT.register((client, window) -> {
            if (this.input == null) return;
            this.input.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        });
    }

    public void setParameters(float x, float y, float width, float height) {
        float i = (float) mc.getWindow().getScaleFactor();
        this.uSize.set(width * i, height * i);
        time_ += (float) (0.55 * AnimationUtility.deltaTime());
        this.Time.set((float) time_);
        //     this.color.set(HudEditor.getColor(0).getRed() / 255f, HudEditor.getColor(0).getGreen() / 255f, HudEditor.getColor(0).getBlue() / 255f, HudEditor.getColor(0).getAlpha() / 255f);
    }

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
        this.Time = this.findUniform("Time");
        this.color = this.findUniform("color");
        var window = MinecraftClient.getInstance().getWindow();
        this.input = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
    }
}
