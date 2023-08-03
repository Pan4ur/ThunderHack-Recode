package thunder.hack.cmd.impl;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import thunder.hack.cmd.Command;
import thunder.hack.utility.player.InventoryUtil;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class EClipCommand extends Command {
    public EClipCommand() {
        super("eclip");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("bedrock").executes(context -> {
            execute(-((float) MC.player.getY()) - 3.0f);
            return SINGLE_SUCCESS;
        }).then(arg("number", FloatArgumentType.floatArg()).executes(context -> {
            float y = -((float) MC.player.getY()) - 3.0f;

            if (y == 0.0f) y = context.getArgument("number", Float.class);
            execute(y);

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("down").executes(context -> {
            int i;
            float y = 0.0f;

            for (i = 1; i < 255; ++i) {
                if (MC.world.getBlockState(BlockPos.ofFloored(MC.player.getPos()).add(0, -i, 0)) == Blocks.AIR.getDefaultState()) {
                    y = -i - 1;
                    break;
                }

                if (MC.world.getBlockState(BlockPos.ofFloored(MC.player.getPos()).add(0, -i, 0)) != Blocks.BEDROCK.getDefaultState())
                    continue;

                sendMessage(Formatting.RED + " можно телепортироваться только под бедрок");
                sendMessage(Formatting.RED + " eclip bedrock");
                return SINGLE_SUCCESS;
            }

            execute(y);
            return SINGLE_SUCCESS;
        }).then(arg("number", FloatArgumentType.floatArg()).executes(context -> {
            int i;
            float y = 0.0f;

            for (i = 1; i < 255; ++i) {
                if (MC.world.getBlockState(BlockPos.ofFloored(MC.player.getPos()).add(0, -i, 0)) == Blocks.AIR.getDefaultState()) {
                    y = -i - 1;
                    break;
                }

                if (MC.world.getBlockState(BlockPos.ofFloored(MC.player.getPos()).add(0, -i, 0)) != Blocks.BEDROCK.getDefaultState())
                    continue;

                sendMessage(Formatting.RED + " можно телепортироваться только под бедрок");
                sendMessage(Formatting.RED + " eclip bedrock");
                return SINGLE_SUCCESS;
            }

            if (y == 0.0f) y = context.getArgument("number", Float.class);

            execute(y);
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("up").executes(context -> {
            int i;
            float y = 0.0f;

            for (i = 4; i < 255; ++i) {
                if (MC.world.getBlockState(BlockPos.ofFloored(MC.player.getPos()).add(0, i, 0)) != Blocks.AIR.getDefaultState())
                    continue;
                y = i + 1;
                break;
            }

            execute(y);
            return SINGLE_SUCCESS;
        }).then(arg("number", FloatArgumentType.floatArg()).executes(context -> {
            int i;
            float y = 0.0f;

            for (i = 4; i < 255; ++i) {
                if (MC.world.getBlockState(BlockPos.ofFloored(MC.player.getPos()).add(0, i, 0)) != Blocks.AIR.getDefaultState())
                    continue;
                y = i + 1;
                break;
            }

            if (y == 0.0f) y = context.getArgument("number", Float.class);
            execute(y);

            return SINGLE_SUCCESS;
        })));
    }

    private void execute(float y) {
        int elytra;

        if ((elytra = InventoryUtil.getItemSlot(Items.ELYTRA)) == -1) {
            sendMessage(Formatting.RED + "вам нужны элитры в инвентаре");
            return;
        }
        if (elytra != -2) {
            MC.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, MC.player);
            MC.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, MC.player);
        }

        MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY(), MC.player.getZ(), false));
        MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY(), MC.player.getZ(), false));
        MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY() + (double) y, MC.player.getZ(), false));
        MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));

        if (elytra != -2) {
            MC.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, MC.player);
            MC.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, MC.player);
        }

        MC.player.setPosition(MC.player.getX(), MC.player.getY() + (double) y, MC.player.getZ());
    }
}
