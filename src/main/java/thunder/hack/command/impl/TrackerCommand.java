package thunder.hack.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import thunder.hack.command.Command;
import thunder.hack.core.ModuleManager;
import thunder.hack.modules.misc.Tracker;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class TrackerCommand extends Command {
    public TrackerCommand() {
        super("tracker");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if(ModuleManager.tracker.isEnabled()){
                Tracker.sendTrack();
            }

            return SINGLE_SUCCESS;
        });
    }
}
