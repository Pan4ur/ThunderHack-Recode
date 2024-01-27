package thunder.hack.utility;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;

public interface UtilsMixin {
    MinecraftClient mc = MinecraftClient.getInstance();

    default void sendPacket(Packet<?> packet) {
        assert mc.getNetworkHandler() != null;

        mc.getNetworkHandler().sendPacket(packet);
    }

    default BlockState getBlockState(BlockPos pos) {
        assert mc.world != null;

        return mc.world.getBlockState(pos);
    }
}
