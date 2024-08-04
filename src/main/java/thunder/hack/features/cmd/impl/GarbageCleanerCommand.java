package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.features.cmd.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class GarbageCleanerCommand extends Command {
    public GarbageCleanerCommand() {
        super("gc", "garbagecleaner", "clearram");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            sendMessage("Cleaning RAM..");
            System.gc();
            sendMessage("Successfully cleaned RAM!");
            return SINGLE_SUCCESS;
        });
    }
}
