package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import thunder.hack.ThunderHack;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.FrameRateCounter;
import thunder.hack.utility.math.MathUtility;


import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static thunder.hack.core.impl.ServerManager.getPing;
import static thunder.hack.gui.hud.impl.PotionHud.getDuration;

public class LegacyHud extends Module {
    public LegacyHud() {
        super("LegacyHud", Category.HUD);
    }

    private static final ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);

    private final Setting<ColorSetting> colorSetting = new Setting<>("Color", new ColorSetting(new Color(0x0077FF)));
    private final Setting<Boolean> renderingUp = new Setting<>("RenderingUp", false);
    private final Setting<Boolean> waterMark = new Setting<>("Watermark", false);
    private final Setting<Boolean> arrayList = new Setting<>("ActiveModules", false);
    private final Setting<Boolean> coords = new Setting<>("Coords", false);
    private final Setting<Boolean> direction = new Setting<>("Direction", false);
    private final Setting<Boolean> armor = new Setting<>("Armor", false);
    private final Setting<Boolean> totems = new Setting<>("Totems", false);
    private final Setting<Boolean> greeter = new Setting<>("Welcomer", false);
    private final Setting<Boolean> speed = new Setting<>("Speed", false);
    public final Setting<Boolean> potions = new Setting<>("Potions", false);
    private final Setting<Boolean> ping = new Setting<>("Ping", false);
    private final Setting<Boolean> tps = new Setting<>("TPS", false);
    private final Setting<Boolean> extraTps = new Setting<>("ExtraTPS", true, v-> tps.getValue());

    private final Setting<Boolean> fps = new Setting<>("FPS", false);
    public Setting<Integer> waterMarkY = new Setting<>("WatermarkPosY", 2, 0, 20, v -> waterMark.getValue());
    public Setting<Boolean> time = new Setting<>("Time", false);
    private int color;

    public void onRender2D(DrawContext context) {
        if (fullNullCheck())
            return;
        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();
        color = colorSetting.getValue().getColor();
        if (waterMark.getValue()) {
            String string = "thunderhack v" + ThunderHack.VERSION;
            context.drawText(mc.textRenderer, Text.of(string), 2, waterMarkY.getValue(), color, true);
        }
        int j = (mc.currentScreen instanceof ChatScreen && !renderingUp.getValue()) ? 14 : 0;
        if (arrayList.getValue())
            if (renderingUp.getValue()) {
                for (Module module : ThunderHack.moduleManager.getEnabledModules().stream().filter(Module::isDrawn).sorted(Comparator.comparing(module -> mc.textRenderer.getWidth(module.getFullArrayString()) * -1)).collect(Collectors.toList())) {
                    if (!module.isDrawn()) {
                        continue;
                    }
                    String str = module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "");
                    context.drawText(mc.textRenderer, Text.of(str), (width - 2 - getStringWidth(str)), (2 + j * 10), color, true);
                    j++;
                }
            } else {
                for (Module module : ThunderHack.moduleManager.getEnabledModules().stream().filter(Module::isDrawn).sorted(Comparator.comparing(module -> mc.textRenderer.getWidth(module.getFullArrayString()) * -1)).collect(Collectors.toList())) {
                    if (!module.isDrawn()) {
                        continue;
                    }
                    String str = module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "");
                    j += 10;
                    context.drawText(mc.textRenderer, Text.of(str), (width - 2 - getStringWidth(str)), (height - j), color, true);
                }
            }
        int i = (mc.currentScreen instanceof ChatScreen && renderingUp.getValue()) ? 13 : (renderingUp.getValue() ? -2 : 0);
        if (renderingUp.getValue()) {
            if (potions.getValue()) {

                List<StatusEffectInstance> effects = new ArrayList<>(mc.player.getStatusEffects());
                for (StatusEffectInstance potionEffect : effects) {
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
                    String s2 = getDuration(potionEffect) + "";
                    i += 10;
                    context.drawText(mc.textRenderer, Text.of(s + " " + s2), (width - getStringWidth(s + " " + s2) - 2), (height - 2 - i), potionEffect.getEffectType().getColor(), true);
                }
            }
            if (speed.getValue()) {
                String str = "Speed " + Formatting.WHITE + MathUtility.round((float) (ThunderHack.playerManager.currentPlayerSpeed * 72f))  + " km/h";
                i += 10;
                context.drawText(mc.textRenderer, Text.of(str), (width - getStringWidth(str) - 2), (height - 2 - i), color, true);
            }
            if (time.getValue()) {
                String str = "Time " + Formatting.WHITE + (new SimpleDateFormat("h:mm a")).format(new Date());
                i += 10;
                context.drawText(mc.textRenderer, Text.of(str), (width - getStringWidth(str) - 2), (height - 2 - i), color, true);
            }
            if (tps.getValue()) {
                String str = "TPS " + Formatting.WHITE + ThunderHack.serverManager.getTPS() + (extraTps.getValue() ? " [" + ThunderHack.serverManager.getTPS2() + "]" : "");
                i += 10;
                context.drawText(mc.textRenderer, Text.of(str), (width - getStringWidth(str) - 2), (height - 2 - i), color, true);
            }
            String fpsText = "FPS " + Formatting.WHITE + FrameRateCounter.INSTANCE.getFps();
            String str1 = "Ping " + Formatting.WHITE + getPing();
            if (getStringWidth(str1) > getStringWidth(fpsText)) {
                if (ping.getValue()) {
                    i += 10;
                    context.drawText(mc.textRenderer, Text.of(str1), (width - getStringWidth(str1) - 2), (height - 2 - i), color, true);
                }
                if (fps.getValue()) {
                    i += 10;
                    context.drawText(mc.textRenderer, Text.of(fpsText), (width - getStringWidth(fpsText) - 2), (height - 2 - i), color, true);
                }
            } else {
                if (fps.getValue()) {
                    i += 10;
                    context.drawText(mc.textRenderer, Text.of(fpsText), (width - getStringWidth(fpsText) - 2), (height - 2 - i), color, true);
                }
                if (ping.getValue()) {
                    i += 10;
                    context.drawText(mc.textRenderer, Text.of(str1), (width - getStringWidth(str1) - 2), (height - 2 - i), color, true);
                }
            }
        } else {
            if (potions.getValue()) {
                List<StatusEffectInstance> effects = new ArrayList<>(mc.player.getStatusEffects());
                for (StatusEffectInstance potionEffect : effects) {
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
                    String s2 = getDuration(potionEffect) + "";
                    context.drawText(mc.textRenderer, Text.of(s + " " + s2), (width - getStringWidth(s + " " + s2) - 2), (2 + i++ * 10), potionEffect.getEffectType().getColor(), true);
                }
            }
            if (speed.getValue()) {
                String str = "Speed " + Formatting.WHITE + MathUtility.round((float) (ThunderHack.playerManager.currentPlayerSpeed * 72f)) + " km/h";
                context.drawText(mc.textRenderer, Text.of(str), (width - getStringWidth(str) - 2), (2 + i++ * 10), color, true);
            }
            if (time.getValue()) {
                String str = "Time " + Formatting.WHITE + (new SimpleDateFormat("h:mm a")).format(new Date());
                context.drawText(mc.textRenderer, Text.of(str), (width - getStringWidth(str) - 2), (2 + i++ * 10), color, true);
            }
            if (tps.getValue()) {
                String str = "TPS " + Formatting.WHITE + ThunderHack.serverManager.getTPS() + (extraTps.getValue() ? " [" + ThunderHack.serverManager.getTPS2() + "]" : "");
                context.drawText(mc.textRenderer, Text.of(str), (width - getStringWidth(str) - 2), (2 + i++ * 10), color, true);
            }
            String fpsText = "FPS " + Formatting.WHITE + FrameRateCounter.INSTANCE.getFps();
            String str1 = "Ping " + Formatting.WHITE + getPing();
            if (getStringWidth(str1) > getStringWidth(fpsText)) {
                if (ping.getValue()) {
                    context.drawText(mc.textRenderer, Text.of(str1), (width - getStringWidth(str1) - 2), (2 + i++ * 10), color, true);
                }
                if (fps.getValue()) {
                    context.drawText(mc.textRenderer, Text.of(fpsText), (width - getStringWidth(fpsText) - 2), (2 + i++ * 10), color, true);
                }
            } else {
                if (fps.getValue()) {
                    context.drawText(mc.textRenderer, Text.of(fpsText), (width - getStringWidth(fpsText) - 2), (2 + i++ * 10), color, true);
                }
                if (ping.getValue()) {
                    context.drawText(mc.textRenderer, Text.of(str1), (width - getStringWidth(str1) - 2), (2 + i++ * 10), color, true);
                }
            }
        }
        boolean inHell = Objects.equals(mc.world.getRegistryKey().getValue().getPath(), "the_nether");
        int posX = (int) mc.player.getX();
        int posY = (int) mc.player.getY();
        int posZ = (int) mc.player.getZ();
        float nether = !inHell ? 0.125F : 8.0F;
        int hposX = (int) (mc.player.getX() * nether);
        int hposZ = (int) (mc.player.getZ() * nether);
        i = (mc.currentScreen instanceof ChatScreen) ? 14 : 0;
        String coordinates = Formatting.WHITE + "XYZ " + Formatting.RESET + (inHell ? (posX + ", " + posY + ", " + posZ + Formatting.WHITE + " [" + Formatting.RESET + hposX + ", " + hposZ + Formatting.WHITE + "]" + Formatting.RESET) : (posX + ", " + posY + ", " + posZ + Formatting.WHITE + " [" + Formatting.RESET + hposX + ", " + hposZ + Formatting.WHITE + "]"));
        String direction1 = "";

        if(direction.getValue()){
            switch (mc.player.getHorizontalFacing()){
            case EAST -> direction1 = "East" + Formatting.WHITE + " [+X]";
                case WEST -> direction1 = "West" + Formatting.WHITE + " [-X]";
                case NORTH -> direction1 = "North" + Formatting.WHITE + " [-Z]";
                case SOUTH -> direction1 = "South" + Formatting.WHITE + " [+Z]";
            }
        }

        String coords1 = coords.getValue() ? coordinates : "";
        i += 10;

        context.drawText(mc.textRenderer, Text.of(direction1), 2, (height - i - 11), color, true);
        context.drawText(mc.textRenderer, Text.of(coords1), 2, (height - i), color, true);

        if (armor.getValue()) renderArmorHUD(true, context);
        if (totems.getValue()) renderTotemHUD(context);
        if (greeter.getValue()) renderGreeter(context);
    }

    private int getStringWidth(String str) {
        return mc.textRenderer.getWidth(str);
    }


    public void renderGreeter(DrawContext context) {
        int width = mc.getWindow().getScaledWidth();
        String text = "";
        if (greeter.getValue())
            text = text + getTimeOfDay() + mc.player.getName().getString();

        context.drawText(mc.textRenderer, Text.of(text), (int) (width / 2.0F - getStringWidth(text) / 2.0F + 2.0F), (int) 2.0F, color, true);
    }

    public static String getTimeOfDay() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(11);
        if (timeOfDay < 12) return "Good Morning ";
        if (timeOfDay < 16) return "Good Afternoon ";
        if (timeOfDay < 21) return "Good Evening ";
        return "Good Night ";
    }

    public void renderTotemHUD(DrawContext context) {
        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();
        int totems = mc.player.getInventory().main.stream().filter(itemStack -> (itemStack.getItem() == Items.TOTEM_OF_UNDYING)).mapToInt(ItemStack::getCount).sum();
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING)
            totems += mc.player.getOffHandStack().getCount();
        if (totems > 0) {
            int i = width / 2;
            int y = height - 55 - ((mc.player.isSubmergedInWater()) ? 10 : 0);
            int x = i - 189 + 180 + 2;
            context.drawItem(totem, x, y);
            context.drawItemInSlot(mc.textRenderer,totem, x, y);
            context.drawCenteredTextWithShadow(mc.textRenderer,totems + "", x + 8, (y - 7), 16777215);
        }
    }

    public void renderArmorHUD(boolean percent, DrawContext context) {
        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();
        int i = width / 2;
        int iteration = 0;
        int y = height - 55 - ((mc.player.isSubmergedInWater()) ? 10 : 0);
        for (ItemStack is : mc.player.getInventory().armor) {
            iteration++;
            if (is.isEmpty())
                continue;
            int x = i - 90 + (9 - iteration) * 20 + 2;

            context.drawItem(is, x, y);
            context.drawItemInSlot(mc.textRenderer,is, x, y);
            String s = (is.getCount() > 1) ? (is.getCount() + "") : "";
            context.drawText(mc.textRenderer,Text.of(s), (x + 19 - 2 - getStringWidth(s)), (y + 9), 16777215, true);
            if (percent) {
                float green = (float) (is.getMaxDamage() - is.getDamage()) / (float) is.getMaxDamage();
                float red = 1.0F - green;
                int dmg = 100 - (int) (red * 100.0F);

                context.drawText(mc.textRenderer,Text.of(dmg + ""), (x + 8 - getStringWidth(dmg + "") / 2), (y - 11), new Color((int) MathUtility.clamp((red * 255.0F), 0, 255f), (int) MathUtility.clamp((green * 255.0F), 0, 255f), 0).getRGB(), true);
            }
        }
    }
}
