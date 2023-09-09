package thunder.hack.modules.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.Identifier;
import thunder.hack.ThunderHack;
import thunder.hack.core.ModuleManager;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.text.DecimalFormat;

public class LagNotifier extends Module {
    private final Setting<Boolean> rubberbandNotify = new Setting<>("rubberband", true);
    private final Setting<Boolean> serverResponseNotify = new Setting<>("serverResponse", true);
    private final Setting<Boolean> tpsNotify = new Setting<>("tps", true);

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

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck()) return;

        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            rubberbandTimer.reset();
        }
        if (e.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            packetTimer.reset();
        }
    }


    public void onRender2D(DrawContext context) {
        Render2DEngine.setupRender();
        RenderSystem.defaultBlendFunc();

        if (!rubberbandTimer.passedMs(5000) && rubberbandNotify.getValue()) {
            DecimalFormat decimalFormat = new DecimalFormat("#.#");

            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                FontRenderers.modules.drawCenteredString(context.getMatrices(), "Обнаружен руббербенд! " + decimalFormat.format((5000f - (float) rubberbandTimer.getTimeMs()) / 1000f), (float) mc.getWindow().getScaledWidth() / 2f, (float) mc.getWindow().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());
            } else {
                FontRenderers.modules.drawCenteredString(context.getMatrices(), "Rubberband detected! " + decimalFormat.format((5000f - (float) rubberbandTimer.getTimeMs()) / 1000f), (float) mc.getWindow().getScaledWidth() / 2f, (float) mc.getWindow().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());
            }
        }

        if (packetTimer.passedMs(5000) && serverResponseNotify.getValue()) {
            DecimalFormat decimalFormat = new DecimalFormat("#.#");

            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                FontRenderers.modules.drawCenteredString(context.getMatrices(), "Сервер перестал отвечать! " + decimalFormat.format((float) packetTimer.getTimeMs() / 1000f), (float) mc.getWindow().getScaledWidth() / 2f, (float) mc.getWindow().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());
            } else {
                FontRenderers.modules.drawCenteredString(context.getMatrices(), "The server stopped responding! " + decimalFormat.format((float) packetTimer.getTimeMs() / 1000f), (float) mc.getWindow().getScaledWidth() / 2f, (float) mc.getWindow().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());
            }

            RenderSystem.setShaderColor(1f, 0.87f, 0f, 1f);
            context.drawTexture(ICON, (int) ((float) mc.getWindow().getScaledWidth() / 2f - 40), (int) ((float) mc.getWindow().getScaledHeight() / 3f - 120), 0, 0, 80, 80, 80, 80);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        if (ThunderHack.serverManager.getTPS() < 10 && notifyTimer.passedMs(60000) && tpsNotify.getValue()) {
            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                ThunderHack.notificationManager.publicity("LagNotifier", "ТПС сервера ниже 10!" + (ModuleManager.tpsSync.isDisabled() ? " Рекомендуется включить TPSSync" : ""), 8, Notification.Type.ERROR);
            } else {
                ThunderHack.notificationManager.publicity("LagNotifier", "Server TPS is below 10!" + (ModuleManager.tpsSync.isDisabled() ? " It is recommended to enable TPSSync" : ""), 8, Notification.Type.ERROR);
            }

            isLag = true;
            notifyTimer.reset();
        }

        if (ThunderHack.serverManager.getTPS() > 15 && isLag) {
            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                ThunderHack.notificationManager.publicity("LagNotifier", "ТПС сервера стабилизировался!", 8, Notification.Type.SUCCESS);
            } else {
                ThunderHack.notificationManager.publicity("LagNotifier", "Server TPS has stabilized!", 8, Notification.Type.SUCCESS);
            }

            isLag = false;
        }

        Render2DEngine.endRender();
    }
}
