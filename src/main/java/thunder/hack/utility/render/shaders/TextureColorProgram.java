package thunder.hack.utility.render.shaders;

import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class TextureColorProgram extends GlProgram {

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
