package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanParent;
import thunder.hack.utility.math.FrameRateCounter;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

import static thunder.hack.gui.clickui.normal.ClickUI.arrow;
import static thunder.hack.utility.render.animation.AnimationUtility.fast;

public class BooleanParentElement extends AbstractElement {
    private final Setting<BooleanParent> parentSetting;

    public BooleanParentElement(Setting setting, boolean small) {
        super(setting, small);
        this.parentSetting = setting;
    }

    float animation, arrowAnimation;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context,mouseX,mouseY,delta);

        MatrixStack matrixStack = context.getMatrices();

        float tx = (float) (x + width - 11);
        float ty = (float) (y + 8.5f);

        arrowAnimation = fast(arrowAnimation, getParentSetting().getValue().isExtended() ? 0 : 1, 15f);

        matrixStack.push();
        matrixStack.translate(tx, ty, 0);
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-180f * arrowAnimation));
        matrixStack.translate(-tx, -ty, 0);
        context.drawTexture(arrow, (int) (x + width - 14), (int) (y + 6), 0, 0, 6, 6, 6, 6);
        matrixStack.pop();

        if(!isSmall()) {
            FontRenderers.getSettingsRenderer().drawString(matrixStack, setting.getName(), x + 6, y + height / 2 - 1f, new Color(-1).getRGB());
        } else {
            FontRenderers.sf_medium_mini.drawString(matrixStack, setting.getName(), x + 6, y + height / 2 - 1f, new Color(-1).getRGB());
        }

        animation = fast(animation, getParentSetting().getValue().isEnabled() ? 1 : 0, 15f);

        double paddingX = 7 * animation;
        Color color = ClickGui.getInstance().getColor(0);
        Render2DEngine.drawRound(context.getMatrices(),(float) (x + width - 36), (float) (y + height / 2 - 4), 15, 8, 4, paddingX > 4 ? color : new Color(0xFFB2B1B1));
        Render2DEngine.drawRound(context.getMatrices(),(float) (x + width - 35 + paddingX), (float) (y + height / 2 - 3), 6, 6, 3, new Color(-1));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered) {
            if(button == 0) getParentSetting().getValue().setEnabled(!getParentSetting().getValue().isEnabled());
            else getParentSetting().getValue().setExtended(!getParentSetting().getValue().isExtended());
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    public Setting<BooleanParent> getParentSetting() {
        return parentSetting;
    }
}
