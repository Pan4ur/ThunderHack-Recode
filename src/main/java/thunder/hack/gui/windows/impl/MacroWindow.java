package thunder.hack.gui.windows.impl;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.StringHelper;
import org.lwjgl.glfw.GLFW;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.MacroManager;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.clickui.impl.SliderElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.windows.WindowBase;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.PositionSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;

import static thunder.hack.features.modules.Module.mc;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class MacroWindow extends WindowBase {
    private static MacroWindow instance;
    private ArrayList<MacroPlate> macroPlates = new ArrayList<>();
    private int listeningId = -1, addBindKeyCode = -1;
    private ListeningType listeningType;
    private String search = "Search", addName = "Name", addBind = "Bind", addText = "Text";

    private enum ListeningType {
        Name, Text, Bind
    }

    public MacroWindow(float x, float y, float width, float height, Setting<PositionSetting> position) {
        super(x, y, width, height, "Macros", position, TextureStorage.macrosIcon);
        refresh();
    }

    public static MacroWindow get(float x, float y, Setting<PositionSetting> position) {
        if (instance == null)
            instance = new MacroWindow(x, y, 200, 180, position);
        instance.refresh();
        return instance;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        super.render(context, mouseX, mouseY);

        Color color = new Color(0xC5333333, true);
        Color color2 = new Color(0xC55B5B5B, true);
        Color hoveredColor = new Color(0xC5494949, true);
        int textColor = new Color(0xBDBDBD).getRGB();

        boolean hover1 = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 90, getY() + 3, 70, 10);

        Render2DEngine.drawRectWithOutline(context.getMatrices(), getX() + getWidth() - 90, getY() + 3, 70, 10, hover1 ? hoveredColor : color, color2);
        FontRenderers.sf_medium_mini.drawString(context.getMatrices(), search, getX() + getWidth() - 86, getY() + 7, new Color(0xD5D5D5).getRGB());

        if (macroPlates.isEmpty()) {
            FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), isRu() ? "Тут пока пусто" : "It's empty here yet",
                    getX() + getWidth() / 2f, getY() + getHeight() / 2f, new Color(0xBDBDBD).getRGB());
        }

        String blink = (System.currentTimeMillis() / 240) % 2 == 0 ? "" : "l";

        float nameX = getX() + 11;
        float nameWidth = getWidth() / 4.5f;

        float bindX = nameX + nameWidth + 2;
        float bindWidth = 27;

        float textX = bindX + bindWidth + 2;
        float textWidth = getWidth() - (textX - getX()) - 17;

        {
            // Name
            boolean hover2 = Render2DEngine.isHovered(mouseX, mouseY, nameX, getY() + 19, nameWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), nameX, getY() + 19, nameWidth, 11, hover2 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), addName + (listeningId == -3 && listeningType == ListeningType.Name ? blink : "")
                    , nameX + 2, getY() + 23, new Color(0xBDBDBD).getRGB());

            // Bind
            boolean hover3 = Render2DEngine.isHovered(mouseX, mouseY, bindX, getY() + 19, bindWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), bindX, getY() + 19, bindWidth, 11, hover3 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), getSbind(addBind) + (listeningId == -3 && listeningType == ListeningType.Bind ? blink : "")
                    , bindX + 13.5f, getY() + 23, textColor);

            // Text
            boolean hover4 = Render2DEngine.isHovered(mouseX, mouseY, textX, getY() + 19, textWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), textX, getY() + 19, textWidth, 11, hover4 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), addText + (listeningId == -3 && listeningType == ListeningType.Text ? blink : "")
                    , textX + 2, getY() + 23, new Color(0xBDBDBD).getRGB());

            // Add
            boolean hover5 = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, getY() + 19, 11, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), getX() + getWidth() - 15, getY() + 19, 11, 11, hover5 ? hoveredColor : color, color2);
            FontRenderers.categories.drawString(context.getMatrices(), "+", getX() + getWidth() - 12, getY() + 23, -1);
        }

        Render2DEngine.horizontalGradient(context.getMatrices(), getX() + 2, getY() + 33f, getX() + 2 + getWidth() / 2f - 2, getY() + 33.5f, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), getX() + 2 + getWidth() / 2f - 2, getY() + 33f, getX() + 2 + getWidth() - 4, getY() + 33.5f, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));

        //   FontRenderers.sf_medium.drawString(context.getMatrices(), "      Name        l   Bind   l                         Text   ",
        //          getX() + 13, getY() + 40, new Color(0xBDBDBD).getRGB());


        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Name", nameX + nameWidth / 2f, getY() + 40, textColor);
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "l   Bind   l", bindX + bindWidth / 2f, getY() + 40, textColor);
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Text", textX + textWidth / 2f, getY() + 40, textColor);


        Render2DEngine.addWindow(context.getMatrices(), getX(), getY() + 50, getX() + getWidth(), getY() + getHeight() - 1, 1f);

        int id = 0;
        for (MacroPlate macroPlate : macroPlates) {
            id++;
            if ((int) (macroPlate.offset + getY() + 25) + getScrollOffset() > getY() + getHeight() || macroPlate.offset + getScrollOffset() + getY() + 10 < getY())
                continue;

            // Name
            boolean hover2 = Render2DEngine.isHovered(mouseX, mouseY, nameX, macroPlate.offset + getY() + 36 + getScrollOffset(), nameWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), nameX, macroPlate.offset + getY() + 36 + getScrollOffset(), nameWidth, 11, hover2 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), macroPlate.macro().getName() + (macroPlate.id() == listeningId && listeningType == ListeningType.Name ? blink : "")
                    , nameX + 2, macroPlate.offset + getY() + 40 + getScrollOffset(), textColor);

            // Bind
            boolean hover3 = Render2DEngine.isHovered(mouseX, mouseY, bindX, macroPlate.offset + getY() + 36 + getScrollOffset(), bindWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), bindX, macroPlate.offset + getY() + 36 + getScrollOffset(), bindWidth, 11, hover3 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), getSbind(toString(macroPlate.macro().getBind())) + (macroPlate.id() == listeningId && listeningType == ListeningType.Bind ? blink : "")
                    , bindX + 13.5f, macroPlate.offset + getY() + 40 + getScrollOffset(), textColor);

            // Text
            boolean hover4 = Render2DEngine.isHovered(mouseX, mouseY, textX, macroPlate.offset + getY() + 36 + getScrollOffset(), textWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), textX, macroPlate.offset + getY() + 36 + getScrollOffset(), textWidth, 11, hover4 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), macroPlate.macro().getText() + (macroPlate.id() == listeningId && listeningType == ListeningType.Text ? blink : "")
                    , textX + 2, macroPlate.offset + getY() + 40 + getScrollOffset(), textColor);

            // Delete
            boolean hover5 = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, macroPlate.offset + getY() + 36 + getScrollOffset(), 11, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), getX() + getWidth() - 15, macroPlate.offset + getY() + 36 + getScrollOffset(), 11, 11, hover5 ? hoveredColor : color, color2);
            FontRenderers.icons.drawString(context.getMatrices(), "w", getX() + getWidth() - 15, macroPlate.offset + getY() + 40 + getScrollOffset(), -1);
            FontRenderers.sf_medium_mini.drawString(context.getMatrices(), id + ".", getX() + 3, macroPlate.offset + getY() + 41 + getScrollOffset(), textColor);
        }
        setMaxElementsHeight(macroPlates.size() * 20);
        Render2DEngine.popWindow();
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 90, getY() + 3, 70, 10)) {
            listeningId = -2;
            search = "";
        }

        if (Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, getY() + 3, 10, 10))
            mc.setScreen(ClickGUI.getClickGui());

        float nameX = getX() + 11;
        float nameWidth = getWidth() / 4.5f;

        float bindX = nameX + nameWidth + 2;
        float bindWidth = 27;

        float textX = bindX + bindWidth + 2;
        float textWidth = getWidth() - (textX - getX()) - 17;

        boolean hoveringName = Render2DEngine.isHovered(mouseX, mouseY, nameX, getY() + 19, nameWidth, 11);
        boolean hoveringBind = Render2DEngine.isHovered(mouseX, mouseY, bindX, getY() + 19, bindWidth, 11);
        boolean hoveringText = Render2DEngine.isHovered(mouseX, mouseY, textX, getY() + 19, textWidth, 11);
        boolean hoveringAdd = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, getY() + 19, 11, 11);

        if (hoveringName) {
            listeningType = ListeningType.Name;
            addName = "";
        }

        if (hoveringBind) {
            listeningType = ListeningType.Bind;
            addBind = "";
            addBindKeyCode = -1;
        }

        if (hoveringText) {
            listeningType = ListeningType.Text;
            addText = "";
        }

        if (hoveringText || hoveringName || hoveringBind)
            listeningId = -3;

        if (hoveringAdd) {
            if (addBindKeyCode != -1) {
                MacroManager.addMacro(new MacroManager.Macro(addName, addText, addBindKeyCode));
                refresh();
            }
        }


        ArrayList<MacroPlate> copy = Lists.newArrayList(macroPlates);
        for (MacroPlate macroPlate : copy) {
            if ((int) (macroPlate.offset + getY() + 50) + getScrollOffset() > getY() + getHeight())
                continue;

            boolean hoveringName1 = Render2DEngine.isHovered(mouseX, mouseY, nameX, macroPlate.offset + getY() + 36 + getScrollOffset(), nameWidth, 11);
            boolean hoveringBind1 = Render2DEngine.isHovered(mouseX, mouseY, bindX, macroPlate.offset + getY() + 36 + getScrollOffset(), bindWidth, 11);
            boolean hoveringText1 = Render2DEngine.isHovered(mouseX, mouseY, textX, macroPlate.offset + getY() + 36 + getScrollOffset(), textWidth, 11);
            boolean hoveringRemove = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, macroPlate.offset + getY() + 36 + getScrollOffset(), 11, 11);

            if (hoveringName1)
                listeningType = ListeningType.Name;

            if (hoveringBind1)
                listeningType = ListeningType.Bind;

            if (hoveringText1)
                listeningType = ListeningType.Text;

            if (hoveringName1 || hoveringBind1 || hoveringText1)
                listeningId = macroPlate.id;

            if (hoveringRemove) {
                Managers.MACRO.removeMacro(macroPlate.macro());
                refresh();
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_F && (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL))) {
            listeningId = -2;
            return;
        }

        if (keyCode == GLFW.GLFW_KEY_V && (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL))) {

            String paste = GLFW.glfwGetClipboardString(mc.getWindow().getHandle());
            if (paste == null)
                return;

            for (MacroWindow.MacroPlate plate : macroPlates) {
                if (listeningId == plate.id) {
                    switch (listeningType) {
                        case Text -> {
                            plate.macro.setText(paste);
                            return;
                        }
                        case Name -> {
                            plate.macro.setName(paste);
                            return;
                        }
                    }
                }
            }

            if (listeningId == -3) {
                switch (listeningType) {
                    case Name -> addName = paste;
                    case Text -> addText = paste;
                }
            }
            return;
        }

        if (listeningId != -1) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_ENTER -> {
                    if (listeningId != -2) {
                        listeningId = -1;
                    }
                }

                case GLFW.GLFW_KEY_ESCAPE -> {
                    if (listeningId == -2)
                        search = "Search";
                    listeningId = -1;
                    refresh();
                }

                case GLFW.GLFW_KEY_BACKSPACE -> {
                    if (listeningId == -2) {
                        search = SliderElement.removeLastChar(search);
                        refresh();
                        if (Objects.equals(search, "")) {
                            listeningId = -1;
                            search = "Search";
                        }
                        return;
                    }

                    for (MacroPlate plate : macroPlates) {
                        if (listeningId == plate.id) {
                            switch (listeningType) {
                                case Name -> {
                                    plate.macro.setName(SliderElement.removeLastChar(plate.macro.getName()));
                                    return;
                                }
                                case Text -> {
                                    plate.macro.setText(SliderElement.removeLastChar(plate.macro.getText()));
                                    return;
                                }
                                case Bind -> {
                                    return;
                                }
                            }
                        }
                    }

                    if (listeningId == -3) {
                        switch (listeningType) {
                            case Name -> {
                                addName = SliderElement.removeLastChar(addName);
                                return;
                            }
                            case Text -> {
                                addText = SliderElement.removeLastChar(addText);
                                return;
                            }
                            case Bind -> {
                                return;
                            }
                        }
                    }
                }

                case GLFW.GLFW_KEY_SPACE -> {
                    if (listeningId == -2) {
                        search = search + " ";
                        return;
                    }
                }
            }

            for (MacroPlate plate : macroPlates)
                if (keyCode != -1 && listeningId == plate.id && listeningType == ListeningType.Bind) {
                    plate.macro.setBind(keyCode);
                    listeningId = -1;
                }

            if (listeningId == -3 && keyCode != -1 && listeningType == ListeningType.Bind) {
                addBind = toString(keyCode);
                addBindKeyCode = keyCode;
                listeningId = -1;
            }
        }
    }

    @Override
    public void charTyped(char key, int keyCode) {
        if (StringHelper.isValidChar(key) && listeningId != -1) {
            if (listeningId == -2)
                search = search + key;

            for (MacroPlate plate : macroPlates) {
                if (listeningId == plate.id) {
                    switch (listeningType) {
                        case Name -> plate.macro.setName(plate.macro.getName() + key);
                        case Text -> plate.macro.setText(plate.macro.getText() + key);
                        case Bind -> {
                        }
                    }
                    return;
                }
            }

            if (listeningId == -3) {
                switch (listeningType) {
                    case Name -> {
                        addName = addName + key;
                    }
                    case Text -> {
                        addText = addText + key;
                    }
                    case Bind -> {
                    }
                }
            }

            refresh();
        }
    }

    private void refresh() {
        resetScroll();
        macroPlates.clear();
        int id1 = 0;
        for (MacroManager.Macro m : Managers.MACRO.getMacros())
            if (search.equals("Search") || search.isEmpty() || m.getName().contains(search) || m.getText().contains(search)) {
                macroPlates.add(new MacroPlate(id1, id1 * 20 + 18, m));
                id1++;
            }
    }

    public String toString(int key) {
        String kn = key > 0 ? GLFW.glfwGetKeyName(key, GLFW.glfwGetKeyScancode(key)) : "None";

        if (kn == null) {
            try {
                for (Field declaredField : GLFW.class.getDeclaredFields()) {
                    if (declaredField.getName().startsWith("GLFW_KEY_")) {
                        int a = (int) declaredField.get(null);
                        if (a == key) {
                            String nb = declaredField.getName().substring("GLFW_KEY_".length());
                            kn = nb.substring(0, 1).toUpperCase() + nb.substring(1).toLowerCase();
                        }
                    }
                }
            } catch (Exception ignored) {
                kn = "unknown." + key;
            }
        }

        return key == -1 ? "None" : (kn + "").toUpperCase();
    }

    private String getSbind(String sbind) {
        if (sbind.equals("LEFT_CONTROL")) {
            sbind = "LCtrl";
        }
        if (sbind.equals("RIGHT_CONTROL")) {
            sbind = "RCtrl";
        }
        if (sbind.equals("LEFT_SHIFT")) {
            sbind = "LShift";
        }
        if (sbind.equals("RIGHT_SHIFT")) {
            sbind = "RShift";
        }
        if (sbind.equals("LEFT_ALT")) {
            sbind = "LAlt";
        }
        if (sbind.equals("RIGHT_ALT")) {
            sbind = "RAlt";
        }
        return sbind;
    }

    private record MacroPlate(int id, float offset, MacroManager.Macro macro) {
    }
}
