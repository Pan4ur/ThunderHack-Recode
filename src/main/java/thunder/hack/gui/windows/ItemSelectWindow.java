package thunder.hack.gui.windows;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.clickui.impl.SliderElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ItemSelectSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static thunder.hack.modules.Module.mc;
import static thunder.hack.modules.client.ClientSettings.isRu;

public class ItemSelectWindow extends WindowBase {

    private Setting<ItemSelectSetting> itemSetting;
    private ArrayList<ItemPlate> itemPlates = new ArrayList<>();
    private ArrayList<ItemPlate> allItems = new ArrayList<>();

    private boolean allTab = true, listening = false;
    private String search = "Search";
    private int scrollOffset = 0, prevScrollOffset = 0;


    public ItemSelectWindow(Setting<ItemSelectSetting> itemSetting) {
        this(mc.getWindow().getScaledWidth() / 2f - 100, mc.getWindow().getScaledHeight() / 2f - 150, 200, 300, itemSetting);
    }

    public ItemSelectWindow(float x, float y, float width, float height, Setting<ItemSelectSetting> itemSetting) {
        super(x, y, width, height, "Items / " + Formatting.GRAY + itemSetting.getModule().getName());
        this.itemSetting = itemSetting;
        refreshItemPlates();

        int id1 = 0;
        for (Block block : Registries.BLOCK) {
            allItems.add(new ItemPlate(id1, id1 * 20, block.asItem()));
            id1++;
        }

        for (Item item : Registries.ITEM) {
            allItems.add(new ItemPlate(id1, id1 * 20, item));
            id1++;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        super.render(context, mouseX, mouseY);
        boolean hover1 = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 90, getY() + 3, 70, 10);

        Render2DEngine.drawRect(context.getMatrices(), getX() + getWidth() - 90, getY() + 3, 70, 10, hover1 ? new Color(0xC5838383, true) : new Color(0xC5575757, true));
        FontRenderers.sf_medium_mini.drawString(context.getMatrices(), search, getX() + getWidth() - 86, getY() + 7, new Color(0xD5D5D5).getRGB());

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        int tabColor1 = allTab ? new Color(0xD5D5D5).getRGB() : Color.GRAY.getRGB();
        int tabColor2 = allTab ? Color.GRAY.getRGB() : new Color(0xBDBDBD).getRGB();

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(getX() + 1.5f, getY() + 29, 0f).color(Color.DARK_GRAY.getRGB()).next();
        bufferBuilder.vertex(getX() + 8, getY() + 29, 0f).color(tabColor1).next();
        bufferBuilder.vertex(getX() + 8, getY() + 19, 0f).color(tabColor1).next();
        bufferBuilder.vertex(getX() + 48, getY() + 19, 0f).color(tabColor1).next();
        bufferBuilder.vertex(getX() + 54, getY() + 29, 0f).color(tabColor1).next();
        bufferBuilder.vertex(getX() + 52, getY() + 25, 0f).color(tabColor2).next();
        bufferBuilder.vertex(getX() + 52, getY() + 19, 0f).color(tabColor2).next();
        bufferBuilder.vertex(getX() + 92, getY() + 19, 0f).color(tabColor2).next();
        bufferBuilder.vertex(getX() + 100, getY() + 29, 0f).color(Color.GRAY.getRGB()).next();
        bufferBuilder.vertex(getX() + getWidth() - 1, getY() + 29, 0f).color(Color.DARK_GRAY.getRGB()).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        FontRenderers.sf_medium_mini.drawString(context.getMatrices(), "All", getX() + 25, getY() + 25, tabColor1);
        FontRenderers.sf_medium_mini.drawString(context.getMatrices(), "Selected", getX() + 60, getY() + 25, tabColor2);


        prevScrollOffset = (int) AnimationUtility.fast(prevScrollOffset, scrollOffset, 12);

        if(!allTab && itemPlates.isEmpty()) {
            FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), isRu() ? "Тут пока пусто" : "It's empty here yet",
                    getX() + getWidth() / 2f, getY() + getHeight() / 2f, new Color(0xBDBDBD).getRGB());
            return;
        }

        for (ItemPlate itemPlate : (allTab ? allItems : itemPlates)) {
            if ((int) (itemPlate.offset + getY() + 50) + prevScrollOffset > getY() + getHeight() || itemPlate.offset + prevScrollOffset + getY() < getY())
                continue;

            context.drawItem(itemPlate.item().getDefaultStack(), (int) (getX() + 6), (int) (itemPlate.offset + getY() + 32) + prevScrollOffset);
            FontRenderers.sf_medium.drawString(context.getMatrices(), itemPlate.item().getName().getString(), (int) (getX() + 26), (int) (itemPlate.offset + getY() + 38) + prevScrollOffset, new Color(0xBDBDBD).getRGB());

            boolean hover2 = Render2DEngine.isHovered(mouseX, mouseY, (int) (getX() + 180), (int) (itemPlate.offset + getY() + 35) + prevScrollOffset, 11, 11);

            Render2DEngine.drawRect(context.getMatrices(), (int) (getX() + 180), (int) (itemPlate.offset + getY() + 35) + prevScrollOffset, 11, 11,
                    hover2 ? new Color(0xC57A7A7A, true) : new Color(0xC5575757, true));

            boolean selected = itemPlates.stream().anyMatch(sI -> sI.item == itemPlate.item);

            if (allTab && !selected) {
                FontRenderers.categories.drawString(context.getMatrices(), "+", getX() + 183, (int) (itemPlate.offset + getY() + 39) + prevScrollOffset, -1);
            } else {
                FontRenderers.icons.drawString(context.getMatrices(), "w", getX() + 180.5f, (int) (itemPlate.offset + getY() + 39) + prevScrollOffset, -1);
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (Render2DEngine.isHovered(mouseX, mouseY, getX() + 8, getY() + 19, 52, 19)) {
            allTab = true;
            prevScrollOffset = 0;
            scrollOffset = 0;
            ThunderHack.soundManager.playBoolean();
        }

        if (Render2DEngine.isHovered(mouseX, mouseY, getX() + 54, getY() + 19, 70, 19)) {
            allTab = false;
            prevScrollOffset = 0;
            scrollOffset = 0;
            ThunderHack.soundManager.playBoolean();
        }

        if (Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 90, getY() + 3, 70, 10)) {
            listening = true;
            search = "";
        }

        if (Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, getY() + 3, 10, 10))
            mc.setScreen(ClickGUI.getClickGui());

        ArrayList<ItemPlate> copy = Lists.newArrayList(allTab ? allItems : itemPlates);
        for (ItemPlate itemPlate : copy) {
            if ((int) (itemPlate.offset + getY() + 50) + prevScrollOffset > getY() + getHeight())
                continue;

            String name = itemPlate.item().getTranslationKey().replace("item.minecraft.", "").replace("block.minecraft.", "");

            if (Render2DEngine.isHovered(mouseX, mouseY, (int) (getX() + 180), (int) (itemPlate.offset + getY() + 35) + prevScrollOffset, 10, 10)) {
                boolean selected = itemPlates.stream().anyMatch(sI -> sI.item == itemPlate.item);

                if (allTab && !selected) {
                    if (itemSetting.getValue().getItemsById().contains(name))
                        continue;
                    itemSetting.getValue().getItemsById().add(name);
                    refreshItemPlates();
                } else {
                    itemSetting.getValue().getItemsById().remove(name);
                    refreshItemPlates();
                }
                ThunderHack.soundManager.playScroll();
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_F && (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL))) {
            listening = !listening;
            return;
        }

        if (listening) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE -> {
                    listening = false;
                    search = "Search";
                    refreshAllItems();
                }

                case GLFW.GLFW_KEY_BACKSPACE -> {
                    search = SliderElement.removeLastChar(search);
                    refreshAllItems();

                    if (Objects.equals(search, "")) {
                        listening = false;
                        search = "Search";
                    }
                }

                case GLFW.GLFW_KEY_SPACE -> search = search + " ";
            }
        }
    }

    @Override
    public void charTyped(char key, int keyCode) {
        if (StringHelper.isValidChar(key) && listening) {
            search = search + key;
            refreshAllItems();
        }
    }

    @Override
    public void mouseScrolled(int i) {
        scrollOffset += i * 2;
    }

    private void refreshItemPlates() {
        itemSetting.getValue().updateItems();
        itemPlates.clear();
        int id = 0;
        for (Item s : itemSetting.getValue().getItems()) {
            itemPlates.add(new ItemPlate(id, id * 20, s));
            id++;
        }
    }

    private void refreshAllItems() {
        allItems.clear();
        prevScrollOffset = 0;
        scrollOffset = 0;
        int id1 = 0;
        for (Block block : Registries.BLOCK) {
            if (search.equals("") || block.asItem().getTranslationKey().contains(search) || block.asItem().getName().getString().toLowerCase().contains(search.toLowerCase())) {
                allItems.add(new ItemPlate(id1, id1 * 20, block.asItem()));
                id1++;
            }
        }

        for (Item item : Registries.ITEM) {
            if (search.equals("") || item.getTranslationKey().contains(search) || item.getName().getString().toLowerCase().contains(search.toLowerCase())) {
                allItems.add(new ItemPlate(id1, id1 * 20, item));
                id1++;
            }
        }
    }

    private record ItemPlate(float id, float offset, Item item) {
    }
}
