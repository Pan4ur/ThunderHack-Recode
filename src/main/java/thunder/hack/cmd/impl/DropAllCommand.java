package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class DropAllCommand extends Command {
    public DropAllCommand() {
        super("dropall", "drop");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ThunderHack.asyncManager.run(() -> {
                for (int i = 5; i <= 45; i++) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 1, SlotActionType.THROW, mc.player);
                    try {
                        Thread.sleep(70);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            }, 1);

            sendMessage("ok");
            return SINGLE_SUCCESS;
        });
    }


}
