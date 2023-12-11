package thunder.hack.gui.autobuy;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;

import static thunder.hack.core.IManager.mc;

public class LogComponent {
    float scroll_animation = 0f;
    private final ItemStack item;
    private final String text;
    private int posX;
    private int posY;
    private int progress;
    private int fade;
    private final int index;
    private boolean first_open = true;
    private float scrollPosY;
    private float prevPosY;

    public LogComponent(ItemStack item, String text, int posX, int posY, int index) {
        this.item = item;
        this.text = text;
        this.posX = posX;
        this.posY = posY;
        fade = 0;
        this.index = index * 5;
        scrollPosY = posY;
        scroll_animation = 0f;
    }


    public void render(DrawContext context, int MouseX, int MouseY) {
        if (scrollPosY != posY) {
            scroll_animation = AnimationUtility.fast(scroll_animation, 1, 15f);
            posY = (int) Render2DEngine.interpolate(prevPosY, scrollPosY, scroll_animation);
        }

        if ((posY > AutoBuyGui.getInstance().main_posY + AutoBuyGui.getInstance().height) || posY < AutoBuyGui.getInstance().main_posY) {
            return;
        }

        if (text.contains("успешно")) {
            Render2DEngine.drawGradientRound(context.getMatrices(), posX + 5, posY, 285, 30, 4f,
                    new Color(55, 44, 66, 255), new Color(55, 44, 66, 255),
                    new Color(0, 80, 68, 255), new Color(0, 103, 88, 255));
        } else {
            Render2DEngine.drawRound(context.getMatrices(), posX + 5, posY, 285, 30, 4f, Render2DEngine.applyOpacity(new Color(44, 35, 52, 255), getFadeFactor()));
        }

        if (first_open) {
            Render2DEngine.addWindow(context.getMatrices(), posX + 5, posY, posX + 5 + 285, posY + 30, 1f);
            Render2DEngine.drawBlurredShadow(context.getMatrices(), MouseX - 20, MouseY - 20, 40, 40, 60, Render2DEngine.applyOpacity(new Color(0xC3555A7E, true), getFadeFactor()));
            Render2DEngine.popWindow();
            first_open = false;
        }

        if (isHovered(MouseX, MouseY)) {
            Render2DEngine.addWindow(context.getMatrices(), posX + 5, posY, posX + 5 + 285, posY + 30, 1f);
            Render2DEngine.drawBlurredShadow(context.getMatrices(), MouseX - 20, MouseY - 20, 40, 40, 60, Render2DEngine.applyOpacity(new Color(0xC3555A7E, true), getFadeFactor()));
            Render2DEngine.popWindow();
        }

        if (item != null) {
            context.drawItem(item, posX + 12, posY + 7);
            context.drawItemInSlot(mc.textRenderer, item, posX + 12, posY + 7);
        }

        FontRenderers.modules.drawString(context.getMatrices(), item.getName().getString(), posX + 37, posY + 6, Render2DEngine.applyOpacity(-1, getFadeFactor()), false);
        FontRenderers.settings.drawString(context.getMatrices(), text, posX + 37, posY + 17, Render2DEngine.applyOpacity(new Color(0xFFBDBDBD, true).getRGB(), getFadeFactor()), false);
    }

    private float getFadeFactor() {
        return 1f;
    }

    public void onTick() {
        if (progress > 4) progress = 0;
        progress++;

        if (fade < 10 + index) fade++;
    }

    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX > posX && mouseX < posX + 295 && mouseY > posY && mouseY < posY + 30;
    }

    public void movePosition(float deltaX, float deltaY) {
        this.posY += deltaY;
        this.posX += deltaX;
        scrollPosY = posY;
    }

    public double getPosX() {
        return this.posX;
    }

    public double getPosY() {
        return this.posY;
    }

    public void scrollElement(float deltaY) {
        scroll_animation = 0;
        prevPosY = posY;
        this.scrollPosY += deltaY;
    }
}
