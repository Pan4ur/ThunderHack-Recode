package thunder.hack.injection;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Icons;
import net.minecraft.client.util.MacWindowUtil;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourcePack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.EventPostTick;
import thunder.hack.events.impl.EventTick;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.utility.render.WindowResizeCallback;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static thunder.hack.modules.Module.mc;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Inject(method = "<init>", at = @At("TAIL"))
    void postWindowInit(RunArgs args, CallbackInfo ci) {
        try {
            FontRenderers.settings = FontRenderers.createDefault(12f, "comfortaa");
            FontRenderers.modules = FontRenderers.createDefault(15f, "comfortaa");
            FontRenderers.categories = FontRenderers.createDefault(18f, "comfortaa");
            FontRenderers.thglitch = FontRenderers.createDefault(36f, "glitched");
            FontRenderers.thglitchBig = FontRenderers.createDefault(72f, "glitched");
            FontRenderers.monsterrat = FontRenderers.createDefault(18f, "monsterrat");
            FontRenderers.sf_bold = FontRenderers.createDefault(16f, "sf_bold");
            FontRenderers.sf_medium = FontRenderers.createDefault(16f, "sf_medium");
            FontRenderers.sf_medium_mini = FontRenderers.createDefault(12f, "sf_medium");
            FontRenderers.sf_medium_modules = FontRenderers.createDefault(14f, "sf_medium");
            FontRenderers.sf_bold_mini = FontRenderers.createDefault(14f, "sf_bold");
            FontRenderers.icons = FontRenderers.createIcons(20);
            FontRenderers.mid_icons = FontRenderers.createIcons(46f);
            FontRenderers.big_icons = FontRenderers.createIcons(72f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    void preTickHook(CallbackInfo ci) {
        if (!Module.fullNullCheck()) ThunderHack.EVENT_BUS.post(new EventTick());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    void postTickHook(CallbackInfo ci) {
        if (!Module.fullNullCheck()) ThunderHack.EVENT_BUS.post(new EventPostTick());
    }

    @Shadow
    @Final
    private Window window;

    @Shadow
    private static MinecraftClient instance;

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void captureResize(CallbackInfo ci) {
        WindowResizeCallback.EVENT.invoker().onResized((MinecraftClient) (Object) this, this.window);
    }

    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void doItemPickHook(CallbackInfo ci) {
        if (ModuleManager.middleClick.isEnabled() && ModuleManager.middleClick.antiPickUp.getValue())
            ci.cancel();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setIcon(Lnet/minecraft/resource/ResourcePack;Lnet/minecraft/client/util/Icons;)V"))
    private void onChangeIcon(Window instance, ResourcePack resourcePack, Icons icons) throws IOException {
        RenderSystem.assertInInitPhase();

        if (GLFW.glfwGetPlatform() == 393218) {
            MacWindowUtil.setApplicationIconImage(icons.getMacIcon(resourcePack));
            return;
        }
        setWindowIcon(ThunderHack.class.getResourceAsStream("/icon.png"), ThunderHack.class.getResourceAsStream("/icon.png"));
    }

    public void setWindowIcon(InputStream img16x16, InputStream img32x32) {
        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            GLFWImage.Buffer buffer = GLFWImage.malloc(2, memorystack);
            List<InputStream> imgList = List.of(img16x16, img32x32);
            List<ByteBuffer> buffers = new ArrayList<>();

            for (int i = 0; i < imgList.size(); i++) {
                NativeImage nativeImage = NativeImage.read(imgList.get(i));
                ByteBuffer bytebuffer = MemoryUtil.memAlloc(nativeImage.getWidth() * nativeImage.getHeight() * 4);

                bytebuffer.asIntBuffer().put(nativeImage.copyPixelsRgba());
                buffer.position(i);
                buffer.width(nativeImage.getWidth());
                buffer.height(nativeImage.getHeight());
                buffer.pixels(bytebuffer);

                buffers.add(bytebuffer);
            }

            GLFW.glfwSetWindowIcon(mc.getWindow().getHandle(), buffer);
            buffers.forEach(MemoryUtil::memFree);
        } catch (IOException ignored) {
        }
    }
}
