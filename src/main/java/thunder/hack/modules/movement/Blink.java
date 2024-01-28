package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import thunder.hack.events.impl.EventTick;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.PlayerEntityCopy;
import thunder.hack.utility.render.Render3DEngine;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Blink extends Module {
    private final Setting<Boolean> pulse = new Setting<>("Pulse", false);
    private final Setting<Boolean> autoDisable = new Setting<>("AutoDisable", false);
    private final Setting<Integer> disablePackets = new Setting<>("DisablePackets", 17, 1, 1000, v-> autoDisable.getValue() );
    private final Setting<Integer> pulsePackets = new Setting<>("PulsePackets", 20, 1, 1000, v-> pulse.getValue());
    private final Setting<Boolean> render = new Setting<>("Render", true);
    private final Setting<RenderMode> renderMode = new Setting<>("Render Mode", RenderMode.Circle, value -> render.getValue());
    private final Setting<ColorSetting> circleColor = new Setting<>("Color", new ColorSetting(0xFFda6464, true), value -> render.getValue() && renderMode.getValue() == RenderMode.Circle || renderMode.getValue() == RenderMode.Both);

    private enum RenderMode {
        Circle,
        Model,
        Both
    }

    private PlayerEntityCopy blinkPlayer;
    private Vec3d lastPos = new Vec3d(0, 0, 0);
    private final Queue<Packet<?>> storedPackets = new LinkedList<>();
    private final AtomicBoolean sending = new AtomicBoolean(false);

    private Packet<?> lastPacket;

    public Blink() {
        super("Blink", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null
                || mc.world == null
                || mc.isIntegratedServerRunning()
                || mc.getNetworkHandler() == null) {
            disable();
            return;
        }

        lastPos = mc.player.getPos();
        mc.world.spawnEntity(new ClientPlayerEntity(mc, mc.world, mc.getNetworkHandler(), mc.player.getStatHandler(), mc.player.getRecipeBook(), mc.player.lastSprinting, mc.player.isSneaking()));
        sending.set(false);
        storedPackets.clear();
    }

    @Override
    public void onDisable() {
        if (mc.world == null || mc.player == null) return;
        while (!storedPackets.isEmpty()) sendPacket(storedPackets.poll());

        if (blinkPlayer != null) blinkPlayer.deSpawn();
        blinkPlayer = null;
    }

    @Override
    public String getDisplayInfo() {
        return Integer.toString(storedPackets.size());
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (fullNullCheck()) return;

        Packet<?> packet = event.getPacket();

        if (sending.get()) {
            return;
        }

        if (pulse.getValue()) {
            if (packet instanceof PlayerMoveC2SPacket) {
                event.cancel();
                storedPackets.add(packet);
            }
        } else if (!(packet instanceof ChatMessageC2SPacket || packet instanceof TeleportConfirmC2SPacket || packet instanceof KeepAliveC2SPacket || packet instanceof AdvancementTabC2SPacket || packet instanceof ClientStatusC2SPacket)) {
            event.cancel();
            storedPackets.add(packet);
        }
    }

    @EventHandler
    public void onUpdate(EventTick event) {
        if (fullNullCheck()) return;

        if (pulse.getValue()) {
            if (storedPackets.size() >= pulsePackets.getValue()) {
                sendPackets();
            }
        }

        if(autoDisable.getValue()) {
            if (storedPackets.size() >= disablePackets.getValue()) {
                disable();
            }
        }
    }

    private void sendPackets() {
        if (mc.player == null) return;
        sending.set(true);

        while (!storedPackets.isEmpty()) {
            Packet<?> packet = storedPackets.poll();
            sendPacket(packet);
            if (packet instanceof PlayerMoveC2SPacket && !(packet instanceof PlayerMoveC2SPacket.LookAndOnGround)) {
                lastPos = new Vec3d(((PlayerMoveC2SPacket) packet).getX(mc.player.getX()), ((PlayerMoveC2SPacket) packet).getY(mc.player.getY()), ((PlayerMoveC2SPacket) packet).getZ(mc.player.getZ()));

                if (renderMode.getValue() == RenderMode.Model || renderMode.getValue() == RenderMode.Both) {
                    blinkPlayer.deSpawn();
                    blinkPlayer = new PlayerEntityCopy();
                    blinkPlayer.spawn();
                }
            }
        }

        sending.set(false);
        storedPackets.clear();
    }

    public void onRender3D(MatrixStack stack) {
        if (mc.player == null || mc.world == null) return;
        if (render.getValue() && lastPos != null) {
            if (renderMode.getValue() == RenderMode.Circle || renderMode.getValue() == RenderMode.Both) {
                float[] hsb = Color.RGBtoHSB(circleColor.getValue().getRed(), circleColor.getValue().getGreen(), circleColor.getValue().getBlue(), null);
                float hue = (float) (System.currentTimeMillis() % 7200L) / 7200F;
                int rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
                ArrayList<Vec3d> vecs = new ArrayList<>();
                double x = lastPos.x;
                double y = lastPos.y;
                double z = lastPos.z;

                for (int i = 0; i <= 360; ++i) {
                    Vec3d vec = new Vec3d(x + Math.sin((double) i * Math.PI / 180.0) * 0.5D, y + 0.01, z + Math.cos((double) i * Math.PI / 180.0) * 0.5D);
                    vecs.add(vec);
                }

                for (int j = 0; j < vecs.size() - 1; ++j) {
                    Render3DEngine.drawLine(vecs.get(j).x, vecs.get(j).y, vecs.get(j).z, vecs.get(j + 1).x, vecs.get(j + 1).y, vecs.get(j + 1).z, new Color(rgb), 2f);
                    hue += (1F / 360F);
                    rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
                }
            }
            if (renderMode.getValue() == RenderMode.Model || renderMode.getValue() == RenderMode.Both) {
                if (blinkPlayer == null) {
                    blinkPlayer = new PlayerEntityCopy();
                    blinkPlayer.spawn();
                }
            }
        }
    }
}

