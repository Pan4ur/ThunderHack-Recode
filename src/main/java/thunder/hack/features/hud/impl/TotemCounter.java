package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

public class TotemCounter extends HudElement {
    public TotemCounter() {
        super("TotemCounter", 0, 0);
    }

    private float angle, prevAngle;

    public void onRender2D(DrawContext context) {
        if (getItemCount(Items.TOTEM_OF_UNDYING) == 0)
            return;

        float xPos = ModuleManager.crosshair.getAnimatedPosX();
        float yPos = ModuleManager.crosshair.getAnimatedPosY();

        float factor = Math.abs(angle < 0 ? angle / 15f : 0f);

        context.getMatrices().push();
        context.getMatrices().translate(xPos, yPos, 0);
        context.getMatrices().multiply(RotationAxis.NEGATIVE_Z.rotation((float) Math.toRadians(-Render2DEngine.interpolateFloat(prevAngle, angle, Render3DEngine.getTickDelta()))));
        context.getMatrices().translate(-xPos, -yPos, 0);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        context.getMatrices().translate(xPos - 36, yPos - 9, 0);
        context.drawItem(Items.TOTEM_OF_UNDYING.getDefaultStack(), 0, 0);
        context.getMatrices().translate(-(xPos - 36), -(yPos - 9), 0);
        RenderSystem.setShaderColor(1f, 1f - factor, 1f - factor, 1f);

        if (factor > 0)
            Render2DEngine.drawBlurredShadow(context.getMatrices(), xPos - 34, yPos - 6, 11, 11, 8, Render2DEngine.injectAlpha(new Color(0xFF0000), (int) (255 * factor)));

        FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), getItemCount(Items.TOTEM_OF_UNDYING) + "",xPos - 28, yPos + 8, -1);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        context.getMatrices().pop();
    }

    @EventHandler
    public void onTotemPop(TotemPopEvent e) {
        if (e.getEntity() == mc.player)
            angle = -15;
    }

    @Override
    public void onUpdate() {
        prevAngle = angle;
        if (angle < 0)
            angle++;
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
