package thunder.hack.injection;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Style;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.CommandManager;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.mainmenu.CreditsScreen;
import thunder.hack.gui.mainmenu.MainMenuScreen;
import thunder.hack.gui.misc.DialogScreen;
import thunder.hack.modules.client.ClientSettings;
import thunder.hack.utility.ClientClickEvent;
import thunder.hack.utility.render.Render2DEngine;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static thunder.hack.modules.Module.mc;
import static thunder.hack.modules.client.ClientSettings.isRu;

@Mixin(Screen.class)
public abstract class MixinScreen {
    /*@ModifyArgs(method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V"))
    private void getList(Args args, MatrixStack matrixStack, ItemStack itemStack, int x, int y) {
        Tooltips tooltips = (Tooltips) Thunderhack.moduleRegistry.get(Tooltips.class);

        if (hasItems(itemStack) && tooltips.storage.getValue() || (itemStack.getItem() == Items.ENDER_CHEST && tooltips.echest.getValue())) {
            //if(args.size() < 3) return;
            //List<Text> lines = args.get(1);

            //int yChanged = y - 4;
            //yChanged -= 10 * lines.size();

            args.set(4, 30);
        }
    }*/

    @Inject(method = "handleTextClick", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", ordinal = 1, remap = false), cancellable = true)
    private void onRunCommand(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (Objects.requireNonNull(style.getClickEvent()) instanceof ClientClickEvent clientClickEvent && clientClickEvent.getValue().startsWith(ThunderHack.commandManager.getPrefix()))
            try {
                CommandManager manager = ThunderHack.commandManager;
                manager.getDispatcher().execute(style.getClickEvent().getValue().substring(ThunderHack.commandManager.getPrefix().length()), manager.getSource());
                cir.setReturnValue(true);
            } catch (CommandSyntaxException ignored) {
            }
    }

    @Inject(method = "filesDragged", at = @At("HEAD"), cancellable = true)
    public void filesDragged(List<Path> paths, CallbackInfo ci) {
        //  Command.sendMessage(paths.get(0).toString());
        String configPath = paths.get(0).toString();
        File cfgFile = new File(configPath);
        String configName = cfgFile.getName();
        if(!configName.contains(".th"))
            return;

        DialogScreen dialogScreen = new DialogScreen(isRu() ? "Обнаружен конфиг!" : "Config detected!",
                isRu() ? "Ты действительно хочешь загрузить " + configName + "?" : "Are you sure you want to load " + configName + "?",
                isRu() ? "Да ебать" : "Do it, piece of shit!", isRu() ? "Не, че за хуйня?" : "Nooo fuck ur ass nigga!",
                () -> {
                    ThunderHack.moduleManager.onUnload();
                    ThunderHack.moduleManager.onUnloadPost();
                    ThunderHack.configManager.load(cfgFile);
                    ThunderHack.moduleManager.onLoad();
                    mc.setScreen(null);
                }, () -> mc.setScreen(null));

        mc.setScreen(dialogScreen);
    }

    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
    public void renderPanoramaBackgroundHook(DrawContext context, float delta, CallbackInfo ci) {
        if(ClientSettings.customMainMenu.getValue() && mc.world == null) {
            ci.cancel();
            Render2DEngine.drawMainMenuShader(context.getMatrices(), 0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
        }
    }
}