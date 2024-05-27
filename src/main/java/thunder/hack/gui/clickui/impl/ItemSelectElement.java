package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.ThunderHack;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.windows.ItemSelectWindow;
import thunder.hack.gui.windows.WindowsScreen;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ItemSelectSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

import static thunder.hack.core.IManager.mc;
import static thunder.hack.gui.clickui.ClickGUI.arrow;

public class ItemSelectElement extends AbstractElement {
    private final Setting<ItemSelectSetting> setting;

    public ItemSelectElement(Setting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context,mouseX,mouseY,delta);
        MatrixStack matrixStack = context.getMatrices();
        FontRenderers.icons.drawString(matrixStack, "H",  x + width - 14f, y + 6f, new Color(0xFFECECEC, true).getRGB());
        FontRenderers.sf_medium_mini.drawString(matrixStack, setting.getName(), x + 6f, (y + height / 2 - 1f), new Color(-1).getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered) {
            mc.setScreen(new WindowsScreen(new ItemSelectWindow(getItemSetting())));
            ThunderHack.soundManager.playSwipeIn();
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    public Setting<ItemSelectSetting> getItemSetting() {
        return setting;
    }
}