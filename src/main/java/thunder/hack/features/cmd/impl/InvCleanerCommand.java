package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.args.ChestStealerArgumentType;
import thunder.hack.core.manager.client.ModuleManager;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class InvCleanerCommand extends Command {
    public InvCleanerCommand() {
        super("invcleaner", "cleaner");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("reset").executes(context -> {
            ModuleManager.inventoryCleaner.items.getValue().clear();
            sendMessage("InvCleaner got reset.");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("add").then(arg("item", ChestStealerArgumentType.create()).executes(context -> {
            String blockName = context.getArgument("item", String.class);

            String result = getRegistered(blockName);
            if(result != null){
                ModuleManager.inventoryCleaner.items.getValue().add(result);
                sendMessage(Formatting.GREEN + blockName + (isRu() ? " добавлен в InvCleaner" : " added to InvCleaner"));
            } else {
                sendMessage(Formatting.RED + (isRu() ? "Такого предмета нет!" : "There is no such item!"));
            }
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("del").then(arg("item", ChestStealerArgumentType.create()).executes(context -> {
            String blockName = context.getArgument("item", String.class);

            String result = getRegistered(blockName);
            if(result != null){
                ModuleManager.inventoryCleaner.items.getValue().remove(result);
                sendMessage(Formatting.GREEN + blockName + (isRu() ? " удален из InvCleaner" : " removed from InvCleaner"));
            } else {
                sendMessage(Formatting.RED + (isRu() ? "Такого предмета нет!" : "There is no such item!"));
            }
            return SINGLE_SUCCESS;
        })));

        builder.executes(context -> {
            if (ModuleManager.inventoryCleaner.items.getValue().getItemsById().isEmpty()) {
                sendMessage("InvCleaner list empty");
            } else {
                StringBuilder f = new StringBuilder("InvCleaner list: ");

                for (String name :  ModuleManager.inventoryCleaner.items.getValue().getItemsById())
                    try {
                        f.append(name).append(", ");
                    } catch (Exception ignored) {
                    }
                sendMessage(f.toString());
            }

            return SINGLE_SUCCESS;
        });
    }

    public static String getRegistered(String Name) {
        for (Block block : Registries.BLOCK) {
            if (block.getTranslationKey().replace("block.minecraft.","").equalsIgnoreCase(Name)) {
                return block.getTranslationKey().replace("block.minecraft.","");
            }
        }
        for (Item item : Registries.ITEM) {
            if (item.getTranslationKey().replace("item.minecraft.","").equalsIgnoreCase(Name)) {
                return item.getTranslationKey().replace("item.minecraft.","");
            }
        }
        return null;
    }
}
