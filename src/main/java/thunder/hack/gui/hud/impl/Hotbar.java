package thunder.hack.gui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
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

    public static final Setting<Mode> lmode = new Setting("LeftHandMode", Mode.Merged);
    public enum Mode {
        Merged, Separately
    }

    public static void renderCustomHotbar(float tickDelta, DrawContext context) {
            PlayerEntity playerEntity = mc.player;
            if (playerEntity != null) {

                MatrixStack matrices = context.getMatrices();
                int i = Util.getScaledResolution().getScaledWidth() / 2;
                int o = Util.getScaledResolution().getScaledHeight() - 16 - 3;

                if(mc.player.getOffHandStack().isEmpty()) {
                    Render2DEngine.drawGradientBlurredShadow(matrices, i - 91, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 5, 182, 22, 10, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
                    Render2DEngine.drawGradientRoundShader(matrices, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), i - 91, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 5, 182, 22, HudEditor.hudRound.getValue());
                    Render2DEngine.drawRoundShader(matrices, i - 90, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 4, 180f, 20, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());
                    Render2DEngine.drawRoundShader(matrices, i - 88 + playerEntity.getInventory().selectedSlot * 19.8f, Util.getScaledResolution().getScaledHeight() - 24, 18, 18, 5f, HudEditor.plateColor.getValue().getColorObject().brighter().brighter().brighter());
                } else if(lmode.getValue() == Mode.Merged){
                    Render2DEngine.drawGradientBlurredShadow(matrices, i - 110, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 5, 200, 22, 10, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
                    Render2DEngine.drawGradientRoundShader(matrices, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), i - 110, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 5, 200, 22, HudEditor.hudRound.getValue());
                    Render2DEngine.drawRoundShader(matrices, i - 109, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 4, 198f, 20, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());
                    Render2DEngine.drawRoundShader(matrices, i - 88 + playerEntity.getInventory().selectedSlot * 19.8f, Util.getScaledResolution().getScaledHeight() - 24, 18, 18, 5f, HudEditor.plateColor.getValue().getColorObject().brighter().brighter().brighter());
                    renderHotbarItem(context, i - 109, o - 5, tickDelta, playerEntity, (ItemStack) playerEntity.getOffHandStack(), -1);
                    Render2DEngine.verticalGradient(matrices,i - 109 + 18, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 4, i - 108 + 18 - 0.5, Util.getScaledResolution().getScaledHeight() - 11 + 1 - 4,Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(),0).getRGB(), HudEditor.textColor.getValue().getColorObject().getRGB());
                    Render2DEngine.verticalGradient(matrices,i - 109 + 18, Util.getScaledResolution().getScaledHeight() - 11 - 4, i - 108 + 18 - 0.5, Util.getScaledResolution().getScaledHeight()  - 5, HudEditor.textColor.getValue().getColorObject().getRGB(),Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(),0).getRGB());
                } else {
                    Render2DEngine.drawGradientBlurredShadow(matrices, i - 91, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 5, 182, 22, 10, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
                    Render2DEngine.drawGradientRoundShader(matrices, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), i - 91, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 5, 182, 22, HudEditor.hudRound.getValue());
                    Render2DEngine.drawRoundShader(matrices, i - 90, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 4, 180f, 20, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());
                    Render2DEngine.drawRoundShader(matrices, i - 88 + playerEntity.getInventory().selectedSlot * 19.8f, Util.getScaledResolution().getScaledHeight() - 24, 18, 18, 5f, HudEditor.plateColor.getValue().getColorObject().brighter().brighter().brighter());

                    Render2DEngine.drawGradientBlurredShadow(matrices, i - 117, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 5, 22, 22, 10, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
                    Render2DEngine.drawGradientRoundShader(matrices, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), i - 117, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 5, 22, 22, HudEditor.hudRound.getValue());
                    Render2DEngine.drawRoundShader(matrices, i - 116, Util.getScaledResolution().getScaledHeight() - 22 + 1 - 4, 20f, 20, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());
                    renderHotbarItem(context, i - 114, o - 5, tickDelta, playerEntity, (ItemStack) playerEntity.getOffHandStack(), -1);
                }

                int l = 1;

                int m;
                int n;
                for (m = 0; m < 9; ++m) {
                    n = i - 90 + m * 20 + 2;
                    if (m == mc.player.getInventory().selectedSlot) {
                        renderHotbarItem(context, n, o - 7, tickDelta, playerEntity, (ItemStack) playerEntity.getInventory().main.get(m), l++);
                    } else {
                        renderHotbarItem(context, n, o - 5, tickDelta, playerEntity, (ItemStack) playerEntity.getInventory().main.get(m), l++);
                    }
                }
            }

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
