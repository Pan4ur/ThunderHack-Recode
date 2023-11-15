package thunder.hack.injection;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

import static thunder.hack.modules.Module.mc;

@Mixin(DownloadingTerrainScreen.class)
public class MixinDownloadingTerrainScreen {
    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"), cancellable = true)
    public void renderHook(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (ModuleManager.mystFinder.isEnabled()) {
            ci.cancel();
            Render2DEngine.drawRect(context.getMatrices(), 0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), new Color(0x252525));
            FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "Ищем мистик...", mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f - 50, 0xFFFFFF);
            FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "Прошло времени: " + (int) (ModuleManager.mystFinder.timeOut.getTimeMs() / 1000) + " c", mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f - 40, 0xFFFFFF);

            if (ModuleManager.mystFinder.events.isEmpty())
                return;
            int offsetY = 10;
            for (String event : ModuleManager.mystFinder.events) {
                FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), event, mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f - (40 - offsetY), 0xFFFFFF);
                offsetY += 10;
            }
        }
    }
}
