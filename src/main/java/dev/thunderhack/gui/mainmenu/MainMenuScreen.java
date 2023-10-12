package dev.thunderhack.gui.mainmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.thunderhack.gui.font.FontRenderers;
import dev.thunderhack.modules.Module;
import dev.thunderhack.modules.client.HudEditor;
import dev.thunderhack.utils.ThunderUtility;
import dev.thunderhack.utils.render.MSAAFramebuffer;
import dev.thunderhack.utils.render.Render2DEngine;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import dev.thunderhack.gui.clickui.ClickUI;

import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class MainMenuScreen extends Screen {
    private static final Identifier TH_LOGO = new Identifier("textures/th.png");
    private final List<MainMenuButton> buttons = new ArrayList<>();
    public boolean confirm = false;

    protected MainMenuScreen() {
        super(Text.of("THMainMenuScreen"));
        INSTANCE = this;

        buttons.add(new MainMenuButton(-110, -70, "SINGLEPLAYER", () -> Module.mc.setScreen(new SelectWorldScreen(this))));
        buttons.add(new MainMenuButton(4, -70, "MULTIPLAYER", () -> Module.mc.setScreen(new MultiplayerScreen(this))));
        buttons.add(new MainMenuButton(-110, -29, "SETTINGS", () -> Module.mc.setScreen(new OptionsScreen(this, Module.mc.options))));
        buttons.add(new MainMenuButton(4, -29, "CLICKGUI", () -> Module.mc.setScreen(ClickUI.getClickGui())));
        buttons.add(new MainMenuButton(-110, 12, "EXIT", () -> Module.mc.scheduleStop()));
    }

    private static MainMenuScreen INSTANCE = new MainMenuScreen();

    public static MainMenuScreen getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MainMenuScreen();
        }
        return INSTANCE;
    }

    @Override
    public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        float halfOfWidth = Module.mc.getWindow().getScaledWidth() / 2f;
        float halfOfHeight = Module.mc.getWindow().getScaledHeight() / 2f;

        float mainX = halfOfWidth - 120f;
        float mainY = halfOfHeight - 80f;
        float mainWidth = 240f;
        float mainHeight = 140;

        Color c1 = HudEditor.getColor(270);
        Color c2 = HudEditor.getColor(0);
        Color c3 = HudEditor.getColor(180);
        Color c4 = HudEditor.getColor(90);


        Render2DEngine.drawMainMenuShader(context.getMatrices(), 0, 0, halfOfWidth * 2f, halfOfHeight * 2);

        Render2DEngine.drawGradientGlow(context.getMatrices(), c1, c2, c3, c4, mainX, mainY, mainWidth, mainHeight, 20, 10);
        Render2DEngine.drawGradientRoundShader(context.getMatrices(), c1, c2, c3, c4, mainX, mainY, mainWidth, mainHeight, 20);
        Render2DEngine.drawRoundShader(context.getMatrices(), mainX + 1, mainY + 1, mainWidth - 2, mainHeight - 2, 20, HudEditor.plateColor.getValue().getColorObject());

        buttons.forEach(b -> b.onRender(context, mouseX, mouseY));

        MSAAFramebuffer.use(() -> {
            // Smooth zone
            boolean hoveredLogo = Render2DEngine.isHovered(mouseX, mouseY, (int) (halfOfWidth - 157), (int) (halfOfHeight - 140), 300, 70);


            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hoveredLogo ? 0.4f : 0.3f);
            context.drawTexture(TH_LOGO,(int) (halfOfWidth - 157), (int) (halfOfHeight - 140), 0, 0, 300, 70, 300, 70);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();

            buttons.forEach(b -> b.onRenderText(context, mouseX, mouseY));

            boolean hovered = Render2DEngine.isHovered(mouseX, mouseY, halfOfWidth - 50, halfOfHeight + 70, 100, 10);
            FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "<-- Back to default menu", halfOfWidth, halfOfHeight + 70, hovered ? -1 : Render2DEngine.applyOpacity(-1, 0.4f));

            FontRenderers.sf_medium.drawString(context.getMatrices(), "By Pan4ur & 06ED", halfOfWidth * 2 - FontRenderers.sf_medium.getStringWidth("By Pan4ur & 06ED") - 5f, halfOfHeight * 2 - 10, Render2DEngine.applyOpacity(-1, 0.4f));

            int offsetY = 10;
            for (String change : ThunderUtility.changeLog) {
                String prefix = getPrefix(change);
                FontRenderers.sf_medium.drawString(context.getMatrices(), prefix, 10, offsetY, Render2DEngine.applyOpacity(-1, 0.4f));
                offsetY += 10;
            }
        });
    }

    private static @NotNull String getPrefix(@NotNull String change) {
        String prefix = "";
        if (change.contains("[+]")) {
            change = change.replace("[+] ", "");
            prefix = Formatting.GREEN + "[+] " + Formatting.RESET;
        } else if (change.contains("[-]")) {
            change = change.replace("[-] ", "");
            prefix = Formatting.RED + "[-] " + Formatting.RESET;
        } else if (change.contains("[/]")) {
            change = change.replace("[/] ", "");
            prefix = Formatting.LIGHT_PURPLE + "[/] " + Formatting.RESET;
        } else if (change.contains("[*]")) {
            change = change.replace("[*] ", "");
            prefix = Formatting.GOLD + "[*] " + Formatting.RESET;
        }
        return prefix + change;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float halfOfWidth = Module.mc.getWindow().getScaledWidth() / 2f;
        float halfOfHeight = Module.mc.getWindow().getScaledHeight() / 2f;
        buttons.forEach(b -> b.onClick((int) mouseX, (int) mouseY));

        if (Render2DEngine.isHovered(mouseX, mouseY, halfOfWidth - 50, halfOfHeight + 70, 100, 10)) {
            confirm = true;
            Module.mc.setScreen(new TitleScreen());
            confirm = false;
        }

        if (Render2DEngine.isHovered(mouseX, mouseY, (int) (halfOfWidth - 157), (int) (halfOfHeight - 140), 300, 70))
            Util.getOperatingSystem().open(URI.create("https://thunderhack.onrender.com/"));

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
