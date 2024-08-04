package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.args.SearchArgumentType;
import thunder.hack.core.manager.client.ModuleManager;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class BlockESPCommand extends Command {
    public BlockESPCommand() {
        super("blockesp");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("reset").executes(context -> {
            ModuleManager.blockESP.selectedBlocks.getValue().clear();
            sendMessage(isRu() ? "BlockESP был очищен!" : "BlockESP got reset.");
            mc.worldRenderer.reload();
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("add").then(arg("block", SearchArgumentType.create()).executes(context -> {
            String blockName = context.getArgument("block", String.class);

            Block result = getRegisteredBlock(blockName);
            if(result != null){
                ModuleManager.blockESP.selectedBlocks.getValue().add(result);
                sendMessage(Formatting.GREEN + blockName + (isRu() ? " добавлен в BlockESP" : " added to BlockESP"));
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
                ModuleManager.blockESP.selectedBlocks.getValue().remove(result);
                sendMessage(Formatting.GREEN + blockName + (isRu() ? " удален из BlockESP" : " removed from BlockESP"));
            } else {
                sendMessage(Formatting.RED + (isRu() ? "Такого блока нет!" : "There is no such block!"));
            }

            mc.worldRenderer.reload();

            return SINGLE_SUCCESS;
        })));

        builder.executes(context -> {
            if (ModuleManager.blockESP.selectedBlocks.getValue().getItemsById().isEmpty()) {
                sendMessage("BlockESP list empty");
            } else {
                StringBuilder f = new StringBuilder("BlockESP list: ");

                for (String name : ModuleManager.blockESP.selectedBlocks.getValue().getItemsById())
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
            if (block.getTranslationKey().replace("block.minecraft.","").equalsIgnoreCase(blockName.replace("block.minecraft.",""))) {
                return block;
            }
        }
        return null;
    }
}
