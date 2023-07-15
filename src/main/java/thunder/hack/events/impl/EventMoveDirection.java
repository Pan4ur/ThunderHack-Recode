package thunder.hack.events.impl;

import thunder.hack.events.Event;

public class EventMoveDirection extends Event {
    private boolean post;

    public EventMoveDirection(boolean post) {
        this.post = post;
    }

    public boolean isPost() {
        return this.post;
    }
}
