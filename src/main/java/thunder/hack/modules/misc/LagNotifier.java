package thunder.hack.modules.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.Identifier;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.text.DecimalFormat;

import static thunder.hack.modules.client.ClientSettings.isRu;

public class LagNotifier extends Module {
    private final Setting<Boolean> rubberbandNotify = new Setting<>("Rubberband", true);
    private final Setting<Boolean> serverResponseNotify = new Setting<>("ServerResponse", true);
    private final Setting<Integer> responseTreshold = new Setting<>("ResponseTreshold", 5, 0, 15, v -> serverResponseNotify.getValue());
    private final Setting<Boolean> tpsNotify = new Setting<>("TPS", true);

    private final Identifier ICON = new Identifier("thunderhack", "textures/hud/elements/lag.png");

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

            FontRenderers.modules.drawCenteredString(context.getMatrices(), (isRu() ? "Обнаружен руббербенд! " : "Rubberband detected! ") + decimalFormat.format((5000f - (float) rubberbandTimer.getTimeMs()) / 1000f), (float) mc.getWindow().getScaledWidth() / 2f, (float) mc.getWindow().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());
        }

        if (packetTimer.passedMs(responseTreshold.getValue() * 1000L) && serverResponseNotify.getValue()) {
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            FontRenderers.modules.drawCenteredString(context.getMatrices(), (isRu() ? "Сервер перестал отвечать! " : "The server stopped responding! ") + decimalFormat.format((float) packetTimer.getTimeMs() / 1000f), (float) mc.getWindow().getScaledWidth() / 2f, (float) mc.getWindow().getScaledHeight() / 3f, new Color(0xFFDF00).getRGB());

            RenderSystem.setShaderColor(1f, 0.87f, 0f, 1f);
            context.drawTexture(ICON, (int) ((float) mc.getWindow().getScaledWidth() / 2f - 40), (int) ((float) mc.getWindow().getScaledHeight() / 3f - 120), 0, 0, 80, 80, 80, 80);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        if (ThunderHack.serverManager.getTPS() < 10 && notifyTimer.passedMs(60000) && tpsNotify.getValue()) {
            String msg = isRu() ? "ТПС сервера ниже 10!" : "Server TPS is below 10!";
            if (ModuleManager.tpsSync.isDisabled()) msg += isRu() ? " Рекомендуется включить TPSSync" : "It is recommended to enable TPSSync";
            ThunderHack.notificationManager.publicity("LagNotifier", msg, 8, Notification.Type.ERROR);

            isLagging = true;
            notifyTimer.reset();
        }

        if (ThunderHack.serverManager.getTPS() > 15 && isLagging) {
            ThunderHack.notificationManager.publicity("LagNotifier", isRu() ? "ТПС сервера стабилизировался!" : "Server TPS has stabilized!", 8, Notification.Type.SUCCESS);
            isLagging = false;
        }
        Render2DEngine.endRender();
    }
}
