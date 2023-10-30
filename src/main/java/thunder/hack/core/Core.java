package thunder.hack.core;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.core.impl.MacroManager;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.*;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.impl.RadarRewrite;
import thunder.hack.gui.thundergui.ThunderGui2;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static thunder.hack.modules.Module.fullNullCheck;
import static thunder.hack.modules.Module.mc;

public final class Core {
    public static boolean lock_sprint, serversprint, hold_mouse0, showSkull;
    public static Map<String, Identifier> heads = new ConcurrentHashMap<>();
    private final Identifier SKULL = new Identifier("textures/skull.png");
    private final Timer skullTimer = new Timer();
    private final Timer lastPacket = new Timer();

    @EventHandler
    public void onTick(PlayerUpdateEvent event) {
        if (fullNullCheck()) return;

        ThunderHack.moduleManager.onUpdate();
        ThunderGui2.getInstance().onTick();

        if (ModuleManager.clickGui.getBind().getKey() == -1) {
            Command.sendMessage(Formatting.RED + "Default clickgui keybind --> P");
            ModuleManager.clickGui.setBind(InputUtil.fromTranslationKey("key.keyboard.p").getCode(), false, false);
        }

        for(PlayerEntity p : mc.world.getPlayers()) {
            if(p.isDead() || p.getHealth() == 0)
                ThunderHack.EVENT_BUS.post(new DeathEvent(p));
        }

        if (!Objects.equals(ThunderHack.commandManager.getPrefix(), MainSettings.prefix.getValue().toString()))
            ThunderHack.commandManager.setPrefix(MainSettings.prefix.getValue());

        new HashMap<>(InteractionUtility.awaiting).forEach((bp, time) -> {
            if (System.currentTimeMillis() - time > 300)
                InteractionUtility.awaiting.remove(bp);
        });
    }

    @EventHandler
    public void onPacketSend(PacketEvent.@NotNull Send e) {
        if (e.getPacket() instanceof PlayerMoveC2SPacket && !(e.getPacket() instanceof PlayerMoveC2SPacket.OnGroundOnly))
            lastPacket.reset();

        if (e.getPacket() instanceof ClientCommandC2SPacket c) {
            if (c.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING || c.getMode() == ClientCommandC2SPacket.Mode.STOP_SPRINTING) {
                if (lock_sprint) {
                    e.setCancelled(true);
                    return;
                }

                switch (c.getMode()) {
                    case START_SPRINTING -> serversprint = true;
                    case STOP_SPRINTING -> serversprint = false;
                }
            }
        }
    }

    @EventHandler
    public void onSync(EventSync event) {
        if (fullNullCheck()) return;
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

        if (e.getPacket() instanceof GameJoinS2CPacket)
            ThunderHack.moduleManager.onLogin();
    }

    @EventHandler
    public void onEntitySpawn(EventEntitySpawn e) {
        new ArrayList<>(InteractionUtility.awaiting.keySet()).forEach(bp -> {
            if (e.getEntity() != null && bp.getSquaredDistance(e.getEntity().getPos()) < 4.)
                InteractionUtility.awaiting.remove(bp);
        });
    }

    public void drawSkull(DrawContext e) {
        if (showSkull && !skullTimer.passedMs(3000) && MainSettings.skullEmoji.getValue()) {
            int xPos = (int) (mc.getWindow().getScaledWidth() / 2f - 150);
            int yPos = (int) (mc.getWindow().getScaledHeight() / 2f - 150);
            float alpha = (1 - (skullTimer.getPassedTimeMs() / 3000f));
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
            e.drawTexture(SKULL, xPos, yPos, 0, 0, 300, 300, 300, 300);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        } else showSkull = false;
    }

    public void drawGps(DrawContext e) {
        if (ThunderHack.gps_position != null) {
            float xOffset = mc.getWindow().getScaledWidth() / 2f;
            float yOffset = mc.getWindow().getScaledHeight() / 2f;
            float yaw = getRotations(new Vec2f(ThunderHack.gps_position.getX(), ThunderHack.gps_position.getZ())) - mc.player.getYaw();
            e.getMatrices().translate(xOffset, yOffset, 0.0F);
            e.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
            e.getMatrices().translate(-xOffset, -yOffset, 0.0F);
            RadarRewrite.drawTracerPointer(e.getMatrices(), xOffset, yOffset - 50, 12.5f, ClickGui.getInstance().getColor(1).getRGB());
            e.getMatrices().translate(xOffset, yOffset, 0.0F);
            e.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
            e.getMatrices().translate(-xOffset, -yOffset, 0.0F);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            FontRenderers.modules.drawCenteredString(e.getMatrices(), "gps (" + getDistance(ThunderHack.gps_position) + "m)", (float) (Math.sin(Math.toRadians(yaw)) * 50f) + xOffset, (float) (yOffset - (Math.cos(Math.toRadians(yaw)) * 50f)) - 20, -1);
        }
    }

    @EventHandler
    public void onKeyPress(EventKeyPress event) {
        if (event.getKey() == -1) return;
        for (MacroManager.Macro m : ThunderHack.macroManager.getMacros()) {
            if (m.bind() == event.getKey()) {
                m.runMacro();
            }
        }
    }

    @EventHandler
    public void onMouse(EventMouse event) {
        if (event.getAction() == 0) hold_mouse0 = false;
        if (event.getAction() == 1) hold_mouse0 = true;
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
}