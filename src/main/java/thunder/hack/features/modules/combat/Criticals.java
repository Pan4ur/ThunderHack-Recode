package thunder.hack.features.modules.combat;

import io.netty.buffer.Unpooled;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.*;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public final class Criticals extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.UpdatedNCP);

    public static boolean cancelCrit;

    public Criticals() {
        super("Criticals", Category.COMBAT);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.@NotNull Send event) {
        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket && getInteractType(event.getPacket()) == InteractType.ATTACK) {
            Entity ent = getEntity(event.getPacket());
            if (ent == null || ent instanceof EndCrystalEntity || cancelCrit)
                return;
            doCrit();
        }
    }

    public void doCrit() {
        if (isDisabled() || mc.player == null || mc.world == null)
            return;
        if ((mc.player.isOnGround() || mc.player.getAbilities().flying || mode.is(Mode.Grim)) && !mc.player.isInLava() && !mc.player.isSubmergedInWater()) {
            switch (mode.getValue()) {
                case OldNCP -> {
                    critPacket(0.00001058293536, false);
                    critPacket(0.00000916580235, false);
                    critPacket(0.00000010371854, false);
                }
                case Ncp -> {
                    critPacket(0.0625D, false);
                    critPacket(0., false);
                }
                case UpdatedNCP -> {
                    critPacket(0.000000271875, false);
                    critPacket(0., false);
                }
                case Strict -> {
                    critPacket(0.062600301692775, false);
                    critPacket(0.07260029960661, false);
                    critPacket(0., false);
                    critPacket(0., false);
                }
                case Grim -> {
                    if (!mc.player.isOnGround())
                        critPacket(-0.000001, true);

                }
            }
        }
    }

    private void critPacket(double yDelta, boolean full) {
        if (!full)
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + yDelta, mc.player.getZ(), false));
        else
            sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() + yDelta, mc.player.getZ(), ((IClientPlayerEntity) mc.player).getLastYaw(), ((IClientPlayerEntity) mc.player).getLastPitch(), false));
    }

    public static Entity getEntity(@NotNull PlayerInteractEntityC2SPacket packet) {
        PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
        packet.write(packetBuf);
        return mc.world.getEntityById(packetBuf.readVarInt());
    }

    public static InteractType getInteractType(@NotNull PlayerInteractEntityC2SPacket packet) {
        PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
        packet.write(packetBuf);
        packetBuf.readVarInt();
        return packetBuf.readEnumConstant(InteractType.class);
    }

    public enum InteractType {
        INTERACT, ATTACK, INTERACT_AT
    }

    public enum Mode {
        Ncp, Strict, OldNCP, UpdatedNCP, Grim
    }
}
