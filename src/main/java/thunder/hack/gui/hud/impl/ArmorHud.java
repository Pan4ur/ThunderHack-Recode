package thunder.hack.gui.hud.impl;

import com.google.common.eventbus.Subscribe;
import net.minecraft.item.ItemStack;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.gui.hud.HudElement;

public class ArmorHud extends HudElement {
    public ArmorHud() {
        super("ArmorHud", "ArmorHud", 60, 25);
    }

    @Subscribe
    public void onRender2D(Render2DEvent e) {
        super.onRender2D(e);
        float xItemOffset = getPosX();
        for (ItemStack itemStack : mc.player.getInventory().armor) {
            if (itemStack.isEmpty()) continue;
            e.getContext().drawItem(itemStack, (int) xItemOffset, (int) getPosY());
            e.getContext().drawItemInSlot(mc.textRenderer,itemStack,  (int) xItemOffset, (int) getPosY());
            xItemOffset += 20;
        }
    }
}
