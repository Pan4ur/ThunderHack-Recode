package thunder.hack.gui.thundergui.components;

import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.ThunderGui2;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanParent;
import thunder.hack.utility.math.FrameRateCounter;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public class BooleanParentComponent extends SettingElement {
    float animation = 0f;
    private final Setting<BooleanParent> parentSetting;

    public BooleanParentComponent(Setting setting) {
        super(setting);
        this.parentSetting = setting;
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
        if ((getY() > ThunderGui2.getInstance().main_posY + ThunderGui2.getInstance().height) || getY() < ThunderGui2.getInstance().main_posY) {
            return;
        }
        FontRenderers.modules.drawString(stack,getSetting().getName(), (float) getX(), (float) getY() + 5, isHovered() ? -1 : new Color(0xB0FFFFFF, true).getRGB(), false);
        animation = fast(animation, getParentSetting().getValue().getState() ? 1 : 0, 15f);
        double paddingX = 7 * animation;
        Color color = ClickGui.getInstance().getColor(0);
        Render2DEngine.drawRound(stack,(float) (x + width - 18), (float) (y + height / 2 - 4), 15, 8, 4, paddingX > 4 ? color : new Color(0xFFB2B1B1));
        Render2DEngine.drawRound(stack,(float) (x + width - 17 + paddingX), (float) (y + height / 2 - 3), 6, 6, 3, new Color(-1));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if ((getY() > ThunderGui2.getInstance().main_posY + ThunderGui2.getInstance().height) || getY() < ThunderGui2.getInstance().main_posY) {
            return;
        }
        if (isHovered()) getParentSetting().getValue().setState(!getParentSetting().getValue().getState());
    }

    public Setting<BooleanParent> getParentSetting() {
        return parentSetting;
    }
}
