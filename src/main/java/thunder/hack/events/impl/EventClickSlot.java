package thunder.hack.events.impl;

import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.events.Event;

public class EventClickSlot extends Event {
    private final SlotActionType slotActionType;
    private final int slot, button, id;

    public EventClickSlot(SlotActionType slotActionType, int slot, int button, int id) {
        this.slot = slot;
        this.button = button;
        this.id = id;
        this.slotActionType = slotActionType;
    }

    public SlotActionType getSlotActionType() {
        return slotActionType;
    }

    public int getSlot() {
        return slot;
    }

    public int getButton() {
        return button;
    }

    public int getId() {
        return id;
    }
}
