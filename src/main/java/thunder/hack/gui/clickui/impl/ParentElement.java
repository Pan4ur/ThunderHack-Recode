package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.setting.Setting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

import java.awt.*;

import static thunder.hack.gui.clickui.normal.ClickUI.arrow;
import static thunder.hack.utility.render.animation.AnimationUtility.fast;

public class ParentElement extends AbstractElement {
    private final Setting<Parent> parentSetting;
    private float animation;

    public ParentElement(Setting setting, boolean small) {
        super(setting, small);
        this.parentSetting = setting;
    }



    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context,mouseX,mouseY,delta);

        MatrixStack matrixStack = context.getMatrices();

        float tx = (float) (x + width - 11f);
        float ty = (float) y + 8.5f;

        animation = fast(animation, getParentSetting().getValue().isExtended() ? 0 : 1, 15f);

        matrixStack.push();
        matrixStack.translate(tx, ty, 0);
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-180f * animation));
        matrixStack.translate(-tx, -ty, 0);
        context.drawTexture(arrow, (int) (x + width - 14), (int) (y + 5.5f), 0, 0, 6, 6, 6, 6);
        matrixStack.pop();

        if(!isSmall()) {
            FontRenderers.getSettingsRenderer().drawString(matrixStack, setting.getName(), (int) (x + 6 + (6 * getParentSetting().getValue().getHierarchy())), (y + height / 2 - 1f), new Color(-1).getRGB());
        } else {
            FontRenderers.sf_medium_mini.drawString(matrixStack, setting.getName(), (int) (x + 6 + (6 * getParentSetting().getValue().getHierarchy())), (y + height / 2 - 1f), new Color(-1).getRGB());
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered) {
            getParentSetting().getValue().setExtended(!getParentSetting().getValue().isExtended());
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    public Setting<Parent> getParentSetting() {
        return parentSetting;
    }
}