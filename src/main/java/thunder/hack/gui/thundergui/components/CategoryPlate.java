package thunder.hack.gui.thundergui.components;

import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

import static thunder.hack.utility.render.animation.AnimationUtility.fast;

public class CategoryPlate {
    float category_animation = 0f;
    private final Module.Category cat;
    private int posX;
    private int posY;

    public CategoryPlate(Module.Category cat, int posX, int posY) {
        this.cat = cat;
        this.posX = posX;
        this.posY = posY;
    }

    public void render(MatrixStack matrixStack, int MouseX, int MouseY) {
        category_animation = fast(category_animation, isHovered(MouseX, MouseY) ? 1 : 0, 15f);
        Render2DEngine.addWindow(matrixStack, new Render2DEngine.Rectangle(posX, posY + 0.5f, posX + 84, posY + 15.5f));
        if (isHovered(MouseX, MouseY)) {
            Render2DEngine.drawRound(matrixStack, posX, posY, 84, 15, 2f, new Color(25, 20, 30, (int) MathUtility.clamp(65 * category_animation, 0, 255)));
            Render2DEngine.drawBlurredShadow(matrixStack, MouseX - 20, MouseY - 20, 40, 40, 60, new Color(0xC3555A7E, true));
        }
        FontRenderers.modules.drawString(matrixStack, cat.getName(), posX + 5, posY + 6, -1);
        Render2DEngine.popWindow();
    }

    public void movePosition(float deltaX, float deltaY) {
        this.posY += deltaY;
        this.posX += deltaX;
    }

    public void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        if (isHovered(mouseX, mouseY)) {
            ThunderGui.getInstance().new_category = this.cat;
            if (ThunderGui.getInstance().current_category == null) {
                ThunderGui.getInstance().current_category = Module.Category.HUD;
                ThunderGui.getInstance().new_category = this.cat;
            }
        }
    }

    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX > posX && mouseX < posX + 84 && mouseY > posY && mouseY < posY + 15;
    }

    public Module.Category getCategory() {
        return this.cat;
    }

    public int getPosY() {
        return this.posY;
    }
}
