package thunder.hack.gui.clickui;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;

import java.util.List;
import java.util.Objects;

import static thunder.hack.modules.Module.mc;

public class ClickGUI extends Screen {
    public static List<AbstractWindow> windows;
    public static boolean anyHovered;

    private boolean firstOpen;
    private float scrollY;
    private boolean setup = false;

    public static String currentDescription = "";
    public static final Identifier arrow = new Identifier("textures/arrow.png");

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
        return INSTANCE;
    }

    public static ClickGUI getClickGui() {
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

            for (final Module.Category category : ThunderHack.moduleManager.getCategories()) {
                if (category == Module.Category.HUD) continue;
                ModuleWindow window = new ModuleWindow(category, ThunderHack.moduleManager.getModulesByCategory(category), 20f + offset, 20, 100, windowHeight);
                window.setOpen(true);
                windows.add(window);
                offset += 102f;
                if (offset > mc.getWindow().getScaledWidth())
                    offset = 0;
            }
            firstOpen = false;
        }
        windows.forEach(AbstractWindow::init);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void tick() {
        windows.forEach(AbstractWindow::tick);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        anyHovered = false;

        if (Module.fullNullCheck())
            Render2DEngine.drawMainMenuShader(context.getMatrices(), 0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());

        if (ModuleManager.clickGui.scrollMode.getValue() == ClickGui.scrollModeEn.Old) {
            for (AbstractWindow window : windows) {
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
        } else for (AbstractWindow window : windows)
            if (scrollY != 0)
                window.setModuleOffset(scrollY, mouseX, mouseY);

        scrollY = 0;
        windows.forEach(w -> w.render(context, mouseX, mouseY, delta));

        if (!Objects.equals(currentDescription, "") && ModuleManager.clickGui.descriptions.getValue()) {
            Render2DEngine.drawHudBase(context.getMatrices(), mouseX + 7, mouseY + 5, FontRenderers.sf_medium.getStringWidth(currentDescription) + 6, 11, 1f);
            FontRenderers.sf_medium.drawString(context.getMatrices(), currentDescription, mouseX + 10, mouseY + 8, HudEditor.getColor(0).getRGB());
            currentDescription = "";
        }

        if (ModuleManager.clickGui.tips.getValue())
            FontRenderers.sf_medium.drawString(context.getMatrices(),
                    "Left Mouse Click to enable module" +
                            "\nRight Mouse Click to open module settings\nMiddle Mouse Click to bind module" +
                            "\nCtrl + F to start searching\nDrag n Drop config there to load" +
                            "\nShift + Left Mouse Click to change module visibility in array list" +
                            "\nMiddle Mouse Click on slider to enter value from keyboard" +
                            "\nDelete + Left Mouse Click on module to reset", 5, mc.getWindow().getScaledHeight() - 80, HudEditor.getColor(0).getRGB());
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        windows.forEach(w -> w.keyTyped(keyCode));

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return false;
    }
}