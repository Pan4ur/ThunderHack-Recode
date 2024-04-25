package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.modules.client.ClientSettings;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.modules.client.ClientSettings.isRu;

public class HorseSpeedCommand extends Command {
    public HorseSpeedCommand() {
        super("gethorsespeed");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if(mc.player.getVehicle() != null && mc.player.getVehicle() instanceof HorseEntity){
                HorseEntity horse = (HorseEntity) mc.player.getVehicle();
                if (ClientSettings.language.getValue() == ClientSettings.Language.RU) sendMessage(Formatting.GREEN + "Скорость лошади: " + horse.forwardSpeed * 43.17f + " из 14.512");
                else sendMessage(Formatting.GREEN + "Horse speed: " + horse.forwardSpeed * 43.17f + " out of 14.512");
            }
            return SINGLE_SUCCESS;
        });
    }
}
