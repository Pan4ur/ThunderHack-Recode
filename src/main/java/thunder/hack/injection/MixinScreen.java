package thunder.hack.injection;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.CommandManager;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.core.manager.client.ProxyManager;
import thunder.hack.events.impl.ClientClickEvent;
import thunder.hack.gui.misc.DialogScreen;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static thunder.hack.features.modules.Module.mc;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

@Mixin(Screen.class)
public abstract class MixinScreen {
    @Shadow
    public abstract void init(MinecraftClient client, int width, int height);

    @Inject(method = "handleTextClick", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", ordinal = 1, remap = false), cancellable = true)
    private void onRunCommand(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (Objects.requireNonNull(style.getClickEvent()) instanceof ClientClickEvent clientClickEvent && clientClickEvent.getValue().startsWith(Managers.COMMAND.getPrefix()))
            try {
                CommandManager manager = Managers.COMMAND;
                manager.getDispatcher().execute(style.getClickEvent().getValue().substring(Managers.COMMAND.getPrefix().length()), manager.getSource());
                cir.setReturnValue(true);
            } catch (CommandSyntaxException ignored) {
            }
    }

    @Inject(method = "filesDragged", at = @At("HEAD"))
    public void filesDragged(List<Path> paths, CallbackInfo ci) {
        String configPath = paths.get(0).toString();
        File cfgFile = new File(configPath);
        String fileName = cfgFile.getName();

        if (fileName.contains(".th")) {
            DialogScreen dialogScreen = new DialogScreen(
                    TextureStorage.questionPic,
                    isRu() ? "Обнаружен конфиг!" : "Config detected!",
                    isRu() ? "Ты действительно хочешь загрузить " + fileName + "?" : "Are you sure you want to load " + fileName + "?",
                    isRu() ? "Да" : "Yes", 
                    isRu() ? "Нет" : "No",
                    () -> {
                        Managers.MODULE.onUnload("none");
                        Managers.CONFIG.load(cfgFile);
                        Managers.MODULE.onLoad("none");
                        mc.setScreen(null);
                    }, () -> mc.setScreen(null));
            mc.setScreen(dialogScreen);

        } else if (fileName.contains(".txt")){
            DialogScreen dialogScreen2 = new DialogScreen(
                    TextureStorage.questionPic,
                    isRu() ? "Обнаружен текстовый файл!" : "Text file detected!",
                    isRu() ? "Импортировать файл " + fileName + " как" : "Import file " + fileName + " as",
                    isRu() ? "Прокси" : "Proxies", 
                    isRu() ? "Забить" : "Cancel",
                    () -> {
                        try {
                            try (BufferedReader reader = new BufferedReader(new FileReader(cfgFile))) {
                                while (reader.ready()) {
                                    String[] line = reader.readLine().split(":");

                                    String ip = line[0];
                                    String port = line[1];
                                    String login = line[2];
                                    String password = line[3];

                                    int p = 80;

                                    try {
                                        p = Integer.parseInt(port);
                                    } catch (Exception e) {
                                        LogUtils.getLogger().warn(e.getMessage());
                                    }

                                    Managers.PROXY.addProxy(new ProxyManager.ThProxy("Proxy" + (int) MathUtility.random(0, 10000), ip, p, login, password));
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        mc.setScreen(null);
                    },
                    () -> {
                        mc.setScreen(null);
                    });
            mc.setScreen(dialogScreen2);
        }
    }

    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
    public void renderPanoramaBackgroundHook(DrawContext context, float delta, CallbackInfo ci) {
        if (ClientSettings.customPanorama.getValue() && mc.world == null) {
            ci.cancel();
            Render2DEngine.drawMainMenuShader(context.getMatrices(), 0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
        }
    }

    @Inject(method = "renderInGameBackground", at = @At("HEAD"), cancellable = true)
    private void renderInGameBackground(CallbackInfo info) {
        if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.disableGuiBackGround.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"), cancellable = true)
    public void onRenderBackground(DrawContext context, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.disableGuiBackGround.getValue() && mc.world != null) {
            ci.cancel();
        }
    }
}
