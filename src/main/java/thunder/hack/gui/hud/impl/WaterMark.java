package thunder.hack.gui.hud.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.client.Media;
import thunder.hack.modules.misc.NameProtect;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextUtil;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static thunder.hack.core.impl.ServerManager.getPing;

public class WaterMark extends HudElement {
    public WaterMark() {
        super("WaterMark", 100, 35);
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.Big);
    private final Setting<Boolean> ru = new Setting<>("RU", false);

    private final Identifier logo = new Identifier("thunderhack", "textures/hud/icons/mini_logo.png");
    private final Identifier player = new Identifier("thunderhack", "textures/gui/headers/player.png");
    private final Identifier server = new Identifier("thunderhack", "textures/hud/icons/server.png");
    private final Identifier baltika = new Identifier("thunderhack", "textures/hud/icons/baltika.png");

    private final TextUtil textUtil = new TextUtil(
            "ТандерХак",
            "ГромХак",
            "ГрозаКлиент",
            "ТандерХуй",
            "ТандерХряк",
            "ТандерХрюк",
            "ТиндерХак",
            "ТундраХак",
            "ГромВзлом"
    );


    private enum Mode {
        Big, Small, Classic, BaltikaClient, Rifk
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        String username = ((ModuleManager.media.isEnabled() && Media.nickProtect.getValue()) || ModuleManager.nameProtect.isEnabled()) ? (ModuleManager.nameProtect.isEnabled() ? NameProtect.getCustomName() : "Protected") : mc.getSession().getUsername();

        if (mode.getValue() == Mode.Big) {
            Render2DEngine.drawHudBase(context.getMatrices(), getPosX(), getPosY(), 106, 30, HudEditor.hudRound.getValue());
            FontRenderers.thglitch.drawString(context.getMatrices(), "THUNDERHACK", getPosX() + 5.5, getPosY() + 5, -1);
            FontRenderers.monsterrat.drawGradientString(context.getMatrices(), "recode", getPosX() + 35.5f, getPosY() + 21f, 1);
            setBounds(getPosX(), getPosY(), 106, 30);
        } else if (mode.getValue() == Mode.Small) {
            if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
                float offset1 = FontRenderers.sf_bold.getStringWidth(username) + 72;
                float offset2 = FontRenderers.sf_bold.getStringWidth((mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address));

                Render2DEngine.drawRoundedBlur(context.getMatrices(), getPosX(), getPosY(), 50f, 15f, 3, HudEditor.blurColor.getValue().getColorObject());
                Render2DEngine.drawRoundedBlur(context.getMatrices(), getPosX() + 55, getPosY(), offset1 + offset2 - 36, 15f, 3,  HudEditor.blurColor.getValue().getColorObject());

                Render2DEngine.setupRender();

                Render2DEngine.drawRect(context.getMatrices(), getPosX() + 13, getPosY() + 1.5f, 0.5f, 11, new Color(0x44FFFFFF, true));

                FontRenderers.sf_bold.drawGradientString(context.getMatrices(), "Recode", getPosX() + 18, getPosY() + 5, 20);
                RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
                RenderSystem.setShaderTexture(0, logo);
                Render2DEngine.renderGradientTexture(context.getMatrices(), getPosX() + 1, getPosY() + 2, 11, 11, 0, 0, 128, 128, 128, 128,
                        HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));

                RenderSystem.setShaderTexture(0, player);
                Render2DEngine.renderGradientTexture(context.getMatrices(), getPosX() + 58, getPosY() + 3, 8, 8, 0, 0, 128, 128, 128, 128,
                        HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));

                RenderSystem.setShaderTexture(0, server);
                Render2DEngine.renderGradientTexture(context.getMatrices(), getPosX() + offset1, getPosY() + 2, 10, 10, 0, 0, 128, 128, 128, 128,
                        HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
                Render2DEngine.endRender();

                Render2DEngine.setupRender();
                RenderSystem.defaultBlendFunc();
                FontRenderers.sf_bold.drawString(context.getMatrices(), username, getPosX() + 68, getPosY() + 4.5f, HudEditor.textColor.getValue().getColor());
                FontRenderers.sf_bold.drawString(context.getMatrices(), (mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address), getPosX() + offset1 + 13, getPosY() + 4.5f, HudEditor.textColor.getValue().getColor());
                Render2DEngine.endRender();
                setBounds(getPosX(), getPosY(), 100, 15f);
            } else {
                String info = Formatting.DARK_GRAY + "| " + Formatting.RESET + username + Formatting.DARK_GRAY + " | " + Formatting.RESET + getPing() + " ms" + Formatting.DARK_GRAY + " | " + Formatting.RESET + (mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address);
                float width = FontRenderers.sf_bold.getStringWidth("ThunderHack " + info) + 5;
                Render2DEngine.drawHudBase(context.getMatrices(), getPosX(), getPosY(), width, 10, 3);
                FontRenderers.sf_bold.drawGradientString(context.getMatrices(), ru.getValue() ? textUtil + " " : "ThunderHack ", getPosX() + 2, getPosY() + 2.5f, 10);
                FontRenderers.sf_bold.drawString(context.getMatrices(), info, getPosX() + 2 + FontRenderers.sf_bold.getStringWidth("ThunderHack "), getPosY() + 2.5f, HudEditor.textColor.getValue().getColor());
                setBounds(getPosX(), getPosY(), width, 10);
            }

        }else if (mode.getValue() == Mode.BaltikaClient) {
            Render2DEngine.drawHudBase(context.getMatrices(), getPosX(), getPosY(), 100, 64, HudEditor.hudRound.getValue());
            context.drawTexture(baltika,  (int)getPosX(), (int)getPosY() + 2, 1, 1, 64, 64, 65, 64);
            FontRenderers.thglitch.drawString(context.getMatrices(), "    BALTIKA", getPosX() + 23, getPosY() + 41.5, -1);
            setBounds(getPosX(), getPosY(), 106, 30);
        } else if (mode.is(Mode.Rifk)){
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            //лень вставлять реал билд дату
            String info = Formatting.GREEN + String.format("th7 | build: 16/06/2024 | rate: %d | %s", Math.round(ThunderHack.serverManager.getTPS()), format.format(date));;
            float width = FontRenderers.profont.getStringWidth(info) + 5;
            Render2DEngine.drawRectWithOutline(context.getMatrices(), getPosX(), getPosY(), width, 8, Color.decode("#192A1A"), Color.decode("#833B7B"));
            Render2DEngine.drawGradientBlurredShadow1(context.getMatrices(), getPosX(), getPosY(), width, 8, 10, Color.decode("#161A1E"), Color.decode("#161A1E"), Color.decode("#382E37"), Color.decode("#382E37"));
            FontRenderers.profont.drawString(context.getMatrices(), info, getPosX() + 2.7, getPosY() + 2.953 , HudEditor.textColor.getValue().getColor());
            setBounds(getPosX(), getPosY(), width, 8);
        } else {
            FontRenderers.monsterrat.drawGradientString(context.getMatrices(), "ThunderHack v" + ThunderHack.VERSION, getPosX() + 5.5f, getPosY() + 5, 10);
            setBounds(getPosX(), getPosY(), 100, 3);
        }
    }
    @Override
    public void onUpdate() {
        textUtil.tick();
    }
}