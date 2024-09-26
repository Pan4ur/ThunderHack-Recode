package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;
import thunder.hack.events.impl.EventClickSlot;
import thunder.hack.injection.MixinClientPlayerInteractionManager;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class ItemScroller extends Module {
    public ItemScroller() {
        super("ItemScroller", Category.MISC);
    }

    public Setting<Integer> delay = new Setting<>("Delay",80,0,500);

    private boolean pauseListening = false;

    @EventHandler
    public void onClick(EventClickSlot e) {
        if ((isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT))
                && (isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) || isKeyPressed(GLFW.GLFW_KEY_RIGHT_CONTROL))
                && e.getSlotActionType() == SlotActionType.THROW
                && !pauseListening) {
            Item copy = mc.player.currentScreenHandler.slots.get(e.getSlot()).getStack().getItem();
            pauseListening = true;
            for (int i2 = 0; i2 < mc.player.currentScreenHandler.slots.size(); ++i2) {
                if (mc.player.currentScreenHandler.slots.get(i2).getStack().getItem() == copy)
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i2, 1, SlotActionType.THROW, mc.player);
            }
            pauseListening = false;
        }
    }
}
