package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventCollision;
import thunder.hack.events.impl.EventFluidCollision;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IPlayerMoveC2SPacket;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class Jesus extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.SOLID);

    public Jesus() {
        super("Jesus", Category.MOVEMENT);
    }

    @EventHandler
    public void onCollide(EventCollision e) {
        if (e.getState().getBlock() instanceof FluidBlock) {
            e.setState(mode.is(Mode.SOLID) ? Blocks.ENDER_CHEST.getDefaultState() : Blocks.OBSIDIAN.getDefaultState());
        }
    }

    public enum Mode {
        SOLID, SOLID2
    }
}
