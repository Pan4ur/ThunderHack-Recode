package dev.thunderhack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.thunderhack.cmd.Command;
import dev.thunderhack.cmd.args.ChestStealerArgumentType;
import dev.thunderhack.modules.client.MainSettings;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import dev.thunderhack.core.ModuleManager;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

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
                sendMessage(Formatting.GREEN + blockName + (MainSettings.isRu() ? " добавлен в ChestStealer" : " added to ChestStealer"));
            } else {
                sendMessage(Formatting.RED + (MainSettings.isRu() ? "Такого предмета нет!" : "There is no such item!"));
            }
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("del").then(arg("item", ChestStealerArgumentType.create()).executes(context -> {
            String blockName = context.getArgument("item", String.class);

            String result = getRegistered(blockName);
            if(result != null){
                ModuleManager.chestStealer.items.remove(result);
                sendMessage(Formatting.GREEN + blockName + (MainSettings.isRu() ? " удален из ChestStealer" : " removed from ChestStealer"));
            } else {
                sendMessage(Formatting.RED + (MainSettings.isRu() ? "Такого предмета нет!" : "There is no such item!"));
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
