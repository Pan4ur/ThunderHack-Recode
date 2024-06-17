package thunder.hack.gui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.combat.Aura;
import thunder.hack.modules.combat.AutoCrystal;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;


public class KillStats extends HudElement {
    int death = 0,killstreak = 0,kills = 0;
    private Identifier icon = new Identifier("thunderhack", "textures/hud/icons/sword.png");
    public KillStats(){
        super("KillStats",100,35);
    }

    @Override
    public void onDisable() {
        death = 0;
        kills = 0;
        killstreak = 0;
    }

    @EventHandler
    private void death(PacketEvent.Receive event){
        if(event.getPacket() instanceof EntityStatusS2CPacket pac && pac.getStatus() == 3){
            if(!(pac.getEntity(mc.world) instanceof PlayerEntity)) return;
            if(pac.getEntity(mc.world) == mc.player){
                death++;
                killstreak = 0;
            }
            else if(Aura.target == pac.getEntity(mc.world) || AutoCrystal.target == pac.getEntity(mc.world)){
                killstreak++;
                kills++;
            }
        }
    }
    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        String streak = "KillStreak: " + Formatting.WHITE + killstreak;
        String kd = " KD: " + Formatting.WHITE + MathUtility.round((float) kills / (death > 0 ? death : 1));
        float pX = getPosX() > mc.getWindow().getScaledWidth() / 2f ? getPosX() - FontRenderers.getModulesRenderer().getStringWidth(streak) - FontRenderers.getModulesRenderer().getStringWidth(kd) : getPosX();

        if(HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            Render2DEngine.drawRoundedBlur(context.getMatrices(), pX, getPosY(), FontRenderers.getModulesRenderer().getStringWidth(streak) + FontRenderers.getModulesRenderer().getStringWidth(kd) + 21, 13f, 3, HudEditor.blurColor.getValue().getColorObject());
            Render2DEngine.drawRect(context.getMatrices(), pX + 14, getPosY() + 2, 0.5f, 8, new Color(0x44FFFFFF, true));
            Render2DEngine.setupRender();
            RenderSystem.setShaderTexture(0, icon);
            Render2DEngine.renderGradientTexture(context.getMatrices(), pX + 2, getPosY() + 2, 10, 10, 0, 0, 16, 16, 16, 16,
                    HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
            Render2DEngine.endRender();
        }

        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), streak, pX + 18, getPosY() + 5, HudEditor.getColor(1).getRGB());
        FontRenderers.getModulesRenderer().drawString(context.getMatrices(),kd,pX + 18 + FontRenderers.getModulesRenderer().getStringWidth(streak),getPosY() + 5,HudEditor.getColor(1).getRGB());
        setBounds(pX, getPosY(), FontRenderers.getModulesRenderer().getStringWidth(streak) + FontRenderers.getModulesRenderer().getStringWidth(kd) + 21, 13f);
    }
}
