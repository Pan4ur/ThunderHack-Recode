package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.ThunderHack;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;

import java.awt.*;

import static thunder.hack.gui.clickui.ClickGUI.arrow;
import static thunder.hack.utility.render.animation.AnimationUtility.fast;

public class ParentElement extends AbstractElement {
    private final Setting<SettingGroup> parentSetting;
    private float animation;

    public ParentElement(Setting setting) {
        super(setting);
        this.parentSetting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        MatrixStack matrixStack = context.getMatrices();

        float tx = x + width - 11f;
        float ty = y + 8.5f;

        animation = fast(animation, getParentSetting().getValue().isExtended() ? 0 : 1, 15f);

        matrixStack.push();
        matrixStack.translate(tx, ty, 0);
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-180f * animation));
        matrixStack.translate(-tx, -ty, 0);
        context.drawTexture(arrow, (int) (x + width - 14), (int) (y + 5.5f), 0, 0, 6, 6, 6, 6);
        matrixStack.pop();
        FontRenderers.sf_medium_mini.drawString(matrixStack, setting.getName(), x + 6 + (6 * getParentSetting().getValue().getHierarchy()), y + height / 2 - 1f, new Color(-1).getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered) {
            getParentSetting().getValue().setExtended(!getParentSetting().getValue().isExtended());
            if (getParentSetting().getValue().isExtended()) {
                ThunderHack.soundManager.playSwipeIn();
            } else {
                ThunderHack.soundManager.playSwipeOut();
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    public Setting<SettingGroup> getParentSetting() {
        return parentSetting;
    }
}