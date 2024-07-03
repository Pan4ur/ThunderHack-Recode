package thunder.hack.events.impl;

import net.minecraft.text.ClickEvent;

/*When using a clickable text client, you should create this object instead of the usual ClickEvent.
If not, a vulnerability could occur as mentioned in this GitHub issue: https://github.com/MeteorDevelopment/meteor-client/pull/4399.*/
public class ClientClickEvent extends ClickEvent {
    public ClientClickEvent(Action action, String value) {
        super(action, value);
    }
}