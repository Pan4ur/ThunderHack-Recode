package dev.thunderhack.utils.player;

import dev.thunderhack.modules.Module;
import net.minecraft.client.network.OtherClientPlayerEntity;

import java.util.Objects;
import java.util.UUID;

public class PlayerEntityCopy extends OtherClientPlayerEntity {
    public PlayerEntityCopy() {
        super(Objects.requireNonNull(Module.mc.world), Objects.requireNonNull(Module.mc.player).getGameProfile());

        copyFrom(Module.mc.player);
        getPlayerListEntry();
        dataTracker.set(PLAYER_MODEL_PARTS, Module.mc.player.getDataTracker().get(PLAYER_MODEL_PARTS));
        setUuid(UUID.randomUUID());
    }

    public void spawn() {
        if (Module.mc.world == null) return;

        unsetRemoved();
        Module.mc.world.addEntity(this);
    }

    public void deSpawn() {
        if (Module.mc.world == null) return;

        Module.mc.world.removeEntity(this.getId(), RemovalReason.DISCARDED);
    }
}
