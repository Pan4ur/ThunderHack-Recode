package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.modules.Module;
import thunder.hack.setting.impl.Bind;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.system.Systems.MANAGER;

public class ResetBindsCommand extends Command {
    public ResetBindsCommand() {
        super("resetbinds", "unbind", "fuckbinds");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            for (Module mod : MANAGER.MODULE.modules) mod.setBind(new Bind(-1, false, false));
            return SINGLE_SUCCESS;
        });
    }
}
