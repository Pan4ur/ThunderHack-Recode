package thunder.hack.utility.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import thunder.hack.utility.interfaces.IShaderEffect;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static thunder.hack.modules.Module.mc;

public class OutlineProgram {

    PostEffectProcessor shader = null;
    int previousWidth, previousHeight;

    private OutlineProgram(Identifier ident, Consumer<OutlineProgram> init) {
        PostEffectProcessor shader1;
        try {
            shader = new PostEffectProcessor(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), ident);
          //  checkUpdateDimensions();
        } catch (Exception e){
            e.printStackTrace();
        }
        init.accept(this);
        //checkUpdateDimensions();
    }

    public static OutlineProgram create(Consumer<OutlineProgram> callback) {
        return new OutlineProgram(new Identifier("thunderhack","shaders/post/outline.json"), callback);
    }

    void checkUpdateDimensions() {
        int currentWidth = mc.getWindow().getFramebufferWidth();
        int currentHeight = mc.getWindow().getFramebufferHeight();
        if (previousWidth != currentWidth || previousHeight != currentHeight) {
            this.shader.setupDimensions(currentWidth, currentHeight);
            previousWidth = currentWidth;
            previousHeight = currentHeight;
        }
    }

    public void setColor( Color color) {
        List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("color")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(color.getRed() / 255f,color.getGreen() / 255f,color.getBlue() / 255f,color.getAlpha() / 255f));
    }

    public void setOutlineColor( Color color) {
        List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("outlinecolor")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(color.getRed() / 255f,color.getGreen() / 255f,color.getBlue() / 255f,color.getAlpha() / 255f));
    }

    public void setLineWidth(int val) {
        List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("lineWidth")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(val));
    }

    public void setQuality(int val) {
        List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("quality")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(val));
    }

    public void setAlpha(int val, boolean gradient) {
        List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("alpha0")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(gradient ? -1.0f : val / 255.0f));
    }

    public void render(float delta) {
        checkUpdateDimensions();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.resetTextureMatrix();
        shader.render(delta);
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        RenderSystem.disableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
    }

    public PostEffectProcessor getShader() {
        return shader;
    }
}
