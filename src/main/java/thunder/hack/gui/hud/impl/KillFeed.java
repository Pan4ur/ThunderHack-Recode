package thunder.hack.gui.hud.impl;

import com.google.common.collect.Lists;
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
import thunder.hack.utility.render.animation.AnimationUtility;

import java.util.ArrayList;
import java.util.List;

public class KillFeed extends HudElement {

    public KillFeed() {
        super("KillFeed", 50, 50);
    }

    private Setting<Boolean> resetOnDeath = new Setting<>("ResetOnDeath", true);

    private final List<String> players = new ArrayList<>();

    private float vAnimation, hAnimation;

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "KillFeed", getPosX() + hAnimation / 2, getPosY() + 2, -1);
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2, getPosY() + 13.7f, getPosX() + 2 + hAnimation / 2f - 2, getPosY() + 14, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2 + hAnimation / 2f - 2, getPosY() + 13.7f, getPosX() + 2 + hAnimation - 4, getPosY() + 14, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));

        Render2DEngine.addWindow(context.getMatrices(), getPosX(), getPosY(), getPosX() + hAnimation, getPosY() + vAnimation, 1f);
        int y_offset = 3;
        for (String player : Lists.newArrayList(players)) {
            FontRenderers.modules.drawString(context.getMatrices(), player, getPosX() + 5, getPosY() + 18 + y_offset, -1, false);
            y_offset += 10;
        }
        Render2DEngine.popWindow();
    }

    public void onRenderShaders(DrawContext context) {
        int y_offset1 = 3;
        float scale_x = 20;

        for (String player : Lists.newArrayList(players)) {
            if (FontRenderers.modules.getStringWidth(player) > scale_x)
                scale_x = FontRenderers.modules.getStringWidth(player);
            y_offset1 += 10;
        }

        vAnimation = AnimationUtility.fast(vAnimation, 20 + y_offset1, 15);
        hAnimation = AnimationUtility.fast(hAnimation, scale_x + 20, 15);

        Render2DEngine.drawHudBase(context.getMatrices(), getPosX(), getPosY(), hAnimation, vAnimation, HudEditor.hudRound.getValue());
        setBounds((int) (scale_x + 20), 20 + y_offset1);
    }

    @EventHandler
    public void onPacket(PacketEvent.@NotNull Receive e) {
        if (!(e.getPacket() instanceof EntityStatusS2CPacket pac)) return;
        if (pac.getStatus() == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES && pac.getEntity(mc.world) instanceof PlayerEntity pl) {
            if (Aura.target != null && Aura.target == pac.getEntity(mc.world)) {
                players.add(Formatting.RED +  "EZ - " + Formatting.RESET + pl.getName().getString());
                return;
            }
            if (AutoCrystal.target != null && AutoCrystal.target == pac.getEntity(mc.world)) {
                players.add(Formatting.RED +  "EZ - " + Formatting.RESET + pl.getName().getString());
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
