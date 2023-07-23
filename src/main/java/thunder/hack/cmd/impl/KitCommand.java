package thunder.hack.cmd.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.item.ItemStack;
import thunder.hack.cmd.Command;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class KitCommand extends Command {
    final static private String pathSave = "ThunderHackRecode/misc/kits/AutoGear.json";

    private static final HashMap<String, String> errorMessage = new HashMap<>() {
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

    private static void errorMessage(String e) {
        Command.sendMessage("Error: " + errorMessage.get(e));
    }

    public static String getCurrentSet() {
        JsonObject completeJson = new JsonObject();
        try {
            completeJson = new JsonParser().parse(new FileReader(pathSave)).getAsJsonObject();
            if (!completeJson.get("pointer").getAsString().equals("none"))
                return completeJson.get("pointer").getAsString();


        } catch (IOException e) {}
        errorMessage("NoEx");
        return "";
    }

    public static String getInventoryKit(String kit) {
        JsonObject completeJson = new JsonObject();
        try {
            // Read json
            completeJson = new JsonParser().parse(new FileReader(pathSave)).getAsJsonObject();
            return completeJson.get(kit).getAsString();


        } catch (IOException e) {
            // Case not found, reset
        }
        errorMessage("NoEx");
        return "";
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage("kit <create/set/del/list> <name>");
            return;
        }
        if (commands.length == 2) {
            if (commands[0].equals("list")) {
                listMessage();
                return;
            }
            return;
        }

        if (commands.length >= 2) {
            switch (commands[0]) {
                case "create": {
                    save(commands[1]);
                    return;
                }
                case "set": {
                    set(commands[1]);
                    return;
                }
                case "del": {
                    delete(commands[1]);
                    return;
                }
            }
            KitCommand.sendMessage(".kit create/set/del");
        }
    }

    private void listMessage() {
        JsonObject completeJson = new JsonObject();
        try {
            completeJson = new JsonParser().parse(new FileReader(pathSave)).getAsJsonObject();
            int lenghtJson = completeJson.entrySet().size();
            for (int i = 0; i < lenghtJson; i++) {
                String item = new JsonParser().parse(new FileReader(pathSave)).getAsJsonObject().entrySet().toArray()[i].toString().split("=")[0];
                if (!item.equals("pointer"))
                    Command.sendMessage("Kit avaible: " + item);
            }
        } catch (IOException e) {
            errorMessage("NoEx");
        }
    }

    private void delete(String name) {
        JsonObject completeJson = new JsonObject();
        try {
            completeJson = new JsonParser().parse(new FileReader(pathSave)).getAsJsonObject();
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
        JsonObject completeJson = new JsonObject();
        try {
            completeJson = new JsonParser().parse(new FileReader(pathSave)).getAsJsonObject();
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
            completeJson = new JsonParser().parse(new FileReader(pathSave)).getAsJsonObject();
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

    private void saveFile(JsonObject completeJson, String name, String operation) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(pathSave));
            bw.write(completeJson.toString());
            bw.close();
            Command.sendMessage("Kit " + name + " " + operation);
        } catch (IOException e) {
            errorMessage("Saving");
        }
    }

}