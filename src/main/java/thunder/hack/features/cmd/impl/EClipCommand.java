package thunder.hack.features.cmd.impl;

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
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;
import thunder.hack.utility.player.InventoryUtility;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class EClipCommand extends Command {
    public EClipCommand() {
        super("eclip");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("bedrock").executes(context -> {
            execute(-((float) mc.player.getY()) - 3.0f);
            return SINGLE_SUCCESS;
        }).then(arg("number", FloatArgumentType.floatArg()).executes(context -> {
            float y = -((float) mc.player.getY()) - 3.0f;

            if (y == 0.0f) y = context.getArgument("number", Float.class);
            execute(y);

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("down").executes(context -> {
            int i;
            float y = 0.0f;

            for (i = 1; i < 255; ++i) {
                if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, -i, 0)) == Blocks.AIR.getDefaultState()) {
                    y = -i - 1;
                    break;
                }

                if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, -i, 0)) != Blocks.BEDROCK.getDefaultState())
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
                if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, -i, 0)) == Blocks.AIR.getDefaultState()) {
                    y = -i - 1;
                    break;
                }

                if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, -i, 0)) != Blocks.BEDROCK.getDefaultState())
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
                if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, i, 0)) != Blocks.AIR.getDefaultState())
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
                if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, i, 0)) != Blocks.AIR.getDefaultState())
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

        if ((elytra = InventoryUtility.findItemInInventory(Items.ELYTRA).slot()) == -1) {
            sendMessage(Formatting.RED + "вам нужны элитры в инвентаре");
            return;
        }
        if (elytra != -2) {
            mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, mc.player);
        }

        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
        mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + (double) y, mc.player.getZ(), false));
        mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));

        if (elytra != -2) {
            mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, mc.player);
        }

        mc.player.setPosition(mc.player.getX(), mc.player.getY() + (double) y, mc.player.getZ());
    }
}
