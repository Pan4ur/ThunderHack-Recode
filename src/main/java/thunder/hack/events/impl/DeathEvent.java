package thunder.hack.events.impl;

import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.events.Event;
import thunder.hack.utility.player.PlayerUtility;

public class DeathEvent extends Event {
    private final PlayerEntity player;

    public DeathEvent(PlayerEntity player) {
        this.player = player;
    }

    public PlayerEntity getPlayer(){
        return player;
    }
}
