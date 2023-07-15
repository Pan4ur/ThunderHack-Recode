package thunder.hack.gui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.Util;
import thunder.hack.utility.render.MSAAFramebuffer;
import thunder.hack.utility.render.Render2DEngine;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class Hotbar extends HudElement {
    public Hotbar() {
        super("Hotbar", "Hotbar", 0, 0);
    }

    public static void renderCustomHotbar(float tickDelta, DrawContext context) {
        MSAAFramebuffer.use(() -> {
            PlayerEntity playerEntity = mc.player;
            if (playerEntity != null) {

                MatrixStack matrices = context.getMatrices();

                int i = Util.getScaledResolution().getScaledWidth() / 2;
                Render2DEngine.drawGradientBlurredShadow(matrices, i - 91, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 5, 182, 22, 10, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
                Render2DEngine.renderRoundedGradientRect(matrices, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), i - 91, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 5, 182, 22, HudEditor.hudRound.getValue());
                Render2DEngine.drawRound(matrices, i - 90, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 4, 180.5f, 20, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());
                Render2DEngine.drawRound(matrices, i - 88 + playerEntity.getInventory().selectedSlot * 19.8f, Util.getScaledResolution().getScaledHeight() - 24, 18, 18, 5f, HudEditor.plateColor.getValue().getColorObject().brighter().brighter().brighter());

                int l = 1;

                int m;
                int n;
                int o;
                for (m = 0; m < 9; ++m) {
                    n = i - 90 + m * 20 + 2;
                    o = Util.getScaledResolution().getScaledHeight() - 16 - 3;
                    if (m == mc.player.getInventory().selectedSlot) {
                        renderHotbarItem(context, n, o - 7, tickDelta, playerEntity, (ItemStack) playerEntity.getInventory().main.get(m), l++);
                    } else {
                        renderHotbarItem(context, n, o - 5, tickDelta, playerEntity, (ItemStack) playerEntity.getInventory().main.get(m), l++);
                    }
                }
            }
        });
    }

    private static void renderHotbarItem(DrawContext context, int i, int j, float f, PlayerEntity playerEntity, ItemStack itemStack, int k) {
        if (!itemStack.isEmpty()) {
            context.getMatrices().push();

            context.getMatrices().translate((float)(i + 8), (float)(j + 12), 0.0F);
            context.getMatrices().scale(0.9f, 0.9f, 1.0F);
            context.getMatrices().translate((float)(-(i + 8)), (float)(-(j + 12)), 0.0F);

           // mc.getItemRenderer().renderInGuiWithOverrides(matrixStack, playerEntity, itemStack, i, j, k);

            context.drawItem(itemStack, i, j);
            context.drawItemInSlot(mc.textRenderer,itemStack, i, j);

            context.getMatrices().pop();

           // mc.getItemRenderer().renderItem(matrixStack, mc.textRenderer, itemStack, i, j);
        }
    }

    public static void renderXpBar(int x, MatrixStack matrices) {
        mc.getProfiler().push("expBar");
        int k;
        int l;
        mc.getProfiler().pop();

        if (mc.player.experienceLevel > 0) {
            mc.getProfiler().push("expLevel");
            String string = "" + mc.player.experienceLevel;
            k = (int) ((Util.getScaledResolution().getScaledWidth() - FontRenderers.sf_bold_mini.getStringWidth(string)) / 2);
            l = Util.getScaledResolution().getScaledHeight() - 31 - 4;
            FontRenderers.sf_bold_mini.drawString(matrices,string,k,l,8453920);
            mc.getProfiler().pop();
        }
    }
}
