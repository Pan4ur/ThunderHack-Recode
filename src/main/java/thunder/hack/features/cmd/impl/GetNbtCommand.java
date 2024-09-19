package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class GetNbtCommand extends Command {
    public GetNbtCommand() {
        super("nbt", "getnbt");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            sendMessage(mc.player.getMainHandStack().getComponents() != null ? mc.player.getMainHandStack().getComponents().toString() : isRu() ? "У этого предмета нет nbt тегов!" : "This item don't contains nbt tags!");
            return SINGLE_SUCCESS;
        });
    }
}
