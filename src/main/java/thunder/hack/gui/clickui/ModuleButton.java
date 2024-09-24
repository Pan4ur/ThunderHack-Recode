package thunder.hack.gui.clickui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.hud.impl.TargetHud;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClickGui;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.clickui.impl.*;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.misc.DialogScreen;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.*;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;
import thunder.hack.utility.render.animation.AnimationUtility;
import thunder.hack.utility.render.animation.GearAnimation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static thunder.hack.features.modules.Module.mc;
import static thunder.hack.features.modules.client.ClientSettings.isRu;
import static thunder.hack.utility.render.animation.AnimationUtility.fast;

public class ModuleButton extends AbstractButton {
    private final List<AbstractElement> elements;
    public final Module module;
    private boolean open;
    private boolean hovered, prevHovered;
    private float animation, animation2;
    float category_animation = 0f;
    int ticksOpened;

    private final GearAnimation gearAnimation = new GearAnimation();

    private boolean binding = false;
    private boolean holdbind = false;

    public ModuleButton(Module module) {
        this.module = module;
        elements = new ArrayList<>();

        for (Setting setting : module.getSettings()) {

            if (setting.getValue() instanceof Boolean && !setting.getName().equals("Enabled") && !setting.getName().equals("Drawn")) {
                elements.add(new BooleanElement(setting));
            } else if (setting.getValue() instanceof ColorSetting) {
                elements.add(new ColorPickerElement(setting));
            } else if (setting.getValue() instanceof BooleanSettingGroup) {
                elements.add(new BooleanParentElement(setting));
            } else if (setting.isNumberSetting() && setting.hasRestriction()) {
                elements.add(new SliderElement(setting));
            } else if (setting.getValue() instanceof ItemSelectSetting) {
                elements.add(new ItemSelectElement(setting));
            } else if (setting.getValue() instanceof SettingGroup) {
                elements.add(new ParentElement(setting));
            } else if (setting.isEnumSetting() && !(setting.getValue() instanceof PositionSetting)) {
                elements.add(new ModeElement(setting));
            } else if (setting.getValue() instanceof Bind && !setting.getName().equals("Keybind")) {
                elements.add(new BindElement(setting));
            } else if ((setting.getValue() instanceof String || setting.getValue() instanceof Character) && !setting.getName().equalsIgnoreCase("displayName")) {
                elements.add(new StringElement(setting));
            }
        }
    }

    public void init() {
        elements.forEach(AbstractElement::init);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = Render2DEngine.isHovered(mouseX, mouseY, x, y, width, height);
        animation = fast(animation, module.isEnabled() ? 1 : 0, 8f);
        animation2 = fast(animation2, 1f, 10f);

        if (hovered) {
            if (!prevHovered)
                Managers.SOUND.playScroll();
            ClickGUI.currentDescription = I18n.translate(module.getDescription());
        }

        prevHovered = hovered;

        float ix = x + 5;
        float iy = y + height / 2f - 3f;

        offsetY = AnimationUtility.fast(offsetY, target_offset, 20f);

        float offsetY = 0;

        if (isOpen()) {
            Render2DEngine.drawGuiBase(context.getMatrices(), x + 4, y + 2f, width - 8, height + (float) getElementsHeight(), 1f, 0);
            Render2DEngine.addWindow(context.getMatrices(), new Render2DEngine.Rectangle(x + 1, y + height - 2, width + x - 2, (float) (height + y + 1f + getElementsHeight())));

            if (mc.player != null && ModuleManager.clickGui.gear.getValue().isEnabled()) {
                Render2DEngine.addWindow(context.getMatrices(), new Render2DEngine.Rectangle(x, y + height + 1, (width) + x + 6, (float) ((height) + y + 1f + getElementsHeight())));
                float px = x + 4 + (width - 8) / 2f;
                float py = y + 12f + (height + (float) getElementsHeight()) / 2f;
                int gScale = ModuleManager.clickGui.gearScale.getValue();
                context.getMatrices().push();
                context.getMatrices().translate(px, py, 0.0F);
                context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(gearAnimation.getValue()));
                context.getMatrices().translate(-px, -py, 0.0F);
                RenderSystem.setShaderTexture(0, TextureStorage.Gear);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
                Render2DEngine.renderGradientTexture(context.getMatrices(), px - gScale / 2f, py - gScale / 2f, gScale, gScale, 0, 0, gScale, gScale, gScale, gScale,
                        Render2DEngine.injectAlpha(HudEditor.getColor(270).darker(), 110),
                        Render2DEngine.injectAlpha(HudEditor.getColor(0).darker(), 110),
                        Render2DEngine.injectAlpha(HudEditor.getColor(180).darker(), 110),
                        Render2DEngine.injectAlpha(HudEditor.getColor(90).darker(), 110));
                RenderSystem.disableBlend();
                context.getMatrices().translate(px, py, 0.0F);
                context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) Render2DEngine.interpolate(mc.player.age - 1, mc.player.age, Render3DEngine.getTickDelta()) * -4f));
                context.getMatrices().translate(-px, -py, 0.0F);
                context.getMatrices().pop();
                Render2DEngine.popWindow();
            }

            if (Render2DEngine.isHovered(mouseX, mouseY, x + 4, y + height - 12f, width - 8, height + (float) getElementsHeight())) {
                Render2DEngine.drawBlurredShadow(context.getMatrices(), mouseX - 10, mouseY - 10, 20, 20, 40, HudEditor.getColor(270));
            }

            for (AbstractElement element : elements) {
                if (!element.isVisible())
                    continue;

                element.setOffsetY(offsetY);
                element.setX(x);
                element.setY(y + height + 2);
                element.setWidth(width);
                element.setHeight(13);

                if (element instanceof ColorPickerElement picker)
                    element.setHeight(picker.getHeight());

                else if (element instanceof SliderElement)
                    element.setHeight(18);

                if (element instanceof ModeElement combobox) {
                    combobox.setWHeight(13);
                    if (combobox.isOpen()) {
                        element.setHeight(13 + (combobox.getSetting().getModes().length * 12));
                    } else element.setHeight(13);
                }
                offsetY += element.getHeight();
            }

            context.getMatrices().push();
            TargetHud.sizeAnimation(context.getMatrices(), x + width / 2f + 6, y + height / 2f - 12, ticksOpened < 5 ? Math.clamp(category_animation / offsetY, 0f, 1f) : 1f);
            elements.forEach(e -> {
                if (e.isVisible())
                    e.render(context, mouseX, mouseY, delta);
            });
            context.getMatrices().pop();

            Render2DEngine.drawBlurredShadow(context.getMatrices(), x + 3, y + height, width - 6, 3, 13, HudEditor.getColor(1));
            if (!module.isEnabled())
                Render2DEngine.draw2DGradientRect(context.getMatrices(), x + 4, y + height - 1f, x + 3f + width - 7f, 3f + y + height, Render2DEngine.applyOpacity(HudEditor.getColor(0), 0), HudEditor.getColor(0), Render2DEngine.applyOpacity(HudEditor.getColor(90), 0), HudEditor.getColor(90));
            Render2DEngine.popWindow();
        } else {
            if (hovered) {
                Render2DEngine.addWindow(context.getMatrices(), x + 1, y, x + width - 2, y + height, 1.);
                Render2DEngine.drawBlurredShadow(context.getMatrices(), mouseX - 10, mouseY - 10, 20, 20, 35, HudEditor.getColor(270));
                Render2DEngine.popWindow();
            }
        }

        category_animation = fast(category_animation, offsetY, 20);

        if (animation < 0.05)
            Render2DEngine.drawRect(context.getMatrices(), x + 4f, y + 1f, width - 8, height - 2, Render2DEngine.applyOpacity(HudEditor.plateColor.getValue().getColorObject().darker(), 0.15f));
        else {

            switch (ModuleManager.clickGui.gradientMode.getValue()) {
                case both -> {
                    Render2DEngine.draw2DGradientRect(context.getMatrices(), x + 4, y + 1f, x + 4 + width - 8, y + 1f + height - 2,
                            Render2DEngine.applyOpacity(HudEditor.getColor(270), animation * 2f),
                            Render2DEngine.applyOpacity(HudEditor.getColor(0), animation * 2f),
                            Render2DEngine.applyOpacity(HudEditor.getColor(180), animation),
                            Render2DEngine.applyOpacity(HudEditor.getColor(90), animation));
                }
                case UpsideDown -> {
                    Render2DEngine.draw2DGradientRect(context.getMatrices(), x + 4, y + 1f, x + 4 + width - 8, y + 1f + height - 2,
                            Render2DEngine.applyOpacity(HudEditor.getColor(270), animation * 2f),
                            Render2DEngine.applyOpacity(HudEditor.getColor(0), animation * 2f),
                            Render2DEngine.applyOpacity(HudEditor.getColor(270), animation),
                            Render2DEngine.applyOpacity(HudEditor.getColor(0), animation));
                }
                case LeftToRight -> {
                    Render2DEngine.draw2DGradientRect(context.getMatrices(), x + 4, y + 1f, x + 4 + width - 8, y + 1f + height - 2,
                            Render2DEngine.applyOpacity(HudEditor.getColor(270), animation * 2f),
                            Render2DEngine.applyOpacity(HudEditor.getColor(270), animation * 2f),
                            Render2DEngine.applyOpacity(HudEditor.getColor(0), animation),
                            Render2DEngine.applyOpacity(HudEditor.getColor(0), animation));
                }
            }
        }

        if (!module.getBind().getBind().equalsIgnoreCase("none") && !binding)
            FontRenderers.sf_medium_modules.drawString(context.getMatrices(), getSbind(), x + width - 11 - FontRenderers.sf_medium_modules.getStringWidth(getSbind()), y + 6, module.isEnabled() ? HudEditor.textColor2.getValue().getColor() : HudEditor.textColor.getValue().getColor());

        if (binding)
            FontRenderers.sf_medium_modules.drawString(context.getMatrices(), holdbind ? (Formatting.GRAY + "Toggle / " + Formatting.RESET + "Hold") : (Formatting.RESET + "Toggle " + Formatting.GRAY + "/ Hold"), x + width - 11 - FontRenderers.sf_medium_modules.getStringWidth("Toggle/Hold"), iy + 2, Render2DEngine.applyOpacity(Color.WHITE.getRGB(), animation2));

        if (hovered && InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_SHIFT)) {
            FontRenderers.sf_medium_modules.drawString(context.getMatrices(), "Drawn " + (module.isDrawn() ? Formatting.GREEN + "TRUE" : Formatting.RED + "FALSE"), ix + 1f, iy + 2, module.isEnabled() ? HudEditor.textColor2.getValue().getColor() : HudEditor.textColor.getValue().getColor());
        } else {
            if (binding)
                FontRenderers.sf_medium_modules.drawString(context.getMatrices(), "PressKey", ix, iy + 2, module.isEnabled() ? Render2DEngine.applyOpacity(HudEditor.textColor2.getValue().getColor(), animation2) : Render2DEngine.applyOpacity(HudEditor.textColor.getValue().getColor(), animation2));
            else {
                if (ModuleManager.clickGui.textSide.getValue() == ClickGui.TextSide.Left)
                    FontRenderers.sf_medium_modules.drawString(context.getMatrices(), module.getName(), ix + 2, iy + 2, module.isEnabled() ? HudEditor.textColor2.getValue().getColor() : HudEditor.textColor.getValue().getColor());
                else
                    FontRenderers.sf_medium_modules.drawCenteredString(context.getMatrices(), module.getName(), ix + getWidth() / 2 - 4, iy + 2, module.isEnabled() ? HudEditor.textColor2.getValue().getColor() : HudEditor.textColor.getValue().getColor());
            }
        }
    }

    @NotNull
    private String getSbind() {
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
        return sbind;
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (binding) {
            if (mouseX > x + 56 && mouseX < x + 67 && mouseY > y && mouseY < y + height) {
                holdbind = false;
                module.getBind().setHold(false);
                return;
            }
            if (mouseX > x + 78 && mouseX < x + 88 && mouseY > y && mouseY < y + height) {
                holdbind = true;
                module.getBind().setHold(true);
                return;
            }
            module.setBind(button, true, holdbind);
            binding = false;
        }

        if (hovered) {
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_SHIFT) && button == 0) {
                module.setDrawn(!module.isDrawn());
                return;
            }

            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.GLFW_KEY_DELETE) && button == 0) {
                DialogScreen dialogScreen = new DialogScreen(
                        TextureStorage.questionPic,
                        isRu() ? "Сброс модуля" : "Reset module",
                        isRu() ? "Ты действительно хочешь сбросить " + module.getName() + "?" : "Are you sure you want to reset " + module.getName() + "?",
                        isRu() ? "Да" : "Yes",
                        isRu() ? "Нет" : "No",
                        () -> {
                            if (module.isEnabled())
                                module.disable("reseting");
                            for (Setting s : module.getSettings()) {
                                if (s.getValue() instanceof ColorSetting cs)
                                    cs.setDefault();
                                else
                                    s.setValue(s.getDefaultValue());
                            }
                            mc.setScreen(null);
                        }, () -> mc.setScreen(null));
                mc.setScreen(dialogScreen);
            }

            if (button == 0) {
                if (module.isToggleable())
                    module.toggle();
            } else if (button == 1 && (module.getSettings().size() > 3)) {
                setOpen(!isOpen());

                if (open) Managers.SOUND.playSwipeIn();
                else Managers.SOUND.playSwipeOut();

                animation = 0.5f;
            } else if (button == 2) {
                animation2 = 0;
                binding = !binding;
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

    public void charTyped(char key, int keyCode) {
        if (isOpen()) {
            for (AbstractElement element : elements)
                element.charTyped(key, keyCode);
        }
    }

    public void keyTyped(int keyCode) {
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
        return category_animation;
    }

    public double interp(double d, double d2, float d3) {
        return d2 + (d - d2) * d3;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void tick() {
        if (isOpen()) {
            gearAnimation.tick();
            ticksOpened++;
        } else {
            ticksOpened = 0;
        }
    }
}
