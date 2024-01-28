package thunder.hack.modules.render;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4d;
import thunder.hack.ThunderHack;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class NameTags extends Module {
    private final Setting<Float> scale = new Setting<>("Scale", 1f, 0.1f, 10f);
    private final Setting<Float> height = new Setting<>("Height", 2f, 0.1f, 10f);

    private final Setting<Boolean> gamemode = new Setting<>("Gamemode", false);
    private final Setting<Boolean> spawners = new Setting<>("SpawnerNameTag", false);
    private final Setting<Boolean> entityOwner = new Setting<>("EntityOwner", false);

    private final Setting<Boolean> ping = new Setting<>("Ping", false);
    private final Setting<Boolean> health = new Setting<>("Health", true);
    private final Setting<Boolean> distance = new Setting<>("Distance", true);
    private final Setting<Boolean> pops = new Setting<>("TotemPops", true);
    private final Setting<Boolean> outline = new Setting<>("Outline", true);
    private final Setting<Boolean> enchantss = new Setting<>("Enchants", true);
    private final Setting<Boolean> onlyHands = new Setting<>("OnlyHands", false, v -> enchantss.getValue());
    private final Setting<Boolean> funtimeHp = new Setting<>("FunTimeHp", false);

    private final Setting<Boolean> potions = new Setting<>("Potions", true);
    //  private final Setting<Boolean> box = new Setting<>("Box", true);
    private final Setting<ColorSetting> fillColorA = new Setting<>("Color", new ColorSetting(0x80000000));

    public static final Setting<Font> font = new Setting<>("FontMode", Font.Fancy);

    public enum Font {
        Fancy, Fast
    }

    public enum Armor {
        None, Full, Durability
    }

    private final Setting<Armor> armorMode = new Setting<>("ArmorMode", Armor.Full);

    public NameTags() {
        super("NameTags", Category.RENDER);
    }

    public void onRender2D(DrawContext context) {
        for (PlayerEntity ent : mc.world.getPlayers()) {
            if (ent == mc.player && mc.options.getPerspective().isFirstPerson()) continue;

            double x = ent.prevX + (ent.getX() - ent.prevX) * mc.getTickDelta();
            double y = ent.prevY + (ent.getY() - ent.prevY) * mc.getTickDelta();
            double z = ent.prevZ + (ent.getZ() - ent.prevZ) * mc.getTickDelta();
            Vec3d vector = new Vec3d(x, y + height.getValue(), z);

            Vector4d position = null;

            vector = Render3DEngine.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
            if (vector.z > 0 && vector.z < 1) {
                position = new Vector4d(vector.x, vector.y, vector.z, 0);
                position.x = Math.min(vector.x, position.x);
                position.y = Math.min(vector.y, position.y);
                position.z = Math.max(vector.x, position.z);
            }

            String final_string = "";

            if (ping.getValue()) final_string += getEntityPing(ent) + "ms ";
            if (gamemode.getValue()) final_string += translateGamemode(getEntityGamemode(ent)) + " ";

            final_string += (ent.getDisplayName().getString()) + " ";

            if (health.getValue()) {
                final_string += getHealthColor(getHealth(ent)) + round2(getHealth(ent)) + " ";
            }

            if (distance.getValue()) final_string += String.format("%.1f", mc.player.distanceTo(ent)) + "m ";
            if (pops.getValue() && ThunderHack.combatManager.getPops(ent) != 0)
                final_string += (Formatting.RESET + "" + ThunderHack.combatManager.getPops(ent));

            if (position != null) {
                double posX = position.x;
                double posY = position.y;
                double endPosX = position.z;

                float diff = (float) (endPosX - posX) / 2;
                float textWidth;

                if (font.getValue() == Font.Fancy) textWidth = (FontRenderers.sf_bold.getStringWidth(final_string) * 1);
                else textWidth = mc.textRenderer.getWidth(final_string);

                float tagX = (float) ((posX + diff - textWidth / 2) * 1);

                ArrayList<ItemStack> stacks = new ArrayList<>();

                if (armorMode.getValue() != Armor.Durability) stacks.add(ent.getOffHandStack());

                stacks.add(ent.getInventory().armor.get(0));
                stacks.add(ent.getInventory().armor.get(1));
                stacks.add(ent.getInventory().armor.get(2));
                stacks.add(ent.getInventory().armor.get(3));

                if (armorMode.getValue() != Armor.Durability) stacks.add(ent.getMainHandStack());

                context.getMatrices().push();
                context.getMatrices().translate(tagX - 2 + (textWidth + 4) / 2f, (float) (posY - 13f) + 6.5f, 0);
                context.getMatrices().scale(scale.getValue(), scale.getValue(), 1f);
                context.getMatrices().translate(-(tagX - 2 + (textWidth + 4) / 2f), -(float) ((posY - 13f) + 6.5f), 0);

                float item_offset = 0;
                if (armorMode.getValue() != Armor.None)
                    for (ItemStack armorComponent : stacks) {
                        if (!armorComponent.isEmpty()) {
                            if (armorMode.getValue() == Armor.Full) {
                                context.getMatrices().push();
                                context.getMatrices().translate(posX - 55 + item_offset, (float) (posY - 35f), 0);
                                context.getMatrices().scale(1.1f, 1.1f, 1.1f);
                                DiffuseLighting.disableGuiDepthLighting();
                                context.drawItem(armorComponent, 0, 0);
                                context.drawItemInSlot(mc.textRenderer, armorComponent, 0, 0);
                                context.getMatrices().pop();
                            } else {
                                context.getMatrices().push();
                                context.getMatrices().translate(posX - 35 + item_offset, (float) (posY - 20), 0);
                                context.getMatrices().scale(0.7f, 0.7f, 0.7f);

                                float durability = armorComponent.getMaxDamage() - armorComponent.getDamage();
                                int percent = (int) ((durability / (float) armorComponent.getMaxDamage()) * 100F);

                                Color color;
                                if (percent < 33) {
                                    color = Color.RED;
                                } else if (percent > 33 && percent < 66) {
                                    color = Color.YELLOW;
                                } else {
                                    color = Color.GREEN;
                                }
                                context.drawText(mc.textRenderer, percent + "%", 0, 0, color.getRGB(), false);
                                context.getMatrices().pop();
                            }

                            float enchantmentY = 0;

                            NbtList enchants = armorComponent.getEnchantments();
                            if (enchantss.getValue()) {
                                if(!onlyHands.getValue() || (armorComponent == ent.getOffHandStack() || armorComponent == ent.getMainHandStack())) {
                                    for (int index = 0; index < enchants.size(); ++index) {
                                        String id = enchants.getCompound(index).getString("id");
                                        short level = enchants.getCompound(index).getShort("lvl");
                                        String encName = " ";

                                        switch (id) {
                                            case "minecraft:blast_protection", "blast_protection" -> encName = "B" + level;
                                            case "minecraft:protection", "protection" -> encName = "P" + level;
                                            case "minecraft:thorns", "thorns" -> encName = "T" + level;
                                            case "minecraft:sharpness", "sharpness" -> encName = "S" + level;
                                            case "minecraft:efficiency", "efficiency" -> encName = "E" + level;
                                            case "minecraft:unbreaking", "unbreaking" -> encName = "U" + level;
                                            case "minecraft:power", "power" -> encName = "PO" + level;
                                            default -> {
                                                continue;
                                            }
                                        }

                                        if (font.getValue() == Font.Fancy) {
                                            FontRenderers.sf_bold.drawString(context.getMatrices(), encName, posX - 50 + item_offset, (float) posY - 45 + enchantmentY, -1);
                                        } else {
                                            context.getMatrices().push();
                                            context.getMatrices().translate((posX - 50f + item_offset), (posY - 45f + enchantmentY), 0);
                                            context.drawText(mc.textRenderer, encName, 0, 0, -1, false);
                                            context.getMatrices().pop();
                                        }
                                        enchantmentY -= 8;
                                    }
                                }
                            }
                        }
                        item_offset += 18f;
                    }

                Render2DEngine.drawRect(context.getMatrices(), tagX - 2, (float) (posY - 13f), textWidth + 4, 11, fillColorA.getValue().getColorObject());

                if (outline.getValue()) {
                    Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 14f), textWidth + 6, 1, HudEditor.getColor(270));
                    Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 3f), textWidth + 6, 1, HudEditor.getColor(0));
                    Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 14f), 1, 11, HudEditor.getColor(180));
                    Render2DEngine.drawRect(context.getMatrices(), tagX + textWidth + 2, (float) (posY - 14f), 1, 11, HudEditor.getColor(90));
                }

                if (font.getValue() == Font.Fancy) {
                    FontRenderers.sf_bold.drawString(context.getMatrices(), final_string, tagX, (float) posY - 10, -1);
                } else {
                    context.getMatrices().push();
                    context.getMatrices().translate(tagX, ((float) posY - 11), 0);
                    context.drawText(mc.textRenderer, final_string, 0, 0, -1, false);
                    context.getMatrices().pop();
                }
                context.getMatrices().pop();

                //    if (box.getValue()) drawBox(ent, context);
            }
        }

        if (spawners.getValue()) drawSpawnerNameTag(context);
        if (entityOwner.getValue()) drawEntityOwner(context);
    }

    private void drawSpawnerNameTag(DrawContext context) {
        for (BlockEntity blockEntity : StorageEsp.getBlockEntities()) {
            if (blockEntity instanceof MobSpawnerBlockEntity spawner) {
                Vec3d vector = new Vec3d(spawner.getPos().getX() + 0.5, spawner.getPos().getY() + 1.5, spawner.getPos().getZ() + 0.5);
                Vector4d position = null;
                vector = Render3DEngine.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
                if (vector.z > 0 && vector.z < 1) {
                    position = new Vector4d(vector.x, vector.y, vector.z, 0);
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                }
                if (spawner.getLogic() == null || spawner.getLogic().getRenderedEntity(mc.world, spawner.getPos()) == null)
                    continue;
                String final_string = spawner.getLogic().getRenderedEntity(mc.world, spawner.getPos()).getName().getString() + " " + String.format("%.1f", ((float) spawner.getLogic().spawnDelay / 20f)) + "s";

                if (spawner.getLogic().getRotation() == spawner.getLogic().getLastRotation() && spawner.getLogic().getRotation() == 0f && (float) spawner.getLogic().spawnDelay / 20f == 1f)
                    final_string = spawner.getLogic().getRenderedEntity(mc.world, spawner.getPos()).getName().getString() + " loot!";


                if (position != null) {
                    double posX = position.x;
                    double posY = position.y;
                    double endPosX = position.z;

                    float diff = (float) (endPosX - posX) / 2;
                    float textWidth = (FontRenderers.sf_bold.getStringWidth(final_string) * 1);
                    float tagX = (float) ((posX + diff - textWidth / 2) * 1);

                    Render2DEngine.drawRect(context.getMatrices(), tagX - 2, (float) (posY - 13f), textWidth + 4, 11, fillColorA.getValue().getColorObject());

                    if (outline.getValue()) {
                        Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 14f), textWidth + 6, 1, HudEditor.getColor(270));
                        Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 3f), textWidth + 6, 1, HudEditor.getColor(0));
                        Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 14f), 1, 11, HudEditor.getColor(180));
                        Render2DEngine.drawRect(context.getMatrices(), tagX + textWidth + 2, (float) (posY - 14f), 1, 11, HudEditor.getColor(90));
                    }
                    FontRenderers.sf_bold.drawString(context.getMatrices(), final_string, tagX, (float) posY - 10, -1);
                }
            }
        }
    }

    public void drawEntityOwner(DrawContext context) {
        for (Entity ent : mc.world.getEntities()) {
            String ownerName = "";
            if (ent instanceof ProjectileEntity pe) {
                if (pe.getOwner() != null) ownerName = pe.getOwner().getDisplayName().getString();
            } else if (ent instanceof HorseEntity he) {
                if (he.getOwnerUuid() != null) ownerName = he.getOwnerUuid().toString();
            } else if (ent instanceof TameableEntity te && te.isTamed() && te.getOwner() != null) {
                ownerName = te.getOwner().getDisplayName().getString();
            } else continue;

            String final_string = "Owned by " + ownerName;
            double x = ent.prevX + (ent.getX() - ent.prevX) * mc.getTickDelta();
            double y = ent.prevY + (ent.getY() - ent.prevY) * mc.getTickDelta();
            double z = ent.prevZ + (ent.getZ() - ent.prevZ) * mc.getTickDelta();
            Vec3d vector = new Vec3d(x, y + 2, z);
            Vector4d position = null;
            vector = Render3DEngine.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
            if (vector.z > 0 && vector.z < 1) {
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

                Render2DEngine.drawRect(context.getMatrices(), tagX - 2, (float) (posY - 13f), textWidth + 4, 11, fillColorA.getValue().getColorObject());

                if (outline.getValue()) {
                    Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 14f), textWidth + 6, 1, HudEditor.getColor(270));
                    Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 3f), textWidth + 6, 1, HudEditor.getColor(0));
                    Render2DEngine.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 14f), 1, 11, HudEditor.getColor(180));
                    Render2DEngine.drawRect(context.getMatrices(), tagX + textWidth + 2, (float) (posY - 14f), 1, 11, HudEditor.getColor(90));
                }
                FontRenderers.sf_bold.drawString(context.getMatrices(), final_string, tagX, (float) posY - 10, -1);
            }
        }
    }


    public void drawPotions(MatrixStack matrices, @NotNull PlayerEntity entity, float posX, float posY) {
        ArrayList<StatusEffectInstance> effects = new ArrayList<>(entity.getStatusEffects());

        int y_offset1 = 0;

        for (StatusEffectInstance potionEffect : effects) {
            if (potionEffect.getDuration() != 0) {
                StatusEffect potion = potionEffect.getEffectType();
                String power = "";
                switch (potionEffect.getAmplifier()) {
                    case 0 -> power = "I";
                    case 1 -> power = "II";
                    case 2 -> power = "III";
                    case 3 -> power = "IV";
                    case 4 -> power = "V";
                }
                String s = potion.getName().getString() + " " + power;
                String s2 = getDuration(potionEffect);

                FontRenderers.sf_bold_mini.drawString(matrices, s + " " + s2, posX, posY + y_offset1, -1);
                y_offset1 += 8;
            }
        }
    }

    public static @NotNull String getDuration(@NotNull StatusEffectInstance pe) {
        if (pe.isInfinite()) return "*:*";
        else {
            int dur = pe.getDuration();
            int mins = dur / 1200;
            int sec = (dur % 1200) / 20;

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

    public float getHealth(PlayerEntity ent) {
        // Первый в комьюнити хп резольвер. Правда, еж?
        if ((mc.getNetworkHandler() != null && mc.getNetworkHandler().getServerInfo() != null && mc.getNetworkHandler().getServerInfo().address.contains("funtime") || funtimeHp.getValue())) {
            ScoreboardObjective scoreBoard = null;
            String resolvedHp = "";
            if ((ent.getScoreboard()).getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                scoreBoard = (ent.getScoreboard()).getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
                if (scoreBoard != null) {
                    ReadableScoreboardScore readableScoreboardScore = ent.getScoreboard().getScore(ent, scoreBoard);
                    MutableText text2 = ReadableScoreboardScore.getFormattedScore(readableScoreboardScore, scoreBoard.getNumberFormatOr(StyledNumberFormat.EMPTY));
                    resolvedHp = text2.getString();
                }
            }
            float numValue = 0;
            try {
                numValue = Float.parseFloat(resolvedHp);
            } catch (NumberFormatException ignored) {}
            return numValue;
        } else return ent.getHealth() + ent.getAbsorptionAmount();
    }

    private @NotNull String getHealthColor(float health) {
        if (health <= 15 && health > 7) return Formatting.YELLOW + "";
        if (health > 15) return Formatting.GREEN + "";
        return Formatting.RED + "";
    }

    public static float round2(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value))
            return 1f;
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
}