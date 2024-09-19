package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.world.GameMode;
import thunder.hack.features.cmd.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        super("gamemode", "gm");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("mode", StringArgumentType.greedyString()).executes(context -> {
            final String mode = context.getArgument("mode", String.class);

            switch (mode) {
                case "survival", "0":
                    mc.interactionManager.setGameMode(GameMode.SURVIVAL);
                case "creative", "1":
                    mc.interactionManager.setGameMode(GameMode.CREATIVE);
                case "spectator", "2":
                    mc.interactionManager.setGameMode(GameMode.SPECTATOR);
                case "adventure", "3":
                    mc.interactionManager.setGameMode(GameMode.ADVENTURE);
            }

            return SINGLE_SUCCESS;
        }));
    }
}
