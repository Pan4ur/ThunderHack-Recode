package thunder.hack.gui.thundergui.components;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.client.ThunderHackGui;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;

public class ConfigComponent {
    float scroll_animation = 0f;
    private final String name;
    private final String date;
    private int posX;
    private int posY;
    private int progress;
    private int fade;
    private final int index;
    private boolean first_open = true;
    private float scrollPosY;
    private float prevPosY;

    public ConfigComponent(String name, String date, int posX, int posY, int index) {
        this.name = name;
        this.date = date;
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

        if ((posY > ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height) || posY < ThunderGui.getInstance().main_posY) {
            return;
        }

        if (Managers.CONFIG.currentConfig.getName().equals(name + ".th")) {
            Render2DEngine.drawGradientRound(context.getMatrices(), posX + 5, posY, 285, 30, 4f,
                    Render2DEngine.applyOpacity(new Color(55, 44, 66, 255), getFadeFactor()),
                    Render2DEngine.applyOpacity(new Color(25, 20, 30, 255), getFadeFactor()),
                    Render2DEngine.applyOpacity(ThunderHackGui.onColor1.getValue().getColorObject(), getFadeFactor()),
                    Render2DEngine.applyOpacity(ThunderHackGui.onColor2.getValue().getColorObject(), getFadeFactor()));
        } else
            Render2DEngine.drawRound(context.getMatrices(), posX + 5, posY, 285, 30, 4f, Render2DEngine.applyOpacity(new Color(44, 35, 52, 255), getFadeFactor()));

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

        Render2DEngine.drawRound(context.getMatrices(), posX + 250, posY + 8, 30, 14, 2f, Render2DEngine.applyOpacity(new Color(25, 20, 30, 255), getFadeFactor()));

        if (Render2DEngine.isHovered(MouseX, MouseY, posX + 252, posY + 10, 10, 10)) {
            Render2DEngine.drawRound(context.getMatrices(), posX + 252, posY + 10, 10, 10, 2f, Render2DEngine.applyOpacity(new Color(21, 58, 0, 255), getFadeFactor()));
        } else
            Render2DEngine.drawRound(context.getMatrices(), posX + 252, posY + 10, 10, 10, 2f, Render2DEngine.applyOpacity(new Color(32, 89, 0, 255), getFadeFactor()));

        if (Render2DEngine.isHovered(MouseX, MouseY, posX + 268, posY + 10, 10, 10)) {
            Render2DEngine.drawRound(context.getMatrices(), posX + 268, posY + 10, 10, 10, 2f, Render2DEngine.applyOpacity(new Color(65, 1, 13, 255), getFadeFactor()));
        } else
            Render2DEngine.drawRound(context.getMatrices(), posX + 268, posY + 10, 10, 10, 2f, Render2DEngine.applyOpacity(new Color(94, 1, 18, 255), getFadeFactor()));

        FontRenderers.icons.drawString(context.getMatrices(), "x", posX + 252, posY + 13, Render2DEngine.applyOpacity(-1, getFadeFactor()));
        FontRenderers.icons.drawString(context.getMatrices(), "w", posX + 268, posY + 13, Render2DEngine.applyOpacity(-1, getFadeFactor()));


        FontRenderers.mid_icons.drawString(context.getMatrices(), "u", posX + 7, posY + 5, Render2DEngine.applyOpacity(-1, getFadeFactor()));
        FontRenderers.modules.drawString(context.getMatrices(), name, posX + 37, posY + 6, Render2DEngine.applyOpacity(-1, getFadeFactor()));
        FontRenderers.settings.drawString(context.getMatrices(), "updated on: " + date, posX + 37, posY + 17, Render2DEngine.applyOpacity(new Color(0xFFBDBDBD, true).getRGB(), getFadeFactor()));
    }

    private float getFadeFactor() {
        return fade / (5f + index);
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

    public void mouseClicked(int MouseX, int MouseY, int clickedButton) {
        if ((posY > ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height) || posY < ThunderGui.getInstance().main_posY) {
            return;
        }
        if (Render2DEngine.isHovered(MouseX, MouseY, posX + 252, posY + 10, 10, 10))
            Managers.CONFIG.load(name);

        if (Render2DEngine.isHovered(MouseX, MouseY, posX + 268, posY + 10, 10, 10)) {
            Managers.CONFIG.delete(name);
            ThunderGui.getInstance().loadConfigs();
        }
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