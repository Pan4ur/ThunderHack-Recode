package thunder.hack.gui.clickui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClickGui;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;
import thunder.hack.utility.render.animation.EaseOutBack;

import java.util.List;
import java.util.Objects;

import static thunder.hack.features.modules.Module.mc;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class ClickGUI extends Screen {
    public static List<AbstractCategory> windows;
    public static boolean anyHovered;

    private boolean firstOpen;
    private float scrollY, closeAnimation, prevYaw, prevPitch, closeDirectionX, closeDirectionY;
    public static boolean close = false, imageDirection;

    public static String currentDescription = "";
    public EaseOutBack imageAnimation = new EaseOutBack(6);

    public ClickGUI() {
        super(Text.of("NewClickGUI"));
        windows = Lists.newArrayList();
        firstOpen = true;
        this.setInstance();
    }

    private static ClickGUI INSTANCE = new ClickGUI();

    public static ClickGUI getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGUI();
        }

        imageDirection = true;

        return INSTANCE;
    }

    public static ClickGUI getClickGui() {
        windows.forEach(AbstractCategory::init);
        return ClickGUI.getInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    protected void init() {
        if (firstOpen) {
            float offset = 0;
            int windowHeight = 18;

            int halfWidth = mc.getWindow().getScaledWidth() / 2;
            int halfWidthCats = (int) ((((float) Module.Category.values().size() - 1) / 2f) * (ModuleManager.clickGui.moduleWidth.getValue() + 4f));

            for (final Module.Category category : Managers.MODULE.getCategories()) {
                if (category == Module.Category.HUD) continue;
                Category window = new Category(category, Managers.MODULE.getModulesByCategory(category), (halfWidth - halfWidthCats) + offset, 20, 100, windowHeight);
                window.setOpen(true);
                windows.add(window);
                offset += ModuleManager.clickGui.moduleWidth.getValue() + 2;
                if (offset > mc.getWindow().getScaledWidth())
                    offset = 0;
            }
            firstOpen = false;
        } else {
            if (windows.getFirst().getX() < 0 || windows.getFirst().getY() < 0) {
                float offset = 0;

                int halfWidth = mc.getWindow().getScaledWidth() / 2;
                int halfWidthCats = (int) (3 * (ModuleManager.clickGui.moduleWidth.getValue() + 4f));

                for (AbstractCategory w : windows) {
                    w.setX((halfWidth - halfWidthCats) + offset);
                    w.setY(20);
                    offset += ModuleManager.clickGui.moduleWidth.getValue() + 2;
                    if (offset > mc.getWindow().getScaledWidth())
                        offset = 0;
                }
            }
        }
        windows.forEach(AbstractCategory::init);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void tick() {
        windows.forEach(AbstractCategory::tick);
        imageAnimation.update(imageDirection);

        if (close) {
            if (mc.player != null) {
                if (mc.player.getPitch() > prevPitch)
                    closeDirectionY = (prevPitch - mc.player.getPitch()) * 300;

                if (mc.player.getPitch() < prevPitch)
                    closeDirectionY = (prevPitch - mc.player.getPitch()) * 300;

                if (mc.player.getYaw() > prevYaw)
                    closeDirectionX = (prevYaw - mc.player.getYaw()) * 300;

                if (mc.player.getYaw() < prevYaw)
                    closeDirectionX = (prevYaw - mc.player.getYaw()) * 300;
            }

            if (closeDirectionX < 1 && closeDirectionY < 1 && closeAnimation > 2)
                closeDirectionY = -3000;

            closeAnimation++;
            if (closeAnimation > 6) {
                close = false;
                windows.forEach(AbstractCategory::restorePos);
                close();
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (ModuleManager.clickGui.blur.getValue())
            applyBlur(delta);

        anyHovered = false;

        ClickGui.Image image = ModuleManager.clickGui.image.getValue();

        if (image != ClickGui.Image.None) {
            RenderSystem.setShaderTexture(0, image.file);

            Render2DEngine.renderTexture(context.getMatrices(),

                    mc.getWindow().getScaledWidth() - image.fileWidth * imageAnimation.getAnimationd(),
                    mc.getWindow().getScaledHeight() - image.fileHeight,

                    image.fileWidth,
                    image.fileHeight,


                    0, 0,
                    image.fileWidth, image.fileHeight, image.fileWidth, image.fileHeight);
        }

        if (closeAnimation <= 6) {
            windows.forEach(w -> {
                w.setX((float) (w.getX() + closeDirectionX * AnimationUtility.deltaTime()));
                w.setY((float) (w.getY() + closeDirectionY * AnimationUtility.deltaTime()));
            });
        }


        if (Module.fullNullCheck())
            renderBackground(context, mouseX, mouseY, delta);
        //   Render2DEngine.drawMainMenuShader(context.getMatrices(), 0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());

        if (ModuleManager.clickGui.scrollMode.getValue() == ClickGui.scrollModeEn.Old) {
            for (AbstractCategory window : windows) {
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 264))
                    window.setY(window.getY() + 2);
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 265))
                    window.setY(window.getY() - 2);
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 262))
                    window.setX(window.getX() + 2);
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 263))
                    window.setX(window.getX() - 2);
                if (scrollY != 0)
                    window.setY(window.getY() + scrollY);
            }
        } else for (AbstractCategory window : windows)
            if (scrollY != 0)
                window.setModuleOffset(scrollY, mouseX, mouseY);

        scrollY = 0;
        windows.forEach(w -> w.render(context, mouseX, mouseY, delta));

        if (!Objects.equals(currentDescription, "") && ModuleManager.clickGui.descriptions.getValue()) {
            Render2DEngine.drawHudBase(context.getMatrices(), mouseX + 7, mouseY + 5, FontRenderers.sf_medium.getStringWidth(currentDescription) + 6, 11, 1f, false);
            FontRenderers.sf_medium.drawString(context.getMatrices(), currentDescription, mouseX + 10, mouseY + 8, HudEditor.getColor(0).getRGB());
            currentDescription = "";
        }

        if (ModuleManager.clickGui.tips.getValue() && !close)
            FontRenderers.sf_medium.drawString(context.getMatrices(),
                    isRu() ? "Щелкните левой кнопкой мыши, чтобы включить модуль." +
                            "\nЩелкните правой кнопкой мыши, чтобы открыть настройки модуля." +
                            "\nЩелкните колёсиком мыши, чтобы привязать модуль" +
                            "\nCtrl + F, чтобы начать поиск" +
                            "\nПерекиньте конфиг в окошко майна, чтобы загрузить его" +
                            "\nShift + Left Mouse Click, чтобы изменить отображение модуля в Array list" +
                            "\nЩелкните колёсиком мыши по слайдеру, чтобы ввести значение с клавиатуры." +
                            "\nDelete + Left Mouse Click по модулю, чтобы сбросить его настройки"
                            :
                            "Left Mouse Click to enable module" +
                                    "\nRight Mouse Click to open module settings" +
                                    "\nMiddle Mouse Click to bind module" +
                                    "\nCtrl + F to start searching" +
                                    "\nDrag n Drop config there to load" +
                                    "\nShift + Left Mouse Click to change module visibility in Array list" +
                                    "\nMiddle Mouse Click on slider to enter value from keyboard" +
                                    "\nDelete + Left Mouse Click on module to reset",
                    5, mc.getWindow().getScaledHeight() - 80, HudEditor.getColor(0).getRGB());

        if (!HudElement.anyHovered && !ClickGUI.anyHovered)
            if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_WAYLAND) {
                GLFW.glfwSetCursor(mc.getWindow().getHandle(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR));
            }

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollY += (int) (verticalAmount * 5D);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        windows.forEach(w -> {
            w.mouseClicked((int) mouseX, (int) mouseY, button);
            windows.forEach(w1 -> {
                if (w.dragging && w != w1) w1.dragging = false;
            });
        });
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        //   if (!setup && ConfigManager.firstLaunch) return false;
        windows.forEach(w -> w.mouseReleased((int) mouseX, (int) mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char key, int modifier) {
        windows.forEach(w -> w.charTyped(key, modifier));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        windows.forEach(w -> w.keyTyped(keyCode));

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (mc.player == null || !ModuleManager.clickGui.closeAnimation.getValue()) {
                imageDirection = false;
                imageAnimation.reset();
                super.keyPressed(keyCode, scanCode, modifiers);
                return true;
            }

            if (close)
                return true;

            imageDirection = false;

            windows.forEach(AbstractCategory::savePos);

            closeDirectionX = 0;
            closeDirectionY = 0;

            close = true;
            mc.mouse.lockCursor();

            closeAnimation = 0;
            if (mc.player != null) {
                prevYaw = mc.player.getYaw();
                prevPitch = mc.player.getPitch();
            }
            return true;
        }

        return false;
    }
}
