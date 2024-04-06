package thunder.hack.gui.hud;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.gui.clickui.AbstractWindow;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.clickui.ModuleWindow;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.ClickGui;

import java.util.List;

import static thunder.hack.modules.Module.mc;

public class HudEditorGui extends Screen {
    public static HudElement currentlyDragging;
    private final List<AbstractWindow> windows;
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
            ModuleWindow window = new ModuleWindow(Module.Category.HUD, ThunderHack.moduleManager.getModulesByCategory(Module.Category.HUD), 20f, 20f, 100f, 18f);
            window.setOpen(true);
            windows.add(window);
            firstOpen = false;
        }
        windows.forEach(AbstractWindow::init);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ClickGUI.anyHovered = false;
        for (AbstractWindow window : windows) {
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.GLFW_KEY_DOWN)) {
                window.setY(window.getY() + 2);
            }
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.GLFW_KEY_UP)) {
                window.setY(window.getY() - 2);
            }
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.GLFW_KEY_RIGHT)) {
                window.setX(window.getX() + 2);
            }
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT)) {
                window.setX(window.getX() - 2);
            }

            if (dWheel != 0) window.setY((float) (window.getY() + dWheel));
        }

        dWheel = 0;

        for (AbstractWindow window : windows) {
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
