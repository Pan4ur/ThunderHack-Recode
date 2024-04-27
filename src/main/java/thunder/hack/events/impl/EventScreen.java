package thunder.hack.events.impl;

import net.minecraft.client.gui.screen.Screen;
import thunder.hack.events.Event;

public class EventScreen extends Event {
    private final Screen screen;

    public EventScreen(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }
}
