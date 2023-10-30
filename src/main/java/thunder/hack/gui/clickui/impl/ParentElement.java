package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.Animation;
import thunder.hack.utility.render.animation.DecelerateAnimation;
import thunder.hack.utility.render.animation.Direction;
import thunder.hack.setting.Setting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

import java.awt.*;

import static thunder.hack.gui.clickui.normal.ClickUI.arrow;

public class ParentElement extends AbstractElement {
    private final Setting<Parent> parentSetting;

    public ParentElement(Setting setting) {
        super(setting);
        this.parentSetting = setting;
    }

    private final Animation rotation = new DecelerateAnimation(240, 1, Direction.FORWARDS);


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context,mouseX,mouseY,delta);

        MatrixStack matrixStack = context.getMatrices();

        rotation.setDirection(getParentSetting().getValue().isExtended() ? Direction.BACKWARDS : Direction.FORWARDS);
        float tx = (float) (x + width - 11);
        float ty = (float) (y + (17 / 2));

        if(getParentSetting().getValue().isExtended()) {
            Render2DEngine.drawRect(context.getMatrices(), (float) x + 4, (float) y, (float) (getWidth() - 8f), (float) (getHeight()), ClickGui.getInstance().getColor(1));
        }

        matrixStack.push();
        matrixStack.translate(tx, ty, 0);
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) (-180f * rotation.getOutput())));
        matrixStack.translate(-tx, -ty, 0);
        context.drawTexture(arrow, (int) (x + width - 14), (int) (y + (17 - 6) / 2), 0, 0, 6, 6, 6, 6);
        matrixStack.pop();

        FontRenderers.getSettingsRenderer().drawString(matrixStack, setting.getName() ,(int) (x + 6 + (6 * getParentSetting().getValue().getHierarchy() )), (y + height / 2 - (6 / 2f)) + 2, new Color(-1).getRGB());
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