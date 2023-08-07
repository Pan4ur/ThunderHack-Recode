package thunder.hack.injection;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventPostTick;
import thunder.hack.events.impl.EventTick;
import thunder.hack.gui.font.FontRenderers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.modules.Module;
import thunder.hack.utility.render.WindowResizeCallback;
import net.minecraft.client.util.Window;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Inject(method = "<init>", at = @At("TAIL"))
    void postWindowInit(RunArgs args, CallbackInfo ci) {
        try {
            FontRenderers.settings = FontRenderers.createDefault(12f, "comfortaa");
            FontRenderers.modules = FontRenderers.createDefault(15f, "comfortaa");
            FontRenderers.categories = FontRenderers.createDefault(18f, "comfortaa");
            FontRenderers.thglitch = FontRenderers.createDefault(36f, "glitched");
            FontRenderers.monsterrat = FontRenderers.createDefault(18f, "monsterrat");
            FontRenderers.sf_bold = FontRenderers.createDefault(16f, "sf_bold");
            FontRenderers.sf_medium = FontRenderers.createDefault(16f, "sf_medium");
            FontRenderers.sf_medium_mini = FontRenderers.createDefault(12f, "sf_medium");
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
        if (!Module.fullNullCheck()) Thunderhack.EVENT_BUS.post(new EventTick());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    void postTickHook(CallbackInfo ci) {
        if (!Module.fullNullCheck()) Thunderhack.EVENT_BUS.post(new EventPostTick());
    }


    @Shadow
    @Final
    private Window window;

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void captureResize(CallbackInfo ci) {
        WindowResizeCallback.EVENT.invoker().onResized((MinecraftClient) (Object) this, this.window);
    }
}
