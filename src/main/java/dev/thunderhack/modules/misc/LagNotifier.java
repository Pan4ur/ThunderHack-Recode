package dev.thunderhack.modules.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.modules.Module;
import dev.thunderhack.modules.client.MainSettings;
import dev.thunderhack.notification.Notification;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.Timer;
import dev.thunderhack.utils.render.Render2DEngine;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.Identifier;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.core.ModuleManager;
import dev.thunderhack.gui.font.FontRenderers;

import java.awt.*;
import java.text.DecimalFormat;

public class LagNotifier extends Module {
    private final Setting<Boolean> rubberbandNotify = new Setting<>("Rubberband", true);
    private final Setting<Boolean> serverResponseNotify = new Setting<>("ServerResponse", true);
    private final Setting<Boolean> tpsNotify = new Setting<>("TPS", true);

    private final Identifier ICON = new Identifier("textures/lagg.png");

    private Timer notifyTimer = new Timer();
    private Timer rubberbandTimer = new Timer();
    private Timer packetTimer = new Timer();

    private boolean isLagging = false;

    public LagNotifier() {
        super("LagNotifier", Category.MISC);
    }

    @Override
    public void onEnable() {
        notifyTimer = new Timer();
        rubberbandTimer = new Timer();
        packetTimer = new Timer();
        isLagging = false;

        super.onEnable();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck()) return;

        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) rubberbandTimer.reset();
        if (e.getPacket() instanceof WorldTimeUpdateS2CPacket) packetTimer.reset();
    }

    public void onRender2D(DrawContext context) {
        Render2DEngine.setupRender();
        RenderSystem.defaultBlendFunc();

        if (!rubberbandTimer.passedMs(5000) && rubberbandNotify.getValue()) {
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            FontRenderers.modules.drawCenteredString(context.getMatrices(), MainSettings.isRu() ? "Обнаружен руббербенд! " : "Rubberband detected! " + decimalFormat.format((5000f - (float) rubberbandTimer.getTimeMs()) / 1000f), (float) mc.getWindow().getScaledWidth() / 2f, (float) mc.getWindow().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());
        }

        if (packetTimer.passedMs(5000) && serverResponseNotify.getValue()) {
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            FontRenderers.modules.drawCenteredString(context.getMatrices(), MainSettings.isRu() ? "Сервер перестал отвечать! " : "The server stopped responding! " + decimalFormat.format((float) packetTimer.getTimeMs() / 1000f), (float) mc.getWindow().getScaledWidth() / 2f, (float) mc.getWindow().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());

            RenderSystem.setShaderColor(1f, 0.87f, 0f, 1f);
            context.drawTexture(ICON, (int) ((float) mc.getWindow().getScaledWidth() / 2f - 40), (int) ((float) mc.getWindow().getScaledHeight() / 3f - 120), 0, 0, 80, 80, 80, 80);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        if (ThunderHack.serverManager.getTPS() < 10 && notifyTimer.passedMs(60000) && tpsNotify.getValue()) {
            String msg = MainSettings.isRu() ? "ТПС сервера ниже 10!" : "Server TPS is below 10!";
            if (ModuleManager.tpsSync.isDisabled()) msg += MainSettings.isRu() ? " Рекомендуется включить TPSSync" : "It is recommended to enable TPSSync";
            ThunderHack.notificationManager.publicity("LagNotifier", msg, 8, Notification.Type.ERROR);

            isLagging = true;
            notifyTimer.reset();
        }

        if (ThunderHack.serverManager.getTPS() > 15 && isLagging) {
            ThunderHack.notificationManager.publicity("LagNotifier", MainSettings.isRu() ? "ТПС сервера стабилизировался!" : "Server TPS has stabilized!", 8, Notification.Type.SUCCESS);
            isLagging = false;
        }
        Render2DEngine.endRender();
    }
}