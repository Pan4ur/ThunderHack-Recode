package thunder.hack.injection;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.Thunderhack;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (message.startsWith(Thunderhack.commandManager.getPrefix())) {
            try {
                Thunderhack.commandManager.getDispatcher().execute(
                        message.substring(Thunderhack.commandManager.getPrefix().length()),
                        Thunderhack.commandManager.getSource()
                );
            } catch (CommandSyntaxException ignored) {}

            ci.cancel();
        }
    }
}
