package thunder.hack.gui.mainmenu;

import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

import static thunder.hack.modules.Module.mc;

public class VersionChangerWidget {
    private final float width = 150, height = 76;

    public void onRender(@NotNull DrawContext context, float mouseX, float mouseY) {
        Color c1 = HudEditor.getColor(270);
        Color c2 = HudEditor.getColor(0);
        Color c3 = HudEditor.getColor(180);
        Color c4 = HudEditor.getColor(90);

        Render2DEngine.drawGradientRoundShader(context.getMatrices(), c1, c2, c3, c4, getX(), getY(), width, height, 10);
        Render2DEngine.drawRoundShader(context.getMatrices(), getX() + 1, getY() + 1, width - 2, height - 2, 10, HudEditor.plateColor.getValue().getColorObject());
    }

    public void onRenderText(@NotNull DrawContext context, float mouseX, float mouseY) {
        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "Protocol", getX() + width / 2f, getY() + 10f, -1);
        FontRenderers.sf_medium_mini.drawCenteredString(context.getMatrices(), "1.20.1",getX() + width / 2f, getY() + height / 2f, Render2DEngine.applyOpacity(-1, 0.5f));
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "1.20.2",getX() + width / 2f, getY() + height / 2f - 7f, -1);
        FontRenderers.sf_medium_mini.drawCenteredString(context.getMatrices(), "1.20.3",getX() + width / 2f, getY() + height / 2f - 15f, Render2DEngine.applyOpacity(-1, 0.5f));
    }

    private float getX() {
        return mc.getWindow().getScaledWidth() - width - 10;
    }

    private float getY() {
        return 10;
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double amount = horizontalAmount == 0 ? verticalAmount : horizontalAmount;
    }
}
