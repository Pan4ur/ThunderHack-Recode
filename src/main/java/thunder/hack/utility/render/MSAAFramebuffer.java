package thunder.hack.utility.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MSAAFramebuffer extends Framebuffer {
    public static final int MIN_SAMPLES = 2;
    public static final int MAX_SAMPLES = GL30.glGetInteger(GL30C.GL_MAX_SAMPLES);

    private static final Map<Integer, MSAAFramebuffer> INSTANCES = new HashMap<>();
    private static final List<MSAAFramebuffer> ACTIVE_INSTANCES = new ArrayList<>();

    private final int samples;
    private int rboColor;
    private int rboDepth;
    private boolean inUse;

    public MSAAFramebuffer(int samples) {
        super(true);
        if (samples < MIN_SAMPLES || samples > MAX_SAMPLES) {
            throw new IllegalArgumentException(String.format("The number of samples should be >= %s and <= %s, got %s.", MIN_SAMPLES, MAX_SAMPLES, samples));
        }
        if ((samples & (samples - 1)) != 0) {
            throw new IllegalArgumentException("The number of samples must be a power of two.");
        }

        this.samples = samples;
        this.setClearColor(1F, 1F, 1F, 0F);
    }

    public static boolean framebufferInUse() {
        return !ACTIVE_INSTANCES.isEmpty();
    }

    public static MSAAFramebuffer getInstance(int samples) {
        return INSTANCES.computeIfAbsent(samples, x -> new MSAAFramebuffer(samples));
    }

    public static void use(Runnable drawAction) {
        use(Math.min(16, MAX_SAMPLES), MinecraftClient.getInstance().getFramebuffer(), drawAction);
    }

    public static void use(int samples, Framebuffer mainBuffer, Runnable drawAction) {
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
        if (this.textureWidth != width || this.textureHeight != height) {
            super.resize(width, height, getError);
        }
    }

    @Override
    public void initFbo(int width, int height, boolean getError) {
        RenderSystem.assertOnRenderThreadOrInit();
        int maxSize = RenderSystem.maxSupportedTextureSize();
        if (width <= 0 || width > maxSize || height <= 0 || height > maxSize) {
            throw new IllegalArgumentException("Window " + width + "x" + height + " size out of bounds (max. size: " + maxSize + ")");
        }

        this.viewportWidth = width;
        this.viewportHeight = height;
        this.textureWidth = width;
        this.textureHeight = height;

        this.fbo = GlStateManager.glGenFramebuffers();
        GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, this.fbo);

        this.rboColor = GlStateManager.glGenRenderbuffers();
        GlStateManager._glBindRenderbuffer(GL30C.GL_RENDERBUFFER, this.rboColor);
        GL30.glRenderbufferStorageMultisample(GL30C.GL_RENDERBUFFER, samples, GL30C.GL_RGBA8, width, height);
        GlStateManager._glBindRenderbuffer(GL30C.GL_RENDERBUFFER, 0);

        this.rboDepth = GlStateManager.glGenRenderbuffers();
        GlStateManager._glBindRenderbuffer(GL30C.GL_RENDERBUFFER, this.rboDepth);
        GL30.glRenderbufferStorageMultisample(GL30C.GL_RENDERBUFFER, samples, GL30C.GL_DEPTH_COMPONENT, width, height);
        GlStateManager._glBindRenderbuffer(GL30C.GL_RENDERBUFFER, 0);

        GL30.glFramebufferRenderbuffer(GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, GL30C.GL_RENDERBUFFER, this.rboColor);
        GL30.glFramebufferRenderbuffer(GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_RENDERBUFFER, this.rboDepth);

        this.colorAttachment = MinecraftClient.getInstance().getFramebuffer().getColorAttachment();
        this.depthAttachment = MinecraftClient.getInstance().getFramebuffer().getDepthAttachment();

        this.checkFramebufferStatus();
        this.clear(getError);
        this.endRead();
    }

    @Override
    public void delete() {
        RenderSystem.assertOnRenderThreadOrInit();
        this.endRead();
        this.endWrite();

        if (this.fbo > -1) {
            GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
            GlStateManager._glDeleteFramebuffers(this.fbo);
            this.fbo = -1;
        }

        if (this.rboColor > -1) {
            GlStateManager._glDeleteRenderbuffers(this.rboColor);
            this.rboColor = -1;
        }

        if (this.rboDepth > -1) {
            GlStateManager._glDeleteRenderbuffers(this.rboDepth);
            this.rboDepth = -1;
        }

        this.colorAttachment = -1;
        this.depthAttachment = -1;
        this.textureWidth = -1;
        this.textureHeight = -1;
    }

    @Override
    public void beginWrite(boolean setViewport) {
        super.beginWrite(setViewport);
        if (!this.inUse) {
            ACTIVE_INSTANCES.add(this);
            this.inUse = true;
        }
    }

    @Override
    public void endWrite() {
        super.endWrite();
        if (this.inUse) {
            this.inUse = false;
            ACTIVE_INSTANCES.remove(this);
        }
    }
}