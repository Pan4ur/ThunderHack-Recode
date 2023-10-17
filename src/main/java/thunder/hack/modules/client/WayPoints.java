package thunder.hack.modules.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.WayPointManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.utility.render.Render3DEngine;

public final class WayPoints extends Module {
    private static final Identifier ICON = new Identifier("textures/waypoint.png");

    private static WayPoints instance;

    public WayPoints() {
        super("WayPoints", Category.CLIENT);
        instance = this;
    }

    @Override
    public void onEnable() {
        sendMessage(ThunderHack.commandManager.getPrefix() + "waypoint add x y z name");
    }

    public void onRender2D(DrawContext context) {
        if (!ThunderHack.wayPointManager.getWayPoints().isEmpty()) {
            for (WayPointManager.WayPoint wp : ThunderHack.wayPointManager.getWayPoints()) {
                if (wp.name() == null) continue;
                if (mc.isInSingleplayer()) continue;
                if (!mc.getNetworkHandler().getServerInfo().address.contains(wp.server())) continue;

                double difX = wp.x() - mc.player.getPos().x;
                double difZ = wp.z() - mc.player.getPos().z;
                float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
                double plYaw = MathHelper.wrapDegrees(mc.player.getYaw());
                if (Math.abs(yaw - plYaw) > 90) continue;

                Vec3d vector = new Vec3d(wp.x(), wp.y(), wp.z());
                Vector4d position = null;
                vector = Render3DEngine.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
                if (vector != null) {
                    position = new Vector4d(vector.x, vector.y, vector.z, 0);
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                }

                if (position != null) {
                    double posX = position.x;
                    double posY = position.y;
                    double endPosX = position.z;

                    float diff = (float) (endPosX - posX) / 2;
                    float tagX = (float) ((posX + diff - FontRenderers.sf_bold_mini.getStringWidth(wp.name()) / 2) * 1);

                    String coords = wp.x() + " " + wp.z();
                    float tagX2 = (float) ((posX + diff - FontRenderers.sf_bold_mini.getStringWidth(coords) / 2) * 1);

                    String distance = String.format("%.0f", Math.sqrt(mc.player.squaredDistanceTo(wp.x(), wp.y(), wp.z()))) + "m";
                    float tagX3 = (float) ((posX + diff - FontRenderers.sf_bold_mini.getStringWidth(distance) / 2) * 1);

                    context.getMatrices().push();
                    context.getMatrices().translate(posX - 10, (posY - 35), 0);
                    context.drawTexture(ICON, 0, 0, 20, 20, 0, 0, 20, 20, 20, 20);
                    context.getMatrices().pop();

                    FontRenderers.sf_bold_mini.drawString(context.getMatrices(), wp.name(), tagX, (float) posY - 10, -1);
                    FontRenderers.sf_bold_mini.drawString(context.getMatrices(), Formatting.GRAY + coords, tagX2, (float) posY - 2, -1);
                    FontRenderers.sf_bold_mini.drawString(context.getMatrices(), Formatting.GRAY + distance, tagX3, (float) posY + 6, -1);
                }
            }
        }
    }

    public static WayPoints getInstance() {
        return instance;
    }
}
