package thunder.hack.cmd.impl;


import thunder.hack.cmd.Command;
import thunder.hack.modules.render.Search;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;

public class SearchCommand  extends Command {

    public SearchCommand() {
        super("search");
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            if (Search.defaultBlocks.isEmpty()) {
                sendMessage("Search list empty");
            } else {
                String f = "Search list: ";
                for (Block name :  Search.defaultBlocks) {
                    try {
                        f = f + name.getTranslationKey() + ", ";
                    } catch (Exception exception) {
                    }
                }
                sendMessage(f);
            }
            return;
        }
        if (commands.length == 2) {
            if ("reset".equals(commands[0])) {
                Search.defaultBlocks.clear();
                sendMessage("Search got reset.");
              //  mc.renderGlobal.loadRenderers();
                mc.worldRenderer.reload();
                return;
            }
            return;
        }

        if (commands.length >= 2) {
            switch (commands[0]) {
                case "add": {
                    Search.defaultBlocks.add(getRegisteredBlock(commands[1]));
                    sendMessage(Formatting.GREEN + commands[1] + " added to search");
                    mc.worldRenderer.reload();
                    return;
                }
                case "del": {
                    Search.defaultBlocks.remove(getRegisteredBlock(commands[1]));
                    sendMessage(Formatting.RED + commands[1] + " removed from search");
                    mc.worldRenderer.reload();
                    return;
                }
            }
            sendMessage("Unknown Command, try search add/del <block name>");
        }

    }

    public static Block getRegisteredBlock(String blockName) {
        for (Block block : Registries.BLOCK) {
            if (block.getTranslationKey().replace("block.minecraft.","").equalsIgnoreCase(blockName)) {
                Command.sendMessage("Asdawd1");
                return block;
            }
        }
        return null;
    }
}