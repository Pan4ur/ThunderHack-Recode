package thunder.hack.gui.autobuy;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.impl.TargetHud;
import thunder.hack.gui.thundergui.components.SettingElement;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.AutoBuy;
import thunder.hack.modules.client.ThunderHackGui;
import thunder.hack.utility.SoundUtility;
import thunder.hack.utility.render.MSAAFramebuffer;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static thunder.hack.core.IManager.mc;
import static thunder.hack.utility.render.animation.AnimationUtility.fast;

public class AutoBuyGui extends Screen {

    public static ItemPlate selected_plate, prev_selected_plate;
    public static boolean open_direction = false;
    private static AutoBuyGui INSTANCE;

    static {
        INSTANCE = new AutoBuyGui();
    }

    public final ArrayList<ItemPlate> components = new ArrayList<>();
    public final ArrayList<LogComponent> logs = new ArrayList<>();

    public final CopyOnWriteArrayList<ABGuiCategory> categories = new CopyOnWriteArrayList<>();
    public final ArrayList<SettingElement> settings = new ArrayList<>();

    private final int main_width = 400;
    public int main_posX = 100, main_posY = 100, prevCategoryY, CategoryY, slider_y, slider_x, main_height = 250, drag_x, drag_y, rescale_y, scroll;

    public Category current_category = Category.Items;
    public Category new_category = Category.Items;

    private float category_animation = 1f, settings_animation = 1f;
    private boolean dragging, rescale, first_open = true, searching, draggingSlider;
    private String search_string = "Search";

    public AutoBuyGui() {
        super(Text.of("AutoBuyGui"));
        setInstance();
        load();
        CategoryY = getCategoryY(new_category);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public static AutoBuyGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AutoBuyGui();
        }
        return INSTANCE;
    }

    public static AutoBuyGui getAutoBuyGui() {
        open_direction = true;
        return getInstance();
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && !str.isEmpty()) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public void load() {
        categories.clear();
        components.clear();
        logs.clear();
        scroll = 0;
        draggingSlider = false;

        int module_y = 0;

        selected_plate = null;

        if (current_category == Category.Items) {
            components.add(new ItemPlate(new AutoBuyItem(Items.AIR, new ArrayList<>(), new ArrayList<>(), -1, -1, false), main_posX + 100, main_posY + 40 + module_y, (int) (module_y / 35f)));
            module_y += 35;
            for (AutoBuyItem item : AutoBuy.items) {
                components.add(new ItemPlate(item, main_posX + 100, main_posY + 40 + module_y, module_y / 35));
                module_y += 35;
            }
        }

        if (current_category == Category.Log) {
            module_y += 5;
            for (ItemLog log : AutoBuy.log) {
                logs.add(new LogComponent(log.item(), log.text(), main_posX + 100, main_posY + 40 + module_y, module_y / 35));
                module_y += 35;
            }
        }

        int category_y = 0;
        for (final Category category : Category.values()) {
            categories.add(new ABGuiCategory(category, main_posX + 8, main_posY + 43 + category_y));
            category_y += 17;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (Module.fullNullCheck()) renderBackground(context, mouseX, mouseY, delta);

        context.getMatrices().push();

        AtomicBoolean reload = new AtomicBoolean(false);
        components.forEach(c -> {
            if (c.getAutoBuyItem().getPrice() < -1) {
                AutoBuy.items.remove(c.getAutoBuyItem());
                reload.set(true);
            }
        });

        if (reload.get()) {
            load();
            if (!Objects.equals(search_string, "") && !Objects.equals(search_string, "Search")) {
                components.clear();
                int module_y = 35;
                components.add(new ItemPlate(new AutoBuyItem(Items.AIR, new ArrayList<>(), new ArrayList<>(), -1, -1, false), main_posX + 100, main_posY + 40 + module_y, (int) (module_y / 35f)));
                for (AutoBuyItem abItem : AutoBuy.items) {
                    if (!abItem.getItem().getTranslationKey().replace("block.minecraft.", "").replace("item.minecraft.", "").toLowerCase().contains(search_string)
                            && !I18n.translate(abItem.getItem().getTranslationKey()).toLowerCase().contains(search_string))
                        continue;
                    ItemPlate mPlate = new ItemPlate(abItem, main_posX + 100, main_posY + 40 + module_y, module_y / 35);
                    if (!components.contains(mPlate))
                        components.add(mPlate);
                    module_y += 35;
                }
            }
        }

        MSAAFramebuffer.use(true, () -> renderGui(context, mouseX, mouseY, delta));

        context.getMatrices().pop();
    }

    public void renderGui(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        if (dragging) {
            float deltaX = (mouseX - drag_x) - main_posX;
            float deltaY = (mouseY - drag_y) - main_posY;

            main_posX = mouseX - drag_x;
            main_posY = mouseY - drag_y;

            slider_y += (int) deltaY;
            slider_x += (int) deltaX;

            logs.forEach(log -> log.movePosition(deltaX, deltaY));
            components.forEach(component -> component.movePosition(deltaX, deltaY));
            categories.forEach(category -> category.movePosition(deltaX, deltaY));
        }

        if (rescale) {
            int deltaY = (mouseY - rescale_y) - main_height;
            if (main_height + deltaY > 250)
                main_height += deltaY;
        }

        if (current_category != null && current_category != new_category) {
            prevCategoryY = getCategoryY(current_category);
            CategoryY = getCategoryY(new_category);
            current_category = new_category;
            category_animation = 1;
            slider_y = 0;
            search_string = "Search";
            load();
        }

        category_animation = fast(category_animation, 0, 15f);

        // Основная плита
        Render2DEngine.drawRound(context.getMatrices(), main_posX, main_posY, main_width, main_height, 9f, ThunderHackGui.getColorByTheme(0));

        if (first_open) {
            FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Я осознаю, что AutoBuy находится в состоянии тестирования, и зашел с альтернативного аккаунта", main_posX + 200, main_posY + 100, -1);
            FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "для избежания бана", main_posX + 200, main_posY + 110, -1);
            Render2DEngine.drawRound(context.getMatrices(), main_posX + 150, main_posY + 200, 100, 30, 9f, ThunderHackGui.getColorByTheme(1));
            FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Да!", main_posX + 200, main_posY + 210, -1);
            return;
        }

        // Плита с лого
        Render2DEngine.drawRound(context.getMatrices(), main_posX + 5, main_posY + 5, 90, 30, 7f, ThunderHackGui.getColorByTheme(1));

        context.getMatrices().push();
        context.getMatrices().scale(0.85f, 0.85f, 1);
        context.getMatrices().translate((main_posX + 10) / 0.85, (main_posY + 15) / 0.85, 0);
        FontRenderers.thglitch.drawGradientString(context.getMatrices(), "AUTOBUY", 15, 0, 30, true);
        context.getMatrices().translate(-(main_posX + 10) / 0.85, -(main_posY + 15) / 0.85, 0);
        context.getMatrices().scale(1, 1, 1);
        context.getMatrices().pop();

        // Левая плита под категриями
        Render2DEngine.drawRound(context.getMatrices(), main_posX + 5, main_posY + 40, 90, 55, 7f, ThunderHackGui.getColorByTheme(4));

        Render2DEngine.drawRound(context.getMatrices(), main_posX + 5, main_posY + 100, 90, 25, 7f, isHoveringItem(main_posX + 5, main_posY + 100, 90, 25, (float) mouseX, (float) mouseY) ? ThunderHackGui.getColorByTheme(4) : ThunderHackGui.getColorByTheme(7));
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Все по 10", main_posX + 50, main_posY + 108, -1);

        Render2DEngine.drawRound(context.getMatrices(), main_posX + 5, main_posY + 130, 90, 25, 7f, isHoveringItem(main_posX + 5, main_posY + 130, 90, 25, (float) mouseX, (float) mouseY) ? ThunderHackGui.getColorByTheme(4) : ThunderHackGui.getColorByTheme(7));
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Все по 100", main_posX + 50, main_posY + 138, -1);

        Render2DEngine.drawRound(context.getMatrices(), main_posX + 5, main_posY + 160, 90, 25, 7f, isHoveringItem(main_posX + 5, main_posY + 160, 90, 25, (float) mouseX, (float) mouseY) ? ThunderHackGui.getColorByTheme(4) : ThunderHackGui.getColorByTheme(7));
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Добавить примеры", main_posX + 50, main_posY + 168, -1);

        Render2DEngine.drawRound(context.getMatrices(), main_posX + 5, main_posY + 190, 90, 25, 7f, isHoveringItem(main_posX + 5, main_posY + 190, 90, 25, (float) mouseX, (float) mouseY) ? ThunderHackGui.getColorByTheme(4) : ThunderHackGui.getColorByTheme(7));
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Очистить", main_posX + 50, main_posY + 198, -1);

        Render2DEngine.drawRound(context.getMatrices(), (float) (main_posX + 8), (float) (Render2DEngine.interpolate(CategoryY, prevCategoryY, category_animation)) + slider_y, 84, 15, 2f, ThunderHackGui.getColorByTheme(7));

        if (selected_plate != prev_selected_plate && selected_plate != null) {
            settings.clear();
            prev_selected_plate = selected_plate;
            settings_animation = 1;

            if (selected_plate.getAutoBuyItem().getItem() == Items.AIR) {
                AutoBuy.items.add(new AutoBuyItem(Items.DIRT, new ArrayList<>(), new ArrayList<>(), 100, 1, false));
                selected_plate = null;
                load();
            } else {
                settings.add(new AutoBuyValue("Предмет", selected_plate.getAutoBuyItem()));
                settings.add(new AutoBuyValue("Макс.Цена", selected_plate.getAutoBuyItem()));
                settings.add(new AutoBuyValue("Зачары", selected_plate.getAutoBuyItem()));
                settings.add(new AutoBuyValue("Мин.Кол-во", selected_plate.getAutoBuyItem()));
                settings.add(new AutoBuyValue("Атрибуты", selected_plate.getAutoBuyItem()));
                settings.add(new AutoBuyValue("Проверять на звезду", selected_plate.getAutoBuyItem()));
            }
        }

        settings_animation = fast(settings_animation, 0, 15f);

        float maxYSlider = (components.size() * 35) + 35;
        float maxVisibleSlider = main_height - 35;

        if (current_category == Category.Items) {
            Render2DEngine.drawRect(context.getMatrices(), main_posX + 193, main_posY + 40, 4, main_height - 45, ThunderHackGui.getColorByTheme(7));
            Render2DEngine.addWindow(context.getMatrices(), main_posX + 193, main_posY + 40, main_posX + 197, main_posY + 40 + main_height - 45, 1);
            Render2DEngine.drawRect(context.getMatrices(), main_posX + 194, main_posY + 40 - scroll + 1, 2, (main_height - 47) * Math.min((maxVisibleSlider / maxYSlider), 1f), ThunderHackGui.getColorByTheme(0));
            Render2DEngine.popWindow();
        }

        if (selected_plate != null)
            Render2DEngine.drawRound(context.getMatrices(), (float) Render2DEngine.interpolate(main_posX + 200, selected_plate.getPosX(), settings_animation), (float) Render2DEngine.interpolate(main_posY + 40, selected_plate.getPosY(), settings_animation), (float) Render2DEngine.interpolate(195, 90, settings_animation), (float) Render2DEngine.interpolate(main_height - 45, 30, settings_animation), 4f, ThunderHackGui.getColorByTheme(7));

        Render2DEngine.addWindow(context.getMatrices(), main_posX + 79, main_posY + 35, main_posX + 396 + 40, main_posY + main_height, 1d);
        components.forEach(components -> components.render(context, mouseX, mouseY));
        Render2DEngine.popWindow();

        Render2DEngine.addWindow(context.getMatrices(), main_posX + 79, main_posY + 35, main_posX + main_width, main_posY + main_height, 1d);
        logs.forEach(log -> log.render(context, mouseX, mouseY));
        if (current_category == Category.Log && logs.isEmpty())
            FontRenderers.sf_medium.drawString(context.getMatrices(), "Тут пока ничего нет (", main_posX + 205, main_posY + 50, new Color(0x7BFFFFFF, true).getRGB(), false);
        if (current_category == Category.Settings)
            FontRenderers.sf_medium.drawString(context.getMatrices(), "Скоро..", main_posX + 225, main_posY + 50, new Color(0x7BFFFFFF, true).getRGB(), false);
        Render2DEngine.popWindow();

        categories.forEach(category -> category.render(context.getMatrices(), mouseX, mouseY));

        Render2DEngine.draw2DGradientRect(context.getMatrices(), main_posX + 98, main_posY + 34, main_posX + 191, main_posY + 50, new Color(37, 27, 41, 0), new Color(37, 27, 41, 245), new Color(37, 27, 41, 0), new Color(37, 27, 41, 245));
        Render2DEngine.draw2DGradientRect(context.getMatrices(), main_posX + 98, main_posY + main_height - 15, main_posX + 191, main_posY + main_height, new Color(37, 27, 41, 245), new Color(37, 27, 41, 0), new Color(37, 27, 41, 245), new Color(37, 27, 41, 0));

        Render2DEngine.drawRound(context.getMatrices(), main_posX + 100, main_posY + 5, 295, 30, 7f, new Color(25, 20, 30, 250));

        FontRenderers.sf_medium.drawString(context.getMatrices(), "Предметов: " + (AutoBuy.items.size()) + ", Упешно: " + AutoBuy.successfully, main_posX + 105, main_posY + 18, new Color(0x7BFFFFFF, true).getRGB(), false);

        // Поиск
        Render2DEngine.drawRound(context.getMatrices(), main_posX + 250, main_posY + 15, 140, 10, 3f, new Color(52, 38, 58, 250));
        FontRenderers.icons.drawString(context.getMatrices(), "s", main_posX + 378, main_posY + 18, searching ? new Color(0xCBFFFFFF, true).getRGB() : new Color(0x83FFFFFF, true).getRGB());

        if (isHoveringItem(main_posX + 250, main_posY + 15, 140, 20, mouseX, mouseY)) {
            Render2DEngine.addWindow(context.getMatrices(), main_posX + 250, main_posY + 15, main_posX + 250 + 140, main_posY + 15 + 10, 1);
            Render2DEngine.drawRound(context.getMatrices(), main_posX + 250, main_posY + 15, 140, 10, 3f, new Color(84, 63, 94, 36));
            Render2DEngine.drawBlurredShadow(context.getMatrices(), mouseX - 20, mouseY - 20, 40, 40, 60, new Color(0xC3555A7E, true));
            Render2DEngine.popWindow();
        }

        FontRenderers.sf_medium.drawString(context.getMatrices(), search_string, main_posX + 252, main_posY + 18, searching ? new Color(0xCBFFFFFF, true).getRGB() : new Color(0x83FFFFFF, true).getRGB(), false);

        if (selected_plate == null)
            return;

        context.getMatrices().push();
        TargetHud.sizeAnimation(context.getMatrices(), selected_plate.getPosX(), selected_plate.getPosY(), 1f - settings_animation);
        if (!settings.isEmpty()) {
            float offsetY = 0;
            for (SettingElement element : settings) {
                element.setOffsetY(offsetY);
                element.setX(main_posX + 210);
                element.setY(main_posY + 45);
                element.setWidth(175);
                element.setHeight(15);
                element.render(context.getMatrices(), mouseX, mouseY, partialTicks);
                offsetY += element.getHeight() + 3f;
            }
            FontRenderers.sf_medium_mini.drawString(context.getMatrices(), "Пример зачарований:\nprotection:4 mending:0 unbreaking:2 (через пробел)\nПример атрибутов:\nавто-плавка,бульдозер,пингер (через запятую без пробела)\nНазвание предмета тоже входит в атрибут\nЕсли нужны кирки с кастом зачаром от 2 лвл:\nдобавляем по отдельности кирки с атрибутами:\nпервая - бульдозер I вторая - бульдозер II и тд\nЧтобы удалить предмет, нажми по нему колесиком\n\nУдачной охоты на AutoTransfer вилда!", main_posX + 210, main_posY + 160, new Color(0x7BFFFFFF, true).getRGB(), false);
        }
        context.getMatrices().pop();
    }

    private int getCategoryY(Category category) {
        for (ABGuiCategory categoryPlate : categories) {
            if (categoryPlate.getCategory() == category) {
                return categoryPlate.getPosY();
            }
        }
        return 0;
    }

    public void onTick() {
        components.forEach(ItemPlate::onTick);
        settings.forEach(SettingElement::onTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int clickedButton) {
        if (isHoveringItem(main_posX, main_posY, main_width, 30, (float) mouseX, (float) mouseY)) {
            drag_x = (int) (mouseX - main_posX);
            drag_y = (int) (mouseY - main_posY);
            dragging = true;
        }

        if (isHoveringItem(main_posX + 193, main_posY + 40, 4, main_height - 45, (float) mouseX, (float) mouseY)) {
            draggingSlider = true;
        }

        if (isHoveringItem(main_posX + 150, main_posY + 200, 100, 30, (float) mouseX, (float) mouseY) && first_open) {
            ModuleManager.soundFX.playSound(SoundUtility.MOAN1_SOUNDEVENT);
            first_open = false;
        }

        if (isHoveringItem(main_posX + 5, main_posY + 100, 90, 25, (float) mouseX, (float) mouseY)) {
            for (Item item : Registries.ITEM)
                if (item != Items.AIR)
                    AutoBuy.items.add(new AutoBuyItem(item, new ArrayList<>(), new ArrayList<>(), 10, 1, false));
            load();
        }

        if (isHoveringItem(main_posX + 5, main_posY + 130, 90, 25, (float) mouseX, (float) mouseY)) {
            for (Item item : Registries.ITEM)
                if (item != Items.AIR)
                    AutoBuy.items.add(new AutoBuyItem(item, new ArrayList<>(), new ArrayList<>(), 100, 1, false));
            load();
        }

        if (isHoveringItem(main_posX + 5, main_posY + 160, 90, 25, (float) mouseX, (float) mouseY)) {
            AutoBuy.items.add(new AutoBuyItem(Items.NETHERITE_HELMET, new ArrayList<>(List.of(new Pair<>("protection", 4), new Pair<>("mending", 0))), new ArrayList<>(), 1000000, 1, false));
            AutoBuy.items.add(new AutoBuyItem(Items.NETHERITE_CHESTPLATE, new ArrayList<>(List.of(new Pair<>("protection", 4), new Pair<>("mending", 0))), new ArrayList<>(), 1000000, 1, false));
            AutoBuy.items.add(new AutoBuyItem(Items.NETHERITE_LEGGINGS, new ArrayList<>(List.of(new Pair<>("protection", 4), new Pair<>("mending", 0))), new ArrayList<>(), 1000000, 1, false));
            AutoBuy.items.add(new AutoBuyItem(Items.NETHERITE_BOOTS, new ArrayList<>(List.of(new Pair<>("protection", 4), new Pair<>("mending", 0))), new ArrayList<>(), 1000000, 1, false));
            AutoBuy.items.add(new AutoBuyItem(Items.NETHERITE_PICKAXE, new ArrayList<>(List.of(new Pair<>("mending", 0))), new ArrayList<>(List.of("бульдозер", "авто-плавка")), 1000000, 1, false));
            AutoBuy.items.add(new AutoBuyItem(Items.NETHERITE_SWORD, new ArrayList<>(List.of(new Pair<>("sharpness", 4))), new ArrayList<>(List.of("яд")), 1000000, 1, false));
            AutoBuy.items.add(new AutoBuyItem(Items.ENCHANTED_GOLDEN_APPLE, new ArrayList<>(), new ArrayList<>(), 10000, 1, false));
            AutoBuy.items.add(new AutoBuyItem(Items.GOLDEN_APPLE, new ArrayList<>(), new ArrayList<>(), 100000, 16, false));
            AutoBuy.items.add(new AutoBuyItem(Items.TOTEM_OF_UNDYING, new ArrayList<>(), new ArrayList<>(), 10000, 1, false));
            AutoBuy.items.add(new AutoBuyItem(Items.NETHERITE_SCRAP, new ArrayList<>(), new ArrayList<>(List.of("трапка")), 100000, 1, true));
            AutoBuy.items.add(new AutoBuyItem(Items.PLAYER_HEAD, new ArrayList<>(), new ArrayList<>(List.of("афина")), 2000000, 1, true));
            load();
        }

        if (isHoveringItem(main_posX + 5, main_posY + 190, 90, 25, (float) mouseX, (float) mouseY)) {
            AutoBuy.items.clear();
            load();
        }

        if (isHoveringItem(main_posX + 250, main_posY + 15, 140, 10, (float) mouseX, (float) mouseY)) {
            searching = true;
            ThunderHack.currentKeyListener = ThunderHack.KeyListening.Strings;
        }

        if (isHoveringItem(main_posX, main_posY + main_height - 6, main_width, 12, (float) mouseX, (float) mouseY)) {
            rescale_y = (int) mouseY - main_height;
            rescale = true;
        }

        settings.forEach(component -> component.mouseClicked((int) mouseX, (int) mouseY, clickedButton));
        components.forEach(components -> components.mouseClicked((int) mouseX, (int) mouseY, clickedButton));
        categories.forEach(category -> category.mouseClicked((int) mouseX, (int) mouseY, 1));
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        rescale = false;
        draggingSlider = false;
        settings.forEach(settingElement -> settingElement.mouseReleased((int) mouseX, (int) mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        try {
            keyTyped(GLFW.glfwGetKeyName(keyCode, scanCode), keyCode);
        } catch (IOException ignored) {
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return false;
    }

    public void keyTyped(String typedChar, int keyCode) throws IOException {
        if (ThunderHack.currentKeyListener != ThunderHack.KeyListening.Strings)
            return;

        if (keyCode == 1) {
            open_direction = false;
            searching = false;
        }

        settings.forEach(settingElement -> settingElement.keyTyped(typedChar, keyCode));

        if (searching) {
            if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT || keyCode == GLFW.GLFW_KEY_LEFT_ALT)
                return;

            components.clear();

            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE: {
                    search_string = "Search";
                    searching = false;
                    return;
                }
                case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT: {
                    return;
                }
                case GLFW.GLFW_KEY_ENTER: {
                    search_string = "Search";
                    searching = false;
                    load();
                    return;
                }
                case GLFW.GLFW_KEY_BACKSPACE: {
                    search_string = removeLastChar(search_string);
                    return;
                }
                case GLFW.GLFW_KEY_SPACE: {
                    search_string = search_string + " ";
                    return;
                }
            }

            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) || InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                if (Objects.equals(GLFW.glfwGetKeyName(keyCode, 0), ";")) {
                    search_string = search_string + ":";
                    return;
                }
                if (Objects.equals(GLFW.glfwGetKeyName(keyCode, 0), "-")) {
                    search_string = search_string + "_";
                    return;
                }
                if (GLFW.glfwGetKeyName(keyCode, 0) != null) {
                    search_string = search_string + GLFW.glfwGetKeyName(keyCode, 0).toUpperCase();
                    return;
                }
            }

            if (GLFW.glfwGetKeyName(keyCode, 0) == null)
                return;

            search_string = search_string + GLFW.glfwGetKeyName(keyCode, 0);

            int module_y = 35;

            components.add(new ItemPlate(new AutoBuyItem(Items.AIR, new ArrayList<>(), new ArrayList<>(), -1, -1, false), main_posX + 100, main_posY + 40 + module_y, (int) (module_y / 35f)));

            for (AutoBuyItem abItem : AutoBuy.items) {
                if (!abItem.getItem().getTranslationKey().replace("block.minecraft.", "").replace("item.minecraft.", "").toLowerCase().contains(search_string)
                        && !I18n.translate(abItem.getItem().getTranslationKey()).toLowerCase().contains(search_string))
                    continue;
                ItemPlate mPlate = new ItemPlate(abItem, main_posX + 100, main_posY + 40 + module_y, module_y / 35);
                if (!components.contains(mPlate))
                    components.add(mPlate);
                module_y += 35;
            }
        }
    }


    public boolean isHoveringItem(float x, float y, float x1, float y1, float mouseX, float mouseY) {
        return (mouseX >= x && mouseY >= y && mouseX <= x1 + x && mouseY <= y1 + y);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        final int dWheel = (int) (verticalAmount * 25);

        if (!components.isEmpty()) {
            if (components.get(0).getPosY() > main_posY + 35 && dWheel > 0)
                return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

            if (components.get(components.size() - 1).getPosY() + 35 < main_posY + main_height && dWheel < 0)
                return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        components.forEach(component -> component.scrollElement(dWheel));
        if (current_category == Category.Log)
            logs.forEach(log -> log.scrollElement(dWheel));

        float maxYSlider = (components.size() * 35) + 35;
        float maxVisibleSlider = main_height - 35;

        scroll += (int) (verticalAmount * 25 * (Math.min((maxVisibleSlider / Math.max(maxYSlider, 1f)), 1f)));
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public enum Category {
        Items("Предметы"),
        Log("Лог"),
        Settings("Настройки");

        private final String name;

        Category(String n) {
            name = n;
        }

        public String getName() {
            return name;
        }
    }
}
