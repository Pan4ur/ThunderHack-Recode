package dev.thunderhack.mixins;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.gui.mainmenu.MainMenuScreen;
import dev.thunderhack.modules.client.MainSettings;

import java.net.URI;

import static dev.thunderhack.modules.Module.mc;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {
    protected MixinTitleScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void postInitHook(CallbackInfo ci) {
        if (MainSettings.customMainMenu.getValue() && !MainMenuScreen.getInstance().confirm) {
            mc.setScreen(MainMenuScreen.getInstance());
        }

        if (ThunderHack.isOutdated && !FabricLoader.getInstance().isDevelopmentEnvironment()) {
            mc.setScreen(new ConfirmScreen(
                    confirm -> {
                        if (confirm) Util.getOperatingSystem().open(URI.create("https://thunderhack.onrender.com/"));
                        else mc.stop();
                    },
                    Text.of(Formatting.RED + "You are using an outdated version of ThunderHack Recode"), Text.of("Please update to the latest release"), Text.of("Download"), Text.of("Quit Game")));
        }
    }
}
