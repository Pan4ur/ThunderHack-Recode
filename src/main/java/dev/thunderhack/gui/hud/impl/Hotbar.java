package dev.thunderhack.gui.hud.impl;

import dev.thunderhack.gui.font.FontRenderers;
import dev.thunderhack.gui.hud.HudElement;
import dev.thunderhack.modules.client.HudEditor;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.render.Render2DEngine;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class Hotbar extends HudElement {
    public Hotbar() {
        super("Hotbar", 0, 0);
    }

    public static final Setting<Mode> lmode = new Setting<>("LeftHandMode", Mode.Merged);

    public enum Mode {
        Merged, Separately
    }

    public static void renderCustomHotbar(float tickDelta, DrawContext context) {
        PlayerEntity playerEntity = mc.player;
        if (playerEntity != null) {

            MatrixStack matrices = context.getMatrices();
            int i = mc.getWindow().getScaledWidth() / 2;
            int o = mc.getWindow().getScaledHeight() - 16 - 3;

            if (mc.player.getOffHandStack().isEmpty()) {
                Render2DEngine.drawGradientGlow(matrices, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), i - 90, mc.getWindow().getScaledHeight() - 25, 180, 20, HudEditor.hudRound.getValue(), 10);
                Render2DEngine.drawGradientRoundShader(matrices, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), i - 90.5f, mc.getWindow().getScaledHeight() - 25.5f, 181, 21, HudEditor.hudRound.getValue());
                Render2DEngine.drawRoundShader(matrices, i - 90, mc.getWindow().getScaledHeight() - 22 + 1 - 4, 180f, 20, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());
                Render2DEngine.drawRoundShader(matrices, i - 88 + playerEntity.getInventory().selectedSlot * 19.8f, mc.getWindow().getScaledHeight() - 24, 18, 18, 5f, HudEditor.plateColor.getValue().getColorObject().brighter().brighter().brighter());
            } else if (lmode.getValue() == Mode.Merged) {
                Render2DEngine.drawGradientGlow(matrices, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), i - 109, mc.getWindow().getScaledHeight() - 25, 198, 20, HudEditor.hudRound.getValue(), 10);
                Render2DEngine.drawGradientRoundShader(matrices, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), i - 109.5f, mc.getWindow().getScaledHeight() - 25.5f, 199, 21, HudEditor.hudRound.getValue());
                Render2DEngine.drawRoundShader(matrices, i - 109, mc.getWindow().getScaledHeight() - 22 + 1 - 4, 198f, 20, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());
                Render2DEngine.drawRoundShader(matrices, i - 88 + playerEntity.getInventory().selectedSlot * 19.8f, mc.getWindow().getScaledHeight() - 24, 18, 18, 5f, HudEditor.plateColor.getValue().getColorObject().brighter().brighter().brighter());
                renderHotbarItem(context, i - 109, o - 5, playerEntity.getOffHandStack());
                Render2DEngine.verticalGradient(matrices, i - 109 + 18, mc.getWindow().getScaledHeight() - 22 + 1 - 4, i - 108 + 18 - 0.5f, mc.getWindow().getScaledHeight() - 11 + 1 - 4, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
                Render2DEngine.verticalGradient(matrices, i - 109 + 18, mc.getWindow().getScaledHeight() - 11 - 4, i - 108 + 18 - 0.5f, mc.getWindow().getScaledHeight() - 5, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));
            } else {
                Render2DEngine.drawGradientGlow(matrices, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), i - 90, mc.getWindow().getScaledHeight() - 25, 180, 20, HudEditor.hudRound.getValue(), 10);
                Render2DEngine.drawGradientRoundShader(matrices, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), i - 91, mc.getWindow().getScaledHeight() - 22 + 1 - 5, 182, 22, HudEditor.hudRound.getValue());
                Render2DEngine.drawRoundShader(matrices, i - 90, mc.getWindow().getScaledHeight() - 22 + 1 - 4, 180f, 20, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());
                Render2DEngine.drawRoundShader(matrices, i - 88 + playerEntity.getInventory().selectedSlot * 19.8f, mc.getWindow().getScaledHeight() - 24, 18, 18, 5f, HudEditor.plateColor.getValue().getColorObject().brighter().brighter().brighter());

                Render2DEngine.drawGradientGlow(matrices, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), i - 117, mc.getWindow().getScaledHeight() - 25, 22, 22, HudEditor.hudRound.getValue(), 10);
                Render2DEngine.drawGradientRoundShader(matrices, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), i - 117, mc.getWindow().getScaledHeight() - 22 + 1 - 5, 22, 22, HudEditor.hudRound.getValue());
                Render2DEngine.drawRoundShader(matrices, i - 116, mc.getWindow().getScaledHeight() - 22 + 1 - 4, 20f, 20, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());
                renderHotbarItem(context, i - 114, o - 5, playerEntity.getOffHandStack());
            }

            for (int m = 0; m < 9; ++m) {
                int n = i - 90 + m * 20 + 2;
                if (m == mc.player.getInventory().selectedSlot) renderHotbarItem(context, n, o - 7, playerEntity.getInventory().main.get(m));
                else renderHotbarItem(context, n, o - 5, playerEntity.getInventory().main.get(m));
            }
        }

    }

    private static void renderHotbarItem(DrawContext context, int i, int j, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            context.getMatrices().push();
            context.getMatrices().translate((float) (i + 8), (float) (j + 12), 0.0F);
            context.getMatrices().scale(0.9f, 0.9f, 1.0F);
            context.getMatrices().translate((float) (-(i + 8)), (float) (-(j + 12)), 0.0F);
            context.drawItem(itemStack, i, j);
            context.drawItemInSlot(mc.textRenderer, itemStack, i, j);
            context.getMatrices().pop();
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
            k = (int) ((mc.getWindow().getScaledWidth() - FontRenderers.sf_bold_mini.getStringWidth(string)) / 2);
            l = mc.getWindow().getScaledHeight() - 31 - 4;
            FontRenderers.sf_bold_mini.drawString(matrices, string, k, l, 8453920);
            mc.getProfiler().pop();
        }
    }
}
