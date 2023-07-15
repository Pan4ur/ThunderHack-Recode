package thunder.hack.cmd.impl;

import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.gui.clickui.ClickUI;
import thunder.hack.modules.Module;
import net.minecraft.util.Formatting;

public class DrawCommand extends Command {
    public DrawCommand() {
        super("draw");
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage("Напиши название модуля");
            return;
        }
        String moduleName = commands[0];
        Module module = Thunderhack.moduleManager.get(moduleName);
        if (module == null) {
            Command.sendMessage("Неизвестный модуль'" + module + "'!");
            return;
        }
        if(module.getDisplayName().toLowerCase().contains("click")) mc.currentScreen = ClickUI.getClickGui();
        module.setDrawn(!module.isDrawn());
        BindCommand.sendMessage("Модуль " + Formatting.GREEN + module.getName() + Formatting.WHITE + " теперь " + (module.isDrawn() ? "виден в ArrayList" : "не виден в ArrayList"));
    }
}
