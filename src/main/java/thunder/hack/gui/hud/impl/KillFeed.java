package thunder.hack.gui.hud.impl;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.combat.Aura;
import thunder.hack.modules.combat.AutoCrystal;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;

import java.util.ArrayList;
import java.util.List;

public class KillFeed extends HudElement {

    public KillFeed() {
        super("KillFeed", 50, 50);
    }

    private Setting<Boolean> resetOnDeath = new Setting<>("ResetOnDeath", true);

    private final List<String> players = new ArrayList<>();

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        float scale_x = 50;
        for (String player : players) {
            if (FontRenderers.modules.getStringWidth(player) > scale_x)
                scale_x = FontRenderers.modules.getStringWidth(player);
        }

        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "KillFeed", getPosX() + scale_x / 2 + 10, getPosY() + 2, -1);
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2, getPosY() + 13.7f, getPosX() + 2 + scale_x / 2f - 2 + 10, getPosY() + 14, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2 + scale_x / 2f - 2 + 10, getPosY() + 13.7f, getPosX() + 2 + scale_x - 4 + 20, getPosY() + 14, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));

        int y_offset = 6;
        for (String player : players) {
            FontRenderers.modules.drawString(context.getMatrices(), player, getPosX() + 5, getPosY() + 18 + y_offset, -1, false);
            y_offset += 10;
        }
    }

    public void onRenderShaders(DrawContext context) {
        int y_offset1 = 6;
        float scale_x = 50;

        for (String player : players) {
            if (FontRenderers.modules.getStringWidth(player) > scale_x)
                scale_x = FontRenderers.modules.getStringWidth(player);
            y_offset1 += 10;
        }

        Render2DEngine.drawGradientGlow(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX(), getPosY(), scale_x + 20, 20 + y_offset1, HudEditor.hudRound.getValue(), 10);
        Render2DEngine.drawGradientRoundShader(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX() - 0.5f, getPosY() - 0.5f, scale_x + 20 + 1, 21 + y_offset1, HudEditor.hudRound.getValue());
        Render2DEngine.drawRoundShader(context.getMatrices(), getPosX(), getPosY(), scale_x + 20, 20 + y_offset1, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());
        setBounds((int) (scale_x + 20), 20 + y_offset1);
    }

    @EventHandler
    public void onPacket(PacketEvent.@NotNull Receive e) {
        if (!(e.getPacket() instanceof EntityStatusS2CPacket pac)) return;
        if (pac.getStatus() == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES && pac.getEntity(mc.world) instanceof PlayerEntity pl) {
            if (Aura.target != null && Aura.target == pac.getEntity(mc.world)) {
                players.add(Formatting.RED +  "EZ -" + Formatting.RESET + pl.getName().getString());
                return;
            }
            if (AutoCrystal.target != null && AutoCrystal.target == pac.getEntity(mc.world)) {
                players.add(Formatting.RED +  "EZ -" + Formatting.RESET + pl.getName().getString());
            }
            if(pl == mc.player && resetOnDeath.getValue())
                players.clear();
        }
    }

    @Override
    public void onDisable() {
        players.clear();
    }
}
