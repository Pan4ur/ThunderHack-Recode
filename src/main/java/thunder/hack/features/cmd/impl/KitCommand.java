package thunder.hack.features.cmd.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;
import thunder.hack.core.manager.client.ModuleManager;

import java.io.*;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class KitCommand extends Command {
    final static private String PATH = "ThunderHackRecode/misc/AutoGear.json";

    public KitCommand() {
        super("kit");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("list").executes(context -> {
            listMessage();

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("create").then(arg("name", StringArgumentType.word()).executes(context -> {
            save(context.getArgument("name", String.class));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("set").then(arg("name", StringArgumentType.word()).executes(context -> {
            set(context.getArgument("name", String.class));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("del").then(arg("name", StringArgumentType.word()).executes(context -> {
            delete(context.getArgument("name", String.class));
            return SINGLE_SUCCESS;
        })));

        builder.executes(context -> {
            sendMessage("kit <create/set/del/list> <name>");
            return SINGLE_SUCCESS;
        });
    }

    public static String getSelectedKit() {
        try {
            JsonObject json = new JsonParser().parse(new FileReader(PATH)).getAsJsonObject();
            if (!json.get("selected").getAsString().equals("none"))
                return json.get("selected").getAsString();
        } catch (Exception ignored) {
        }
        sendMessage(isRu() ? "Кит не найден" : "Kit not found");
        return "";
    }

    public static String getKitItems(String kit) {
        try {
            JsonObject json = new JsonParser().parse(new FileReader(PATH)).getAsJsonObject();
            return json.get(kit).getAsString();
        } catch (Exception ignored) {
        }
        sendMessage(isRu() ? "Кит не найден" : "Kit not found");
        return "";
    }

    private void listMessage() {
        try {
            JsonObject json = new JsonParser().parse(new FileReader(PATH)).getAsJsonObject();
            sendMessage(isRu() ? "Доступные киты:" : "Available kits:");
            for (int i = 0; i < json.entrySet().size(); i++) {
                String item = json.entrySet().toArray()[i].toString().split("=")[0];
                sendMessage(Formatting.GRAY + "-> " + item + (item.equals("selected") ? (isRu() ? "(Выбран)" : " (Selected)") : ""));
            }
        } catch (Exception e) {
            sendMessage(isRu() ? "Проблема с конфигурацией китов!" : "Error with kit cfg!");
        }
    }

    private void delete(String name) {
        try {
            JsonObject json = new JsonParser().parse(new FileReader(PATH)).getAsJsonObject();
            if (json.get(name) != null && !name.equals("selected")) {
                json.remove(name);
                if (json.get("selected").getAsString().equals(name))
                    json.addProperty("selected", "none");
                saveFile(json, name, isRu() ? "удален" : "deleted");
            } else sendMessage("Kit not found");

        } catch (Exception e) {
            sendMessage(isRu() ? "Кит не найден" : "Kit not found");
        }
    }

    private void set(String name) {
        try {
            JsonObject json = new JsonParser().parse(new FileReader(PATH)).getAsJsonObject();
            if (json.get(name) != null && !name.equals("selected")) {
                json.addProperty("selected", name);
                saveFile(json, name, isRu() ? "выбран" : "selected");
                ModuleManager.autoGear.setup();
            } else sendMessage(isRu() ? "Кит не найден" : "Kit not found");
        } catch (Exception e) {
            sendMessage(isRu() ? "Кит не найден" : "Kit not found");
        }
    }

    private void save(String name) {
        JsonObject json = new JsonObject();
        try {
            json = new JsonParser().parse(new FileReader(PATH)).getAsJsonObject();
            if (json.get(name) != null && !name.equals("selected")) {
                sendMessage(isRu() ? "Этот кит уже существует" : "This kit arleady exist");
                return;
            }
        } catch (IOException e) {
            json.addProperty("selected", "none");
        }

        StringBuilder jsonInventory = new StringBuilder();

        for (ItemStack item : mc.player.getInventory().main)
            jsonInventory.append(item.getItem() instanceof PotionItem ? item.getItem().getTranslationKey() + item.getItem().getComponents().get(DataComponentTypes.POTION_CONTENTS).getColor() : item.getItem().getTranslationKey()).append(" ");

        json.addProperty(name, jsonInventory.toString());
        saveFile(json, name, isRu() ? "сохранен" : "saved");
    }

    private void saveFile(@NotNull JsonObject completeJson, String name, String operation) {
        try {
            File file = new File(PATH);
            try {
                file.createNewFile();
            } catch (Exception ignored) {
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(PATH));
            bw.write(completeJson.toString());
            bw.close();
            sendMessage((isRu() ? "Кит " : "Kit ") + Formatting.AQUA + name + Formatting.RESET + " " + operation);
        } catch (IOException e) {
            sendMessage(isRu() ? "Ошибка сохранения файла" : "Error saving the file");
        }
    }
}
