package thunder.hack.gui.thundergui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ConfigManager;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ThunderHackGui;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.components.*;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.EaseOutBack;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static thunder.hack.features.modules.Module.mc;
import static thunder.hack.utility.render.animation.AnimationUtility.fast;

/**
 * Кто спиздит у того мать у меня под столом
 *
 * @Copyright by Pan4ur#2144
 **/
public class ThunderGui extends Screen {
    public static CurrentMode currentMode = CurrentMode.Modules;
    public static boolean scroll_lock = false;
    public static ModulePlate selected_plate, prev_selected_plate;
    public static EaseOutBack open_animation = new EaseOutBack(5);
    public static boolean open_direction = false;
    private static ThunderGui INSTANCE;

    static {
        INSTANCE = new ThunderGui();
    }

    public final ArrayList<ModulePlate> components = new ArrayList<>();
    public final CopyOnWriteArrayList<CategoryPlate> categories = new CopyOnWriteArrayList<>();
    public final ArrayList<SettingElement> settings = new ArrayList<>();
    public final CopyOnWriteArrayList<ConfigComponent> configs = new CopyOnWriteArrayList<>();
    public final CopyOnWriteArrayList<FriendComponent> friends = new CopyOnWriteArrayList<>();
    private final int main_width = 400;

    public int main_posX = 100;
    public int main_posY = 100;
    public Module.Category current_category = Module.Category.COMBAT;
    public Module.Category new_category = Module.Category.COMBAT;
    float category_animation = 1f;
    float settings_animation = 1f;
    float manager_animation = 1f;
    int prevCategoryY, CategoryY, slider_y, slider_x;
    private int main_height = 250;
    private boolean dragging = false;
    private boolean rescale = false;
    private int drag_x = 0;
    private int drag_y = 0;
    private int rescale_y = 0;
    private float scroll = 0;
    private boolean first_open = true;
    private boolean searching = false;
    private boolean listening_friend = false;
    private boolean listening_config = false;
    private String search_string = "Search";
    private String config_string = "Save config";
    private String friend_string = "Add friend";
    private CurrentMode prevMode = CurrentMode.Modules;

    public static boolean mouse_state;
    public static int mouse_x;
    public static int mouse_y;

    public ThunderGui() {
        super(Text.of("ThunderGui2"));
        this.setInstance();
        this.load();
        CategoryY = getCategoryY(new_category);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public static ThunderGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ThunderGui();
        }
        return INSTANCE;
    }

    public static ThunderGui getThunderGui() {
        open_animation = new EaseOutBack();
        open_direction = true;
        return getInstance();
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && str.length() > 0) {
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
        configs.clear();
        friends.clear();

        int module_y = 0;
        for (Module module : Managers.MODULE.getModulesByCategory(current_category)) {
            components.add(new ModulePlate(module, main_posX + 100, main_posY + 40 + module_y, module_y / 35));
            module_y += 35;
        }

        int category_y = 0;
        for (final Module.Category category : Managers.MODULE.getCategories()) {
            categories.add(new CategoryPlate(category, main_posX + 8, main_posY + 43 + category_y));
            category_y += 17;
        }
    }

    public void loadConfigs() {
        friends.clear();
        configs.clear();
        (new Thread(() -> {
            int config_y = 3;
            for (String file1 : Objects.requireNonNull(Managers.CONFIG.getConfigList())) {
                configs.add(new ConfigComponent(file1, ConfigManager.getConfigDate(file1), main_posX + 100, main_posY + 40 + config_y, config_y / 35));
                config_y += 35;
            }
        })).start();
    }

    public void loadFriends() {
        configs.clear();
        friends.clear();
        int friend_y = 3;
        for (String friend : Managers.FRIEND.getFriends()) {
            friends.add(new FriendComponent(friend, main_posX + 100, main_posY + 40 + friend_y, friend_y / 35));
            friend_y += 35;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (Module.fullNullCheck())
            renderBackground(context, mouseX, mouseY, delta);
        context.getMatrices().push();
        mouse_x = mouseX;
        mouse_y = mouseY;
        if (open_animation.getAnimationd() > 0) {
            renderGui(context, mouseX, mouseY, delta);
        }
        if (open_animation.getAnimationd() <= 0.01 && !open_direction) {
            open_animation = new EaseOutBack();
            mc.currentScreen = null;
            mc.setScreen(null);
        }
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

            this.configs.forEach(configComponent -> configComponent.movePosition(deltaX, deltaY));
            this.friends.forEach(friendComponent -> friendComponent.movePosition(deltaX, deltaY));
            this.components.forEach(component -> component.movePosition(deltaX, deltaY));
            this.categories.forEach(category -> category.movePosition(deltaX, deltaY));
        }

        if (rescale) {
            float deltaY = (mouseY - rescale_y) - main_height;
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
            config_string = "Save config";
            friend_string = "Add friend";
            currentMode = CurrentMode.Modules;
            this.load();
        }

        manager_animation = fast(manager_animation, 0, 15f);
        category_animation = fast(category_animation, 0, 15f);

        // Основная плита / Main GUI
        Render2DEngine.drawRound(context.getMatrices(), main_posX, main_posY, main_width, main_height, 9f, ThunderHackGui.getColorByTheme(0));

        // Плита с лого / Main GUI logo
        Render2DEngine.drawRound(context.getMatrices(), main_posX + 5, main_posY + 5, 90, 30, 7f, ThunderHackGui.getColorByTheme(1));

        context.getMatrices().push();
        context.getMatrices().scale(0.85f, 0.85f, 1);
        context.getMatrices().translate((main_posX + 10) / 0.85, (main_posY + 15) / 0.85, 0);
        FontRenderers.thglitch.drawString(context.getMatrices(), "THUNDERHACK", 0, 0, ThunderHackGui.getColorByTheme(2).getRGB());
        context.getMatrices().translate(-(main_posX + 10) / 0.85, -(main_posY + 15) / 0.85, 0);
        context.getMatrices().scale(1, 1, 1);
        context.getMatrices().pop();

        FontRenderers.settings.drawString(context.getMatrices(), "recode v" + ThunderHack.VERSION, main_posX + 91 - (FontRenderers.settings.getStringWidth("recode v" + ThunderHack.VERSION)), main_posY + 30, ThunderHackGui.getColorByTheme(3).getRGB());

        // Левая плита под категриями
        Render2DEngine.drawRound(context.getMatrices(), main_posX + 5, main_posY + 40, 90, 120, 7f, ThunderHackGui.getColorByTheme(4));

        // Выбор между CfgManager и FriendManager
        if (currentMode == CurrentMode.Modules) {
            Render2DEngine.drawRound(context.getMatrices(), main_posX + 20, main_posY + 195, 60, 20, 4f, ThunderHackGui.getColorByTheme(4));
        } else if (currentMode == CurrentMode.CfgManager) {
            Render2DEngine.drawGradientRound(context.getMatrices(), main_posX + 20, main_posY + 195, 60, 20, 4f, ThunderHackGui.getColorByTheme(4), ThunderHackGui.getColorByTheme(4), ThunderHackGui.getColorByTheme(5), ThunderHackGui.getColorByTheme(5));
        } else {
            Render2DEngine.drawGradientRound(context.getMatrices(), main_posX + 20, main_posY + 195, 60, 20, 4f, ThunderHackGui.getColorByTheme(5), ThunderHackGui.getColorByTheme(5), ThunderHackGui.getColorByTheme(4), ThunderHackGui.getColorByTheme(4));
        }

        Render2DEngine.drawRound(context.getMatrices(), main_posX + 49.5f, main_posY + 197, 1, 16, 0.5f, ThunderHackGui.getColorByTheme(6));

        FontRenderers.mid_icons.drawString(context.getMatrices(), "u", main_posX + 20, main_posY + 196, currentMode == CurrentMode.CfgManager ? ThunderHackGui.getColorByTheme(2).getRGB() : new Color(0x8D8D8D).getRGB());
        FontRenderers.mid_icons.drawString(context.getMatrices(), "v", main_posX + 54, main_posY + 197, currentMode == CurrentMode.FriendManager ? ThunderHackGui.getColorByTheme(2).getRGB() : new Color(0x8D8D8D).getRGB());

        if (isHoveringItem(main_posX + 20, main_posY + 195, 60, 20, mouseX, mouseY)) {
            //   Render2DEngine.drawRound(context.getMatrices(),main_posX + 20, main_posY + 195, 60, 20, 4f, new Color(76, 56, 93, 31));

            Render2DEngine.addWindow(context.getMatrices(), main_posX + 20, main_posY + 195, main_posX + 20 + 60, main_posY + 195 + 20, 1);
            Render2DEngine.drawBlurredShadow(context.getMatrices(), mouseX - 20, mouseY - 20, 40, 40, 60, new Color(0xC3555A7E, true));
            Render2DEngine.popWindow();
        }

        if (first_open) {
            category_animation = 1;
            Render2DEngine.drawRound(context.getMatrices(), (float) (main_posX + 8), (float) CategoryY + slider_y, 84, 15, 2f, ThunderHackGui.getColorByTheme(7));
            first_open = false;
        } else {
            if (currentMode == CurrentMode.Modules)
                Render2DEngine.drawRound(context.getMatrices(), (float) (main_posX + 8), (float) (Render2DEngine.interpolate(CategoryY, prevCategoryY, category_animation)) + slider_y, 84, 15, 2f, ThunderHackGui.getColorByTheme(7));
        }

        if (selected_plate != prev_selected_plate) {
            prev_selected_plate = selected_plate;
            settings_animation = 1;
            settings.clear();
            scroll = 0;

            if (selected_plate != null) {
                for (Setting<?> setting : selected_plate.getModule().getSettings()) {
                    if (setting.getValue() instanceof SettingGroup) {
                        settings.add(new ParentComponent(setting));
                    }
                    if (setting.getValue() instanceof Boolean && !setting.getName().equals("Enabled") && !setting.getName().equals("Drawn")) {
                        settings.add(new BooleanComponent(setting));
                    }
                    if (setting.getValue() instanceof BooleanSettingGroup) {
                        settings.add(new BooleanParentComponent(setting));
                    }
                    if (setting.getValue().getClass().isEnum()) {
                        settings.add(new ModeComponent(setting));
                    }
                    if (setting.getValue() instanceof ColorSetting) {
                        settings.add(new ColorPickerComponent(setting));
                    }
                    if (setting.isNumberSetting() && setting.hasRestriction()) {
                        settings.add(new SliderComponent(setting));
                    }
                }
            }
        }

        settings_animation = fast(settings_animation, 0, 15f);

        if (currentMode != prevMode) {
            if (prevMode != CurrentMode.CfgManager) {
                manager_animation = 1;
                if (currentMode == CurrentMode.CfgManager) {
                    loadConfigs();
                }
            }

            if (prevMode != CurrentMode.FriendManager) {
                manager_animation = 1;
                if (currentMode == CurrentMode.FriendManager) {
                    loadFriends();
                }
            }
            prevMode = currentMode;
        }

        if (selected_plate != null) {
            if (currentMode == CurrentMode.Modules)
                Render2DEngine.drawRound(context.getMatrices(), (float) Render2DEngine.interpolate(main_posX + 200, selected_plate.getPosX(), settings_animation), (float) Render2DEngine.interpolate(main_posY + 40, selected_plate.getPosY(), settings_animation), (float) Render2DEngine.interpolate(195, 90, settings_animation), (float) Render2DEngine.interpolate(main_height - 45, 30, settings_animation), 4f, ThunderHackGui.getColorByTheme(7));
        }


        if (currentMode != CurrentMode.Modules) {
            searching = false;

            Render2DEngine.addWindow(context.getMatrices(), (float) Render2DEngine.interpolate(main_posX + 80, main_posX + 200, manager_animation), main_posY + 39, (float) Render2DEngine.interpolate(399, 195, manager_animation) + main_posX + 36, (float) main_height + main_posY - 3, 1d);

            Render2DEngine.drawRound(context.getMatrices(), main_posX + 100, (float) main_posY + 40, (float) 295, (float) main_height - 44, 4f, ThunderHackGui.getColorByTheme(7));
            this.configs.forEach(components -> components.render(context, mouseX, mouseY));
            this.friends.forEach(components -> components.render(context, mouseX, mouseY));
            Render2DEngine.draw2DGradientRect(context.getMatrices(), main_posX + 102, main_posY + 34, main_posX + 393, main_posY + 60, new Color(25, 20, 30, 0), ThunderHackGui.getColorByTheme(7), new Color(25, 20, 30, 0), new Color(37, 27, 41, 245));
            Render2DEngine.draw2DGradientRect(context.getMatrices(), main_posX + 102, main_posY + main_height - 35, main_posX + 393, main_posY + main_height, ThunderHackGui.getColorByTheme(7), new Color(25, 20, 30, 0), ThunderHackGui.getColorByTheme(7), new Color(37, 27, 41, 0));
            Render2DEngine.popWindow();
        }

        Render2DEngine.addWindow(context.getMatrices(), main_posX + 79, main_posY + 35, main_posX + 396 + 40, main_posY + main_height, 1d);

        this.components.forEach(components -> components.render(context.getMatrices(), mouseX, mouseY));
        Render2DEngine.popWindow();
        this.categories.forEach(category -> category.render(context.getMatrices(), mouseX, mouseY));

        if (currentMode == CurrentMode.Modules) {
            Render2DEngine.draw2DGradientRect(context.getMatrices(), main_posX + 98, main_posY + 34, main_posX + 191, main_posY + 50, new Color(37, 27, 41, 0), new Color(37, 27, 41, 245), new Color(37, 27, 41, 0), new Color(37, 27, 41, 245));
            Render2DEngine.draw2DGradientRect(context.getMatrices(), main_posX + 98, main_posY + main_height - 15, main_posX + 191, main_posY + main_height, new Color(37, 27, 41, 245), new Color(37, 27, 41, 0), new Color(37, 27, 41, 245), new Color(37, 27, 41, 0));
        }

        Render2DEngine.drawRound(context.getMatrices(), main_posX + 100, main_posY + 5, 295, 30, 7f, new Color(25, 20, 30, 250));

        // Конфиг
        if (isHoveringItem(main_posX + 105, main_posY + 14, 11, 11, mouseX, mouseY)) {
            Render2DEngine.drawRound(context.getMatrices(), main_posX + 105, main_posY + 14, 11, 11, 3f, new Color(68, 49, 75, 250));
        } else {
            Render2DEngine.drawRound(context.getMatrices(), main_posX + 105, main_posY + 14, 11, 11, 3f, new Color(52, 38, 58, 250));
        }
        FontRenderers.modules.drawString(context.getMatrices(), "current cfg: " + Managers.CONFIG.currentConfig.getName(), main_posX + 120, main_posY + 18, new Color(0xCDFFFFFF, true).getRGB());
        FontRenderers.icons.drawString(context.getMatrices(), "t", main_posX + 106, main_posY + 17, new Color(0xC2FFFFFF, true).getRGB());

        // Поиск
        Render2DEngine.drawRound(context.getMatrices(), main_posX + 250, main_posY + 15, 140, 10, 3f, new Color(52, 38, 58, 250));
        if (currentMode == CurrentMode.Modules)
            FontRenderers.icons.drawString(context.getMatrices(), "s", main_posX + 378, main_posY + 18, searching ? new Color(0xCBFFFFFF, true).getRGB() : new Color(0x83FFFFFF, true).getRGB());

        if (isHoveringItem(main_posX + 250, main_posY + 15, 140, 20, mouseX, mouseY)) {
            Render2DEngine.addWindow(context.getMatrices(), main_posX + 250, main_posY + 15, main_posX + 250 + 140, main_posY + 15 + 10, 1);
            //   GL11.glPushMatrix();
            Render2DEngine.drawRound(context.getMatrices(), main_posX + 250, main_posY + 15, 140, 10, 3f, new Color(84, 63, 94, 36));
            // Stencil.write(false);
            // Particles.roundedRect(main_posX + 250, main_posY + 15, 140, 10, 6, new Color(0, 0, 0, 255));
            //  Stencil.erase(true);
            Render2DEngine.drawBlurredShadow(context.getMatrices(), mouseX - 20, mouseY - 20, 40, 40, 60, new Color(0xC3555A7E, true));
            // Stencil.dispose();
            // GL11.glPopMatrix();
            Render2DEngine.popWindow();
        }

        if (currentMode == CurrentMode.Modules)
            FontRenderers.modules.drawString(context.getMatrices(), search_string, main_posX + 252, main_posY + 19, searching ? new Color(0xCBFFFFFF, true).getRGB() : new Color(0x83FFFFFF, true).getRGB());
        if (currentMode == CurrentMode.CfgManager) {
            FontRenderers.modules.drawString(context.getMatrices(), config_string, main_posX + 252, main_posY + 19, listening_config ? new Color(0xCBFFFFFF, true).getRGB() : new Color(0x83FFFFFF, true).getRGB());
            Render2DEngine.drawRound(context.getMatrices(), main_posX + 368, main_posY + 17, 20, 6, 1f, isHoveringItem(main_posX + 368, main_posY + 17, 20, 6, mouseX, mouseY) ? new Color(59, 42, 63, 194) : new Color(33, 23, 35, 194));
            FontRenderers.modules.drawCenteredString(context.getMatrices(), "+", main_posX + 378, main_posY + 16, ThunderHackGui.getColorByTheme(2).getRGB());
        }
        if (currentMode == CurrentMode.FriendManager) {
            FontRenderers.modules.drawString(context.getMatrices(), friend_string, main_posX + 252, main_posY + 19, listening_friend ? new Color(0xCBFFFFFF, true).getRGB() : new Color(0x83FFFFFF, true).getRGB());
            Render2DEngine.drawRound(context.getMatrices(), main_posX + 368, main_posY + 17, 20, 6, 1f, isHoveringItem(main_posX + 368, main_posY + 17, 20, 6, mouseX, mouseY) ? new Color(59, 42, 63, 194) : new Color(33, 23, 35, 194));
            FontRenderers.modules.drawCenteredString(context.getMatrices(), "+", main_posX + 378, main_posY + 16, ThunderHackGui.getColorByTheme(2).getRGB());
        }

        if (selected_plate == null) return;

        float scissorX1 = (float) Render2DEngine.interpolate(main_posX + 200, selected_plate.getPosX(), settings_animation) - 20;
        float scissorY1 = (float) Render2DEngine.interpolate(main_posY + 40, selected_plate.getPosY(), settings_animation);
        float scissorX2 = Math.max((float) Render2DEngine.interpolate(395, 90, settings_animation) + main_posX, main_posX + 205) + 40;
        float scissorY2 = Math.max((float) Render2DEngine.interpolate(main_height - 5, 30, settings_animation) + main_posY, main_posY + 45);

        if (scissorX2 < scissorX1) scissorX2 = scissorX1;
        if (scissorY2 < scissorY1) scissorY2 = scissorY1;

        Render2DEngine.addWindow(context.getMatrices(), scissorX1, scissorY1, scissorX2, scissorY2, 1d);

        if (!settings.isEmpty()) {
            float offsetY = 0;
            for (SettingElement element : settings) {
                if (!element.isVisible()) {
                    continue;
                }
                element.setOffsetY(offsetY);
                element.setX(main_posX + 210);
                element.setY(main_posY + 45 + scroll);
                element.setWidth(175);
                element.setHeight(15);

                if (element instanceof ColorPickerComponent)
                    if (((ColorPickerComponent) element).isOpen())
                        element.setHeight(56);

                if (element instanceof ModeComponent) {
                    ModeComponent component = (ModeComponent) element;
                    component.setWHeight(15);

                    if (component.isOpen()) {
                        offsetY += (component.getSetting().getModes().length * 6);
                        element.setHeight(element.getHeight() + (component.getSetting().getModes().length * 6) + 3);
                    } else {
                        element.setHeight(15);
                    }
                }
                element.render(context.getMatrices(), mouseX, mouseY, partialTicks);
                offsetY += element.getHeight() + 3f;
            }
        }
        if (selected_plate != null && settings_animation < 0.99) {
            // Render2DEngine.drawRound(stack,(float) Render2DEngine.interpolate(main_posX + 200, selected_plate.getPosX(), settings_animation), (float) Render2DEngine.interpolate(main_posY + 40, selected_plate.getPosY(), settings_animation), (float) Render2DEngine.interpolate(195, 90, settings_animation), (float) Render2DEngine.interpolate(main_height - 45, 30, settings_animation), 4f, Render2DEngine.applyOpacity(ThunderHackGui.getColorByTheme(7),  settings_animation));
        }
        Render2DEngine.popWindow();
    }

    private int getCategoryY(Module.Category category) {
        for (CategoryPlate categoryPlate : categories) {
            if (categoryPlate.getCategory() == category) {
                return categoryPlate.getPosY();
            }
        }
        return 0;
    }

    public void onTick() {
        open_animation.update(open_direction);
        this.components.forEach(ModulePlate::onTick);
        this.settings.forEach(SettingElement::onTick);
        this.configs.forEach(ConfigComponent::onTick);
        this.friends.forEach(FriendComponent::onTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int clickedButton) {
        mouse_state = true;
        if (isHoveringItem(main_posX + 368, main_posY + 17, 20, 6, (float) mouseX, (float) mouseY)) {
            if (listening_config) {
                Managers.CONFIG.save(config_string);
                config_string = "Save config";
                listening_config = false;
                loadConfigs();
                return super.mouseClicked(mouseX, mouseY, clickedButton);
            }
            if (listening_friend) {
                Managers.FRIEND.addFriend(friend_string);
                friend_string = "Add friend";
                listening_friend = false;
                loadFriends();
                return super.mouseClicked(mouseX, mouseY, clickedButton);
            }
        }
        if (isHoveringItem(main_posX + 105, main_posY + 14, 11, 11, (float) mouseX, (float) mouseY)) {
            try {
                net.minecraft.util.Util.getOperatingSystem().open(new File("ThunderHackRecode/configs/").toURI());
            } catch (Exception e) {
                Command.sendMessage("Не удалось открыть проводник!");
            }
        }

        if (isHoveringItem(main_posX + 20, main_posY + 195, 28, 20, (float) mouseX, (float) mouseY)) {
            current_category = null;
            currentMode = CurrentMode.CfgManager;
            settings.clear();
            components.clear();
        }
        if (isHoveringItem(main_posX + 50, main_posY + 195, 28, 20, (float) mouseX, (float) mouseY)) {
            current_category = null;
            currentMode = CurrentMode.FriendManager;
            settings.clear();
            components.clear();
        }
        if (isHoveringItem(main_posX, main_posY, main_width, 30, (float) mouseX, (float) mouseY)) {
            drag_x = (int) (mouseX - main_posX);
            drag_y = (int) (mouseY - main_posY);
            dragging = true;
        }

        if (isHoveringItem(main_posX + 250, main_posY + 15, 140, 10, (float) mouseX, (float) mouseY) && currentMode == CurrentMode.Modules) {
            searching = true;
            ThunderHack.currentKeyListener = ThunderHack.KeyListening.ThunderGui;
        }

        if (isHoveringItem(main_posX + 250, main_posY + 15, 110, 10, (float) mouseX, (float) mouseY) && currentMode == CurrentMode.CfgManager) {
            listening_config = true;
            ThunderHack.currentKeyListener = ThunderHack.KeyListening.ThunderGui;
        }

        if (isHoveringItem(main_posX + 250, main_posY + 15, 110, 10, (float) mouseX, (float) mouseY) && currentMode == CurrentMode.FriendManager) {
            listening_friend = true;
            ThunderHack.currentKeyListener = ThunderHack.KeyListening.ThunderGui;
        }

        if (isHoveringItem(main_posX, main_posY + main_height - 6, main_width, 12, (float) mouseX, (float) mouseY)) {
            rescale_y = (int) mouseY - main_height;
            rescale = true;
        }

        this.settings.forEach(component -> component.mouseClicked((int) mouseX, (int) mouseY, clickedButton));
        this.components.forEach(components -> components.mouseClicked((int) mouseX, (int) mouseY, clickedButton));
        this.categories.forEach(category -> category.mouseClicked((int) mouseX, (int) mouseY, 0));
        this.configs.forEach(component -> component.mouseClicked((int) mouseX, (int) mouseY, clickedButton));
        this.friends.forEach(component -> component.mouseClicked((int) mouseX, (int) mouseY, clickedButton));
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        mouse_state = false;
        dragging = false;
        rescale = false;
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
        if (ThunderHack.currentKeyListener != ThunderHack.KeyListening.Sliders && ThunderHack.currentKeyListener != ThunderHack.KeyListening.ThunderGui)
            return;

        if (keyCode == 1) {
            open_direction = false;
            searching = false;
        }

        settings.forEach(settingElement -> settingElement.keyTyped(typedChar, keyCode));
        components.forEach(component -> component.keyTyped(typedChar, keyCode));

        if (searching) {
            if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT)
                return;

            components.clear();

            if (search_string.equalsIgnoreCase("search"))
                search_string = "";

            int module_y = 0;

            for (Module module : Managers.MODULE.getModulesSearch(search_string)) {
                ModulePlate mPlate = new ModulePlate(module, main_posX + 100, main_posY + 40 + module_y, module_y / 35);
                if (!components.contains(mPlate))
                    components.add(mPlate);
                module_y += 35;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                search_string = "Search";
                searching = false;
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                search_string = (removeLastChar(search_string));
                return;
            }
            if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z || keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9)
                search_string = (search_string + typedChar);
        }
        if (listening_config) {
            if (config_string.equalsIgnoreCase("Save config")) {
                config_string = "";
            }
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE -> {
                    config_string = "Save config";
                    listening_config = false;
                    return;
                }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    config_string = (removeLastChar(config_string));
                    return;
                }
                case GLFW.GLFW_KEY_ENTER -> {
                    if (!config_string.equals("Save config") && !config_string.equals("")) {
                        Managers.CONFIG.save(config_string);
                        config_string = "Save config";
                        listening_config = false;
                        loadConfigs();
                    }
                    return;
                }
            }
            config_string = (config_string + typedChar);
        }

        if (listening_friend) {
            if (friend_string.equalsIgnoreCase("Add friend")) {
                friend_string = "";
            }
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE -> {
                    friend_string = "Add friend";
                    listening_friend = false;
                    return;
                }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    friend_string = (removeLastChar(friend_string));
                    return;
                }
                case GLFW.GLFW_KEY_ENTER -> {
                    if (!friend_string.equals("Add friend") && !config_string.equals("")) {
                        Managers.FRIEND.addFriend(friend_string);
                        friend_string = "Add friend";
                        listening_friend = false;
                        loadFriends();
                    }
                    return;
                }
            }
            friend_string = (friend_string + typedChar);
        }
    }


    public boolean isHoveringItem(float x, float y, float x1, float y1, float mouseX, float mouseY) {
        return (mouseX >= x && mouseY >= y && mouseX <= x1 + x && mouseY <= y1 + y);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        final float dWheel = (int) (verticalAmount * 10D);
        settings.forEach(component -> component.checkMouseWheel(dWheel));
        if (scroll_lock) {
            scroll_lock = false;
        } else {
            if (isHoveringItem(main_posX + 200, main_posY + 40, main_posX + 395, main_posY - 5 + main_height, (float) mouseX, (float) mouseY))
                scroll += dWheel * ThunderHackGui.scrollSpeed.getValue();
            else {
                components.forEach(component -> component.scrollElement(dWheel * ThunderHackGui.scrollSpeed.getValue()));
            }
            configs.forEach(component -> component.scrollElement(dWheel * ThunderHackGui.scrollSpeed.getValue()));
            friends.forEach(component -> component.scrollElement(dWheel * ThunderHackGui.scrollSpeed.getValue()));
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public enum CurrentMode {
        Modules,
        CfgManager,
        FriendManager,
        WayPointManager,
        MacroManager
    }
}
