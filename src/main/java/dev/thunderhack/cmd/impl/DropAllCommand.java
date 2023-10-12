package dev.thunderhack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.NotNull;
import dev.thunderhack.cmd.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class DropAllCommand extends Command {
    public DropAllCommand() {
        super("dropall");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            new Thread(() -> {
                for (int i = 5; i <= 45; i++) {
                    MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, i, 1, SlotActionType.THROW, MC.player);
                    try {
                        Thread.sleep(70);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                MC.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(MC.player.currentScreenHandler.syncId));
            });
            return SINGLE_SUCCESS;
        });
    }


}
