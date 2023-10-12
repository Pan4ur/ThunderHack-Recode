package dev.thunderhack.modules.movement;

import dev.thunderhack.event.events.EventTick;
import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.ColorSetting;
import dev.thunderhack.utils.player.PlayerEntityCopy;
import dev.thunderhack.utils.render.Render3DEngine;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
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
    private final Setting<Boolean> strict = new Setting<>("Strict", false);
    private final Setting<Float> factor = new Setting<>("Factor", 1F, 0.01F, 10F);
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
        if (sending.get()) return;
        if (pulse.getValue()) {
            if (packet instanceof PlayerMoveC2SPacket) {
                if (strict.getValue() && !((PlayerMoveC2SPacket) packet).isOnGround()) {
                    sendPackets();
                } else {
                    event.cancel();
                    storedPackets.add(packet);
                }
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
            if (storedPackets.size() >= factor.getValue() * 10F) {
                sendPackets();
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

