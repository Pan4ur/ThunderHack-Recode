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

    @Override
    public void use() {
        super.use();
    }

    @Override
    protected void setup() {

    }
}
