package thunder.hack.events.impl.entity;

public class EventPlayerJump {
    private boolean pre;

    public EventPlayerJump(boolean pre) {
        this.pre = pre;
    }

    public boolean isPre() {
        return pre;
    }
}
