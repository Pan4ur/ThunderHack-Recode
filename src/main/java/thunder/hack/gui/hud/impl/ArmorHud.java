package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import thunder.hack.gui.hud.HudElement;

public class ArmorHud extends HudElement {
    public ArmorHud() {
        super("ArmorHud", 60, 25);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        float xItemOffset = getPosX();
        for (ItemStack itemStack : mc.player.getInventory().armor) {
            if (itemStack.isEmpty()) continue;
            context.drawItem(itemStack, (int) xItemOffset, (int) getPosY());
            context.drawItemInSlot(mc.textRenderer,itemStack,  (int) xItemOffset, (int) getPosY());
            xItemOffset += 20;
        }
    }
}
