package thunder.hack.gui.windows.impl;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;
import org.lwjgl.glfw.GLFW;
import thunder.hack.core.Managers;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.clickui.impl.SliderElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.windows.WindowBase;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ItemSelectSetting;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

import static thunder.hack.features.modules.Module.mc;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class ItemSelectWindow extends WindowBase {
    private Setting<ItemSelectSetting> itemSetting;
    private ArrayList<ItemPlate> itemPlates = new ArrayList<>();
    private ArrayList<ItemPlate> allItems = new ArrayList<>();

    private boolean allTab = true, listening = false;
    private String search = "Search";

    public ItemSelectWindow(Setting<ItemSelectSetting> itemSetting) {
        this(mc.getWindow().getScaledWidth() / 2f - 100, mc.getWindow().getScaledHeight() / 2f - 150, 200, 300, itemSetting);
    }

    public ItemSelectWindow(float x, float y, float width, float height, Setting<ItemSelectSetting> itemSetting) {
        super(x, y, width, height, "Items / " + Formatting.GRAY + itemSetting.getModule().getName(), null, null);
        this.itemSetting = itemSetting;
        refreshItemPlates();

        int id1 = 0;
        for (Block block : Registries.BLOCK) {
            allItems.add(new ItemPlate(id1, id1 * 20, block.asItem(), block.getTranslationKey()));
            id1++;
        }

        for (Item item : Registries.ITEM) {
            allItems.add(new ItemPlate(id1, id1 * 20, item, item.getTranslationKey()));
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

        int tabColor1 = allTab ? new Color(0xD5D5D5).getRGB() : Color.GRAY.getRGB();
        int tabColor2 = allTab ? Color.GRAY.getRGB() : new Color(0xBDBDBD).getRGB();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(getX() + 1.5f, getY() + 29, 0f).color(Color.DARK_GRAY.getRGB());
        bufferBuilder.vertex(getX() + 8, getY() + 29, 0f).color(tabColor1);
        bufferBuilder.vertex(getX() + 8, getY() + 19, 0f).color(tabColor1);
        bufferBuilder.vertex(getX() + 48, getY() + 19, 0f).color(tabColor1);
        bufferBuilder.vertex(getX() + 54, getY() + 29, 0f).color(tabColor1);
        bufferBuilder.vertex(getX() + 52, getY() + 25, 0f).color(tabColor2);
        bufferBuilder.vertex(getX() + 52, getY() + 19, 0f).color(tabColor2);
        bufferBuilder.vertex(getX() + 92, getY() + 19, 0f).color(tabColor2);
        bufferBuilder.vertex(getX() + 100, getY() + 29, 0f).color(Color.GRAY.getRGB());
        bufferBuilder.vertex(getX() + getWidth() - 1, getY() + 29, 0f).color(Color.DARK_GRAY.getRGB());
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        FontRenderers.sf_medium_mini.drawString(context.getMatrices(), "All", getX() + 25, getY() + 25, tabColor1);
        FontRenderers.sf_medium_mini.drawString(context.getMatrices(), "Selected", getX() + 60, getY() + 25, tabColor2);

        if (!allTab && itemPlates.isEmpty()) {
            FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), isRu() ? "Тут пока пусто" : "It's empty here yet",
                    getX() + getWidth() / 2f, getY() + getHeight() / 2f, new Color(0xBDBDBD).getRGB());
        }

        Render2DEngine.addWindow(context.getMatrices(), getX(), getY() + 30, getX() + getWidth(), getY() + getHeight() - 1, 1f);

        for (ItemPlate itemPlate : (allTab ? allItems : itemPlates)) {
            if (itemPlate.offset + getY() + 25 + getScrollOffset() > getY() + getHeight() || itemPlate.offset + getScrollOffset() + getY() + 10 < getY())
                continue;

            context.getMatrices().push();
            context.getMatrices().translate(getX() + 6, itemPlate.offset + getY() + 32 + getScrollOffset(), 0);
            context.drawItem(itemPlate.item().getDefaultStack(), 0, 0);
            context.getMatrices().pop();

            FontRenderers.sf_medium.drawString(context.getMatrices(), I18n.translate(itemPlate.key()), getX() + 26, itemPlate.offset + getY() + 38 + getScrollOffset(), new Color(0xBDBDBD).getRGB());

            boolean hover2 = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 20, itemPlate.offset + getY() + 35 + getScrollOffset(), 11, 11);

            Render2DEngine.drawRect(context.getMatrices(), getX() + getWidth() - 20, itemPlate.offset + getY() + 35 + getScrollOffset(), 11, 11,
                    hover2 ? new Color(0xC57A7A7A, true) : new Color(0xC5575757, true));

            boolean selected = itemPlates.stream().anyMatch(sI -> Objects.equals(sI.key, itemPlate.key));

            if (allTab && !selected) {
                FontRenderers.categories.drawString(context.getMatrices(), "+", getX() + getWidth() - 17, itemPlate.offset + getY() + 39 + getScrollOffset(), -1);
            } else {
                FontRenderers.icons.drawString(context.getMatrices(), "w", getX() + getWidth() - 19.5f, itemPlate.offset + getY() + 39 + getScrollOffset(), -1);
            }
        }
        setMaxElementsHeight((allTab ? allItems : itemPlates).size() * 20);
        Render2DEngine.popWindow();
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        if (Render2DEngine.isHovered(mouseX, mouseY, getX() + 8, getY() + 19, 52, 19)) {
            allTab = true;
            resetScroll();
            Managers.SOUND.playBoolean();
        }

        if (Render2DEngine.isHovered(mouseX, mouseY, getX() + 54, getY() + 19, 70, 19)) {
            allTab = false;
            resetScroll();
            Managers.SOUND.playBoolean();
        }

        if (Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 90, getY() + 3, 70, 10)) {
            listening = true;
            search = "";
        }

        if (Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, getY() + 3, 10, 10))
            mc.setScreen(ClickGUI.getClickGui());

        ArrayList<ItemPlate> copy = Lists.newArrayList(allTab ? allItems : itemPlates);
        for (ItemPlate itemPlate : copy) {
            if ((int) (itemPlate.offset + getY() + 50) + getScrollOffset() > getY() + getHeight())
                continue;

            String name = itemPlate.key().replace("item.minecraft.", "").replace("block.minecraft.", "");

            if (Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 20, itemPlate.offset + getY() + 35 + getScrollOffset(), 10, 10)) {
                boolean selected = itemPlates.stream().anyMatch(sI -> Objects.equals(sI.key(), itemPlate.key));

                if (allTab && !selected) {
                    if (itemSetting.getValue().getItemsById().contains(name))
                        continue;
                    itemSetting.getValue().getItemsById().add(name);
                    refreshItemPlates();
                } else {
                    itemSetting.getValue().getItemsById().remove(name);
                    refreshItemPlates();
                }
                Managers.SOUND.playScroll();
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


    private void refreshItemPlates() {
        itemPlates.clear();

        int id = 0;
        for (Block block : Registries.BLOCK) {
            if (itemSetting.getValue().getItemsById().contains(block.getTranslationKey().replace("block.minecraft.", ""))) {
                itemPlates.add(new ItemPlate(id, id * 20, block.asItem(), block.getTranslationKey()));
                id++;
            }
        }

        for (Item item : Registries.ITEM)
            if (itemSetting.getValue().getItemsById().contains(item.getTranslationKey().replace("item.minecraft.", ""))) {
                itemPlates.add(new ItemPlate(id, id * 20, item, item.getTranslationKey()));
                id++;
            }
    }

    private void refreshAllItems() {
        allItems.clear();
        resetScroll();
        int id1 = 0;
        for (Block block : Registries.BLOCK) {
            if (search.equals("Search") || search.isEmpty() || block.getTranslationKey().contains(search) || I18n.translate(block.getTranslationKey()).toLowerCase().contains(search.toLowerCase())) {
                allItems.add(new ItemPlate(id1, id1 * 20, block.asItem(), block.getTranslationKey()));
                id1++;
            }
        }

        for (Item item : Registries.ITEM) {
            if (search.equals("Search") || search.isEmpty() || item.getTranslationKey().contains(search) || item.getName().getString().toLowerCase().contains(search.toLowerCase())) {
                allItems.add(new ItemPlate(id1, id1 * 20, item, item.getTranslationKey()));
                id1++;
            }
        }
    }

    private record ItemPlate(float id, float offset, Item item, String key) {
    }
}
