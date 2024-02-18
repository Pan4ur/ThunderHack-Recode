package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.args.ModuleArgumentType;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class DrawCommand extends Command {
    public DrawCommand() {
        super("draw");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("module", ModuleArgumentType.create()).executes(context -> {
            Module module = context.getArgument("module", Module.class);

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
