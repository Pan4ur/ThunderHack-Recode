package thunder.hack.gui.thundergui.components;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ThunderHackGui;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;
import java.util.Objects;

import static thunder.hack.features.modules.Module.mc;

public class ModulePlate {
    float scroll_animation = 0f;
    private final Module module;
    private int posX;
    private int posY;
    private float scrollPosY;
    private float prevPosY;
    private int progress;
    private int fade;
    private final int index;
    private boolean first_open = true;
    private boolean listening_bind = false;
    private boolean holdbind = false;


    public ModulePlate(Module module, int posX, int posY, int index) {
        this.module = module;
        this.posX = posX;
        this.posY = posY;
        fade = 0;
        this.index = index * 5;
        scrollPosY = posY;
        scroll_animation = 0;
    }

    public void render(MatrixStack stack, int MouseX, int MouseY) {
        if (scrollPosY != posY) {
            scroll_animation = AnimationUtility.fast(scroll_animation, 1, 15f);
            posY = (int) Render2DEngine.interpolate(prevPosY, scrollPosY, scroll_animation);
        }

        if ((posY > ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height) || posY < ThunderGui.getInstance().main_posY) {
            return;
        }

        Render2DEngine.addWindow(stack, new Render2DEngine.Rectangle(posX + 1, posY + 1, posX + 90, posY + 30));

        if (module.isOn()) {
            Render2DEngine.drawGradientRound(stack, posX + 1, posY, 89, 30, 4f,
                    Render2DEngine.applyOpacity(ThunderHackGui.onColor1.getValue().getColorObject(), getFadeFactor()),
                    Render2DEngine.applyOpacity(ThunderHackGui.onColor1.getValue().getColorObject(), getFadeFactor()),
                    Render2DEngine.applyOpacity(ThunderHackGui.onColor2.getValue().getColorObject(), getFadeFactor()),
                    Render2DEngine.applyOpacity(ThunderHackGui.onColor2.getValue().getColorObject(), getFadeFactor()));
        } else {
            Render2DEngine.drawRound(stack, posX + 1, posY, 89, 30, 4f, Render2DEngine.applyOpacity(new Color(25, 20, 30, 255), getFadeFactor()));
        }

        if (first_open) {
            Render2DEngine.drawBlurredShadow(stack, MouseX - 20, MouseY - 20, 40, 40, 60, Render2DEngine.applyOpacity(new Color(0xC3555A7E, true), getFadeFactor()));
            first_open = false;
        }

        if (isHovered(MouseX, MouseY)) {
            Render2DEngine.drawBlurredShadow(stack, MouseX - 20, MouseY - 20, 40, 40, 60, Render2DEngine.applyOpacity(new Color(0xC3555A7E, true), getFadeFactor()));
        }

        if (ThunderGui.selected_plate != this)
            FontRenderers.icons.drawString(stack, "H", (int) (posX + 80f), (int) (posY + 22f), Render2DEngine.applyOpacity(new Color(0xFFECECEC, true).getRGB(), getFadeFactor()));
        else {

            stack.push();
            stack.translate((posX + 91f), (posY + 15f), 0.0F);
            stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(mc.player.age * 4));
            stack.translate(-(posX + 91f), -(posY + 15f), 0.0F);
            FontRenderers.big_icons.drawString(stack, "H", (posX + 78f), (posY + 5f), Render2DEngine.applyOpacity(new Color(0xFF646464, true).getRGB(), getFadeFactor()));
            stack.translate((posX + 91f), (posY + 15f), 0.0F);
            stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-mc.player.age * 4));
            stack.translate(-(posX + 91f), -(posY + 15f), 0.0F);
            stack.pop();
        }

        if (!listening_bind) {
            FontRenderers.sf_medium.drawString(stack, module.getName(), posX + 5, posY + 5, Render2DEngine.applyOpacity(-1, getFadeFactor()));
        }

        if (listening_bind) {
            FontRenderers.modules.drawString(stack, "PressKey", posX + 85 - FontRenderers.modules.getStringWidth("PressKey"), posY + 5, Render2DEngine.applyOpacity(new Color(0xB0B0B0), getFadeFactor()).getRGB());
        } else if (!Objects.equals(module.getBind().getBind(), "None")) {
            String sbind = module.getBind().getBind();
            if (sbind.equals("LEFT_CONTROL")) {
                sbind = "LCtrl";
            }
            if (sbind.equals("RIGHT_CONTROL")) {
                sbind = "RCtrl";
            }
            if (sbind.equals("LEFT_SHIFT")) {
                sbind = "LShift";
            }
            if (sbind.equals("RIGHT_SHIFT")) {
                sbind = "RShift";
            }
            if (sbind.equals("LEFT_ALT")) {
                sbind = "LAlt";
            }
            if (sbind.equals("RIGHT_ALT")) {
                sbind = "RAlt";
            }

            FontRenderers.modules.drawString(stack, sbind, posX + 86 - FontRenderers.modules.getStringWidth(sbind), posY + 6, Render2DEngine.applyOpacity(new Color(0xB0B0B0), getFadeFactor()).getRGB());
        }

        if (!listening_bind && module.getDescription() != null) {
            int step = 0;
            StringBuilder firstString = new StringBuilder();
            for (String word : I18n.translate(module.getDescription()).split(" ")) {
                firstString.append(word + " ");
                String[] splitString2 = firstString.toString().split("\n");
                if (FontRenderers.sf_medium_mini.getStringWidth(splitString2[step]) > 70) {
                    firstString.append("\n");
                    step++;
                }
            }
            FontRenderers.sf_medium_mini.drawString(stack, firstString.toString(), posX + 5, posY + 14, Render2DEngine.applyOpacity(new Color(0xFFBDBDBD, true).getRGB(), getFadeFactor()));
        }

        if (listening_bind) {
            Render2DEngine.drawRound(stack, posX + 5, posY + 5, 40, 20, 3, Color.BLACK);

            if (!holdbind) {
                Render2DEngine.drawRound(stack, posX + 6, posY + 6, 38, 8, 2, Render2DEngine.injectAlpha(ThunderHackGui.onColor1.getValue().getColorObject(), 170));
                FontRenderers.settings.drawCenteredString(stack, "Toggle", posX + 25, posY + 7, -1);
                FontRenderers.settings.drawCenteredString(stack, "Hold", posX + 25, posY + 17, new Color(0xA8FFFFFF, true).getRGB());
            } else {
                Render2DEngine.drawRound(stack, posX + 6, posY + 16, 38, 8, 2, Render2DEngine.injectAlpha(ThunderHackGui.onColor1.getValue().getColorObject(), 170));
                FontRenderers.settings.drawCenteredString(stack, "Hold", posX + 25, posY + 17, -1);
                FontRenderers.settings.drawCenteredString(stack, "Toggle", posX + 25, posY + 7, new Color(0xA8FFFFFF, true).getRGB());
            }
        }

        Render2DEngine.popWindow();
    }

    private float getFadeFactor() {
        return fade / (5f + index);
    }


    public void onTick() {
        if (progress > 4) {
            progress = 0;
        }
        progress++;

        if (fade < 10 + index) {
            fade++;
        }
    }

    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX > posX && mouseX < posX + 90 && mouseY > posY && mouseY < posY + 30;
    }

    public void movePosition(float deltaX, float deltaY) {
        this.posY += deltaY;
        this.posX += deltaX;
        scrollPosY = posY;
    }

    public void scrollElement(float deltaY) {
        scroll_animation = 0;
        prevPosY = posY;
        this.scrollPosY += deltaY;
    }

    public void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        if ((posY > ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height) || posY < ThunderGui.getInstance().main_posY) {
            return;
        }
        if (listening_bind) {
            if (mouseX > posX + 6 && mouseX < posX + 44 && mouseY > posY + 6 && mouseY < posY + 14) {
                holdbind = false;
                module.getBind().setHold(false);
                return;
            }
            if (mouseX > posX + 6 && mouseX < posX + 44 && mouseY > posY + 16 && mouseY < posY + 24) {
                holdbind = true;
                module.getBind().setHold(true);
                return;
            }
            module.setBind(clickedButton, true, holdbind);
            Command.sendMessage(module.getName() + " бинд изменен на " + module.getBind().getBind());
            listening_bind = false;
        }

        if (mouseX > posX && mouseX < posX + 90 && mouseY > posY && mouseY < posY + 30) {
            switch (clickedButton) {
                case 0:
                    module.toggle();
                    break;
                case 1:
                    ThunderGui.selected_plate = this;
                    break;
                case 2:
                    listening_bind = !listening_bind;
                    break;
            }
        }
    }

    public void keyTyped(String typedChar, int keyCode) {
        if (listening_bind) {
            Bind bind = new Bind(keyCode, false, holdbind);
            if (bind.getBind().equalsIgnoreCase("Escape")) {
                return;
            }
            if (bind.getBind().equalsIgnoreCase("Delete")) {
                bind = new Bind(-1, false, holdbind);
            }
            module.setBind(bind);
            listening_bind = false;
        }
    }

    public double getPosX() {
        return this.posX;
    }

    public double getPosY() {
        return this.posY;
    }

    public Module getModule() {
        return this.module;
    }
}
