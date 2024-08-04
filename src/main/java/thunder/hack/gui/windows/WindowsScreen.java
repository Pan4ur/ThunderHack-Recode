package thunder.hack.gui.windows;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static thunder.hack.core.manager.IManager.mc;

public class WindowsScreen extends Screen {
    private List<WindowBase> windows = new ArrayList<>();
    public static WindowBase lastClickedWindow;
    public static WindowBase draggingWindow;
    private static final Identifier clickGuiIcon = Identifier.of("thunderhack", "textures/gui/elements/clickgui.png");

    public WindowsScreen(WindowBase... windows) {
        super(Text.of("THWindows"));
        this.windows.clear();
        lastClickedWindow = null;
        this.windows = Arrays.stream(windows).toList();
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //   super.render(context, mouseX, mouseY, delta);
        if (Module.fullNullCheck())
            renderBackground(context, mouseX, mouseY, delta);

        MatrixStack matrices = context.getMatrices();
        int i = mc.getWindow().getScaledWidth() / 2;

        float offset = (windows.size() * 20f) / -2f - 23;

        Render2DEngine.drawHudBase(matrices, i + offset - 1.5f, mc.getWindow().getScaledHeight() - 25, windows.size() * 20f + 23f, 19, HudEditor.hudRound.getValue());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, Render2DEngine.isHovered(mouseX, mouseY, (i + offset) + 1, mc.getWindow().getScaledHeight() - 23, 15, 15) ? 0.95f : 0.7f);
        context.drawTexture(clickGuiIcon, (int) (i + offset) + 1, mc.getWindow().getScaledHeight() - 23, 15, 15, 0, 0, 15, 15, 15, 15);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();

        Render2DEngine.drawLine(i + offset + 20, mc.getWindow().getScaledHeight() - 23, i + offset + 20, mc.getWindow().getScaledHeight() - 9, Color.GRAY.getRGB());

        offset += 23;
        for (WindowBase w : windows) {
            Color c = Render2DEngine.isHovered(mouseX, mouseY, i + offset, mc.getWindow().getScaledHeight() - 24, 17, 17) ? new Color(0x7C2F2F2F, true) :
                    !w.isVisible() ? new Color(0x7C1E1E1E, true) : new Color(0x7C3B3B3B, true);
            Render2DEngine.drawRect(matrices, i + offset, mc.getWindow().getScaledHeight() - 24, 17, 17, HudEditor.hudRound.getValue(), 0.7f, c, c, c, c);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.setShaderColor(1f, 1f, 1f, Render2DEngine.isHovered(mouseX, mouseY, (i + offset) + 1, mc.getWindow().getScaledHeight() - 23, 15, 15) ? 0.95f : 0.7f);
            context.drawTexture(w.getIcon() != null ? w.getIcon() : TextureStorage.configIcon, (int) (i + offset) + 3, mc.getWindow().getScaledHeight() - 21, 11, 11, 0, 0, 11, 11, 11, 11);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
            offset += 20f;
        }

        windows.stream().filter(WindowBase::isVisible).forEach(w -> {
            if (w != lastClickedWindow)
                w.render(context, mouseX, mouseY);
        });

        if (lastClickedWindow != null && lastClickedWindow.isVisible())
            lastClickedWindow.render(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        windows.forEach(w -> w.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        windows.stream().filter(WindowBase::isVisible).forEach(w -> w.mouseClicked(mouseX, mouseY, button));

        int i = mc.getWindow().getScaledWidth() / 2;
        float offset = (windows.size() * 20f) / -2f - 23;

        if (Render2DEngine.isHovered(mouseX, mouseY, (i + offset) + 1, mc.getWindow().getScaledHeight() - 23, 15, 15))
            mc.setScreen(ClickGUI.getClickGui());

        offset += 23;
        for (WindowBase w : windows) {
            if (Render2DEngine.isHovered(mouseX, mouseY, i + offset, mc.getWindow().getScaledHeight() - 24, 17, 17))
                w.setVisible(!w.isVisible());
            offset += 20f;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        windows.stream().filter(WindowBase::isVisible).forEach(w -> w.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char key, int keyCode) {
        windows.stream().filter(WindowBase::isVisible).forEach(w -> w.charTyped(key, keyCode));
        return super.charTyped(key, keyCode);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        windows.stream().filter(WindowBase::isVisible).forEach(w -> w.mouseScrolled((int) (verticalAmount * 5D)));
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
