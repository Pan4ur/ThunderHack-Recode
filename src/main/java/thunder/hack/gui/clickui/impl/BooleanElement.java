package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.FrameRateCounter;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

import static thunder.hack.utility.render.animation.AnimationUtility.fast;

public class BooleanElement extends AbstractElement {
    public BooleanElement(Setting setting, boolean small) {
        super(setting, small);
    }

    float animation = 0f;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context,mouseX, mouseY, delta);
        animation = fast(animation, (boolean) setting.getValue() ? 1 : 0, 15f);
        double paddingX = 7 * animation;


        Render2DEngine.drawBlurredShadow(context.getMatrices(),(float) (x + width - 21), (float) (y + height / 2 - 4), 15, 8, 8, Render2DEngine.injectAlpha(new Color(0x000000), (int) (100 * (paddingX / 7f))));

        Render2DEngine.drawRound(context.getMatrices(),(float) (x + width - 21), (float) (y + height / 2 - 4), 15, 8, 4, paddingX > 4 ? ClickGui.getInstance().getColor(0) : new Color(0xFFB2B1B1));
        Render2DEngine.drawRound(context.getMatrices(),(float) (x + width - 20 + paddingX), (float) (y + height / 2 - 3), 6, 6, 3, new Color(-1));

        if(setting.parent != null) {
            Render2DEngine.drawRect(context.getMatrices(), (float) x + 4, (float) y, 1f, 15, ClickGui.getInstance().getColor(1));
        }

        if(!isSmall()) {
            FontRenderers.settings.drawString(context.getMatrices(), setting.getName(), (setting.parent != null ? 2f : 0f) + (x + 6), (y + height / 2 - (6 / 2f)) + 2, new Color(-1).getRGB());
        } else {
            FontRenderers.sf_medium_mini.drawString(context.getMatrices(), setting.getName(), (setting.parent != null ? 2f : 0f) + (x + 6), (y + height / 2 - (6 / 2f)) + 2, new Color(-1).getRGB());
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered && button == 0) {
            setting.setValue(!((Boolean) setting.getValue()));
        }
        super.mouseClicked(mouseX, mouseY, button);
    }
}