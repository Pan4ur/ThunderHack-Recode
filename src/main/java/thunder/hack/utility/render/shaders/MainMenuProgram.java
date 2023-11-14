package thunder.hack.utility.render.shaders;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;
import thunder.hack.utility.render.WindowResizeCallback;

import static thunder.hack.modules.Module.mc;

public class MainMenuProgram extends GlProgram {

    private GlUniform Time;
    private GlUniform uSize;
    private Framebuffer input;
    public static float time_ = 0f;

    public MainMenuProgram() {
        super(new Identifier("thunderhack", "mainmenu"), VertexFormats.POSITION);
        WindowResizeCallback.EVENT.register((client, window) -> {
            if (this.input == null) return;
            this.input.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        });
    }

    public void setParameters(float x, float y, float width, float height) {
        this.uSize.set(width * mc.options.getGuiScale().getValue(), height * mc.options.getGuiScale().getValue());
        this.Time.set((float) time_);
    }

    public static void increaseTime() {
        time_ += 0.01f;
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
        var window = MinecraftClient.getInstance().getWindow();
        this.input = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
    }
}
