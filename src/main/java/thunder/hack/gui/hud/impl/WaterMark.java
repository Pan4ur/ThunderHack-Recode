package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
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

import static thunder.hack.core.impl.ServerManager.getPing;

public class WaterMark extends HudElement {
    public WaterMark() {
        super("WaterMark", 100, 35);
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.Big);
    private final Setting<Boolean> ru = new Setting<>("RU", false);

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
        Big, Small, Classic
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        String username = ((ModuleManager.media.isEnabled() && Media.nickProtect.getValue()) || ModuleManager.nameProtect.isEnabled()) ? (ModuleManager.nameProtect.isEnabled() ? NameProtect.getCustomName() : "Protected") : mc.getSession().getUsername();

        if (mode.getValue() == Mode.Big) {
            Render2DEngine.drawHudBase(context.getMatrices(), getPosX(), getPosY(), 106, 30, HudEditor.hudRound.getValue());
            FontRenderers.thglitch.drawString(context.getMatrices(), "THUNDERHACK", getPosX() + 5.5, getPosY() + 5, -1);
            FontRenderers.monsterrat.drawGradientString(context.getMatrices(), "recode", getPosX() + 35.5f, getPosY() + 21f, 1);
            setBounds(106, 30);
        } else if (mode.getValue() == Mode.Small) {
            String info = Formatting.DARK_GRAY + "| " + Formatting.RESET + username + Formatting.DARK_GRAY + " | " + Formatting.RESET + getPing() + " ms" + Formatting.DARK_GRAY + " | " + Formatting.RESET + (mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address);
            float width = FontRenderers.sf_bold.getStringWidth("ThunderHack " + info) + 5;
            Render2DEngine.drawHudBase(context.getMatrices(), getPosX(), getPosY(), width, 10, 3);
            FontRenderers.sf_bold.drawGradientString(context.getMatrices(), ru.getValue() ? textUtil + " " : "ThunderHack ", getPosX() + 2, getPosY() + 2.5f, 10);
            FontRenderers.sf_bold.drawString(context.getMatrices(), info, getPosX() + 2 + FontRenderers.sf_bold.getStringWidth("ThunderHack "), getPosY() + 2.5f, HudEditor.textColor.getValue().getColor());
            setBounds((int) width, 3);
        } else {
            FontRenderers.monsterrat.drawGradientString(context.getMatrices(), "ThunderHack v" + ThunderHack.VERSION, getPosX() + 5.5f, getPosY() + 5, 10);
            setBounds(100, 3);
        }
    }

    @Override
    public void onUpdate() {
        textUtil.tick();
    }
}