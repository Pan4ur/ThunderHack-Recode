package thunder.hack.gui.hud;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClickGui;
import thunder.hack.gui.clickui.AbstractCategory;
import thunder.hack.gui.clickui.Category;
import thunder.hack.gui.clickui.ClickGUI;

import java.util.List;

import static thunder.hack.features.modules.Module.mc;

public class HudEditorGui extends Screen {
    public static HudElement currentlyDragging;
    private final List<AbstractCategory> windows;
    private static HudEditorGui instance = new HudEditorGui();

    private boolean firstOpen;
    private double dWheel;

    public HudEditorGui() {
        super(Text.of("HudEditorGui"));
        windows = Lists.newArrayList();
        firstOpen = true;

        this.setInstance();
    }

    @Override
    protected void init() {
        if (firstOpen) {
            Category window = new Category(Module.Category.HUD, Managers.MODULE.getModulesByCategory(Module.Category.HUD), mc.getWindow().getScaledWidth() / 2f - 50, 20f, 100f, 18f);
            window.setOpen(true);
            windows.add(window);
            firstOpen = false;
        }
        windows.forEach(AbstractCategory::init);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ClickGUI.anyHovered = false;

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
                if (dWheel != 0)
                    window.setY((float) (window.getY() + dWheel));
            }
        } else for (AbstractCategory window : windows)
            if (dWheel != 0)
                window.setModuleOffset((float) dWheel, mouseX, mouseY);

        dWheel = 0;

        for (AbstractCategory window : windows) {
            window.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        dWheel = (int) (verticalAmount * 5D);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        windows.forEach(w -> {
            w.mouseClicked((int) mouseX, (int) mouseY, button);

            windows.forEach(w1 -> {
                if (w.dragging && w != w1)
                    w1.dragging = false;
            });
        });
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
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

    @Override
    public void removed() {
        ThunderHack.EVENT_BUS.unsubscribe(this);
    }

    public void hudClicked(Module module) {
        for (AbstractCategory window : windows) {
            window.hudClicked(module);
        }
    }

    public static HudEditorGui getInstance() {
        if (instance == null) {
            instance = new HudEditorGui();
        }
        return instance;
    }

    public static HudEditorGui getHudGui() {
        return HudEditorGui.getInstance();
    }

    private void setInstance() {
        instance = this;
    }
}
