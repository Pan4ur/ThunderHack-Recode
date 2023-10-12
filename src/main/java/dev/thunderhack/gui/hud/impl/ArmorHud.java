package dev.thunderhack.gui.hud.impl;

import dev.thunderhack.gui.hud.HudElement;
import dev.thunderhack.modules.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ArmorHud extends HudElement {
    public ArmorHud() {
        super("ArmorHud", 60, 25);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        float xItemOffset = getPosX();
        for (ItemStack itemStack : Module.mc.player.getInventory().armor) {
            if (itemStack.isEmpty()) continue;
            context.drawItem(itemStack, (int) xItemOffset, (int) getPosY());
            context.drawItemInSlot(Module.mc.textRenderer,itemStack,  (int) xItemOffset, (int) getPosY());
            xItemOffset += 20;
        }
    }
}
