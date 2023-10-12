package dev.thunderhack.gui.thundergui.components;

import dev.thunderhack.gui.font.FontRenderers;
import dev.thunderhack.gui.thundergui.ThunderGui;
import dev.thunderhack.modules.client.ClickGui;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.math.FrameRateCounter;
import dev.thunderhack.utils.math.MathUtility;
import dev.thunderhack.utils.render.Render2DEngine;
import net.minecraft.client.util.math.MatrixStack;


import java.awt.*;

import static dev.thunderhack.utils.math.MathUtility.clamp;

public class BooleanComponent extends SettingElement {
    float animation = 0f;

    public BooleanComponent(Setting setting) {
        super(setting);
    }

    public static double deltaTime() {
        return FrameRateCounter.INSTANCE.getFps() > 0 ? (1.0000 / FrameRateCounter.INSTANCE.getFps() ) : 1;
    }

    public static float fast(float end, float start, float multiple) {
        return (1 - MathUtility.clamp((float) (deltaTime() * multiple), 0, 1)) * end + MathUtility.clamp((float) (deltaTime() * multiple), 0, 1) * start;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack,mouseX, mouseY, partialTicks);
        if ((getY() > ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height) || getY() < ThunderGui.getInstance().main_posY) {
            return;
        }
        FontRenderers.modules.drawString(stack,getSetting().getName(), (float) getX(), (float) getY() + 5, isHovered() ? -1 : new Color(0xB0FFFFFF, true).getRGB(), false);
        animation = fast(animation, (boolean) setting.getValue() ? 1 : 0, 15f);
        double paddingX = 7 * animation;
        Color color = ClickGui.getInstance().getColor(0);
        Render2DEngine.drawRound(stack,(float) (x + width - 18), (float) (y + height / 2 - 4), 15, 8, 4, paddingX > 4 ? color : new Color(0xFFB2B1B1));
        Render2DEngine.drawRound(stack,(float) (x + width - 17 + paddingX), (float) (y + height / 2 - 3), 6, 6, 3, new Color(-1));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if ((getY() > ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height) || getY() < ThunderGui.getInstance().main_posY) {
            return;
        }
        if (isHovered()) setting.setValue(!((Boolean) setting.getValue()));
    }
}
