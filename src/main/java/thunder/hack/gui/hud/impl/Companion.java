package thunder.hack.gui.hud.impl;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.combat.AntiBot;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

import static thunder.hack.modules.client.ClientSettings.isRu;

public class Companion extends HudElement {

    public Companion() {
        super("2D Companion", 50, 10);
    }

    private final Identifier BOYKISSER = new Identifier("thunderhack", "textures/hud/elements/boykisser.png");
    private final Identifier PAIMON = new Identifier("thunderhack", "textures/hud/elements/paimon.png");
    private final Identifier BALTIKA = new Identifier("thunderhack", "textures/hud/elements/baltika.png");
    private final Identifier KOWK = new Identifier("thunderhack", "textures/hud/elements/kowk.png"); // kowk!!!1

    public Setting<Integer> scale = new Setting<>("Scale", 50, 0, 100);
    public Setting<Mode> mode = new Setting<>("Mode", Mode.Boykisser);

    public static int currentFrame;
    private String message = "";
    private Timer lastPop = new Timer();
    private Timer frameRate = new Timer();

    @Override
    public void onUpdate() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || AntiBot.bots.contains(player) || player.getHealth() > 0 || !ThunderHack.combatManager.popList.containsKey(player.getName().getString()))
                continue;

            if (isRu())
                message = player.getName().getString() + " попнул " + (ThunderHack.combatManager.popList.get(player.getName().getString()) > 1 ? ThunderHack.combatManager.popList.get(player.getName().getString()) + "" + " тотемов и сдох!" : "тотем и сдох!");
            else
                message = player.getName().getString() + " popped " + (ThunderHack.combatManager.popList.get(player.getName().getString()) > 1 ? ThunderHack.combatManager.popList.get(player.getName().getString()) + "" + " totems and died EZ LMAO!" : "totem and died EZ LMAO!");
            lastPop.reset();

        }
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        context.getMatrices().push();
        context.getMatrices().translate((int) getPosX() + 100, (int) getPosY() + 100, 0);
        context.getMatrices().scale((float) scale.getValue() / 100f, (float) scale.getValue() / 100f, 1);
        context.getMatrices().translate(-((int) getPosX() + 100), -((int) getPosY() + 100), 0);
        if(mode.getValue() == Mode.Boykisser)
            context.drawTexture(BOYKISSER, (int) getPosX(), (int) getPosY(), 0, currentFrame * 128, 130, 128, 130, 6784);
        else if(mode.getValue() == Mode.Paimon)
            context.drawTexture(PAIMON, (int) getPosX(), (int) getPosY(), 0, currentFrame * 200, 200, 200, 200, 10600);
        else if(mode.getValue() == Mode.Baltika)
            context.drawTexture(BALTIKA, (int) getPosX(), (int) getPosY(), 0, 0, 421, 800, 421, 800);
        else if(mode.getValue() == Mode.Kowk)
            context.drawTexture(KOWK, (int) getPosX(), (int) getPosY(), 0, 0, 287, 252, 287, 252);
        context.getMatrices().pop();

        if (!lastPop.passedMs(2000)) {
            float w = FontRenderers.sf_bold.getStringWidth(message) + 8;
            float factor = MathUtility.clamp(lastPop.getPassedTimeMs(), 0, 500) / 500f;
            Render2DEngine.drawRound(context.getMatrices(), getPosX() + scale.getValue() / 3f, getPosY() + 70 - scale.getValue(), factor * w, 10, 3, new Color(0xFCD7DD));

            Render2DEngine.addWindow(context.getMatrices(), getPosX() + scale.getValue() / 3f, getPosY()  + 72 - scale.getValue(), factor * w + getPosX() + scale.getValue() / 3f, 20 + getPosY()  + 72 - scale.getValue(), 1f);
            FontRenderers.sf_bold.drawString(context.getMatrices(), message, getPosX() + 2 + scale.getValue() / 3f, getPosY() + 72 - scale.getValue(), new Color(0x484848).getRGB());
            Render2DEngine.popWindow();
        }

        if (frameRate.passedMs(64)) {
            frameRate.reset();
            currentFrame++;
            if (currentFrame > 52)
                currentFrame = 0;
        }
		
        if(mode.getValue() == Mode.Baltika)
            setBounds(getPosX() + 100, getPosY() + 100, (scale.getValue() * 3f), (scale.getValue() * 3f));
        else
            setBounds(getPosX(), getPosY(), (scale.getValue() * 3f), (scale.getValue() * 3f));
    }


    @EventHandler
    public void onTotemPop(@NotNull TotemPopEvent event) {
        if (event.getEntity() == mc.player) return;

        if (isRu())
            message = event.getEntity().getName().getString() + " попнул " + (event.getPops() > 1 ? event.getPops() + "" + " тотемов!" : "тотем!");
        else
            message = event.getEntity().getName().getString() + " popped " + (event.getPops() > 1 ? event.getPops() + "" + " totems!" : " a totem!");
        lastPop.reset();
    }
    private enum Mode {
        Boykisser, Paimon, Baltika, Kowk
    }
}
