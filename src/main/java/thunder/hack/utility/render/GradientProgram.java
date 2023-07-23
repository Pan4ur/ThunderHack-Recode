package thunder.hack.utility.render;

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

public class GradientProgram {
    PostEffectProcessor shader = null;
    int previousWidth, previousHeight;
    public float time = 0;

    private GradientProgram(Identifier ident, Consumer<GradientProgram> init) {
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

    public static GradientProgram create(Consumer<GradientProgram> callback) {
        return new GradientProgram(new Identifier("thunderhack","shaders/post/gradient.json"), callback);
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

    public void setQuality(int val) {
        java.util.List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("quality")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(val));
    }

    public void setLineWidth(int val) {
        java.util.List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("lineWidth")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(val));
    }

    public void setAlpha2(int val) {
        List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("alpha2")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(val / 255.0f));
    }

    public void setOctaves(int val) {
        java.util.List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("oct")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(val));
    }

    public void setTime() {
        java.util.List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("time")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(time));
    }

    public void setResolution(float x, float y) {
        java.util.List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("resolution")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(x,y));
    }

    public void setAlpha(int val, boolean gradient) {
        java.util.List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("alpha0")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(gradient ? -1.0f : val / 255.0f));
    }

    public void setAlpha1(int val) {
        List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("alpha1")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(val / 255.0f));
    }

    public void setMoreGradient(float val) {
        List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("moreGradient")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(val));
    }

    public void setFactor(float val) {
        List<PostEffectPass> passes = ((IShaderEffect) shader).getPassesHook();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName("factor")).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(val));
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
        update(0.1);
    }

    public PostEffectProcessor getShader() {
        return shader;
    }

    public void update(double speed) {
        this.time += speed;
    }
}
