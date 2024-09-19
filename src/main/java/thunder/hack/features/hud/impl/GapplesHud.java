package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventEatFood;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

public class GapplesHud extends HudElement {
    public GapplesHud() {
        super("GapplesHud", 0, 0);
    }

    private float angle, prevAngle;

    private final Setting<Boolean> crapple = new Setting<>("Crapple", true);

    public void onRender2D(DrawContext context) {
        Item targetItem = crapple.getValue() ? Items.GOLDEN_APPLE : Items.ENCHANTED_GOLDEN_APPLE;

        if (getItemCount(targetItem) == 0)
            return;

        float xPos = ModuleManager.crosshair.getAnimatedPosX();
        float yPos = ModuleManager.crosshair.getAnimatedPosY();

        float factor = angle > 0 ? angle / 15f : 0f;
        float factor2 = 1f - mc.player.getItemUseTime() / 40f;

        if (mc.player.getActiveItem().getItem() != targetItem)
            factor2 = 1f;

        factor2 = MathUtility.clamp(factor2, 0.01f, 1f);

        context.getMatrices().push();
        context.getMatrices().translate(xPos, yPos, 0);
        context.getMatrices().multiply(RotationAxis.NEGATIVE_Z.rotation((float) Math.toRadians(-Render2DEngine.interpolateFloat(prevAngle, angle, Render3DEngine.getTickDelta()))));
        context.getMatrices().translate(-xPos, -yPos, 0);

        RenderSystem.setShaderColor(0.3f, 0.3f, 0.3f, 1f);
        context.getMatrices().translate(xPos + 20, yPos - 9, 0);
        context.drawItem(targetItem.getDefaultStack(), 0, 0);
        context.getMatrices().translate(-(xPos + 20), -(yPos - 9), 0);
        RenderSystem.setShaderColor(1f, 1f - factor, 1f - factor, 1f);

        context.getMatrices().translate((xPos + 28), (yPos - 1), 0);
        context.getMatrices().scale(factor2, factor2, 1f);
        context.drawItem(targetItem.getDefaultStack(), -8, -8);
        context.getMatrices().scale(factor2 != 0 ? 1f / factor2 : 1f, factor2 != 0 ? 1f / factor2 : 1f, 1f);
        context.getMatrices().translate(-(xPos + 28), -(yPos - 1), 0);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (factor > 0)
            Render2DEngine.drawBlurredShadow(context.getMatrices(), xPos + 22, yPos - 6, 11, 11, 8, Render2DEngine.injectAlpha(new Color(0xFF1500), (int) (255 * factor)));

        FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), getItemCount(targetItem) + "", xPos + 28.5f, yPos + 8, -1);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        context.getMatrices().pop();
    }

    @EventHandler
    public void onEatFood(EventEatFood e) {
        if (e.getFood().getItem() == Items.GOLDEN_APPLE || e.getFood().getItem() == Items.ENCHANTED_GOLDEN_APPLE)
            angle = 15;
    }

    @Override
    public void onUpdate() {
        prevAngle = angle;
        if (angle > 0)
            angle--;
    }

    public int getItemCount(Item item) {
        if (mc.player == null) return 0;
        int n = 0;
        int n2 = 44;
        for (int i = 0; i <= n2; ++i) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (itemStack.getItem() != item) continue;
            n += itemStack.getCount();
        }
        return n;
    }
}
