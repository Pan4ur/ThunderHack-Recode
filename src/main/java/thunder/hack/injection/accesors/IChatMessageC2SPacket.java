package thunder.hack.injection.accesors;

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(ChatMessageC2SPacket.class)
public interface IChatMessageC2SPacket {
    @Mutable
    @Accessor("chatMessage")
    void setChatMessage(String val);
}