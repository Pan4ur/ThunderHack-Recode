package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import thunder.hack.cmd.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.modules.client.MainSettings.isRu;

public class TreasureCommand extends Command {
    public TreasureCommand() {
        super("gettreasure", "treasure");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (mc.player.getMainHandStack().getItem().toString().equals("filled_map")) {
                StringBuilder result = new StringBuilder();
                String rawNbt = mc.player.getMainHandStack().getNbt().toString();
                for (int i = rawNbt.indexOf("x"); i < rawNbt.indexOf("]") - 2; i++)
                    result.append(rawNbt.charAt(i));
                sendMessage(isRu() ? "Нашел! Координаты: " + result : "Found! Coords: " + result);
            } else sendMessage(isRu() ? "Возьми карту в руки!" : "Get map in hand!");
            return SINGLE_SUCCESS;
        });
    }
}
