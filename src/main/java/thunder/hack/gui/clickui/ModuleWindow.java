package thunder.hack.gui.clickui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import thunder.hack.cmd.Command;
import thunder.hack.core.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.Animation;
import thunder.hack.utility.render.animation.Direction;
import thunder.hack.utility.render.animation.EaseBackIn;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleWindow extends AbstractWindow {
    private final Identifier ICON;

    private final Animation animation = new EaseBackIn(270, 1f, 1.03f, Direction.BACKWARDS);

    private boolean scrollHover; // scroll hover
    private List<AbstractButton> buttons;

    public ModuleWindow(Module.Category category, List<Module> features, int index, double x, double y, double width, double height) {
        super(category.getName(), x, y, width, height);
        buttons = new ArrayList<>();
        ICON = new Identifier("textures/" + category.getName().toLowerCase() + ".png");

        if (category.getName().equals("ThunderHack")) {
            SearchBar search = new SearchBar();
            search.setHeight(17);
            buttons.add(search);
        }

        features.forEach(feature -> {
            ModuleButton button = new ModuleButton(feature);
            button.setHeight(17);
            buttons.add(button);
        });
    }

    @Override
    public void init() {
        buttons.forEach(AbstractButton::init);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, Color color) {
        super.render(context, mouseX, mouseY, delta, color);

        scrollHover = Render2DEngine.isHovered(mouseX, mouseY, x, y + height, width, 4000);

        animation.setDirection(isOpen() ? Direction.FORWARDS : Direction.BACKWARDS);
        context.getMatrices().push();

        boolean popStack = false;
        //RoundedShader.drawRoundDoubleColor(matrixStack,(float) x + 2, (float) (y + height - 8), (float) width - 4, (float) ((getButtonsHeight() + 11) * animation.getOutput()), 3, ClickGui.getInstance().getColor(200),ClickGui.getInstance().getColor(0));

        float height1;
        if (ModuleManager.clickGui.scrollMode.getValue() == ClickGui.scrollModeEn.Old || (getButtonsHeight() + 8) < ModuleManager.clickGui.catHeight.getValue()) {
            height1 = (float) ((getButtonsHeight() + 8) * animation.getOutput());
        } else {
            height1 = (float) ((ModuleManager.clickGui.catHeight.getValue()) * animation.getOutput());
        }

        if (ModuleManager.clickGui.scrollMode.getValue() == ClickGui.scrollModeEn.Old || (getButtonsHeight() + 8) < ModuleManager.clickGui.catHeight.getValue()) {
            if(ModuleManager.clickGui.outline.getValue())
                Render2DEngine.drawRound(context.getMatrices(), (float) x + 2, (float) (y + height - 7), (float) width - 4, height1 + 2, 3, ClickGui.getInstance().getColor(1));
            Render2DEngine.drawRound(context.getMatrices(), (float) x + 3, (float) (y + height - 6), (float) width - 6, height1, 3, ClickGui.getInstance().plateColor.getValue().getColorObject());
        } else {
            if(ModuleManager.clickGui.outline.getValue())
                Render2DEngine.drawRound(context.getMatrices(), (float) x + 2, (float) (y + height - 7), (float) width - 4, height1 + 2, 3, ClickGui.getInstance().getColor(1));
            Render2DEngine.drawRound(context.getMatrices(), (float) x + 3, (float) (y + height - 6), (float) width - 6, height1, 3, ClickGui.getInstance().plateColor.getValue().getColorObject());
            Render2DEngine.addWindow(context.getMatrices(), (float) x + 3, (float) (y + height - 6), (float) (x + 3 + (float) width - 6), (float) ((y + height - 6) + (float) ((ModuleManager.clickGui.catHeight.getValue()) * animation.getOutput())), 1f);
            popStack = true;
        }

        if (animation.finished(Direction.FORWARDS)) {
            Render2DEngine.drawBlurredShadow(context.getMatrices(), (int) x + 4, (int) (y + height - 6), (int) width - 8, 8, 7, new Color(0, 0, 0, 180));
            for (AbstractButton button : buttons) {
                if (button instanceof ModuleButton mb && SearchBar.listening && !mb.module.getName().toLowerCase().contains(SearchBar.moduleName))
                    continue;
                /*
                if (button.getY() + button.getHeight() > y && button.getY() - button.getHeight() < height1) {
                    button.setHiden(false);
                } else {
                    button.setHiden(true);
                }
                 */

                AbstractButton lastElement = buttons.get(buttons.size() - 1);

                if (popStack && lastElement.getY() + lastElement.getHeight() + moduleOffset < height1) {
                    //button.setY(y);
                } //else {

                    if (popStack && buttons.get(0).getY() + moduleOffset < y + height) {
                        button.setY(y + height + moduleOffset);
                    } else {
                        button.setY(y + height);
                        moduleOffset = 0f;
                    }
               // }


                button.setX(x + 2);
                button.setWidth(width - 4);
                button.setHeight(17);
                button.render(context, mouseX, mouseY, delta, color);
            }
        }

        if (popStack) Render2DEngine.popWindow();

        Render2DEngine.drawRoundD(context.getMatrices(), x + 2, y - 3, width - 4, height, 4, ClickGui.getInstance().catColor.getValue().getColorObject());

        context.drawTexture(ICON, (int) (x + 7), (int) (y + (height - 18) / 2), 0, 0, 12, 12, 12, 12);

        FontRenderers.categories.drawCenteredString(context.getMatrices(), getName(), ((int) x + 2 + (width - 4) / 2), (int) y + (int) height / 2f - 7, new Color(-1).getRGB());
        context.getMatrices().pop();
        updatePosition();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {

        if (button == 1 && hovered) {
            setOpen(!isOpen());
        }
        super.mouseClicked(mouseX, mouseY, button);

        if (isOpen() && scrollHover)
            buttons.forEach(b -> b.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        if (isOpen())
            buttons.forEach(b -> b.mouseReleased(mouseX, mouseY, button));
    }

    @Override
    public boolean keyTyped(int keyCode) {
        if (isOpen()) {
            for (AbstractButton button : buttons) {
                button.keyTyped(keyCode);
            }
        }
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        buttons.forEach(AbstractButton::onGuiClosed);
    }

    private void updatePosition() {
        double offsetY = 0;
        double openY = 0;
        for (AbstractButton button : buttons) {
            if (button instanceof ModuleButton mb && SearchBar.listening && !mb.module.getName().toLowerCase().contains(SearchBar.moduleName)) {
                continue;
            }
            button.setOffsetY(offsetY);
            if (button instanceof ModuleButton mbutton) {
                if (mbutton.isOpen()) {
                    for (AbstractElement element : mbutton.getElements()) {
                        if (element.isVisible())
                            offsetY += element.getHeight();
                    }
                    offsetY += 2;
                }
            }
            offsetY += button.getHeight() + openY;
        }
    }

    public double getButtonsHeight() {
        double height = 0;
        for (AbstractButton button : buttons) {
            if (button instanceof ModuleButton mb && SearchBar.listening && !mb.module.getName().toLowerCase().contains(SearchBar.moduleName)) {
                continue;
            }
            if (button instanceof ModuleButton mbutton) {
                height += mbutton.getElementsHeight();
            }
            height += button.getHeight();
        }
        return height;
    }
}
