package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.args.SearchArgumentType;
import thunder.hack.modules.render.Search;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.modules.client.MainSettings.isRu;

public class SearchCommand extends Command {
    public SearchCommand() {
        super("search");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("reset").executes(context -> {
            Search.defaultBlocks.clear();
            sendMessage("Search got reset.");

            mc.worldRenderer.reload();
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("add").then(arg("block", SearchArgumentType.create()).executes(context -> {
            String blockName = context.getArgument("block", String.class);

            Block result = getRegisteredBlock(blockName);
            if(result != null){
                Search.defaultBlocks.add(result);
                sendMessage(Formatting.GREEN + blockName + (isRu() ? " добавлен в Search" : " added to Search"));
            } else {
                sendMessage(Formatting.RED + (isRu() ? "Такого блока нет!" : "There is no such block!"));
            }

            mc.worldRenderer.reload();

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("del").then(arg("block", SearchArgumentType.create()).executes(context -> {
            String blockName = context.getArgument("block", String.class);

            Block result = getRegisteredBlock(blockName);
            if(result != null){
                Search.defaultBlocks.remove(result);
                sendMessage(Formatting.GREEN + blockName + (isRu() ? " удален из Search" : " removed from Search"));
            } else {
                sendMessage(Formatting.RED + (isRu() ? "Такого блока нет!" : "There is no such block!"));
            }

            mc.worldRenderer.reload();

            return SINGLE_SUCCESS;
        })));

        builder.executes(context -> {
            if (Search.defaultBlocks.isEmpty()) {
                sendMessage("Search list empty");
            } else {
                StringBuilder f = new StringBuilder("Search list: ");

                for (Block name :  Search.defaultBlocks)
                    try {
                        f.append(name.getTranslationKey()).append(", ");
                    } catch (Exception ignored) {
                    }

                sendMessage(f.toString());
            }

            return SINGLE_SUCCESS;
        });
    }

    public static Block getRegisteredBlock(String blockName) {
        for (Block block : Registries.BLOCK) {
            if (block.getTranslationKey().replace("block.minecraft.","").equalsIgnoreCase(blockName)) {
                return block;
            }
        }
        return null;
    }
}
