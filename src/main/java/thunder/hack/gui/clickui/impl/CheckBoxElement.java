package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.FrameRateCounter;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public class CheckBoxElement extends AbstractElement {


    public CheckBoxElement(Setting setting) {super(setting);}

    float animation = 0f;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context,mouseX, mouseY, delta);
        animation = fast(animation, (boolean) setting.getValue() ? 1 : 0, 15f);
        double paddingX = 7 * animation;
        Color color = ClickGui.getInstance().getColor(0);
        Render2DEngine.drawRound(context.getMatrices(),(float) (x + width - 21), (float) (y + height / 2 - 4), 15, 8, 4, paddingX > 4 ? color : new Color(0xFFB2B1B1));
        Render2DEngine.drawRound(context.getMatrices(),(float) (x + width - 20 + paddingX), (float) (y + height / 2 - 3), 6, 6, 3, new Color(-1));
        FontRenderers.settings.drawString(context.getMatrices(),setting.getName(), (int) (x + 6), (int) (y + height / 2 - (6 / 2f)) + 2, new Color(-1).getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered && button == 0) {
            setting.setValue(!((Boolean) setting.getValue()));
        }
    }

    public static double deltaTime() {
        return FrameRateCounter.INSTANCE.getFps() > 0 ? (1.0000 / FrameRateCounter.INSTANCE.getFps()) : 1;
    }

    public static float fast(float end, float start, float multiple) {
        return (1 - MathUtility.clamp((float) (deltaTime() * multiple), 0, 1)) * end + MathUtility.clamp((float) (deltaTime() * multiple), 0, 1) * start;
    }

}
