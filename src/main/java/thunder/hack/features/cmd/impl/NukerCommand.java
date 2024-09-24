package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.args.SearchArgumentType;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class NukerCommand extends Command {
    public NukerCommand() {
        super("nuker");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("reset").executes(context -> {
            ModuleManager.nuker.selectedBlocks.getValue().clear();
            sendMessage(isRu() ? "Все блоки были удалены!" : "Nuker got reset!");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("add").then(arg("block", SearchArgumentType.create()).executes(context -> {
            String blockName = context.getArgument("block", String.class);

            Block result = getRegisteredBlock(blockName);
            if (result != null) {
                ModuleManager.nuker.selectedBlocks.getValue().add(result);
                sendMessage(Formatting.GREEN + blockName + (isRu() ? " добавлен в Nuker" : " added to Nuker"));
            } else {
                sendMessage(Formatting.RED + (isRu() ? "Такого блока нет!" : "There is no such block!"));
            }

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("del").then(arg("block", SearchArgumentType.create()).executes(context -> {
            String blockName = context.getArgument("block", String.class);

            Block result = getRegisteredBlock(blockName);
            if (result != null) {
                ModuleManager.nuker.selectedBlocks.getValue().remove(blockName);
                sendMessage(Formatting.GREEN + blockName + (isRu() ? " удален из Nuker" : " removed from Nuker"));
            } else {
                sendMessage(Formatting.RED + (isRu() ? "Такого блока нет!" : "There is no such block!"));
            }

            return SINGLE_SUCCESS;
        })));

        builder.executes(context -> {
            if (ModuleManager.nuker.selectedBlocks.getValue().getItemsById().isEmpty()) {
                sendMessage("Nuker list empty");
            } else {
                StringBuilder f = new StringBuilder("Nuker list: ");

                for (String name : ModuleManager.nuker.selectedBlocks.getValue().getItemsById())
                    try {
                        f.append(name).append(", ");
                    } catch (Exception ignored) {
                    }

                sendMessage(f.toString());
            }

            return SINGLE_SUCCESS;
        });
    }

    public static Block getRegisteredBlock(String blockName) {
        for (Block block : Registries.BLOCK) {
            if (block.getTranslationKey().replace("block.minecraft.", "").equalsIgnoreCase(blockName.replace("block.minecraft.", ""))) {
                return block;
            }
        }
        return null;
    }
}
