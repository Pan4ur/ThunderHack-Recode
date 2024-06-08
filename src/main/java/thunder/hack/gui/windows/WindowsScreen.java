package thunder.hack.gui.windows;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import thunder.hack.modules.Module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WindowsScreen extends Screen {
    private List<WindowBase> windows = new ArrayList<>();
    public static WindowBase lastClickedWindow;
    public static WindowBase draggingWindow;

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

        windows.forEach(w -> {
            if (w != lastClickedWindow)
                w.render(context, mouseX, mouseY);
        });
        if (lastClickedWindow != null)
            lastClickedWindow.render(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        windows.forEach(w -> w.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        windows.forEach(w -> w.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        windows.forEach(w -> w.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char key, int keyCode) {
        windows.forEach(w -> w.charTyped(key, keyCode));
        return super.charTyped(key, keyCode);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        windows.forEach(w -> w.mouseScrolled((int) (verticalAmount * 5D)));
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
