package thunder.hack.utility.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;

import java.util.HashMap;
import java.util.Map;

public class MSAAFramebuffer extends Framebuffer {
    public static final int MAX_SAMPLES = GL30.glGetInteger(GL30C.GL_MAX_SAMPLES);
    private static final Map<Integer, MSAAFramebuffer> INSTANCES = new HashMap<>();

    private final int samples;
    private int rboColor;
    private int rboDepth;

    public MSAAFramebuffer(int samples) {
        super(true);
        this.samples = samples;
        setClearColor(1F, 1F, 1F, 0F);
    }

    public static MSAAFramebuffer getInstance(int samples) {
        return INSTANCES.computeIfAbsent(samples, x -> new MSAAFramebuffer(samples));
    }

    public static void use(boolean fancy, Runnable drawAction) {
        use(Math.min(fancy ? 16 : 4, MAX_SAMPLES), MinecraftClient.getInstance().getFramebuffer(), drawAction);
    }

    public static void use(int samples, @NotNull Framebuffer mainBuffer, @NotNull Runnable drawAction) {
        RenderSystem.assertOnRenderThreadOrInit();
        MSAAFramebuffer msaaBuffer = MSAAFramebuffer.getInstance(samples);
        msaaBuffer.resize(mainBuffer.textureWidth, mainBuffer.textureHeight, false);

        GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, mainBuffer.fbo);
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, msaaBuffer.fbo);
        GlStateManager._glBlitFrameBuffer(0, 0, msaaBuffer.textureWidth, msaaBuffer.textureHeight, 0, 0, msaaBuffer.textureWidth, msaaBuffer.textureHeight, GL30C.GL_COLOR_BUFFER_BIT, GL30C.GL_LINEAR);
        msaaBuffer.beginWrite(true);

        drawAction.run();
        msaaBuffer.endWrite();

        GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, msaaBuffer.fbo);
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, mainBuffer.fbo);
        GlStateManager._glBlitFrameBuffer(0, 0, msaaBuffer.textureWidth, msaaBuffer.textureHeight, 0, 0, msaaBuffer.textureWidth, msaaBuffer.textureHeight, GL30C.GL_COLOR_BUFFER_BIT, GL30C.GL_LINEAR);
        msaaBuffer.clear(false);
        mainBuffer.beginWrite(false);
    }

    @Override
    public void resize(int width, int height, boolean getError) {
        if (textureWidth != width || textureHeight != height) {
            super.resize(width, height, getError);
        }
    }

    @Override
    public void initFbo(int width, int height, boolean getError) {
        RenderSystem.assertOnRenderThreadOrInit();
        viewportWidth = width;
        viewportHeight = height;
        textureWidth = width;
        textureHeight = height;

        fbo = GlStateManager.glGenFramebuffers();
        GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, fbo);

        rboColor = GlStateManager.glGenRenderbuffers();
        GlStateManager._glBindRenderbuffer(GL30C.GL_RENDERBUFFER, rboColor);
        GL30.glRenderbufferStorageMultisample(GL30C.GL_RENDERBUFFER, samples, GL30C.GL_RGBA8, width, height);
        GlStateManager._glBindRenderbuffer(GL30C.GL_RENDERBUFFER, 0);

        rboDepth = GlStateManager.glGenRenderbuffers();
        GlStateManager._glBindRenderbuffer(GL30C.GL_RENDERBUFFER, rboDepth);
        GL30.glRenderbufferStorageMultisample(GL30C.GL_RENDERBUFFER, samples, GL30C.GL_DEPTH_COMPONENT, width, height);
        GlStateManager._glBindRenderbuffer(GL30C.GL_RENDERBUFFER, 0);

        GL30.glFramebufferRenderbuffer(GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, GL30C.GL_RENDERBUFFER, rboColor);
        GL30.glFramebufferRenderbuffer(GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_RENDERBUFFER, rboDepth);

        colorAttachment = MinecraftClient.getInstance().getFramebuffer().getColorAttachment();
        depthAttachment = MinecraftClient.getInstance().getFramebuffer().getDepthAttachment();

        checkFramebufferStatus();
        clear(getError);
        endRead();
    }

    @Override
    public void delete() {
        RenderSystem.assertOnRenderThreadOrInit();
        endRead();
        endWrite();

        if (fbo > -1) {
            GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
            GlStateManager._glDeleteFramebuffers(fbo);
            fbo = -1;
        }

        if (rboColor > -1) {
            GlStateManager._glDeleteRenderbuffers(rboColor);
            rboColor = -1;
        }

        if (rboDepth > -1) {
            GlStateManager._glDeleteRenderbuffers(rboDepth);
            rboDepth = -1;
        }

        colorAttachment = -1;
        depthAttachment = -1;
        textureWidth = -1;
        textureHeight = -1;
    }
}