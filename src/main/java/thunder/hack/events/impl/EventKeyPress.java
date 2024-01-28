package thunder.hack.events.impl;

import thunder.hack.events.Event;

public class EventKeyPress extends Event {
    private final int key;
    private final int scanCode;

    public EventKeyPress(int key, int scanCode) {
        this.key = key;
        this.scanCode = scanCode;
    }

    public int getKey() {
        return key;
    }

    public int getScanCode() {
        return scanCode;
    }
}
