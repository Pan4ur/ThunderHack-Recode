package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class HorseSpeedCommand extends Command {
    public HorseSpeedCommand() {
        super("gethorsespeed", "horsespeed");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (mc.player.getVehicle() != null && mc.player.getVehicle() instanceof HorseEntity horse) {
                if (!horse.isSaddled()) {
                    if (isRu()) sendMessage(Formatting.RED + "У тебя нет седла!");
                    else sendMessage(Formatting.RED + "You don't have a saddle!");
                    return SINGLE_SUCCESS;
                }

                float speed = horse.forwardSpeed * 43.17f;

                float ratio = speed / 14.512f;

                String verbose = "";

                if (ratio < 0.3)
                    verbose = isRu() ? "У тебя кринжовая лошадь :(" : "Your horse is shitty :(";

                if (ratio > 0.3 && ratio < 0.6)
                    verbose = isRu() ? "У тебя нормальная лошадь" : "Your horse is normal";

                if (ratio > 0.6)
                    verbose = isRu() ? "У тебя пиздатая лошадь :)" : "Your horse is good :)";

                if (ratio > 0.9)
                    verbose = isRu() ? "Цыганы уже в пути" : "Your horse is very good :)";

                if (isRu()) sendMessage(Formatting.GREEN + "Скорость лошади: " + speed + " из 14.512. " + verbose);
                else sendMessage(Formatting.GREEN + "Horse speed: " + speed + " out of 14.512. " + verbose);
            } else {
                if (isRu()) sendMessage(Formatting.RED + "У тебя нет лошади!");
                else sendMessage(Formatting.RED + "You don't have a horse!");
            }
            return SINGLE_SUCCESS;
        });
    }
}
