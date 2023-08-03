package thunder.hack.injection;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.Thunderhack;
import thunder.hack.core.CommandManager2;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (message.startsWith(String.valueOf(CommandManager2.PREFIX))) {
            try {
                Thunderhack.commandManager2.getDispatcher().execute(
                        message.substring(1),
                        Thunderhack.commandManager2.getSource()
                );
            } catch (CommandSyntaxException ignored) {}

            ci.cancel();
        }
    }
}
