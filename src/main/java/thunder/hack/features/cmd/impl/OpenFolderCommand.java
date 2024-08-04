package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;

import java.io.File;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class OpenFolderCommand extends Command {
    public OpenFolderCommand() {
        super("openfolder", "folder");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Util.getOperatingSystem().open(new File("ThunderHackRecode/configs/"));
            return SINGLE_SUCCESS;
        });
    }
}
