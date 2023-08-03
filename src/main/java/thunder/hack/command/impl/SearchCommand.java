package thunder.hack.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import thunder.hack.command.Command;
import thunder.hack.modules.render.Search;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class SearchCommand extends Command {
    public SearchCommand() {
        super("search");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("reset").executes(context -> {
            Search.defaultBlocks.clear();
            sendMessage("Search got reset.");

            MC.worldRenderer.reload();
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("add").then(arg("block", StringArgumentType.word())).executes(context -> {
            String blockName = context.getArgument("block", String.class);

            Search.defaultBlocks.add(getRegisteredBlock(blockName));
            sendMessage(Formatting.GREEN + blockName + " added to search");
            MC.worldRenderer.reload();

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("del").then(arg("block", StringArgumentType.word()).executes(context -> {
            String blockName = context.getArgument("block", String.class);

            Search.defaultBlocks.remove(getRegisteredBlock(blockName));
            sendMessage(Formatting.RED + blockName + " removed from search");
            MC.worldRenderer.reload();

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
                sendMessage("Asdawd1");
                return block;
            }
        }
        return null;
    }
}
