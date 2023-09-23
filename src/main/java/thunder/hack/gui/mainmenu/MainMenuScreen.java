package thunder.hack.gui.mainmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import thunder.hack.ThunderHack;
import thunder.hack.gui.clickui.ClickUI;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.MSAAFramebuffer;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static thunder.hack.modules.Module.mc;

public class MainMenuScreen extends Screen {

    private List<MainMenuButton> buttons = new ArrayList<>();

    private final Identifier TH_LOGO = new Identifier("textures/th.png");


    protected MainMenuScreen() {
        super(Text.of("THMainMenuScreen"));
        INSTANCE = this;

        buttons.add(new MainMenuButton(-110, -70, "SINGLEPLAYER", () -> mc.setScreen(new SelectWorldScreen(this))));
        buttons.add(new MainMenuButton(4, -70, "MULTIPLAYER", () -> mc.setScreen(new MultiplayerScreen(this))));
        buttons.add(new MainMenuButton(-110, -29, "SETTINGS", () -> mc.setScreen(new OptionsScreen(this, mc.options))));
        buttons.add(new MainMenuButton(4, -29, "CLICKGUI", () -> mc.setScreen(ClickUI.getClickGui())));
        buttons.add(new MainMenuButton(-110, 12, "EXIT", () -> mc.scheduleStop()));
    }

    private static MainMenuScreen INSTANCE = new MainMenuScreen();

    public static MainMenuScreen getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MainMenuScreen();
        }
        return INSTANCE;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        float halfOfWidth = mc.getWindow().getScaledWidth() / 2f;
        float halfOfHeight = mc.getWindow().getScaledHeight() / 2f;


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

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.3f);
            Render2DEngine.drawTexture(context, TH_LOGO, (int) (halfOfWidth - 157), (int) (halfOfHeight - 140), 300, 70);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            buttons.forEach(b -> b.onRenderText(context, mouseX, mouseY));

        });
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        buttons.forEach(b -> b.onClick((int) mouseX, (int) mouseY));
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
