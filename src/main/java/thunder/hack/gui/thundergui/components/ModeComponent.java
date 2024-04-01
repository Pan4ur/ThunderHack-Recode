package thunder.hack.gui.thundergui.components;

import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class ModeComponent extends SettingElement {

    int progress = 0;
    private double wheight;
    private boolean open;

    public ModeComponent(Setting setting) {
        super(setting);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack,mouseX, mouseY, partialTicks);
        if ((getY() > ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height) || getY() < ThunderGui.getInstance().main_posY) {
            return;
        }
        FontRenderers.modules.drawString(stack,getSetting().getName(), (float) getX(), (float) getY() + 5, isHovered() ? -1 : new Color(0xB0FFFFFF, true).getRGB());


        if (open) {
            double offsetY2 = 0;
            for (int i = 0; i <= setting.getModes().length - 1; i++) {
                offsetY2 += 12;
            }
            Render2DEngine.drawRound(stack,(float) (x + 114), (float) (y + 2), 62F, (float) (11 + offsetY2), 0.5f, new Color(50, 35, 60, 121));
        }

        if (mouseX > x + 114 && mouseX < x + 176 && mouseY > y + 2 && mouseY < y + 15) {
            Render2DEngine.drawRound(stack,(float) (x + 114), (float) (y + 2), 62, 11, 0.5f, new Color(82, 57, 100, 178));
        } else {
            Render2DEngine.drawRound(stack,(float) (x + 114), (float) (y + 2), 62, 11, 0.5f, new Color(50, 35, 60, 178));
        }

        FontRenderers.modules.drawString(stack,setting.currentEnumName(), (float) (x + 116), (float) (y + 6), new Color(0xB0FFFFFF, true).getRGB());

        String arrow = "n";
        switch (progress) {
            case 0:
                arrow = "n";
                break;
            case 1:
                arrow = "o";
                break;
            case 2:
                arrow = "p";
                break;
            case 3:
                arrow = "q";
                break;
            case 4:
                arrow = "r";
                break;
        }
        FontRenderers.icons.drawString(stack,arrow, (int) (x + 166), (int) (y + 7), -1);

        double offsetY = 13;
        if (open) {
            Color color = HudEditor.getColor(1);
            for (int i = 0; i <= setting.getModes().length - 1; i++) {
                FontRenderers.settings.drawString(stack,setting.getModes()[i], (float) (x + 116), (float) ((y + 5) + offsetY), setting.currentEnumName().equalsIgnoreCase(setting.getModes()[i]) ? color.getRGB() : -1);
                offsetY += 12;
            }
        }
    }

    @Override
    public void onTick() {
        if (open && progress > 0) {
            progress--;
        }
        if (!open && progress < 4) {
            progress++;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if ((getY() > ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height) || getY() < ThunderGui.getInstance().main_posY) {
            return;
        }
        if (mouseX > x + 114 && mouseX < x + 176 && mouseY > y + 2 && mouseY < y + 15) {
            open = !open;
        }
        if (open) {
            double offsetY = 0;
            for (int i = 0; i <= setting.getModes().length - 1; i++) {
                if (Render2DEngine.isHovered(mouseX, mouseY, x, y + wheight + offsetY, width, 12) && button == 0)
                    setting.setEnumByNumber(i);
                offsetY += 12;
            }
        }
    }

    public void setWHeight(double height) {
        this.wheight = height;
    }

    public boolean isOpen() {
        return open;
    }

}
