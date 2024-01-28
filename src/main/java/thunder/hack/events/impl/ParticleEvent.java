package thunder.hack.events.impl;

import net.minecraft.client.particle.Particle;
import thunder.hack.events.Event;

public class ParticleEvent extends Event {
    public Particle particle;

    public ParticleEvent(Particle particle) {
        this.particle = particle;
    }
}