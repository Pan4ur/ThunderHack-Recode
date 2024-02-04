package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.args.SearchArgumentType;
import thunder.hack.modules.misc.Nuker;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.modules.client.MainSettings.isRu;

public class NukerCommand extends Command {
    public NukerCommand() {
        super("nuker");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("reset").executes(context -> {
            Nuker.selectedBlocks.clear();
            sendMessage("Nuker got reset.");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("add").then(arg("block", SearchArgumentType.create()).executes(context -> {
            String blockName = context.getArgument("block", String.class);

            Block result = getRegisteredBlock(blockName);
            if(result != null){
                Nuker.selectedBlocks.add(result);
                sendMessage(Formatting.GREEN + blockName + (isRu() ? " добавлен в Nuker" : " added to Nuker"));
            } else {
                sendMessage(Formatting.RED + (isRu() ? "Такого блока нет!" : "There is no such block!"));
            }

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("del").then(arg("block", SearchArgumentType.create()).executes(context -> {
            String blockName = context.getArgument("block", String.class);

            Block result = getRegisteredBlock(blockName);
            if(result != null){
                Nuker.selectedBlocks.remove(result);
                sendMessage(Formatting.GREEN + blockName + (isRu() ? " удален из Nuker" : " removed from Nuker"));
            } else {
                sendMessage(Formatting.RED + (isRu() ? "Такого блока нет!" : "There is no such block!"));
            }

            return SINGLE_SUCCESS;
        })));

        builder.executes(context -> {
            if (Nuker.selectedBlocks.isEmpty()) {
                sendMessage("Nuker list empty");
            } else {
                StringBuilder f = new StringBuilder("Nuker list: ");

                for (Block name :  Nuker.selectedBlocks)
                    try {
                        f.append(name.getTranslationKey().replace("block.minecraft.","")).append(", ");
                    } catch (Exception ignored) {
                    }

                sendMessage(f.toString());
            }

            return SINGLE_SUCCESS;
        });
    }

    public static Block getRegisteredBlock(String blockName) {
        for (Block block : Registries.BLOCK) {
            if (block.getTranslationKey().replace("block.minecraft.","").equalsIgnoreCase(blockName.replace("block.minecraft.",""))) {
                return block;
            }
        }
        return null;
    }
}
