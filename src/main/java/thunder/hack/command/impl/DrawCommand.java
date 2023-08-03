package thunder.hack.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.impl.BindCommand;
import thunder.hack.command.Command;
import thunder.hack.command.args.ModuleArgumentType;
import thunder.hack.gui.clickui.ClickUI;
import thunder.hack.modules.Module;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class DrawCommand extends Command {
    public DrawCommand() {
        super("draw");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("module", ModuleArgumentType.create()).executes(context -> {
            Module module = context.getArgument("module", Module.class);

            if(module.getDisplayName().toLowerCase().contains("click"))
                MC.currentScreen = ClickUI.getClickGui();

            module.setDrawn(!module.isDrawn());
            sendMessage("Модуль " + Formatting.GREEN + module.getName() + Formatting.WHITE + " теперь " + (module.isDrawn() ? "виден в ArrayList" : "не виден в ArrayList"));

            return SINGLE_SUCCESS;
        }));
    }
}
