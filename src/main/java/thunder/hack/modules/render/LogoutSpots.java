package thunder.hack.modules.render;

import com.google.common.collect.Maps;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.modules.misc.FakePlayer;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LogoutSpots extends Module {
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x8800FF00));
    private final Setting<Boolean> notifications = new Setting<>("Notifications", true);
    private final Setting<Boolean> ignoreBots = new Setting<>("IgnoreBots", true);

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
                        if(ignoreBots.getValue() && isABot(pl)) continue;
                        if(notifications.getValue()) sendMessage(pl.getName().getString() + " logged back at  X: " + (int) pl.getX() + " Y: " + (int) pl.getY() + " Z: " + (int) pl.getZ());
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
                    if(ignoreBots.getValue() && isABot(pl)) continue;
                    if(pl != null) {
                        if(notifications.getValue()) sendMessage(pl.getName().getString() + " logged out at  X: " + (int) pl.getX() + " Y: " + (int) pl.getY() + " Z: " + (int) pl.getZ());
                        if (!logoutCache.containsKey(uuid))
                            logoutCache.put(uuid, pl);
                    }
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
            if(data != null) {
                Render3DEngine.drawBoxOutline(data.getBoundingBox(), color.getValue().getColorObject(), 2);
            }
        }
    }

    public void onRender2D(DrawContext context) {
        for (UUID uuid : logoutCache.keySet()) {
            final PlayerEntity data = logoutCache.get(uuid);
            if(data != null) {
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
    private boolean isABot(PlayerEntity ent){
        return !ent.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + ent.getName().getString()).getBytes(StandardCharsets.UTF_8))) && ent instanceof OtherClientPlayerEntity
                && (FakePlayer.fakePlayer == null || ent.getId() != FakePlayer.fakePlayer.getId())
                && !ent.getName().getString().contains("-");
    }
}