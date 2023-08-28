package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Util;
import thunder.hack.cmd.Command;

import java.io.File;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class OpenFolderCommand extends Command {
    public OpenFolderCommand() {
        super("openfolder");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Util.getOperatingSystem().open(new File("ThunderHackRecode/configs/"));
            return SINGLE_SUCCESS;
        });
    }
}
