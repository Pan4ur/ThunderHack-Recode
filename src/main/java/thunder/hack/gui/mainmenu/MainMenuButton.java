package thunder.hack.gui.mainmenu;

import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;

import static thunder.hack.features.modules.Module.mc;

public class MainMenuButton {
    private final float posX, posY, width, height;
    private final String name;
    private final Runnable action;

    public MainMenuButton(float posX, float posY, @NotNull String name, Runnable action, boolean isExit) {
        this.name = name;
        this.posX = posX;
        this.posY = posY;

        this.action = action;

        this.width = isExit ? 222f : 107f;
        this.height = 38f;
    }

    public MainMenuButton(float posX, float posY, @NotNull String name, Runnable action) {
        this(posX, posY, name, action, false);
    }

    public void onRender(DrawContext context, float mouseX, float mouseY) {
        float halfOfWidth = mc.getWindow().getScaledWidth() / 2f;
        float halfOfHeight = mc.getWindow().getScaledHeight() / 2f;
        Render2DEngine.drawHudBase(context.getMatrices(), halfOfWidth + posX, halfOfHeight + posY, width, height, 10);
        boolean hovered = Render2DEngine.isHovered(mouseX, mouseY, halfOfWidth + posX, halfOfHeight + posY, width, height);
        FontRenderers.monsterrat.drawCenteredString(context.getMatrices(), name, halfOfWidth + posX + width / 2f, halfOfHeight + posY + height / 2f - 3f, hovered ? -1 : Render2DEngine.applyOpacity(-1, 0.7f));
    }


    public void onClick(int mouseX, int mouseY) {
        float halfOfWidth = mc.getWindow().getScaledWidth() / 2f;
        float halfOfHeight = mc.getWindow().getScaledHeight() / 2f;
        boolean hovered = Render2DEngine.isHovered(mouseX, mouseY, halfOfWidth + posX, halfOfHeight + posY, width, height);
        if (hovered) action.run();
    }
}
