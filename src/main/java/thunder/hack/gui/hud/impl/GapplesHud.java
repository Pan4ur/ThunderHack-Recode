package thunder.hack.gui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.events.impl.EventEatFood;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public class GapplesHud extends HudElement {
    public GapplesHud() {
        super("GapplesHud", 0, 0);
    }

    private float angle, prevAngle;
    private boolean flip;

    public void onRender2D(DrawContext context) {

        if(getItemCount(Items.GOLDEN_APPLE) == 0)
            return;

        int xPos = mc.getWindow().getScaledWidth() / 2;
        int yPos = mc.getWindow().getScaledHeight() / 2;

        float factor = angle > 0 ? angle / 15f : 0f;
        float factor2 = 1f - mc.player.getItemUseTime() / 40f;

        context.getMatrices().push();
        context.getMatrices().translate(xPos, yPos, 0);
        context.getMatrices().multiply(RotationAxis.NEGATIVE_Z.rotation((float) Math.toRadians(-Render2DEngine.interpolateFloat(prevAngle, angle, mc.getTickDelta()))));
        context.getMatrices().translate(-xPos, -yPos, 0);


        RenderSystem.setShaderColor(0.3f, 0.3f, 0.3f, 1f);
        context.drawItem(Items.GOLDEN_APPLE.getDefaultStack(), xPos + 20, yPos - 9);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.setShaderColor(1f, 1f - factor, 1f - factor, 1f);

        context.getMatrices().translate((xPos + 28), (yPos - 1), 0);
        context.getMatrices().scale(factor2, factor2, 1f);
        context.drawItem(Items.GOLDEN_APPLE.getDefaultStack(), -8, -8);
        context.getMatrices().scale( 1f / factor2 + 0.1f, 1f / factor2 + 0.1f, 1f);
        context.getMatrices().translate(-(xPos + 28), -(yPos - 1), 0);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if(factor > 0)
            Render2DEngine.drawBlurredShadow(context.getMatrices(), xPos + 22, yPos - 6, 11, 11, 8, Render2DEngine.injectAlpha(new Color(0xFF1500), (int) (255 * factor)));

        FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), getItemCount(Items.GOLDEN_APPLE) + "",xPos + 28.5f, yPos + 5, -1);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        context.getMatrices().pop();
    }

    @EventHandler
    public void onEatFood(EventEatFood e) {
        if(e.getFood().getItem() == Items.GOLDEN_APPLE) {
            angle = 15;
        }
    }

    @Override
    public void onUpdate() {
        prevAngle = angle;
        if(angle > 0)
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
