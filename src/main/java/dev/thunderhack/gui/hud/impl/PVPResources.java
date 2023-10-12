package dev.thunderhack.gui.hud.impl;

import dev.thunderhack.gui.hud.HudElement;
import dev.thunderhack.modules.Module;
import dev.thunderhack.modules.client.HudEditor;
import dev.thunderhack.utils.render.Render2DEngine;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import dev.thunderhack.gui.font.FontRenderers;

import java.util.ArrayList;
import java.util.List;

public class PVPResources extends HudElement {

    public PVPResources() {
        super("PVPResources", 60, 60);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
    }

    public void onRenderShaders(DrawContext context) {
        Render2DEngine.drawGradientGlow(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX(), getPosY(), 50, 50, HudEditor.hudRound.getValue(), 10);
        Render2DEngine.drawGradientRoundShader(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX() - 0.5f, getPosY() - 0.5f, 51, 51, HudEditor.hudRound.getValue());
        Render2DEngine.drawRoundShader(context.getMatrices(), getPosX(), getPosY(), 50, 50, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());

        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2, getPosY() + 24.5f, getPosX() + 26, getPosY() + 25, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 26, getPosY() + 24.5f, getPosX() + 48, getPosY() + 25, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));

        Render2DEngine.verticalGradient(context.getMatrices(), getPosX() + 25.5f, getPosY() + 2, getPosX() + 26, getPosY() + 23, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.verticalGradient(context.getMatrices(), getPosX() + 25.5f, getPosY() + 23, getPosX() + 26, getPosY() + 48, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));

        int totemCount = getItemCount(Items.TOTEM_OF_UNDYING);
        int xpCount = getItemCount(Items.EXPERIENCE_BOTTLE);
        int crystalCount = getItemCount(Items.END_CRYSTAL);
        int gappleCount = getItemCount(Items.ENCHANTED_GOLDEN_APPLE);

        List<ItemStack> list = new ArrayList<>();

        if (totemCount > 0) list.add(new ItemStack(Items.TOTEM_OF_UNDYING, totemCount));
        if (xpCount > 0) list.add(new ItemStack(Items.EXPERIENCE_BOTTLE, xpCount));
        if (crystalCount > 0) list.add(new ItemStack(Items.END_CRYSTAL, crystalCount));
        if (gappleCount > 0) list.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, gappleCount));

        for (int i = 0; i < list.size(); ++i) {
            int offsetX = i % 2 * 25;
            int offsetY = i / 2 * 25;
            context.drawItem(list.get(i), (int) (getPosX() + offsetX + 4), (int) (getPosY() + offsetY + 2));
            FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), String.valueOf(list.get(i).getCount()), (int) (getPosX() + offsetX + 12), (int) (getPosY() + offsetY + 16), HudEditor.textColor.getValue().getColor());
        }
    }

    public int getItemCount(Item item) {
        if (Module.mc.player == null) return 0;
        int n = 0;
        int n2 = 44;
        for (int i = 0; i <= n2; ++i) {
            ItemStack itemStack = Module.mc.player.getInventory().getStack(i);
            if (itemStack.getItem() != item) continue;
            n += itemStack.getCount();
        }
        return n;
    }
}