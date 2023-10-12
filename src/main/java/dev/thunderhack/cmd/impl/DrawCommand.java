package dev.thunderhack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.thunderhack.cmd.Command;
import dev.thunderhack.cmd.args.ModuleArgumentType;
import dev.thunderhack.modules.Module;
import dev.thunderhack.modules.client.MainSettings;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import dev.thunderhack.gui.clickui.ClickUI;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class DrawCommand extends Command {
    public DrawCommand() {
        super("draw");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("module", ModuleArgumentType.create()).executes(context -> {
            Module module = context.getArgument("module", Module.class);

            if (module.getDisplayName().toLowerCase().contains("click"))
                MC.currentScreen = ClickUI.getClickGui();

            module.setDrawn(!module.isDrawn());

            if(MainSettings.isRu()){
                sendMessage("Модуль " + Formatting.GREEN + module.getName() + Formatting.WHITE + " теперь " + (module.isDrawn() ? "виден в ArrayList" : "не виден в ArrayList"));
            } else {
                sendMessage(Formatting.GREEN + module.getName() + Formatting.WHITE + " is now " + (module.isDrawn() ? "visible in ArrayList" : "invisible in ArrayList"));
            }

            return SINGLE_SUCCESS;
        }));
    }
}
