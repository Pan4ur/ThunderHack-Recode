package thunder.hack.injection;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.modules.Module;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void sendChatMessageHook(@NotNull String message, CallbackInfo ci) {
        if (message.equals(String.valueOf(ModuleManager.unHook.code)) && ModuleManager.unHook.isEnabled())
            ModuleManager.unHook.disable();

        if(Module.fullNullCheck()) return;
        if (message.startsWith(ThunderHack.commandManager.getPrefix())) {
            try {
                ThunderHack.commandManager.getDispatcher().execute(
                        message.substring(ThunderHack.commandManager.getPrefix().length()),
                        ThunderHack.commandManager.getSource()
                );
            } catch (CommandSyntaxException ignored) {}

            ci.cancel();
        }
    }
}
