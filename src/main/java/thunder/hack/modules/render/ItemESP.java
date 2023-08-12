package thunder.hack.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Box;
import org.joml.Vector4d;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;


public class ItemESP extends Module {

    public ItemESP() {
        super("ItemESP", Category.RENDER);
    }
    private  Setting<Boolean> shadow = new Setting<>("Shadow", true);
    private  Setting<Boolean> box = new Setting<>("Box", true);
    private  Setting<ColorSetting> scolor = new Setting<>("ShadowColor", new ColorSetting(new Color(0x000000).getRGB()));
    private  Setting<ColorSetting> tcolor = new Setting<>("TextColor", new ColorSetting(new Color(-1).getRGB()));


    public void onRender2D(DrawContext context){
        for(Entity ent : mc.world.getEntities()){
            if(!(ent instanceof ItemEntity)) continue;
            double x = ent.prevX + (ent.getX() - ent.prevX) * mc.getTickDelta();
            double y = ent.prevY + (ent.getY() - ent.prevY) * mc.getTickDelta();
            double z = ent.prevZ + (ent.getZ() - ent.prevZ) * mc.getTickDelta();
            Box axisAlignedBB2 = ent.getBoundingBox();
            Box axisAlignedBB = new Box(axisAlignedBB2.minX - ent.getX() + x - 0.05, axisAlignedBB2.minY - ent.getY() + y, axisAlignedBB2.minZ - ent.getZ() + z - 0.05, axisAlignedBB2.maxX - ent.getX() + x + 0.05, axisAlignedBB2.maxY - ent.getY() + y + 0.15, axisAlignedBB2.maxZ - ent.getZ() + z + 0.05);
            Vec3d[] vectors = new Vec3d[]{new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)};

            Vector4d position = null;
            for (Vec3d vector : vectors) {
                vector = Render3DEngine.worldSpaceToScreenSpace( new Vec3d(vector.x, vector.y, vector.z));
                if (vector != null && vector.z > 0 && vector.z < 1) {
                    if (position == null)
                        position = new Vector4d(vector.x, vector.y, vector.z, 0);
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                    position.w = Math.max(vector.y, position.w);
                }
            }

            if (position != null) {
                double posX = position.x;
                double posY = position.y;
                double endPosX = position.z;
                double endPosY = position.w;

                Render2DEngine.drawRectDumbWay(context.getMatrices(), (float) (posX - 1F), (float) posY, (float) (posX + 0.5), (float) (endPosY + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
                Render2DEngine.drawRectDumbWay(context.getMatrices(), (float) (posX - 1F), (float) (posY - 0.5), (float) (endPosX + 0.5), (float) (posY + 0.5 + 0.5),  Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
                Render2DEngine.drawRectDumbWay(context.getMatrices(), (float) (endPosX - 0.5 - 0.5), (float) posY, (float) (endPosX + 0.5), (float) (endPosY + 0.5),  Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
                Render2DEngine.drawRectDumbWay(context.getMatrices(), (float) (posX - 1), (float) (endPosY - 0.5 - 0.5), (float) (endPosX + 0.5), (float) (endPosY + 0.5),  Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);

                Render2DEngine.drawRectDumbWay(context.getMatrices(), (float) (posX - 0.5f), (float) posY, (float) (posX + 0.5 - 0.5), (float) endPosY, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(0),  HudEditor.getColor(270));
                Render2DEngine.drawRectDumbWay(context.getMatrices(), (float) posX, (float) (endPosY - 0.5f), (float) endPosX, (float) endPosY,HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(180),  HudEditor.getColor(0));
                Render2DEngine.drawRectDumbWay(context.getMatrices(), (float) (posX - 0.5), (float) posY, (float) endPosX, (float) (posY + 0.5),HudEditor.getColor(180), HudEditor.getColor(90), HudEditor.getColor(90),  HudEditor.getColor(180));
                Render2DEngine.drawRectDumbWay(context.getMatrices(), (float) (endPosX - 0.5), (float) posY, (float) endPosX, (float) endPosY,HudEditor.getColor(90), HudEditor.getColor(270), HudEditor.getColor(270),  HudEditor.getColor(90));

                float diff = (float) (endPosX - posX) / 2;
                float textWidth = (FontRenderers.sf_bold_mini.getStringWidth(ent.getDisplayName().getString()) * 1);
                float tagX = (float) ((posX + diff - textWidth / 2) * 1);
              //  if(shadow.getValue()) Render2DEngine.drawBlurredShadow(context.getMatrices(), (float) - FontRenderers.sf_bold_mini.getStringWidth(ent.getDisplayName().getString()) / 2,0,FontRenderers.sf_bold_mini.getStringWidth(ent.getDisplayName().getString()),10,14,scolor.getValue().getColorObject());

                FontRenderers.sf_bold_mini.drawString(context.getMatrices(),ent.getDisplayName().getString(), tagX, (float) posY - 10, -1);

            }
        }
    }
}
