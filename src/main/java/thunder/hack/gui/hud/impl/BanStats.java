package thunder.hack.gui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Formatting;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.gui.font.FontRenderer;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class BanStats extends HudElement {
    public BanStats() {
        super("BanStats", 60, 25);
    }
    private int banCount = 0;
    private int lastBans = 0;
    private final Timer banTimer = new Timer();
    private int color;
    private final Setting<ColorSetting> colorSetting = new Setting<>("Color", new ColorSetting(new Color(0x0077FF)));
    public final Setting<Integer> minutes = new Setting<>("Minutes", 15, 1, 15);
    public final Setting<Integer> x = new Setting<>("x", 100, 0, 800);
    public final Setting<Integer> y = new Setting<>("y", 100, 0, 500);

    public void onRender2D(DrawContext context) {
        color = colorSetting.getValue().getColor();
        if(banTimer.passedMs(minutes.getValue() * 60 * 1000)){
            lastBans = banCount;
            banTimer.reset();
            banCount = 0;
        }
        context.drawText(mc.textRenderer, "Банов за последнии " + minutes.getValue() + " минут: " + lastBans, x.getValue(), y.getValue(), color, true);
    }
    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof GameMessageS2CPacket pac) {
            String content = pac.content().getString().toLowerCase();
            if (content.contains("забанил") || content.contains("banned") || content.contains("забанен") || content.contains("временно заблокировал игрока")) {
                banCount += 1;
            }
        }
    }
}