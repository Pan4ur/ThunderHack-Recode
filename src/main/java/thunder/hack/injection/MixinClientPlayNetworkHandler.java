package thunder.hack.injection;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.ThunderHack;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(@NotNull String message, CallbackInfo ci) {
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
