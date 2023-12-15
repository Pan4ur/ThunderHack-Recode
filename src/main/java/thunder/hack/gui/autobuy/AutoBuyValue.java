package thunder.hack.gui.autobuy;

import net.minecraft.block.Block;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Pair;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.components.SettingElement;
import thunder.hack.modules.client.AutoBuy;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static thunder.hack.core.IManager.mc;

public class AutoBuyValue extends SettingElement {

    private final AutoBuyItem item;
    public boolean listening;
    public String Stringnumber = "";
    public String value = "";
    public String name = "";

    public AutoBuyValue(String name, AutoBuyItem item) {
        super(null);
        this.name = name;
        this.item = item;
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && !str.isEmpty()) output = str.substring(0, str.length() - 1);
        return output;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        if ((getY() > AutoBuyGui.getInstance().main_posY + AutoBuyGui.getInstance().height) || getY() < AutoBuyGui.getInstance().main_posY) {
            return;
        }

        if (name.equals("Предмет") && item.getItem() != null) {
            value = item.getItem().getTranslationKey().replace("block.minecraft.", "").replace("item.minecraft.", "");
        }
        if (name.equals("Макс.Цена")) {
            value = item.getPrice() + "";
        }
        if (name.equals("Мин.Кол-во")) {
            value = item.getCount() + "";
        }
        if (name.equals("Зачары")) {
            value = String.join(" ", item.getEnchantmentsToArray());
        }
        if (name.equals("Атрибуты")) {
            value = String.join(",", item.getAttributesToArray());
        }

        FontRenderers.sf_medium.drawString(stack, name, getX(), getY() + 5, isHovered() ? -1 : new Color(0xB0FFFFFF, true).getRGB(), false);

        if(!name.equals("Проверять на звезду")) {
            Render2DEngine.drawRound(stack, x + 50, y + height - 11, 120, 9, 0.5f, (mouseX > x + 50 && mouseX < x + 170 && mouseY > y + height - 11 && mouseY < y + height - 4) ? new Color(82, 57, 100, 178) : new Color(50, 35, 60, 178));

            if (!listening) {
                FontRenderers.sf_medium.drawString(stack, value, x + 53, y + height - 9, new Color(0xBAFFFFFF, true).getRGB(), false);
            } else {
                String s = getRegistered(Stringnumber);
                if (name.equals("Предмет"))
                    FontRenderers.sf_medium.drawString(stack, s, x + 53, y + height - 9, new Color(0x45FFFFFF, true).getRGB(), false);

                FontRenderers.sf_medium.drawString(stack, Objects.equals(Stringnumber, "") ? "..." : Stringnumber + (mc.player != null && (mc.player.age / 10) % 2 == 1 ? "|" : "") , x + 53, y + height - 9, new Color(0xBAFFFFFF, true).getRGB(), false);
            }
        } else {
            if(item.checkForStar())
                Render2DEngine.drawRound(stack, x + 92, y + height - 9, 5, 5, 0.5f, new Color(178, 100, 250, 255));

            if (mouseX > x + 90 && mouseX < x + 99 && mouseY > y + height - 11 && mouseY < y + height - 2)
                Render2DEngine.drawRound(stack, x + 92, y + height - 9, 5, 5, 0.5f, new Color(178, 100, 250, 150));

            Render2DEngine.drawRound(stack, x + 90, y + height - 11, 9, 9, 0.5f,  new Color(50, 35, 60, 178));
        }
    }

    public static String getRegistered(String Name) {
        if (Name == null) return "none";

        for (Block block : Registries.BLOCK)
            if (block.getTranslationKey().replace("block.minecraft.", "").contains(Name.toLowerCase()))
                return block.getTranslationKey().replace("block.minecraft.", "");

        for (Item item : Registries.ITEM)
            if (item.getTranslationKey().replace("item.minecraft.", "").contains(Name.toLowerCase()))
                return item.getTranslationKey().replace("item.minecraft.", "");

        return "dirt";
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if ((getY() > AutoBuyGui.getInstance().main_posY + AutoBuyGui.getInstance().height) || getY() < AutoBuyGui.getInstance().main_posY) {
            return;
        }

        if(!name.equals("Проверять на звезду")) {
            if (mouseX > x + 50 && mouseX < x + 170 && mouseY > y + height - 11 && mouseY < y + height - 4) {
                Stringnumber = value;
                this.listening = true;
            }
        } else {
            if (mouseX > x + 90 && mouseX < x + 99 && mouseY > y + height - 11 && mouseY < y + height - 2) {
                item.setCheckForStar(!item.checkForStar());
            }
        }

        if (listening) ThunderHack.currentKeyListener = ThunderHack.KeyListening.Strings;
    }

    @Override
    public void keyTyped(String typedChar, int keyCode) {
        if (ThunderHack.currentKeyListener != ThunderHack.KeyListening.Strings) return;

        if (this.listening) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE: {
                    listening = false;
                    Stringnumber = "";
                    return;
                }
                case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT: {
                    return;
                }
                case GLFW.GLFW_KEY_ENTER: {
                    try {
                        if (Objects.equals(name, "Мин.Кол-во")) item.setCount(MathUtility.clamp(Integer.valueOf(Stringnumber), 0, 64));
                        if (Objects.equals(name, "Макс.Цена")) item.setPrice(Integer.valueOf(Stringnumber));
                        if (Objects.equals(name, "Предмет")) item.setItem(InventoryUtility.getItem(Stringnumber));
                        if (Objects.equals(name, "Зачары")) {
                            String[] enchArray = Stringnumber.split(" ");
                            ArrayList<Pair<String, Integer>> list = new ArrayList<>();
                            for(String str : enchArray) {
                                Pair<String, Integer> ench = AutoBuy.parseEnchant(str);
                                if(ench != null)
                                    list.add(ench);
                            }
                            item.setEnchantments(list);
                        }
                        if (Objects.equals(name, "Атрибуты")) {
                            String[] atrArray = Stringnumber.split(",");
                            ArrayList<String> list = new ArrayList<>();
                            list.addAll(Arrays.asList(atrArray));
                            item.setAttributes(list);
                        }
                    } catch (Exception e) {
                    }
                    Stringnumber = "";
                    listening = false;
                    return;
                }
                case GLFW.GLFW_KEY_BACKSPACE: {
                    Stringnumber = removeLastChar(Stringnumber);
                    return;
                }
                case GLFW.GLFW_KEY_TAB: {
                    Stringnumber = getRegistered(Stringnumber);
                    return;
                }
                case GLFW.GLFW_KEY_SPACE: {
                    Stringnumber = Stringnumber + " ";
                    return;
                }
            }

            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) || InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                if (Objects.equals(GLFW.glfwGetKeyName(keyCode, 0), ";")) {
                    Stringnumber = Stringnumber + ":";
                    return;
                }
                if (Objects.equals(GLFW.glfwGetKeyName(keyCode, 0), "-")) {
                    Stringnumber = Stringnumber + "_";
                    return;
                }
                if (GLFW.glfwGetKeyName(keyCode, 0) != null) {
                    Stringnumber = Stringnumber + GLFW.glfwGetKeyName(keyCode, 0).toUpperCase();
                    return;
                }
            }

            if (GLFW.glfwGetKeyName(keyCode, 0) == null)
                return;

            Stringnumber = Stringnumber + GLFW.glfwGetKeyName(keyCode, 0);
        }
    }
}
