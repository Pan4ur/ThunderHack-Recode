package thunder.hack.gui.misc;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.misc.NoCommentExploit;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.ArrayList;

import static thunder.hack.features.modules.Module.mc;

public class GuiScanner extends Screen {
    public static boolean neartrack = false;
    public static boolean track = false;
    public static boolean busy = false;

    public ArrayList<NoCommentExploit.Cout> consoleout = new ArrayList<>();

    int radarx, radary, radarx1, radary1, centerx, centery, consolex, consoley, consolex1, consoley1, hovery, hoverx, searchx, searchy, wheely;

    public GuiScanner() {
        super(Text.of("GuiScanner"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (mc.player == null) return;

        radarx = mc.getWindow().getScaledWidth() / 8;
        radarx1 = ((mc.getWindow().getScaledWidth() * 5) / 8);
        radary = (mc.getWindow().getScaledHeight() / 2) - ((radarx1 - radarx) / 2);
        radary1 = (mc.getWindow().getScaledHeight() / 2) + ((radarx1 - radarx) / 2);

        centerx = (radarx + radarx1) / 2;
        centery = (radary + radary1) / 2;

        consolex = (int) ((mc.getWindow().getScaledWidth() * 5.5f) / 8f);
        consolex1 = (mc.getWindow().getScaledWidth() - 50);
        consoley = radary;
        consoley1 = radary1 - 50;


        Render2DEngine.drawRectDumbWay(context.getMatrices(), consolex, consoley, consolex1, consoley1, new Color(0xF70C0C0C, true));

        Render2DEngine.drawRectDumbWay(context.getMatrices(), consolex, consoley1 + 3, consolex1, consoley1 + 17, new Color(0xF70C0C0C, true));
        FontRenderers.monsterrat.drawString(context.getMatrices(), "cursor pos: " + hoverx * 64 + "x" + "  " + hovery * 64 + "z", consolex + 4, consoley1 + 6, -1);

        if (!track) {
            Render2DEngine.drawRectDumbWay(context.getMatrices(), consolex, consoley1 + 20, consolex1, consoley1 + 35, new Color(0xF70C0C0C, true));
            FontRenderers.monsterrat.drawString(context.getMatrices(), "tracker off", consolex + 4, consoley1 + 26, -1);
        } else {
            Render2DEngine.drawRectDumbWay(context.getMatrices(), consolex, consoley1 + 20, consolex1, consoley1 + 35, new Color(0xF75E5E5E, true));
            FontRenderers.monsterrat.drawString(context.getMatrices(), "tracker on", consolex + 4, consoley1 + 26, -1);
        }

        Render2DEngine.drawRectDumbWay(context.getMatrices(), consolex, consoley1 + 38, consolex1, consoley1 + 53, new Color(0xF70C0C0C, true));
        FontRenderers.monsterrat.drawString(context.getMatrices(), "clear console", consolex + 4, consoley1 + 42, -1);

        Render2DEngine.drawRectDumbWay(context.getMatrices(), radarx, radary, radarx1, radary1, new Color(0xE0151515, true));

        for (NoCommentExploit.Dot point : new ArrayList<>(NoCommentExploit.dots)) {
            if (point.type() == NoCommentExploit.DotType.Searched) {
                Render2DEngine.drawRectDumbWay(context.getMatrices(), (point.posX() / 4f) + centerx, (point.posY() / 4f) + centery, ((point.posX() / 4f) + (radarx1 - radarx) / 300f) + centerx, ((point.posY() / 4f) + (radary1 - radary) / 300f) + centery, new Color(0xE7A8A8A8, true));
            } else {
                Render2DEngine.drawRectDumbWay(context.getMatrices(), (point.posX() / 4f) + centerx, (point.posY() / 4f) + centery, ((point.posX() / 4f) + (radarx1 - radarx) / 300f) + centerx, ((point.posY() / 4f) + (radary1 - radary) / 300f) + centery, new Color(0x3CE708));
            }
        }

        Render2DEngine.drawRectDumbWay(context.getMatrices(), centerx - 1f, centery - 1f, centerx + 1f, centery + 1f, new Color(0xFF0303));
        Render2DEngine.drawRectDumbWay(context.getMatrices(), (float) ((mc.player.getX() / 16 / 4f) + centerx), (float) ((mc.player.getZ() / 16 / 4f) + centery), (float) (((mc.player.getX() / 16 / 4f) + (radarx1 - radarx) / 300f) + centerx), (float) (((mc.player.getZ() / 16 / 4f) + (radary1 - radary) / 300f) + centery), new Color(0x0012FF));

        if (mouseX > radarx && mouseX < radarx1 && mouseY > radary && mouseY < radary1) {
            hoverx = mouseX - centerx;
            hovery = mouseY - centery;
        }

        Render2DEngine.addWindow(context.getMatrices(), consolex, consoley, consolex1, consoley1 - 10, 1f);

        for (NoCommentExploit.Cout out : new ArrayList<>(consoleout)) {
            FontRenderers.monsterrat.drawString(context.getMatrices(), out.out(), consolex + 4, consoley + 6 + (out.posY() * 11) + wheely, -1);
        }
        Render2DEngine.popWindow();

        FontRenderers.monsterrat.drawString(context.getMatrices(), "X+", radarx1 + 5, centery, -1);
        FontRenderers.monsterrat.drawString(context.getMatrices(), "X-", radarx - 15, centery, -1);
        FontRenderers.monsterrat.drawString(context.getMatrices(), "Y+", centerx, radary1 + 5, -1);
        FontRenderers.monsterrat.drawString(context.getMatrices(), "Y-", centerx, radary - 8, -1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX > radarx && mouseX < radarx1 && mouseY > radary && mouseY < radary1) {
            busy = true;
            searchx = (int) (mouseX - centerx);
            searchy = (int) (mouseY - centery);
            ModuleManager.noCommentExploit.rerun(searchx * 64, searchy * 64);
            consoleout.add(new NoCommentExploit.Cout(ModuleManager.noCommentExploit.couti, "Selected pos " + searchx * 65 + "x " + searchy * 64 + "z "));
            ++ModuleManager.noCommentExploit.couti;
        }
        if (mouseX > consolex && mouseX < consolex1 && mouseY > consoley1 + 20 && mouseY < consoley1 + 36) {
            track = !track;
        }
        if (mouseX > consolex && mouseX < consolex1 && mouseY > consoley1 + 38 && mouseY < consoley1 + 53) {
            ModuleManager.noCommentExploit.couti = 1;
            consoleout.clear();
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        wheely += (int) (verticalAmount * 5D);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
