package thunder.hack.utility.render.shaders;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;
import thunder.hack.utility.render.WindowResizeCallback;

import java.awt.*;

import static thunder.hack.modules.Module.mc;

public class TextureColorProgram  extends GlProgram {

    public TextureColorProgram() {
        super(new Identifier("thunderhack", "position_tex_color2"), VertexFormats.POSITION);
    }

    public void setParameters(float x, float y, float width, float height, float radius, Color color) {
        int i = mc.options.getGuiScale().getValue();
    }

    @Override
    public void use() {
        /*
        var buffer = MinecraftClient.getInstance().getFramebuffer();
        this.input.beginWrite(false);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer.fbo);
        GL30.glBlitFramebuffer(0, 0, buffer.textureWidth, buffer.textureHeight, 0, 0, buffer.textureWidth, buffer.textureHeight, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_LINEAR);
        buffer.beginWrite(false);

         */
        super.use();
    }

    @Override
    protected void setup() {

    }
}
