package dev.thunderhack.modules.render;

import com.google.common.collect.Maps;
import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.ColorSetting;
import dev.thunderhack.utils.render.Render2DEngine;
import dev.thunderhack.utils.render.Render3DEngine;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;
import dev.thunderhack.gui.font.FontRenderers;

import java.awt.*;
import java.util.*;

public class LogoutSpots extends Module {
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x8800FF00));

    private final Map<UUID, PlayerEntity> playerCache = Maps.newConcurrentMap();
    private final Map<UUID, PlayerEntity> logoutCache = Maps.newConcurrentMap();

    public LogoutSpots() {
        super("LogoutSpots", Category.RENDER);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof PlayerListS2CPacket pac) {
            if (pac.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                for (PlayerListS2CPacket.Entry ple : pac.getPlayerAdditionEntries()) {
                    for (UUID uuid : logoutCache.keySet()) {
                        if (!uuid.equals(ple.profile().getId())) continue;
                        PlayerEntity pl = logoutCache.get(uuid);
                        sendMessage(pl.getName().getString() + " logged back at  X: " + (int) pl.getX() + " Y: " + (int) pl.getY() + " Z: " + (int) pl.getZ());
                        logoutCache.remove(uuid);
                    }
                }
            }
            playerCache.clear();
        }

        if (e.getPacket() instanceof PlayerRemoveS2CPacket pac) {
            for (UUID uuid2 : pac.profileIds) {
                for (UUID uuid : playerCache.keySet()) {
                    if (!uuid.equals(uuid2)) continue;
                    final PlayerEntity pl = playerCache.get(uuid);
                    sendMessage(pl.getName().getString() + " logged out at  X: " + (int) pl.getX() + " Y: " + (int) pl.getY() + " Z: " + (int) pl.getZ());
                    if (!logoutCache.containsKey(uuid)) logoutCache.put(uuid, pl);
                }
            }
            playerCache.clear();
        }
    }

    @Override
    public void onEnable() {
        playerCache.clear();
        logoutCache.clear();
    }

    @Override
    public void onUpdate() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == null || player.equals(mc.player)) continue;
            playerCache.put(player.getGameProfile().getId(), player);
        }
    }

    public void onRender3D(MatrixStack event) {
        for (UUID uuid : logoutCache.keySet()) {
            final PlayerEntity data = logoutCache.get(uuid);
            Render3DEngine.drawBoxOutline(data.getBoundingBox(), color.getValue().getColorObject(), 2);
        }
    }

    public void onRender2D(DrawContext context) {
        for (UUID uuid : logoutCache.keySet()) {
            final PlayerEntity data = logoutCache.get(uuid);

            Vec3d vector = new Vec3d(data.getX(), data.getY() + 2, data.getZ());
            Vector4d position = null;

            vector = Render3DEngine.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
            if (vector.z > 0 && vector.z < 1) {
                position = new Vector4d(vector.x, vector.y, vector.z, 0);
                position.x = Math.min(vector.x, position.x);
                position.y = Math.min(vector.y, position.y);
                position.z = Math.max(vector.x, position.z);
            }

            String string = data.getName().getString() + " " + String.format("%.1f", (data.getHealth() + data.getAbsorptionAmount())) + " X: " + (int) data.getX() + " " + " Z: " + (int) data.getZ();

            if (position != null) {
                float diff = (float) (position.z - position.x) / 2;
                float textWidth = (FontRenderers.sf_bold.getStringWidth(string) * 1);
                float tagX = (float) ((position.x + diff - textWidth / 2) * 1);

                Render2DEngine.drawRect(context.getMatrices(), tagX - 2, (float) (position.y - 13f), textWidth + 4, 11, new Color(0x99000001, true));
                FontRenderers.sf_bold.drawString(context.getMatrices(), string, tagX, (float) position.y - 10, -1);
            }
        }
    }
}