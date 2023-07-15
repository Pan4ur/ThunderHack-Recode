package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.Animation;
import thunder.hack.utility.render.animation.DecelerateAnimation;
import thunder.hack.utility.render.animation.Direction;
import thunder.hack.setting.Setting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.awt.*;

public class ParentElement extends AbstractElement {

    private final Setting<Parent> parentSetting;

    final Identifier arrow = new Identifier("textures/arrow.png");

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

        matrixStack.push();
        matrixStack.translate(tx, ty, 0);
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) (-180f * rotation.getOutput())));
        matrixStack.translate(-tx, -ty, 0);
        Render2DEngine.drawTexture(context,arrow, (int) (x + width - 14), (int) (y + (17 - 6) / 2), 6, 6);
        matrixStack.pop();


        


        FontRenderers.getRenderer().drawString(matrixStack, setting.getName() ,(int) (x + 6 + (6 * getParentSetting().getValue().getHierarchy() )), (int) (y + height / 2 - 2), new Color(-1).getRGB());
    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered) {
            getParentSetting().getValue().setExtended(!getParentSetting().getValue().isExtended());
        }
    }




    public Setting<Parent> getParentSetting() {
        return parentSetting;
    }

}