package thunder.hack.gui.hud.impl;

import com.google.common.eventbus.Subscribe;
import net.minecraft.util.Formatting;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.events.impl.RenderBlurEvent;
import thunder.hack.gui.font.FontRenderer;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.combat.Aura;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;

import java.text.Format;

public class WaterMark extends HudElement {
    public WaterMark() {
        super("WaterMark", "WaterMark", 100,35);
    }


    public static final Setting<Mode> mode = new Setting("Mode", Mode.Big);

    private enum Mode {
        Big,Small,Classic
    }



    @Subscribe
    public void onRender2D(Render2DEvent e) {
        super.onRender2D(e);
        if(mode.getValue() == Mode.Big) {
            FontRenderers.thglitch.drawString(e.getMatrixStack(), "THUNDERHACK", getPosX() + 5.5, getPosY() + 5, -1);
            FontRenderers.monsterrat.drawGradientString(e.getMatrixStack(), "recode", getPosX() + 35.5f, getPosY() + 21f, 1);
        } else if (mode.getValue() == Mode.Small){
            String info = Formatting.GRAY + "| " + Formatting.RESET + mc.getSession().getUsername() + Formatting.GRAY + " | "+ Formatting.RESET + PingHud.getPing() + " ms" + Formatting.GRAY +" | " + Formatting.RESET+ (mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address);
            FontRenderers.sf_bold.drawGradientString(e.getMatrixStack(),"ThunderHack ",getPosX() + 2, getPosY() + 3,10);
            FontRenderers.sf_bold.drawString(e.getMatrixStack(),info,getPosX() + 2 + FontRenderers.sf_bold.getStringWidth("ThunderHack "), getPosY() + 3,HudEditor.textColor.getValue().getColor());
        } else {
            FontRenderers.monsterrat.drawGradientString(e.getMatrixStack(), "ThunderHack v" + Thunderhack.version, getPosX() + 5.5f, getPosY() + 5, 10);
        }
    }

    @Subscribe
    public void onRenderShader(RenderBlurEvent e){
        if(mode.getValue() == Mode.Big) {
          //  Render2DEngine.drawBlurredShadow(e.getMatrixStack(), getPosX(), getPosY(), 103, 30, 8, HudEditor.getColor(270));
            Render2DEngine.drawGradientGlow(e.getMatrixStack(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90),getPosX(),getPosY(),103,30,HudEditor.hudRound.getValue(),10);
            Render2DEngine.drawGradientRoundShader(e.getMatrixStack(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX() - 0.5f, getPosY() - 0.5f, 104, 31, HudEditor.hudRound.getValue());
            Render2DEngine.drawRoundShader(e.getMatrixStack(), getPosX(), getPosY(), 103, 30, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());
        } else if (mode.getValue() == Mode.Small){
            String info = Formatting.GRAY + "| " + Formatting.RESET + mc.getSession().getUsername() + Formatting.GRAY + " | "+ Formatting.RESET + PingHud.getPing() + " ms" + Formatting.GRAY +" | " + Formatting.RESET+ (mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address);
            Render2DEngine.drawGradientGlow(e.getMatrixStack(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90),getPosX(),getPosY(),FontRenderers.sf_bold.getStringWidth("ThunderHack " +info) + 5,10,3,10);
            Render2DEngine.drawGradientRoundShader(e.getMatrixStack(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX() - 1f, getPosY() - 1f,  FontRenderers.sf_bold.getStringWidth("ThunderHack " +info) + 7, 12, 3);
            Render2DEngine.drawRoundShader(e.getMatrixStack(), getPosX() - 0.5f, getPosY() - 0.5f,  FontRenderers.sf_bold.getStringWidth("ThunderHack " +info) + 6, 11, 3, HudEditor.plateColor.getValue().getColorObject());
        }
    }
}
