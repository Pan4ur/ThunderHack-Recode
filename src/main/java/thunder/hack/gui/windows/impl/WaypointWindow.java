package thunder.hack.gui.windows.impl;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.StringHelper;
import org.lwjgl.glfw.GLFW;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.world.WayPointManager;
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
import java.util.ArrayList;
import java.util.Objects;

import static thunder.hack.features.modules.Module.fullNullCheck;
import static thunder.hack.features.modules.Module.mc;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class WaypointWindow extends WindowBase {
    private static WaypointWindow instance;
    private ArrayList<WaypointPlate> waypointPlates = new ArrayList<>();
    private int listeningId = -1;
    private ListeningType listeningType;

    private String search = "Search", addName = "Name", addX = "X", addY = "Y", addZ = "Z", addServer = "2b2t.org", addDimension = "overworld";

    private enum ListeningType {
        Name, X, Y, Z, Server
    }

    public WaypointWindow(float x, float y, float width, float height, Setting<PositionSetting> position) {
        super(x, y, width, height, "Waypoints", position, TextureStorage.waypointIcon);
        refresh();
    }

    public static WaypointWindow get(float x, float y, Setting<PositionSetting> position) {
        if (instance == null)
            instance = new WaypointWindow(x, y, 340, 180, position);
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

        if (waypointPlates.isEmpty()) {
            FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), isRu() ? "Тут пока пусто" : "It's empty here yet",
                    getX() + getWidth() / 2f, getY() + getHeight() / 2f, new Color(0xBDBDBD).getRGB());
        }

        String blink = (System.currentTimeMillis() / 240) % 2 == 0 ? "" : "l";

        float nameX = getX() + 11;
        float nameWidth = getWidth() / 5.5f;

        float posXX = nameX + nameWidth + 2;
        float posXWidth = getWidth() / 8f;

        float posYX = posXX + posXWidth + 2;
        float posYWidth = getWidth() / 8f;

        float posZX = posYX + posYWidth + 2;
        float posZWidth = getWidth() / 8f;

        float serverX = posZX + posZWidth + 2;
        float serverWidth = getWidth() / 6f;

        float dimensionX = serverX + serverWidth + 2;
        float dimensionWidth = getWidth() / 7f;

        {
            // Name
            boolean hover2 = Render2DEngine.isHovered(mouseX, mouseY, nameX, getY() + 19, nameWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), nameX, getY() + 19, nameWidth, 11, hover2 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), addName + (listeningId == -3 && listeningType == ListeningType.Name ? blink : "")
                    , nameX + 2, getY() + 23, new Color(0xBDBDBD).getRGB());

            // X
            boolean hover3 = Render2DEngine.isHovered(mouseX, mouseY, posXX, getY() + 19, posXWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), posXX, getY() + 19, posXWidth, 11, hover3 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), addX + (listeningId == -3 && listeningType == ListeningType.X ? blink : "")
                    , posXX + 2, getY() + 23, textColor);

            // Y
            boolean hover4 = Render2DEngine.isHovered(mouseX, mouseY, posYX, getY() + 19, posYWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), posYX, getY() + 19, posYWidth, 11, hover4 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), addY + (listeningId == -3 && listeningType == ListeningType.Y ? blink : "")
                    , posYX + 2, getY() + 23, new Color(0xBDBDBD).getRGB());

            // Z
            boolean hover5 = Render2DEngine.isHovered(mouseX, mouseY, posZX, getY() + 19, posZWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), posZX, getY() + 19, posZWidth, 11, hover5 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), addZ + (listeningId == -3 && listeningType == ListeningType.Z ? blink : "")
                    , posZX + 2, getY() + 23, new Color(0xBDBDBD).getRGB());

            // Server
            boolean hover6 = Render2DEngine.isHovered(mouseX, mouseY, serverX, getY() + 19, serverWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), serverX, getY() + 19, serverWidth, 11, hover6 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), addServer + (listeningId == -3 && listeningType == ListeningType.Server ? blink : "")
                    , serverX + 2, getY() + 23, new Color(0xBDBDBD).getRGB());

            // dimension
            boolean hover7 = Render2DEngine.isHovered(mouseX, mouseY, dimensionX, getY() + 19, dimensionWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), dimensionX, getY() + 19, dimensionWidth, 11, hover7 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), addDimension, dimensionX + 2, getY() + 23, new Color(0xBDBDBD).getRGB());

            // Add
            boolean hover8 = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, getY() + 19, 11, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), getX() + getWidth() - 15, getY() + 19, 11, 11, hover8 ? hoveredColor : color, color2);
            FontRenderers.categories.drawString(context.getMatrices(), "+", getX() + getWidth() - 12, getY() + 23, -1);
        }

        Render2DEngine.horizontalGradient(context.getMatrices(), getX() + 2, getY() + 33f, getX() + 2 + getWidth() / 2f - 2, getY() + 33.5f, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), getX() + 2 + getWidth() / 2f - 2, getY() + 33f, getX() + 2 + getWidth() - 4, getY() + 33.5f, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));


        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Name", nameX + nameWidth / 2f, getY() + 40, textColor);
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "X", posXX + posXWidth / 2f, getY() + 40, textColor);
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Y", posYX + posYWidth / 2f, getY() + 40, textColor);
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Z", posZX + posZWidth / 2f, getY() + 40, textColor);
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Server", serverX + serverWidth / 2f, getY() + 40, textColor);
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Dimension", dimensionX + dimensionWidth / 2f, getY() + 40, textColor);

        Render2DEngine.addWindow(context.getMatrices(), getX(), getY() + 50, getX() + getWidth(), getY() + getHeight() - 1, 1f);

        int id = 0;
        for (WaypointPlate waypointPlate : waypointPlates) {
            id++;
            if ((int) (waypointPlate.offset + getY() + 25) + getScrollOffset() > getY() + getHeight() || waypointPlate.offset + getScrollOffset() + getY() + 10 < getY())
                continue;

            boolean hover2 = Render2DEngine.isHovered(mouseX, mouseY, nameX, getY() + 36 + getScrollOffset() + waypointPlate.offset, nameWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), nameX, getY() + 36 + getScrollOffset() + waypointPlate.offset, nameWidth, 11, hover2 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), waypointPlate.waypoint.getName() + (listeningId == waypointPlate.id && listeningType == ListeningType.Name ? blink : "")
                    , nameX + 2, getY() + 40 + getScrollOffset() + waypointPlate.offset, new Color(0xBDBDBD).getRGB());

            // X
            boolean hover3 = Render2DEngine.isHovered(mouseX, mouseY, posXX, getY() + 36 + getScrollOffset() + waypointPlate.offset, posXWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), posXX, getY() + 36 + getScrollOffset() + waypointPlate.offset, posXWidth, 11, hover3 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), waypointPlate.waypoint.getX() + (listeningId == waypointPlate.id && listeningType == ListeningType.X ? blink : "")
                    , posXX + 2, getY() + 40 + getScrollOffset() + waypointPlate.offset, textColor);

            // Y
            boolean hover4 = Render2DEngine.isHovered(mouseX, mouseY, posYX, getY() + 36 + getScrollOffset() + waypointPlate.offset, posYWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), posYX, getY() + 36 + getScrollOffset() + waypointPlate.offset, posYWidth, 11, hover4 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), waypointPlate.waypoint.getY() + (listeningId == waypointPlate.id && listeningType == ListeningType.Y ? blink : "")
                    , posYX + 2, getY() + 40 + getScrollOffset() + waypointPlate.offset, new Color(0xBDBDBD).getRGB());

            // Z
            boolean hover5 = Render2DEngine.isHovered(mouseX, mouseY, posZX, getY() + 36 + getScrollOffset() + waypointPlate.offset, posZWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), posZX, getY() + 36 + getScrollOffset() + waypointPlate.offset, posZWidth, 11, hover5 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), waypointPlate.waypoint.getZ() + (listeningId == waypointPlate.id && listeningType == ListeningType.Z ? blink : "")
                    , posZX + 2, getY() + 40 + getScrollOffset() + waypointPlate.offset, new Color(0xBDBDBD).getRGB());

            // Server
            boolean hover6 = Render2DEngine.isHovered(mouseX, mouseY, serverX, getY() + 36 + getScrollOffset() + waypointPlate.offset, serverWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), serverX, getY() + 36 + getScrollOffset() + waypointPlate.offset, serverWidth, 11, hover6 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), waypointPlate.waypoint.getServer() + (listeningId == waypointPlate.id && listeningType == ListeningType.Server ? blink : "")
                    , serverX + 2, getY() + 40 + getScrollOffset() + waypointPlate.offset, new Color(0xBDBDBD).getRGB());

            // dimension
            boolean hover7 = Render2DEngine.isHovered(mouseX, mouseY, dimensionX, getY() + 36 + getScrollOffset() + waypointPlate.offset, dimensionWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), dimensionX, getY() + 36 + getScrollOffset() + waypointPlate.offset, dimensionWidth, 11, hover7 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), waypointPlate.waypoint.getDimension(), dimensionX + 2, getY() + 40 + getScrollOffset() + waypointPlate.offset, new Color(0xBDBDBD).getRGB());

            // Add
            boolean hover8 = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, getY() + 36 + getScrollOffset() + waypointPlate.offset, 11, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), getX() + getWidth() - 15, getY() + 36 + getScrollOffset() + waypointPlate.offset, 11, 11, hover8 ? hoveredColor : color, color2);
            FontRenderers.icons.drawString(context.getMatrices(), "w", getX() + getWidth() - 15, waypointPlate.offset + getY() + 40 + getScrollOffset(), -1);
            FontRenderers.sf_medium_mini.drawString(context.getMatrices(), id + ".", getX() + 3, getY() + 41 + getScrollOffset() + waypointPlate.offset, textColor);
        }
        setMaxElementsHeight(waypointPlates.size() * 20);
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
        float nameWidth = getWidth() / 5.5f;

        float posXX = nameX + nameWidth + 2;
        float posXWidth = getWidth() / 8f;

        float posYX = posXX + posXWidth + 2;
        float posYWidth = getWidth() / 8f;

        float posZX = posYX + posYWidth + 2;
        float posZWidth = getWidth() / 8f;

        float serverX = posZX + posZWidth + 2;
        float serverWidth = getWidth() / 6f;

        float dimensionX = serverX + serverWidth + 2;
        float dimensionWidth = getWidth() / 7f;

        {
            boolean hoveringName = Render2DEngine.isHovered(mouseX, mouseY, nameX, getY() + 19, nameWidth, 11);
            boolean hoveringX = Render2DEngine.isHovered(mouseX, mouseY, posXX, getY() + 19, posXWidth, 11);
            boolean hoveringY = Render2DEngine.isHovered(mouseX, mouseY, posYX, getY() + 19, posYWidth, 11);
            boolean hoveringZ = Render2DEngine.isHovered(mouseX, mouseY, posZX, getY() + 19, posZWidth, 11);
            boolean hoveringServer = Render2DEngine.isHovered(mouseX, mouseY, serverX, getY() + 19, serverWidth, 11);
            boolean hoveringDimension = Render2DEngine.isHovered(mouseX, mouseY, dimensionX, getY() + 19, dimensionWidth, 11);
            boolean hoveringAdd = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, getY() + 19, 11, 11);

            if (hoveringName) {
                listeningType = ListeningType.Name;
                addName = "";
            }

            if (hoveringX) {
                listeningType = ListeningType.X;
                addX = "";
            }

            if (hoveringY) {
                listeningType = ListeningType.Y;
                addY = "";
            }

            if (hoveringZ) {
                listeningType = ListeningType.Z;
                addZ = "";
            }

            if (hoveringServer) {
                listeningType = ListeningType.Server;
                addServer = "";
            }

            if (hoveringDimension) {
                addDimension = switchType(addDimension);
            }

            if (hoveringName || hoveringX || hoveringY || hoveringZ || hoveringServer || hoveringDimension)
                listeningId = -3;

            if (hoveringAdd) {
                try {
                    Managers.WAYPOINT.addWayPoint(new WayPointManager.WayPoint(Integer.parseInt(addX), Integer.parseInt(addY), Integer.parseInt(addZ), addName, addServer, addDimension));
                } catch (Exception ignored) {
                }
                refresh();
            }
        }

        ArrayList<WaypointPlate> copy = Lists.newArrayList(waypointPlates);
        for (WaypointPlate waypointPlate : copy) {
            if ((int) (waypointPlate.offset + getY() + 50) + getScrollOffset() > getY() + getHeight())
                continue;

            boolean hoveringName = Render2DEngine.isHovered(mouseX, mouseY, nameX, getY() + 36 + getScrollOffset() + waypointPlate.offset, nameWidth, 11);
            boolean hoveringX = Render2DEngine.isHovered(mouseX, mouseY, posXX, getY() + 36 + getScrollOffset() + waypointPlate.offset, posXWidth, 11);
            boolean hoveringY = Render2DEngine.isHovered(mouseX, mouseY, posYX, getY() + 36 + getScrollOffset() + waypointPlate.offset, posYWidth, 11);
            boolean hoveringZ = Render2DEngine.isHovered(mouseX, mouseY, posZX, getY() + 36 + getScrollOffset() + waypointPlate.offset, posZWidth, 11);
            boolean hoveringServer = Render2DEngine.isHovered(mouseX, mouseY, serverX, getY() + 36 + getScrollOffset() + waypointPlate.offset, serverWidth, 11);
            boolean hoveringDimension = Render2DEngine.isHovered(mouseX, mouseY, dimensionX, getY() + 36 + getScrollOffset() + waypointPlate.offset, dimensionWidth, 11);
            boolean hoveringRemove = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, getY() + 36 + getScrollOffset() + waypointPlate.offset, 11, 11);

            if (hoveringName)
                listeningType = ListeningType.Name;

            if (hoveringX)
                listeningType = ListeningType.X;

            if (hoveringY)
                listeningType = ListeningType.Y;

            if (hoveringZ)
                listeningType = ListeningType.Z;

            if (hoveringServer)
                listeningType = ListeningType.Server;

            if (hoveringDimension)
                waypointPlate.waypoint.setDimension(switchType(waypointPlate.waypoint.getDimension()));

            if (hoveringName || hoveringX || hoveringY || hoveringZ || hoveringServer || hoveringDimension)
                listeningId = waypointPlate.id;

            if (hoveringRemove) {
                Managers.WAYPOINT.removeWayPoint(waypointPlate.waypoint);
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

                    for (WaypointPlate plate : waypointPlates) {
                        if (listeningId == plate.id) {
                            switch (listeningType) {
                                case Name -> {
                                    plate.waypoint.setName(SliderElement.removeLastChar(plate.waypoint.getName()));
                                    return;
                                }
                                case Server -> {
                                    plate.waypoint.setServer(SliderElement.removeLastChar(plate.waypoint.getServer()));
                                    return;
                                }
                                case X -> {
                                    String num = SliderElement.removeLastChar(String.valueOf(plate.waypoint.getX()));
                                    if (num.isEmpty())
                                        num = "0";

                                    plate.waypoint.setX(Integer.parseInt(num));
                                    return;
                                }
                                case Y -> {
                                    String num = SliderElement.removeLastChar(String.valueOf(plate.waypoint.getY()));
                                    if (num.isEmpty())
                                        num = "0";

                                    plate.waypoint.setY(Integer.parseInt(num));
                                    return;
                                }
                                case Z -> {
                                    String num = SliderElement.removeLastChar(String.valueOf(plate.waypoint.getZ()));
                                    if (num.isEmpty())
                                        num = "0";

                                    plate.waypoint.setZ(Integer.parseInt(num));
                                    return;
                                }
                            }
                        }
                    }

                    if (listeningId == -3) {
                        switch (listeningType) {
                            case Name -> addName = SliderElement.removeLastChar(addName);
                            case Server -> addServer = SliderElement.removeLastChar(addServer);
                            case X -> addX = SliderElement.removeLastChar(addX);
                            case Y -> addY = SliderElement.removeLastChar(addY);
                            case Z -> addZ = SliderElement.removeLastChar(addZ);
                        }
                    }
                }

                case GLFW.GLFW_KEY_SPACE -> {
                    if (listeningId == -2)
                        search = search + " ";
                }
            }
        }
    }

    @Override
    public void charTyped(char key, int keyCode) {
        if (StringHelper.isValidChar(key) && listeningId != -1) {
            if (listeningId == -2)
                search = search + key;

            for (WaypointPlate plate : waypointPlates) {
                if (listeningId == plate.id) {
                    try {
                        switch (listeningType) {
                            case Name -> plate.waypoint.setName(plate.waypoint.getName() + key);
                            case Server -> plate.waypoint.setServer(plate.waypoint.getServer() + key);
                            case X ->
                                    plate.waypoint.setX(Integer.parseInt(String.valueOf(plate.waypoint.getX()) + key));
                            case Y ->
                                    plate.waypoint.setY(Integer.parseInt(String.valueOf(plate.waypoint.getY()) + key));
                            case Z ->
                                    plate.waypoint.setZ(Integer.parseInt(String.valueOf(plate.waypoint.getZ()) + key));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }

            if (listeningId == -3) {
                try {
                    switch (listeningType) {
                        case Name -> addName = addName + key;
                        case Server -> addServer = addServer + key;
                        case X -> addX = addX + key;
                        case Y -> addY = addY + key;
                        case Z -> addZ = addZ + key;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            refresh();
        }
    }

    private void refresh() {
        resetScroll();
        waypointPlates.clear();
        int id1 = 0;
        for (WayPointManager.WayPoint w : Managers.WAYPOINT.getWayPoints())
            if (search.equals("Search") || search.isEmpty() || w.getName().contains(search) || w.getServer().contains(search)) {
                waypointPlates.add(new WaypointPlate(id1, id1 * 20 + 18, w));
                id1++;
            }

        addServer = mc.isInSingleplayer() || fullNullCheck() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address;
        addDimension = fullNullCheck() ? "overworld" : mc.world.getRegistryKey().getValue().getPath();
    }

    private String switchType(String t) {
        switch (t) {
            case "the_end" -> {
                return "overworld";
            }
            case "overworld" -> {
                return "the_nether";
            }
            case "the_nether" -> {
                return "the_end";
            }
        }
        return "overworld";
    }

    @Override
    public int getMinWidth() {
        return 340;
    }

    private record WaypointPlate(int id, float offset, WayPointManager.WayPoint waypoint) {
    }
}
