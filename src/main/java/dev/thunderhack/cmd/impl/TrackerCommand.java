package dev.thunderhack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.thunderhack.cmd.Command;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import dev.thunderhack.core.ModuleManager;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class TrackerCommand extends Command {
    public TrackerCommand() {
        super("tracker");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (ModuleManager.tracker.isEnabled()) {
                ModuleManager.tracker.sendTrack();
            }

            return SINGLE_SUCCESS;
        });
    }
}
