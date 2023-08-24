package thunder.hack.core;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.impl.RadarRewrite;
import thunder.hack.gui.thundergui.ThunderGui2;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.utility.Macro;
import thunder.hack.utility.Timer;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import thunder.hack.events.impl.*;
import thunder.hack.utility.player.InteractionUtility;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static thunder.hack.modules.Module.mc;

public class Core {

    Timer lastPacket = new Timer();

    public static boolean lock_sprint, serversprint;
    public static Map<String, Identifier> heads = new ConcurrentHashMap<>();

    @EventHandler
    public void onTick(PlayerUpdateEvent event) {
        if (!fullNullCheck()) {
            Thunderhack.moduleManager.onUpdate();
            Thunderhack.moduleManager.sortModules();
        }
        if (!fullNullCheck()) {
            if (ModuleManager.clickGui.getBind().getKey() == -1) {
                Command.sendMessage(Formatting.RED + "Default clickgui keybind --> P");
                ModuleManager.clickGui.setBind(InputUtil.fromTranslationKey("key.keyboard.p").getCode(), false, false);
            }
        }
        ThunderGui2.getInstance().onTick();
        Thunderhack.moduleManager.onTick();

        HashMap<BlockPos, Long> cache = new HashMap<>(InteractionUtility.awaiting);
        if(!cache.isEmpty()){
            cache.forEach((bp, time) -> {
                if(System.currentTimeMillis() - time > 1000){
                    InteractionUtility.awaiting.remove(bp);
                }
            });
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (e.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround || e.getPacket() instanceof PlayerMoveC2SPacket.Full || e.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround) {
            lastPacket.reset();
        }

        if (e.getPacket() instanceof ClientCommandC2SPacket command) {

            if (lock_sprint) {
                e.setCancelled(true);
                return;
            }

            if (command.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING)
                serversprint = true;

            if (command.getMode() == ClientCommandC2SPacket.Mode.STOP_SPRINTING)
                serversprint = false;
        }
    }

    @EventHandler
    public void onSync(EventSync event) {
        if (fullNullCheck())
            return;
        thunder.hack.modules.movement.Timer.onEntitySync(event);
    }

    public void onRender2D(DrawContext e) {
        drawGps(e);
        drawSkull(e);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck()) return;

        if (e.getPacket() instanceof GameMessageS2CPacket) {
            final GameMessageS2CPacket packet = e.getPacket();
            if (packet.content().getString().contains("skull")) {
                showSkull = true;
                skullTimer.reset();
                mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.ENTITY_SKELETON_DEATH, SoundCategory.BLOCKS, 1f, 1f);
            }
        }
    }

    public void drawSkull(DrawContext e) {
        if (showSkull && !skullTimer.passedMs(3000)) {
            int xPos = (int) (mc.getWindow().getScaledWidth() / 2f - 150);
            int yPos = (int) (mc.getWindow().getScaledHeight() / 2f - 150);
            float alpha = (1 - (skullTimer.getPassedTimeMs() / 3000f));
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
            e.drawTexture(SKULL, xPos, yPos, 0, 0, 300, 300, 300, 300);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        } else {
            showSkull = false;
        }
    }

    public void drawGps(DrawContext e) {
        if (Thunderhack.gps_position != null) {
            float xOffset = mc.getWindow().getScaledWidth() / 2f;
            float yOffset = mc.getWindow().getScaledHeight() / 2f;
            float yaw = getRotations(new Vec2f(Thunderhack.gps_position.getX(), Thunderhack.gps_position.getZ())) - mc.player.getYaw();
            e.getMatrices().translate(xOffset, yOffset, 0.0F);
            e.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
            e.getMatrices().translate(-xOffset, -yOffset, 0.0F);
            RadarRewrite.drawTracerPointer(e.getMatrices(), xOffset, yOffset - 50, 12.5f, ClickGui.getInstance().getColor(1).getRGB());
            e.getMatrices().translate(xOffset, yOffset, 0.0F);
            e.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
            e.getMatrices().translate(-xOffset, -yOffset, 0.0F);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            FontRenderers.modules.drawCenteredString(e.getMatrices(), "gps (" + getDistance(Thunderhack.gps_position) + "m)", (float) (Math.sin(Math.toRadians(yaw)) * 50f) + xOffset, (float) (yOffset - (Math.cos(Math.toRadians(yaw)) * 50f)) - 20, -1);
        }
    }

    @EventHandler
    public void onKeyPress(EventKeyPress event) {
        if (event.getKey() == -1) return;
        for (Macro m : Thunderhack.macroManager.getMacros()) {
            if (m.getBind() == event.getKey()) {
                m.runMacro();
            }
        }
    }

    public static boolean hold_mouse0;

    @EventHandler
    public void onMouse(EventMouse event) {
        if (event.getAction() == 0) {
            hold_mouse0 = false;
        }
        if (event.getAction() == 1) {
            hold_mouse0 = true;
        }
    }

    public int getDistance(BlockPos bp) {
        double d0 = mc.player.getX() - bp.getX();
        double d2 = mc.player.getZ() - bp.getZ();
        return (int) (MathHelper.sqrt((float) (d0 * d0 + d2 * d2)));
    }

    public static float getRotations(Vec2f vec) {
        if (mc.player == null) return 0;
        double x = vec.x - mc.player.getPos().x;
        double z = vec.y - mc.player.getPos().z;
        return (float) -(Math.atan2(x, z) * (180 / Math.PI));
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }

    private final Identifier SKULL = new Identifier("textures/skull.png");

    private final Timer skullTimer = new Timer();
    private boolean showSkull = false;
}
