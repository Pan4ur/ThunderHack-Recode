package dev.thunderhack.event.events;

import dev.thunderhack.event.Event;

public class PostPlayerUpdateEvent extends Event {
    private int iterations;

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int in) {
        iterations = in;
    }
}