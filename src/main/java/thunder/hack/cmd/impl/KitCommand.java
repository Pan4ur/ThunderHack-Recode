package thunder.hack.cmd.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thunder.hack.cmd.Command;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class KitCommand extends Command {
    final static private String PATH_TO_SAVE = "ThunderHackRecode/misc/kits/AutoGear.json";

    private static final HashMap<String, String> ERROR_MESSAGES = new HashMap<>() {
        {
            put("NoPar", "Not enough parameters");
            put("Exist", "This kit arleady exist");
            put("Saving", "Error saving the file");
            put("NoEx", "Kit not found");
        }
    };

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

    private void errorMessage(String e) {
        sendMessage("Error: " + ERROR_MESSAGES.get(e));
    }

    public String getCurrentSet() {
        JsonObject completeJson;
        try {
            completeJson = new JsonParser().parse(new FileReader(PATH_TO_SAVE)).getAsJsonObject();
            if (!completeJson.get("pointer").getAsString().equals("none"))
                return completeJson.get("pointer").getAsString();


        } catch (IOException ignored) {}
        errorMessage("NoEx");
        return "";
    }

    public String getInventoryKit(String kit) {
        JsonObject completeJson;

        try {
            // Read json
            completeJson = new JsonParser().parse(new FileReader(PATH_TO_SAVE)).getAsJsonObject();
            return completeJson.get(kit).getAsString();


        } catch (IOException ignored) {
        }

        errorMessage("NoEx");
        return "";
    }

    private void listMessage() {
        JsonObject completeJson;
        try {
            completeJson = new JsonParser().parse(new FileReader(PATH_TO_SAVE)).getAsJsonObject();
            int lenghtJson = completeJson.entrySet().size();
            for (int i = 0; i < lenghtJson; i++) {
                String item = new JsonParser().parse(new FileReader(PATH_TO_SAVE)).getAsJsonObject().entrySet().toArray()[i].toString().split("=")[0];
                if (!item.equals("pointer"))
                    sendMessage("Kit avaible: " + item);
            }
        } catch (IOException e) {
            errorMessage("NoEx");
        }
    }

    private void delete(String name) {
        JsonObject completeJson;
        try {
            completeJson = new JsonParser().parse(new FileReader(PATH_TO_SAVE)).getAsJsonObject();
            if (completeJson.get(name) != null && !name.equals("pointer")) {
                completeJson.remove(name);
                if (completeJson.get("pointer").getAsString().equals(name))
                    completeJson.addProperty("pointer", "none");
                saveFile(completeJson, name, "deleted");
            } else errorMessage("NoEx");

        } catch (IOException e) {
            errorMessage("NoEx");
        }
    }

    private void set(String name) {
        JsonObject completeJson;

        try {
            completeJson = new JsonParser().parse(new FileReader(PATH_TO_SAVE)).getAsJsonObject();
            if (completeJson.get(name) != null && !name.equals("pointer")) {
                completeJson.addProperty("pointer", name);
                saveFile(completeJson, name, "selected");
            } else errorMessage("NoEx");

        } catch (IOException e) {
            errorMessage("NoEx");
        }
    }

    private void save(String name) {
        JsonObject completeJson = new JsonObject();

        try {
            completeJson = new JsonParser().parse(new FileReader(PATH_TO_SAVE)).getAsJsonObject();
            if (completeJson.get(name) != null && !name.equals("pointer")) {
                errorMessage("Exist");
                return;
            }
        } catch (IOException e) {
            completeJson.addProperty("pointer", "none");
        }

        StringBuilder jsonInventory = new StringBuilder();

        for (ItemStack item : mc.player.getInventory().main) {
            jsonInventory.append(item.getTranslationKey()).append(" ");
        }
        completeJson.addProperty(name, jsonInventory.toString());
        saveFile(completeJson, name, "saved");
    }

    private void saveFile(@NotNull JsonObject completeJson, String name, String operation) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(PATH_TO_SAVE));
            bw.write(completeJson.toString());
            bw.close();
            sendMessage("Kit " + name + " " + operation);
        } catch (IOException e) {
            errorMessage("Saving");
        }
    }
}
