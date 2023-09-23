package thunder.hack.gui.hud;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.gui.clickui.AbstractWindow;
import thunder.hack.gui.clickui.ModuleWindow;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.utility.render.MSAAFramebuffer;
import thunder.hack.utility.render.animation.Animation;
import thunder.hack.utility.render.animation.DecelerateAnimation;
import thunder.hack.utility.render.animation.Direction;
import thunder.hack.utility.render.animation.EaseBackIn;

import java.util.List;

import static thunder.hack.modules.Module.mc;

public class HudEditorGui extends Screen {

    private Animation openAnimation, bgAnimation, rAnimation;
    private final List<AbstractWindow> windows;

    private boolean firstOpen;
    private double dWheel;

    public HudEditorGui() {
        super(Text.of("HudEditorGui"));
        windows = Lists.newArrayList();
        firstOpen = true;
        this.setInstance();
    }

    private static HudEditorGui INSTANCE = new HudEditorGui();

    public static HudEditorGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HudEditorGui();
        }
        return INSTANCE;
    }

    public static HudEditorGui getHudGui() {
        return HudEditorGui.getInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }


    @Override
    protected void init() {
        openAnimation = new EaseBackIn(270, .4f, 1.13f);
        rAnimation = new DecelerateAnimation(300, 1f);
        bgAnimation = new DecelerateAnimation(300, 1f);
        if (firstOpen) {
            double x = 20, y = 20;
            double offset = 0;
            int windowHeight = 18;

            int i = 0;
            for (final Module.Category category : ThunderHack.moduleManager.getCategories()) {
                if (!category.getName().contains("HUD")) continue;
                ModuleWindow window = new ModuleWindow(category, ThunderHack.moduleManager.getModulesByCategory(category), i, x + offset, y, 108, windowHeight);
                window.setOpen(true);
                windows.add(window);
                offset += 110;

                if (offset > mc.getWindow().getScaledWidth()) {
                    offset = 0;
                }
                i++;
            }
            firstOpen = false;
        }
        windows.forEach(AbstractWindow::init);
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        if (openAnimation.isDone() && openAnimation.getDirection().equals(Direction.BACKWARDS)) {
            windows.forEach(AbstractWindow::onClose);
            mc.currentScreen = null;
        }


        double anim = (openAnimation.getOutput() + .6f);


        double centerX = width >> 1;
        double centerY = height >> 1;

        context.getMatrices().translate(centerX, centerY, 0);
        context.getMatrices().scale((float) anim, (float) anim, 1);
        context.getMatrices().translate(-centerX, -centerY, 0);

        for (AbstractWindow window : windows) {
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 264)) {
                window.setY(window.getY() + 2);
            }
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 265)) {
                window.setY(window.getY() - 2);
            }
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 262)) {
                window.setX(window.getX() + 2);
            }
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 263)) {
                window.setX(window.getX() - 2);
            }

            if (dWheel != 0) {
                window.setY(window.getY() + dWheel);
            }
        }

        dWheel = 0;

        if (ClickGui.getInstance().msaa.getValue()) {
            if (!MSAAFramebuffer.framebufferInUse()) {
                MSAAFramebuffer.use(() -> {
                    for (AbstractWindow window : windows) {
                        window.render(context, mouseX, mouseY, delta, ClickGui.getInstance().hcolor1.getValue().getColorObject());
                    }
                });
            } else {
                for (AbstractWindow window : windows) {
                    window.render(context, mouseX, mouseY, delta, ClickGui.getInstance().hcolor1.getValue().getColorObject());
                }
            }
        } else {
            for (AbstractWindow window : windows) {
                window.render(context, mouseX, mouseY, delta, ClickGui.getInstance().hcolor1.getValue().getColorObject());
            }
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
        windows.forEach(w -> {
            w.keyTyped(keyCode);
        });

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            bgAnimation.setDirection(Direction.BACKWARDS);
            rAnimation.setDirection(Direction.BACKWARDS);
            openAnimation.setDirection(Direction.BACKWARDS);
            super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return false;
    }

    @Override
    public void removed() {
        ThunderHack.EVENT_BUS.unsubscribe(this);
    }
}
