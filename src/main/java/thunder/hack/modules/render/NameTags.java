package thunder.hack.modules.render;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.Box;
import org.joml.Vector4d;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.events.impl.RenderBlurEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.impl.Hotbar;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.client.Media;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.Setting;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;


public class NameTags extends Module {
    public NameTags() {
        super("NameTags", "NameTags", Category.RENDER);
    }

    private Setting<Boolean> gamemode = new Setting<>("Gamemode", false);
    private  Setting<Boolean> spawners = new Setting<>("SpawnerNameTag", false);
    private  Setting<Boolean> entityOwner = new Setting<>("EntityOwner", false);

    private  Setting<Boolean> ping = new Setting<>("Ping", false);
    private  Setting<Boolean> health = new Setting<>("Health", true);
    private  Setting<Boolean> distance = new Setting<>("Distance", true);
    private  Setting<Boolean> pops = new Setting<>("TotemPops", true);
    private  Setting<Boolean> outline = new Setting<>("Outline", true);
    private  Setting<Boolean> enchantss = new Setting<>("Enchants", true);
    private  Setting<Boolean> potions = new Setting<>("Potions", true);
    private  Setting<Boolean> box = new Setting<>("Box", true);
    private  Setting<ColorSetting> fillColorA = new Setting<>("Color", new ColorSetting(0x80000000));

    public static final Setting<Font> font = new Setting<>("FontMode", Font.Fancy);
    public enum Font {
        Fancy, Fast
    }

    public static final Setting<Armor> armorMode = new Setting<>("ArmorMode", Armor.Full);
    public enum Armor {
        None, Full, Durability
    }

    @Subscribe
    public void onRender2D(Render2DEvent e){
        long time = System.currentTimeMillis();

        for(Entity ent : mc.world.getPlayers()){
            if (ent == mc.player && mc.options.getPerspective().isFirstPerson()) continue;
            double x = ent.prevX + (ent.getX() - ent.prevX) * mc.getTickDelta();
            double y = ent.prevY + (ent.getY() - ent.prevY) * mc.getTickDelta();
            double z = ent.prevZ + (ent.getZ() - ent.prevZ) * mc.getTickDelta();
            Vec3d vector = new Vec3d(x, y + 2, z);

            Vector4d position = null;


            vector = Render3DEngine.worldSpaceToScreenSpace( new Vec3d(vector.x, vector.y, vector.z));
            if (vector != null && vector.z > 0 && vector.z < 1) {
                if (position == null)
                    position = new Vector4d(vector.x, vector.y, vector.z, 0);
                position.x = Math.min(vector.x, position.x);
                position.y = Math.min(vector.y, position.y);
                position.z = Math.max(vector.x, position.z);
            }


            PlayerEntity p = (PlayerEntity) ent;

            String final_string = "";

            if (ping.getValue()) {
                final_string += getEntityPing(p) + "ms ";
            }
            if (gamemode.getValue()) {
                final_string += translateGamemode(getEntityGamemode(p)) + " ";
            }

            final_string += p.getDisplayName().getString() + " ";

            if (health.getValue()) {
                final_string += getHealthColor(p) + round2(p.getAbsorptionAmount() + p.getHealth()) + " ";
            }
            if (distance.getValue()) {
                final_string += String.format("%.1f", mc.player.distanceTo(p)) + "m ";
            }
            if (pops.getValue() && Thunderhack.combatManager.getPops(p) != 0) {
                final_string += (Formatting.RESET + "" +Thunderhack.combatManager.getPops(p));
            }
            if (position != null) {
                double posX = position.x;
                double posY = position.y;
                double endPosX = position.z;

                float diff = (float) (endPosX - posX) / 2;

                float textWidth;

                if(font.getValue() == Font.Fancy) {
                    textWidth = (FontRenderers.sf_bold.getStringWidth(final_string) * 1);
                } else {
                    textWidth = mc.textRenderer.getWidth(final_string);
                }
                float tagX = (float) ((posX + diff - textWidth / 2) * 1);


                ArrayList<ItemStack> stacks = new ArrayList();
                if(armorMode.getValue() != Armor.Durability) {
                    stacks.add(p.getOffHandStack());
                }
                stacks.add(p.getInventory().armor.get(0));
                stacks.add(p.getInventory().armor.get(1));
                stacks.add(p.getInventory().armor.get(2));
                stacks.add(p.getInventory().armor.get(3));
                if(armorMode.getValue() != Armor.Durability) {
                    stacks.add(p.getMainHandStack());
                }

                float item_offset = 0;
                if(armorMode.getValue() != Armor.None)
                    for(ItemStack armorComponent : stacks) {
                        if (!armorComponent.isEmpty()) {
                            if(armorMode.getValue() == Armor.Full) {
                                e.getMatrixStack().push();
                                e.getMatrixStack().translate(posX - 55 + item_offset, (float) (posY - 35f), 0);
                                e.getMatrixStack().scale(1.1f, 1.1f, 1.1f);
                                DiffuseLighting.disableGuiDepthLighting();
                                e.getContext().drawItem(armorComponent, 0, 0);
                                e.getContext().drawItemInSlot(mc.textRenderer, armorComponent, 0, 0);
                                e.getMatrixStack().pop();
                            } else {
                                e.getMatrixStack().push();
                                e.getMatrixStack().translate(posX - 35 + item_offset, (float) (posY - 20), 0);
                                e.getMatrixStack().scale(0.7f, 0.7f, 0.7f);

                                int percent = (int) ((Math.ceil((double) (armorComponent.getMaxDamage() - armorComponent.getDamage()) / armorComponent.getMaxDamage())) * 100f);
                                Color color;
                                if(percent < 33){
                                    color = Color.RED;
                                } else if(percent > 33 && percent < 66){
                                    color = Color.YELLOW;
                                } else {
                                    color = Color.GREEN;
                                }
                                e.getContext().drawText(mc.textRenderer, percent + "%", 0, 0, color.getRGB(), false);
                                e.getMatrixStack().pop();
                            }




                            float enchantmentY = 0;

                            NbtList enchants = armorComponent.getEnchantments();
                            if(enchantss.getValue())
                                for (int index = 0; index < enchants.size(); ++index) {
                                    String id = enchants.getCompound(index).getString("id");
                                    short level = enchants.getCompound(index).getShort("lvl");
                                    String encName = " ";

                                    if(id.equals("minecraft:protection")){
                                        encName = "P" + level;
                                    } else
                                    if(id.equals("minecraft:thorns")){
                                        encName = "T" + level;
                                    } else
                                    if(id.equals("minecraft:sharpness")){
                                        encName = "S" + level;
                                    } else
                                    if(id.equals("minecraft:efficiency")){
                                        encName = "E" + level;
                                    } else
                                    if(id.equals("minecraft:unbreaking")){
                                        encName = "U" + level;
                                    } else
                                    if(id.equals("minecraft:power")){
                                        encName = "PO" + level;
                                    } else continue;

                                    if(font.getValue() == Font.Fancy) {
                                        FontRenderers.sf_bold.drawString(e.getMatrixStack(), encName, posX - 50 + item_offset, (float) posY - 45 + enchantmentY, -1);
                                    } else {
                                        e.getContext().drawText(mc.textRenderer, encName, (int) ((int)posX - 50 + item_offset), (int) ((int) posY - 45 + enchantmentY), -1,false);
                                    }
                                    enchantmentY -= 8;
                                }
                        }
                        item_offset += 18f;
                    }



                Render2DEngine.drawRect(e.getMatrixStack(),tagX -2 , (float) (posY - 13f), textWidth + 4, 11,fillColorA.getValue().getColorObject());

                if(outline.getValue()) {
                    Render2DEngine.drawRect(e.getMatrixStack(), tagX - 3, (float) (posY - 14f), textWidth + 6, 1, HudEditor.getColor(270));
                    Render2DEngine.drawRect(e.getMatrixStack(), tagX - 3, (float) (posY - 3f), textWidth + 6, 1, HudEditor.getColor(0));
                    Render2DEngine.drawRect(e.getMatrixStack(), tagX - 3, (float) (posY - 14f), 1, 11, HudEditor.getColor(180));
                    Render2DEngine.drawRect(e.getMatrixStack(), tagX + textWidth + 2, (float) (posY - 14f), 1, 11, HudEditor.getColor(90));
                }


                if(font.getValue() == Font.Fancy){
                    FontRenderers.sf_bold.drawString(e.getMatrixStack(),final_string, tagX, (float) posY - 10, -1);
                } else {
                    e.getContext().drawText(mc.textRenderer,final_string, (int) tagX, (int) ((float) posY - 11), -1,false);
                }
                if(box.getValue()) drawBox(p,e);
            }
        }
        // Command.sendMessage(System.currentTimeMillis() - time + "");
        if(spawners.getValue())
            for (BlockEntity blockEntity : StorageEsp.getBlockEntities()) {
                if (blockEntity instanceof MobSpawnerBlockEntity spawner) {
                    Vec3d vector = new Vec3d( spawner.getPos().getX() + 0.5, spawner.getPos().getY() + 1.5, spawner.getPos().getZ() + 0.5);
                    Vector4d position = null;
                    vector = Render3DEngine.worldSpaceToScreenSpace( new Vec3d(vector.x, vector.y, vector.z));
                    if (vector != null && vector.z > 0 && vector.z < 1) {
                        position = new Vector4d(vector.x, vector.y, vector.z, 0);
                        position.x = Math.min(vector.x, position.x);
                        position.y = Math.min(vector.y, position.y);
                        position.z = Math.max(vector.x, position.z);
                    }
                    if(spawner.getLogic() == null || spawner.getLogic().renderedEntity == null) continue;
                    String final_string = spawner.getLogic().renderedEntity.getName().getString() + " " + String.format("%.1f",((float)spawner.getLogic().spawnDelay / 20f)) + "s";

                    if (position != null) {
                        double posX = position.x;
                        double posY = position.y;
                        double endPosX = position.z;

                        float diff = (float) (endPosX - posX) / 2;
                        float textWidth = (FontRenderers.sf_bold.getStringWidth(final_string) * 1);
                        float tagX = (float) ((posX + diff - textWidth / 2) * 1);

                        Render2DEngine.drawRect(e.getMatrixStack(), tagX - 2, (float) (posY - 13f), textWidth + 4, 11, fillColorA.getValue().getColorObject());

                        if (outline.getValue()) {
                            Render2DEngine.drawRect(e.getMatrixStack(), tagX - 3, (float) (posY - 14f), textWidth + 6, 1, HudEditor.getColor(270));
                            Render2DEngine.drawRect(e.getMatrixStack(), tagX - 3, (float) (posY - 3f), textWidth + 6, 1, HudEditor.getColor(0));
                            Render2DEngine.drawRect(e.getMatrixStack(), tagX - 3, (float) (posY - 14f), 1, 11, HudEditor.getColor(180));
                            Render2DEngine.drawRect(e.getMatrixStack(), tagX + textWidth + 2, (float) (posY - 14f), 1, 11, HudEditor.getColor(90));
                        }
                        FontRenderers.sf_bold.drawString(e.getMatrixStack(), final_string, tagX, (float) posY - 10, -1);
                    }
                }
            }
        if(entityOwner.getValue()){
            for (Entity ent : mc.world.getEntities()) {

                String ownerName = "";
                if(ent instanceof ProjectileEntity pe){
                    if(pe.getOwner() != null)
                        ownerName = pe.getOwner().getDisplayName().getString();
                } else if(ent instanceof HorseEntity he){
                    if(he.getOwnerUuid() != null)
                        ownerName = he.getOwnerUuid().toString();
                }
                else if(ent instanceof TameableEntity te && te.isTamed()){
                    ownerName = te.getOwner().getDisplayName().getString();
                }
                else continue;

                String final_string =  "Owned by " + ownerName;
                double x = ent.prevX + (ent.getX() - ent.prevX) * mc.getTickDelta();
                double y = ent.prevY + (ent.getY() - ent.prevY) * mc.getTickDelta();
                double z = ent.prevZ + (ent.getZ() - ent.prevZ) * mc.getTickDelta();
                Vec3d vector = new Vec3d(x, y + 2, z);
                Vector4d position = null;
                vector = Render3DEngine.worldSpaceToScreenSpace( new Vec3d(vector.x, vector.y, vector.z));
                if (vector != null && vector.z > 0 && vector.z < 1) {
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
                    float textWidth = (FontRenderers.sf_bold.getStringWidth(final_string) * 1);
                    float tagX = (float) ((posX + diff - textWidth / 2) * 1);

                    Render2DEngine.drawRect(e.getMatrixStack(), tagX - 2, (float) (posY - 13f), textWidth + 4, 11, fillColorA.getValue().getColorObject());

                    if (outline.getValue()) {
                        Render2DEngine.drawRect(e.getMatrixStack(), tagX - 3, (float) (posY - 14f), textWidth + 6, 1, HudEditor.getColor(270));
                        Render2DEngine.drawRect(e.getMatrixStack(), tagX - 3, (float) (posY - 3f), textWidth + 6, 1, HudEditor.getColor(0));
                        Render2DEngine.drawRect(e.getMatrixStack(), tagX - 3, (float) (posY - 14f), 1, 11, HudEditor.getColor(180));
                        Render2DEngine.drawRect(e.getMatrixStack(), tagX + textWidth + 2, (float) (posY - 14f), 1, 11, HudEditor.getColor(90));
                    }
                    FontRenderers.sf_bold.drawString(e.getMatrixStack(), final_string, tagX, (float) posY - 10, -1);
                }
            }
        }
    }

    public void drawBox(PlayerEntity ent,Render2DEvent e){
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

            Render2DEngine.drawRectDumbWay(e.getMatrixStack(), (float) (posX - 1F), (float) posY, (float) (posX + 0.5), (float) (endPosY + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
            Render2DEngine.drawRectDumbWay(e.getMatrixStack(), (float) (posX - 1F), (float) (posY - 0.5), (float) (endPosX + 0.5), (float) (posY + 0.5 + 0.5),  Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
            Render2DEngine.drawRectDumbWay(e.getMatrixStack(), (float) (endPosX - 0.5 - 0.5), (float) posY, (float) (endPosX + 0.5), (float) (endPosY + 0.5),  Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
            Render2DEngine.drawRectDumbWay(e.getMatrixStack(), (float) (posX - 1), (float) (endPosY - 0.5 - 0.5), (float) (endPosX + 0.5), (float) (endPosY + 0.5),  Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);

            Render2DEngine.drawRectDumbWay(e.getMatrixStack(), (float) (posX - 0.5f), (float) posY, (float) (posX + 0.5 - 0.5), (float) endPosY, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(0),  HudEditor.getColor(270));
            Render2DEngine.drawRectDumbWay(e.getMatrixStack(), (float) posX, (float) (endPosY - 0.5f), (float) endPosX, (float) endPosY,HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(180),  HudEditor.getColor(0));
            Render2DEngine.drawRectDumbWay(e.getMatrixStack(), (float) (posX - 0.5), (float) posY, (float) endPosX, (float) (posY + 0.5),HudEditor.getColor(180), HudEditor.getColor(90), HudEditor.getColor(90),  HudEditor.getColor(180));
            Render2DEngine.drawRectDumbWay(e.getMatrixStack(), (float) (endPosX - 0.5), (float) posY, (float) endPosX, (float) endPosY,HudEditor.getColor(90), HudEditor.getColor(270), HudEditor.getColor(270),  HudEditor.getColor(90));

            Render2DEngine.drawRectDumbWay(e.getMatrixStack(), (float) (posX - 5), (float) posY, (float) posX - 3, (float) endPosY, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
            Render2DEngine.drawRectDumbWay(e.getMatrixStack(), (float) (posX - 5), (float) (float) (endPosY + (posY - endPosY) * ((PlayerEntity)ent).getHealth() / 20f), (float) posX- 3, (float) endPosY, Color.RED, Color.RED, Color.RED, Color.RED);
            if (potions.getValue())
                drawPotions(e.getMatrixStack(),ent, (float) (endPosX + 7), (float) posY);

        }
    }


    public void drawPotions(MatrixStack matrices, PlayerEntity entity, float posX, float posY){
        ArrayList<StatusEffectInstance> effects = new ArrayList<>();

        int y_offset1 = 0;

        for (StatusEffectInstance potionEffect : entity.getStatusEffects()) {
            if (potionEffect.getDuration() != 0) {
                effects.add(potionEffect);
                StatusEffect potion = potionEffect.getEffectType();
                String power = "";
                if (potionEffect.getAmplifier() == 0) {
                    power = "I";
                } else if (potionEffect.getAmplifier() == 1) {
                    power = "II";
                } else if (potionEffect.getAmplifier() == 2) {
                    power = "III";
                } else if (potionEffect.getAmplifier() == 3) {
                    power = "IV";
                } else if (potionEffect.getAmplifier() == 4) {
                    power = "V";
                }
                String s = potion.getName().getString() + " " + power;
                String s2 = getDuration(potionEffect) + "";

                FontRenderers.sf_bold_mini.drawString(matrices,s + " " + s2,posX,posY + y_offset1,-1);
                y_offset1 += 8;
            }
        }
    }


    public static String getDuration(StatusEffectInstance pe) {
        if (pe.isInfinite()) {
            return "*:*";
        } else {
            int var1 = pe.getDuration();
            int mins = var1 / 1200;
            int sec = (var1 % 1200) / 20;

            return mins + ":" + sec;
        }
    }

    public static int getEntityPing(PlayerEntity entity) {
        if (mc.getNetworkHandler() == null) return 0;
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
        if (playerListEntry == null) return 0;
        return playerListEntry.getLatency();
    }

    public static GameMode getEntityGamemode(PlayerEntity entity) {
        if (entity == null) return null;
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
        return playerListEntry == null ? null : playerListEntry.getGameMode();
    }

    private String translateGamemode(GameMode gamemode) {
        if (gamemode == null) return "[BOT]";
        return switch (gamemode) {
            case SURVIVAL -> "[S]";
            case CREATIVE -> "[C]";
            case SPECTATOR -> "[SP]";
            case ADVENTURE -> "[A]";
        };
    }

    private String getHealthColor(PlayerEntity entity) {
        int health = (int) ((int) entity.getHealth() + entity.getAbsorptionAmount());
        if (health <= 15 && health > 7) return Formatting.YELLOW + "";
        if (health > 15) return Formatting.GREEN + "";
        return Formatting.RED + "";
    }


    public static float round2(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
}
