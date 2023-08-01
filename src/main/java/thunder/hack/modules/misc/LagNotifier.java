package thunder.hack.modules.misc;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.Identifier;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.notification.Notification;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.text.DecimalFormat;

public class LagNotifier extends Module {
    private final Identifier ICON = new Identifier("textures/lagg.png");

    private Timer notifyTimer = new Timer();
    private Timer rubberbandTimer = new Timer();
    private Timer packetTimer = new Timer();

    private boolean isLag = false;

    public LagNotifier() {
        super("LagNotifier", "Предупреждает о лагах на сервере", Category.MISC);
    }

    @Override
    public void onEnable() {
        notifyTimer = new Timer();
        rubberbandTimer = new Timer();
        packetTimer = new Timer();
        isLag = false;

        super.onEnable();
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck()) return;

        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            rubberbandTimer.reset();
        }
        if (e.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            packetTimer.reset();
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    private void onRender(Render2DEvent e) {
        Render2DEngine.setupRender();
        RenderSystem.defaultBlendFunc();

        if (!rubberbandTimer.passedMs(5000)) {
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                FontRenderers.modules.drawCenteredString(e.getMatrixStack(), "Обнаружен руббербенд! " + decimalFormat.format((5000f - (float) rubberbandTimer.getTimeMs()) / 1000f), (float) mc.getWindow().getScaledWidth() / 2f, (float) mc.getWindow().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());
            } else {
                FontRenderers.modules.drawCenteredString(e.getMatrixStack(), "Rubberband detected! " + decimalFormat.format((5000f - (float) rubberbandTimer.getTimeMs()) / 1000f), (float) mc.getWindow().getScaledWidth() / 2f, (float) mc.getWindow().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());
            }
        }

        if (packetTimer.passedMs(5000)) {
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                FontRenderers.modules.drawCenteredString(e.getMatrixStack(), "Сервер перестал отвечать! " + decimalFormat.format((float) packetTimer.getTimeMs() / 1000f), (float) mc.getWindow().getScaledWidth() / 2f, (float) mc.getWindow().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());
            } else {
                FontRenderers.modules.drawCenteredString(e.getMatrixStack(), "The server stopped responding! " + decimalFormat.format((float) packetTimer.getTimeMs() / 1000f), (float) mc.getWindow().getScaledWidth() / 2f, (float) mc.getWindow().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());
            }
            RenderSystem.setShaderColor(1f, 0.87f, 0f, 1f);
            e.getContext().drawTexture(ICON, (int) ((float) mc.getWindow().getScaledWidth() / 2f - 40), (int) ((float) mc.getWindow().getScaledHeight() / 3f - 120), 0, 0, 80, 80, 80, 80);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        if (Thunderhack.serverManager.getTPS() < 10 && notifyTimer.passedMs(60000) && Thunderhack.moduleManager.get("TpsSync").isDisabled()) {
            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                Thunderhack.notificationManager.publicity("LagNotifier", "ТПС сервера ниже 10! Рекомендуется включить TPSSync", 8, Notification.Type.ERROR);
            } else {
                Thunderhack.notificationManager.publicity("LagNotifier", "Server TPS is below 10! It is recommended to enable TPSSync", 8, Notification.Type.ERROR);
            }
            isLag = true;
            notifyTimer.reset();
        }

        if (Thunderhack.serverManager.getTPS() > 15 && isLag) {
            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                Thunderhack.notificationManager.publicity("LagNotifier", "ТПС сервера стабилизировался!", 8, Notification.Type.SUCCESS);
            } else {
                Thunderhack.notificationManager.publicity("LagNotifier", "Server TPS has stabilized!", 8, Notification.Type.SUCCESS);
            }
            isLag = false;
        }

        Render2DEngine.endRender();
    }
}
