package thunder.hack.events.impl;

import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.events.Event;

public class EventDeath extends Event {
    private final PlayerEntity player;

    public EventDeath(PlayerEntity player) {
        this.player = player;
    }

    public PlayerEntity getPlayer(){
        return player;
    }
}
