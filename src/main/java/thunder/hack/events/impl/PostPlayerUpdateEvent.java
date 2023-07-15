package thunder.hack.events.impl;

import thunder.hack.events.Event;

public class PostPlayerUpdateEvent extends Event {
    private int iterations;

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int in) {
        iterations = in;
    }
}