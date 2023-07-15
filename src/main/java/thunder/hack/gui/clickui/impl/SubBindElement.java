package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SubBind;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;


public class SubBindElement extends AbstractElement {
    public SubBindElement(Setting setting) {
        super(setting);
    }

    public boolean isListening;




    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context,mouseX,mouseY,delta);
        if (this.isListening) {
            FontRenderers.getRenderer().drawString(context.getMatrices(),"...", (int) (x + 3), (int) (y + height / 2 - (6 / 2f)), new Color(-1).getRGB());
        } else {
            FontRenderers.getRenderer().drawString(context.getMatrices(),"SubBind " + this.setting.getValue().toString().toUpperCase(), (int) (x + 3), (int) (y + height / 2 - (6 / 2f)), new Color(-1).getRGB());
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered && button == 0) {
            isListening = !isListening;
        }
    }

    @Override
    public void keyTyped(int keyCode) {
        if (this.isListening) {
            SubBind subBindbind = new SubBind(keyCode);
            if (subBindbind.toString().equalsIgnoreCase("Escape")) {
                return;
            }
            if (subBindbind.toString().equalsIgnoreCase("Delete")) {
                subBindbind = new SubBind(-1);
            }
            this.setting.setValue(subBindbind);
            isListening = !isListening;
        }
    }
}
