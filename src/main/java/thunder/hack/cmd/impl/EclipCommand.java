package thunder.hack.cmd.impl;


import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.math.NumberUtils;
import thunder.hack.cmd.Command;
import thunder.hack.utility.InventoryUtil;

public class EclipCommand extends Command {
    public EclipCommand() {
        super("eclip");
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage(".eclip <value> , /up/down/bedrock");
            return;
        }

        int elytra;
        int i;
        float y = 0.0f;
        if (commands[0].equals("bedrock")) {
            y = -((float) mc.player.getY()) - 3.0f;
        }
        if (commands[0].equals("down")) {
            for (i = 1; i < 255; ++i) {
                if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, -i, 0)) == Blocks.AIR.getDefaultState()) {
                    y = -i - 1;
                    break;
                }
                if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, -i, 0)) != Blocks.BEDROCK.getDefaultState())
                    continue;
                Command.sendMessage(Formatting.RED + " можно телепортироваться только под бедрок");
                Command.sendMessage(Formatting.RED + " eclip bedrock");
                return;
            }
        }
        if (commands[0].equals("up")) {
            for (i = 4; i < 255; ++i) {
                if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, i, 0)) != Blocks.AIR.getDefaultState())
                    continue;
                y = i + 1;
                break;
            }
        }
        if (y == 0.0f) {
            if (NumberUtils.isNumber(commands[0])) {
                y = Float.parseFloat(commands[0]);
            } else {
                Command.sendMessage(Formatting.RED + commands[0] + Formatting.GRAY + "не являестя числом");
                return;
            }
        }
        if ((elytra = InventoryUtil.getItemSlot(Items.ELYTRA)) == -1) {
            Command.sendMessage(Formatting.RED + "вам нужны элитры в инвентаре");
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