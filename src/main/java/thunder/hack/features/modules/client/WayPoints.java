package thunder.hack.features.modules.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.world.WayPointManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.modules.Module;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;

public final class WayPoints extends Module {
    public WayPoints() {
        super("WayPoints", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        sendMessage(Managers.COMMAND.getPrefix() + "waypoint add x y z name");
    }

    public void onRender2D(DrawContext context) {
        if (!Managers.WAYPOINT.getWayPoints().isEmpty() && !fullNullCheck()) {
            for (WayPointManager.WayPoint wp : Managers.WAYPOINT.getWayPoints()) {
                if (wp.getName() == null) continue;
                if ((mc.isInSingleplayer() && wp.getServer().equals("SinglePlayer"))
                        || (mc.getNetworkHandler().getServerInfo() != null && !mc.getNetworkHandler().getServerInfo().address.contains(wp.getServer()))) continue;
                if (!mc.world.getRegistryKey().getValue().getPath().equals(wp.getDimension())) continue;
                double difX = wp.getX() - mc.player.getPos().x;
                double difZ = wp.getZ() - mc.player.getPos().z;
                float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
                double plYaw = MathHelper.wrapDegrees(mc.player.getYaw());
                if (Math.abs(yaw - plYaw) > 90) continue;

                Vec3d vector = new Vec3d(wp.getX(), wp.getY(), wp.getZ());
                Vector4d position = null;
                vector = Render3DEngine.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
                position = new Vector4d(vector.x, vector.y, vector.z, 0);
                position.x = Math.min(vector.x, position.x);
                position.y = Math.min(vector.y, position.y);
                position.z = Math.max(vector.x, position.z);

                double posX = position.x;
                double posY = position.y;
                double endPosX = position.z;

                float diff = (float) (endPosX - posX) / 2;
                float tagX = (float) ((posX + diff - FontRenderers.sf_bold_mini.getStringWidth(wp.getName()) / 2) * 1);

                String coords = wp.getX() + " " + wp.getZ();
                float tagX2 = (float) ((posX + diff - FontRenderers.sf_bold_mini.getStringWidth(coords) / 2) * 1);

                String distance = String.format("%.0f", Math.sqrt(mc.player.squaredDistanceTo(wp.getX(), wp.getY(), wp.getZ()))) + "m";
                float tagX3 = (float) ((posX + diff - FontRenderers.sf_bold_mini.getStringWidth(distance) / 2) * 1);

                context.getMatrices().push();
                context.getMatrices().translate(posX - 10, (posY - 35), 0);
                context.drawTexture(TextureStorage.waypoint, 0, 0, 20, 20, 0, 0, 20, 20, 20, 20);
                context.getMatrices().pop();

                FontRenderers.sf_bold_mini.drawString(context.getMatrices(), wp.getName(), tagX, (float) posY - 10, -1);
                FontRenderers.sf_bold_mini.drawString(context.getMatrices(), Formatting.GRAY + coords, tagX2, (float) posY - 2, -1);
                FontRenderers.sf_bold_mini.drawString(context.getMatrices(), Formatting.GRAY + distance, tagX3, (float) posY + 6, -1);
            }
        }
    }
}
