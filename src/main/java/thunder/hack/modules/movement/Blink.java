package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.events.impl.EventTick;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.Timer;
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
    public Blink() {
        super("Blink", Category.MOVEMENT);
    }


    private Setting<Boolean> pulse = new Setting<>("Pulse", false);
    private Setting<Boolean> strict = new Setting<>("Strict", false);
    private Setting<Float> factor = new Setting<>("Factor", 1F, 0.01F, 10F);
    private  Setting<Boolean> render = new Setting<>("Render", true);
    public  Setting<ColorSetting> circleColor = new Setting<>("Color", new ColorSetting(0xFFda6464, true));



    private Queue<Packet> storedPackets = new LinkedList<>();

    private Vec3d lastPos = new Vec3d(0, 0, 0);

    private AtomicBoolean sending = new AtomicBoolean(false);

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        if(fullNullCheck()){
            return;
        }
        Packet packet = event.getPacket();
        if (sending.get()) return;
        if (pulse.getValue()) {
            if (packet instanceof PlayerMoveC2SPacket) {
                if (strict.getValue() && !((PlayerMoveC2SPacket) packet).isOnGround()) {
                    sending.set(true);
                    while(!storedPackets.isEmpty()) {
                        Packet pckt = storedPackets.poll();
                        mc.player.networkHandler.sendPacket(pckt);
                        if (pckt instanceof PlayerMoveC2SPacket && !(pckt instanceof PlayerMoveC2SPacket.LookAndOnGround)) {
                            lastPos = new Vec3d(((PlayerMoveC2SPacket) pckt).getX(mc.player.getX()), ((PlayerMoveC2SPacket) pckt).getY(mc.player.getY()), ((PlayerMoveC2SPacket) pckt).getZ(mc.player.getZ()));
                        }
                    }
                    sending.set(false);
                    storedPackets.clear();
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
        if(fullNullCheck()){
            return;
        }
        if (pulse.getValue()) {
            if (storedPackets.size() >= factor.getValue() * 10F) {
                sending.set(true);
                while(!storedPackets.isEmpty()) {
                    Packet pckt = storedPackets.poll();
                    mc.player.networkHandler.sendPacket(pckt);
                    if (pckt instanceof PlayerMoveC2SPacket && !(pckt instanceof PlayerMoveC2SPacket.LookAndOnGround)) {
                        lastPos = new Vec3d(((PlayerMoveC2SPacket) pckt).getX(mc.player.getX()), ((PlayerMoveC2SPacket) pckt).getY(mc.player.getY()), ((PlayerMoveC2SPacket) pckt).getZ(mc.player.getZ()));
                    }
                }
                sending.set(false);
                storedPackets.clear();
            }
        }
    }

    private Timer pulseTimer = new Timer();



    public void onRender3D(MatrixStack stack) {
        if (mc.player == null || mc.world == null) return;
        if (render.getValue() && lastPos != null) {
            float[] hsb = Color.RGBtoHSB(circleColor.getValue().getRed(), circleColor.getValue().getGreen(), circleColor.getValue().getBlue(), null);
            float initialHue = (float) (System.currentTimeMillis() % 7200L) / 7200F;
            float hue = initialHue;
            int rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
            ArrayList<Vec3d> vecs = new ArrayList<>();
            double x = lastPos.x;
            double y = lastPos.y;
            double z = lastPos.z;
            for (int i = 0; i <= 360; ++i) {
                Vec3d vec = new Vec3d(x + Math.sin((double) i * Math.PI / 180.0) * 0.5D, y + 0.01, z + Math.cos((double) i * Math.PI / 180.0) * 0.5D);
                vecs.add(vec);
            }
         //   ModelRenderer.LINES.lineWidth = 1.5F;
            for (int j = 0; j < vecs.size() - 1; ++j) {
                Render3DEngine.drawLine(vecs.get(j).x, vecs.get(j).y, vecs.get(j).z, vecs.get(j + 1).x, vecs.get(j + 1).y, vecs.get(j + 1).z,new Color(rgb),2f);

                hue += (1F / 360F);
                rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
            }
           // ModelRenderer.LINES.lineWidth = 1F;
        }
    }

    @Override
    public void onDisable() {
        if(mc.world == null || mc.player == null) return;
        while(!storedPackets.isEmpty()) {
            mc.player.networkHandler.sendPacket(storedPackets.poll());
        }
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null || mc.isIntegratedServerRunning()) {
            disable();
            return;
        }
        lastPos = mc.player.getPos();
        sending.set(false);
        storedPackets.clear();
    }

    @Override
    public String getDisplayInfo() {
        return Integer.toString(storedPackets.size());
    }
}
