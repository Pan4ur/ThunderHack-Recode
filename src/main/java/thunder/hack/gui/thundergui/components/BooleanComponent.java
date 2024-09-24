package thunder.hack.gui.thundergui.components;

import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

import static thunder.hack.utility.render.animation.AnimationUtility.fast;

public class BooleanComponent extends SettingElement {
    float animation = 0f;

    public BooleanComponent(Setting setting) {
        super(setting);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        if ((getY() > ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height) || getY() < ThunderGui.getInstance().main_posY) {
            return;
        }
        FontRenderers.modules.drawString(stack, getSetting().getName(), getX(), getY() + 5, isHovered() ? -1 : new Color(0xB0FFFFFF, true).getRGB());
        animation = fast(animation, (boolean) setting.getValue() ? 1 : 0, 15f);
        double paddingX = 7 * animation;
        Color color = HudEditor.getColor(1);
        Render2DEngine.drawRound(stack, x + width - 18, y + height / 2 - 4, 15, 8, 4, paddingX > 4 ? color : new Color(0xFFB2B1B1));
        Render2DEngine.drawRound(stack, (float) (x + width - 17 + paddingX), y + height / 2 - 3, 6, 6, 3, new Color(-1));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if ((getY() > ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height) || getY() < ThunderGui.getInstance().main_posY) {
            return;
        }
        if (isHovered()) setting.setValue(!((Boolean) setting.getValue()));
    }
}
