package thunder.hack.gui.clickui.small;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import thunder.hack.cmd.Command;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.clickui.AbstractButton;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.clickui.impl.*;
import thunder.hack.gui.clickui.normal.ClickUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.impl.TargetHud;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.*;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static thunder.hack.modules.Module.mc;
import static thunder.hack.modules.client.MainSettings.isRu;
import static thunder.hack.utility.render.animation.AnimationUtility.fast;

public class ModuleButton extends AbstractButton {
    private final List<AbstractElement> elements;
    public final Module module;
    private boolean open;
    private boolean hovered, prevHovered;

    private boolean binding = false;
    private boolean holdbind = false;

    public ModuleButton(Module module) {
        this.module = module;
        elements = new ArrayList<>();

        for (Setting setting : module.getSettings()) {
            if (setting.getValue() instanceof Boolean && !setting.getName().equals("Enabled") && !setting.getName().equals("Drawn")) {
                elements.add(new BooleanElement(setting, true));
            } else if (setting.getValue() instanceof ColorSetting) {
                elements.add(new ColorPickerElement(setting, true));
            } else if (setting.getValue() instanceof BooleanParent) {
                elements.add(new BooleanParentElement(setting, true));
            } else if (setting.isNumberSetting() && setting.hasRestriction()) {
                elements.add(new SliderElement(setting, true));
            } else if (setting.isEnumSetting() && !(setting.getValue() instanceof Parent) && !(setting.getValue() instanceof PositionSetting)) {
                elements.add(new ModeElement(setting, true));
            } else if (setting.getValue() instanceof Bind && !setting.getName().equals("Keybind")) {
                elements.add(new BindElement(setting, true));
            } else if ((setting.getValue() instanceof String || setting.getValue() instanceof Character) && !setting.getName().equalsIgnoreCase("displayName")) {
                elements.add(new StringElement(setting, true));
            } else if (setting.getValue() instanceof Parent) {
                elements.add(new ParentElement(setting, true));
            }
        }
    }

    public void init() {
        elements.forEach(AbstractElement::init);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, Color color) {
        hovered = Render2DEngine.isHovered(mouseX, mouseY, x, y, width, height);

        if (hovered) {
            if (!prevHovered)
                ModuleManager.soundFX.playScroll();
            ClickUI.currentDescription = I18n.translate(module.getDescription());
        }

        prevHovered = hovered;

        double ix = x + 5;
        double iy = y + height / 2 - (6 / 2f);

        if (isHiden()) return;

        offset_animation = fast(1f, 0f, 15f);
        if (target_offset != offsetY) {
            offsetY = interp(offsetY, target_offset, offset_animation);
        } else offset_animation = 1f;

        if (isOpen()) {
            Color sbg = ClickGui.getInstance().disabled.getValue().getColorObject();

            Render2DEngine.drawRoundDoubleColor(context.getMatrices(), x + 4, y + height - 12, (width - 8), (height) + getElementsHeight(), 2f, module.isEnabled() ? Render2DEngine.applyOpacity(ClickGui.getInstance().getColor(200), 0.8f) : sbg, module.isEnabled() ? Render2DEngine.applyOpacity(ClickGui.getInstance().getColor(0), 0.8f) : sbg);

            Render2DEngine.addWindow(context.getMatrices(), new Render2DEngine.Rectangle((float) x, (float) (y + height - 15), (float) ((width) + x + 6), (float) ((height) + y + getElementsHeight())));

            context.getMatrices().push();
            TargetHud.sizeAnimation(context.getMatrices(), x + width / 2 + 6, y + height / 2 - 12, 1f - category_animation);
            double offsetY = 0;
            for (AbstractElement element : elements) {
                if (!element.isVisible())
                    continue;

                element.setOffsetY(offsetY);
                element.setX(x);
                element.setY(y + height + 2);
                element.setWidth(width);
                element.setHeight(13);

                if (element instanceof ColorPickerElement)
                    element.setHeight(66);

                else if (element instanceof SliderElement)
                    element.setHeight(18);

                if (element instanceof ModeElement combobox) {
                    combobox.setWHeight(13);
                    if (combobox.isOpen()) {
                        element.setHeight(13 + (combobox.getSetting().getModes().length * 12));
                    } else element.setHeight(13);
                }

                element.render(context, mouseX, mouseY, delta);

                offsetY += element.getHeight();
            }
            context.getMatrices().pop();

            Render2DEngine.drawBlurredShadow(context.getMatrices(), (float) x + 3, (float) (y + height), (float) width - 6, 3, 13, new Color(0, 0, 0, 255));
            Render2DEngine.popWindow();
        } else category_animation = fast(1, 0, 1f);


        if (module.isEnabled()) {
            if (hovered) {
                Render2DEngine.drawBlurredShadow(context.getMatrices(), (float) x + 4, (float) y, (float) width - 8, (float) height + 2, 32, new Color(0, 0, 0, 200));
                Render2DEngine.drawRoundDoubleColor(context.getMatrices(), x + 4, y, width - 8, height - 2, 2f, ClickGui.getInstance().getColor(200), ClickGui.getInstance().getColor(0));
            } else {
                Render2DEngine.drawRoundDoubleColor(context.getMatrices(), x + 4, y + 1f, width - 8, height - 2, 2f, ClickGui.getInstance().getColor(200), ClickGui.getInstance().getColor(0));
            }
        } else {
            if (hovered)
                Render2DEngine.drawRound(context.getMatrices(), (float) (x + 4f), (float) (y + 1f), (float) (width - 8), (float) (height - 2), 2f, ClickGui.getInstance().plateColor.getValue().getColorObject().darker());
        }


        if (!ClickGui.getInstance().showBinds.getValue()) {
            if (module.getSettings().size() > 3)
                FontRenderers.sf_medium_modules.drawString(context.getMatrices(), isOpen() ? "-" : "+", x + width - 12, y + 7, -1);
        } else {
            if (!module.getBind().getBind().equalsIgnoreCase("none")) {
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
                if (!binding)
                    FontRenderers.sf_medium_modules.drawString(context.getMatrices(), sbind, x + width - 11 - FontRenderers.sf_medium_modules.getStringWidth(sbind), y + 6 + (hovered ? -1 : 0), new Color(-1).getRGB());
            }
            if (binding)
                FontRenderers.sf_medium_modules.drawString(context.getMatrices(), holdbind ? (Formatting.GRAY + "Toggle / " + Formatting.RESET + "Hold") : (Formatting.RESET + "Toggle " + Formatting.GRAY + "/ Hold"), x + width - 11 - FontRenderers.sf_medium_modules.getStringWidth("Toggle/Hold"), iy + 2 + (hovered ? -1 : 0), new Color(-1).getRGB());
        }

        if (hovered && InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_SHIFT)) {
            FontRenderers.sf_medium_modules.drawString(context.getMatrices(), "Drawn " + (module.isDrawn() ? Formatting.GREEN + "TRUE" : Formatting.RED + "FALSE"), ix + 1f, iy + 2 + (hovered ? -1 : 0), new Color(0xFFEAEAEA).getRGB());
        } else {
            if (this.binding)
                FontRenderers.sf_medium_modules.drawString(context.getMatrices(), "PressKey", ix, iy + 2 + (hovered ? -1 : 0), new Color(0xFFEAEAEA).getRGB());
            else {
                if (ClickGui.getInstance().textSide.getValue() == ClickGui.TextSide.Left)
                    FontRenderers.sf_medium_modules.drawString(context.getMatrices(), module.getName(), ix + 2, iy + 2 + (hovered ? -1 : 0), new Color(0xFFEAEAEA).getRGB());
                else
                    FontRenderers.sf_medium_modules.drawCenteredString(context.getMatrices(), module.getName(), ix + 38, iy - 2 + (hovered ? -1 : 0), new Color(0xFFEAEAEA).getRGB());
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHiden()) return;

        if (this.binding) {
            Command.sendMessage((mouseX - x) + "");
            if (mouseX > x + 37 && mouseX < x + 58 && mouseY > y && mouseY < y + height) {
                holdbind = false;
                module.getBind().setHold(false);
                return;
            }
            if (mouseX > x + 62 && mouseX < x + 77 && mouseY > y && mouseY < y + height) {
                holdbind = true;
                module.getBind().setHold(true);
                return;
            }

            module.setBind(button, true, holdbind);
            Command.sendMessage(module.getName() + " бинд изменен на " + module.getBind().getBind());
            binding = false;
        }
        if (hovered) {
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_SHIFT) && button == 0) {
                module.setDrawn(!module.isDrawn());
                if (MainSettings.isRu()) {
                    Command.sendMessage("Модуль " + Formatting.GREEN + module.getName() + Formatting.WHITE + " теперь " + (module.isDrawn() ? "виден в ArrayList" : "не виден в ArrayList"));
                } else {
                    Command.sendMessage(Formatting.GREEN + module.getName() + Formatting.WHITE + " is now " + (module.isDrawn() ? "visible in ArrayList" : "invisible in ArrayList"));
                }
                return;
            }

            if (button == 0) {
                module.toggle();
            } else if (button == 1 && (module.getSettings().size() > 3)) {
                setOpen(!isOpen());
            } else if (button == 2) {
                this.binding = !this.binding;
            }
        }

        if (open)
            elements.forEach(element -> {
                if (element.isVisible())
                    element.mouseClicked(mouseX, mouseY, button);
            });
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (isOpen())
            elements.forEach(element -> element.mouseReleased(mouseX, mouseY, button));
    }

    public void keyTyped(int keyCode) {
        if (isHiden()) return;

        if (isOpen()) {
            for (AbstractElement element : elements)
                element.keyTyped(keyCode);
        }

        if (this.binding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                module.setBind(-1, false, holdbind);
                Command.sendMessage((isRu() ? "Удален бинд с модуля " : "Removed bind from ") + module.getName());
            } else {
                module.setBind(keyCode, false, holdbind);
                Command.sendMessage(module.getName() + (isRu() ? " бинд изменен на " : " bind changed to ") + module.getBind().getBind());
            }
            binding = false;
        }
    }

    public void onGuiClosed() {
        elements.forEach(AbstractElement::onClose);
    }

    public List<AbstractElement> getElements() {
        return elements;
    }

    public double getElementsHeight() {
        float offsetY = 0;
        float offsetY1 = 0;
        if (isOpen()) {
            for (AbstractElement element : getElements()) {
                if (element.isVisible())
                    offsetY += element.getHeight();
            }
            category_animation = fast(category_animation, 0, 8f);
            offsetY1 = (float) interp(offsetY1, offsetY, category_animation);
        }
        return offsetY1;
    }

    float category_animation = 0f;
    float offset_animation = 0f;

    public double interp(double d, double d2, float d3) {
        return d2 + (d - d2) * d3;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}