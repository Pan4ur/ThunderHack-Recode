package thunder.hack.events.impl;

import net.minecraft.util.math.Vec3d;

public class EventPlayerJump {

    private boolean pre;

    public EventPlayerJump(boolean pre) {
        this.pre = pre;
    }

    public boolean isPre() {
        return pre;
    }
}
