package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.args.ChestStealerArgumentType;
import thunder.hack.core.impl.ModuleManager;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.modules.client.MainSettings.isRu;

public class ChestStealerCommand extends Command {
    public ChestStealerCommand() {
        super("cheststealer", "stealer", "rat");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("reset").executes(context -> {
            ModuleManager.chestStealer.items.clear();
            sendMessage("ChestStealer got reset.");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("add").then(arg("item", ChestStealerArgumentType.create()).executes(context -> {
            String blockName = context.getArgument("item", String.class);

            String result = getRegistered(blockName);
            if(result != null){
                ModuleManager.chestStealer.items.add(result);
                sendMessage(Formatting.GREEN + blockName + (isRu() ? " добавлен в ChestStealer" : " added to ChestStealer"));
            } else {
                sendMessage(Formatting.RED + (isRu() ? "Такого предмета нет!" : "There is no such item!"));
            }
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("del").then(arg("item", ChestStealerArgumentType.create()).executes(context -> {
            String blockName = context.getArgument("item", String.class);

            String result = getRegistered(blockName);
            if(result != null){
                ModuleManager.chestStealer.items.remove(result);
                sendMessage(Formatting.GREEN + blockName + (isRu() ? " удален из ChestStealer" : " removed from ChestStealer"));
            } else {
                sendMessage(Formatting.RED + (isRu() ? "Такого предмета нет!" : "There is no such item!"));
            }
            return SINGLE_SUCCESS;
        })));

        builder.executes(context -> {
            if (ModuleManager.chestStealer.items.isEmpty()) {
                sendMessage("ChestStealer list empty");
            } else {
                StringBuilder f = new StringBuilder("ChestStealer list: ");

                for (String name :  ModuleManager.chestStealer.items)
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
