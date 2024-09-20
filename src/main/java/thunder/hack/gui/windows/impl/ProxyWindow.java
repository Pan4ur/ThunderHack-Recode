package thunder.hack.gui.windows.impl;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;
import org.lwjgl.glfw.GLFW;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ProxyManager;
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

import static thunder.hack.features.modules.Module.mc;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class ProxyWindow extends WindowBase {
    // name ip port login password
    private static ProxyWindow instance;
    private ArrayList<ProxyPlate> proxyPlates = new ArrayList<>();
    private int listeningId = -1;
    private ListeningType listeningType;

    private String search = "Search", addIp = "Ip", addPort = "Port", addPassword = "Password", addLogin = "Login", addName = "Name";
    private String[] pinging = new String[]{".", "..", "..."};

    private enum ListeningType {
        Name, Ip, Port, Login, Password
    }

    public ProxyWindow(float x, float y, float width, float height, Setting<PositionSetting> position) {
        super(x, y, width, height, "Proxies", position, TextureStorage.proxyIcon);
        refresh();
    }

    public static ProxyWindow get(float x, float y, Setting<PositionSetting> position) {
        if (instance == null)
            instance = new ProxyWindow(x, y, 340, 180, position);
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

        if (proxyPlates.isEmpty()) {
            FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), isRu() ? "Тут пока пусто" : "It's empty here yet",
                    getX() + getWidth() / 2f, getY() + getHeight() / 2f, new Color(0xBDBDBD).getRGB());
        }

        String blink = (System.currentTimeMillis() / 240) % 2 == 0 ? "" : "l";

        float nameX = getX() + 11;
        float nameWidth = getWidth() / 5.5f;

        float ipX = nameX + nameWidth + 2;
        float posXWidth = getWidth() / 5f;

        float portX = ipX + posXWidth + 2;
        float posYWidth = getWidth() / 12f;

        float posZX = portX + posYWidth + 2;
        float posZWidth = getWidth() / 6.5f;

        float serverX = posZX + posZWidth + 2;
        float serverWidth = getWidth() / 6f;

        {
            boolean hover2 = Render2DEngine.isHovered(mouseX, mouseY, nameX, getY() + 19, nameWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), nameX, getY() + 19, nameWidth, 11, hover2 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), addName + (listeningId == -3 && listeningType == ListeningType.Name ? blink : "")
                    , nameX + 2, getY() + 23, new Color(0xBDBDBD).getRGB());

            boolean hover3 = Render2DEngine.isHovered(mouseX, mouseY, ipX, getY() + 19, posXWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), ipX, getY() + 19, posXWidth, 11, hover3 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), addIp + (listeningId == -3 && listeningType == ListeningType.Ip ? blink : "")
                    , ipX + 2, getY() + 23, textColor);

            boolean hover4 = Render2DEngine.isHovered(mouseX, mouseY, portX, getY() + 19, posYWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), portX, getY() + 19, posYWidth, 11, hover4 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), addPort + (listeningId == -3 && listeningType == ListeningType.Port ? blink : "")
                    , portX + 2, getY() + 23, new Color(0xBDBDBD).getRGB());

            boolean hover5 = Render2DEngine.isHovered(mouseX, mouseY, posZX, getY() + 19, posZWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), posZX, getY() + 19, posZWidth, 11, hover5 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), addLogin + (listeningId == -3 && listeningType == ListeningType.Login ? blink : "")
                    , posZX + 2, getY() + 23, new Color(0xBDBDBD).getRGB());

            boolean hover6 = Render2DEngine.isHovered(mouseX, mouseY, serverX, getY() + 19, serverWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), serverX, getY() + 19, serverWidth, 11, hover6 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), addPassword + (listeningId == -3 && listeningType == ListeningType.Password ? blink : "")
                    , serverX + 2, getY() + 23, new Color(0xBDBDBD).getRGB());

            boolean hover8 = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, getY() + 19, 11, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), getX() + getWidth() - 15, getY() + 19, 11, 11, hover8 ? hoveredColor : color, color2);
            FontRenderers.categories.drawString(context.getMatrices(), "+", getX() + getWidth() - 12, getY() + 23, -1);
        }

        Render2DEngine.horizontalGradient(context.getMatrices(), getX() + 2, getY() + 33f, getX() + 2 + getWidth() / 2f - 2, getY() + 33.5f, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), getX() + 2 + getWidth() / 2f - 2, getY() + 33f, getX() + 2 + getWidth() - 4, getY() + 33.5f, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));


        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Name", nameX + nameWidth / 2f, getY() + 40, textColor);
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "IP", ipX + posXWidth / 2f, getY() + 40, textColor);
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Port", portX + posYWidth / 2f, getY() + 40, textColor);
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Login", posZX + posZWidth / 2f, getY() + 40, textColor);
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Password", serverX + serverWidth / 2f, getY() + 40, textColor);

        Render2DEngine.addWindow(context.getMatrices(), getX(), getY() + 50, getX() + getWidth(), getY() + getHeight() - 1, 1f);

        int id = 0;
        for (ProxyPlate proxyPlate : proxyPlates) {
            id++;
            if ((int) (proxyPlate.offset + getY() + 25) + getScrollOffset() > getY() + getHeight() || proxyPlate.offset + getScrollOffset() + getY() + 10 < getY())
                continue;

            boolean hover2 = Render2DEngine.isHovered(mouseX, mouseY, nameX, getY() + 36 + getScrollOffset() + proxyPlate.offset, nameWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), nameX, getY() + 36 + getScrollOffset() + proxyPlate.offset, nameWidth, 11, hover2 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), proxyPlate.proxy.getName() + (listeningId == proxyPlate.id && listeningType == ListeningType.Name ? blink : "")
                    , nameX + 2, getY() + 40 + getScrollOffset() + proxyPlate.offset, new Color(0xBDBDBD).getRGB());

            boolean hover3 = Render2DEngine.isHovered(mouseX, mouseY, ipX, getY() + 36 + getScrollOffset() + proxyPlate.offset, posXWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), ipX, getY() + 36 + getScrollOffset() + proxyPlate.offset, posXWidth, 11, hover3 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), proxyPlate.proxy.getIp() + (listeningId == proxyPlate.id && listeningType == ListeningType.Ip ? blink : "")
                    , ipX + 2, getY() + 40 + getScrollOffset() + proxyPlate.offset, textColor);

            boolean hover4 = Render2DEngine.isHovered(mouseX, mouseY, portX, getY() + 36 + getScrollOffset() + proxyPlate.offset, posYWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), portX, getY() + 36 + getScrollOffset() + proxyPlate.offset, posYWidth, 11, hover4 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), proxyPlate.proxy.getPort() + (listeningId == proxyPlate.id && listeningType == ListeningType.Port ? blink : "")
                    , portX + 2, getY() + 40 + getScrollOffset() + proxyPlate.offset, new Color(0xBDBDBD).getRGB());

            boolean hover5 = Render2DEngine.isHovered(mouseX, mouseY, posZX, getY() + 36 + getScrollOffset() + proxyPlate.offset, posZWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), posZX, getY() + 36 + getScrollOffset() + proxyPlate.offset, posZWidth, 11, hover5 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), hover5 ? (proxyPlate.proxy.getL() + (listeningId == proxyPlate.id && listeningType == ListeningType.Login ? blink : "")) : "*******"
                    , posZX + 2, getY() + 40 + getScrollOffset() + proxyPlate.offset, new Color(0xBDBDBD).getRGB());

            boolean hover6 = Render2DEngine.isHovered(mouseX, mouseY, serverX, getY() + 36 + getScrollOffset() + proxyPlate.offset, serverWidth, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), serverX, getY() + 36 + getScrollOffset() + proxyPlate.offset, serverWidth, 11, hover6 ? hoveredColor : color, color2);
            FontRenderers.sf_medium.drawString(context.getMatrices(), hover6 ? (proxyPlate.proxy.getP() + (listeningId == proxyPlate.id && listeningType == ListeningType.Password ? blink : "")) : "*******"
                    , serverX + 2, getY() + 40 + getScrollOffset() + proxyPlate.offset, new Color(0xBDBDBD).getRGB());

            boolean hover8 = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, getY() + 36 + getScrollOffset() + proxyPlate.offset, 11, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), getX() + getWidth() - 15, getY() + 36 + getScrollOffset() + proxyPlate.offset, 11, 11, hover8 ? hoveredColor : color, color2);
            FontRenderers.icons.drawString(context.getMatrices(), "w", getX() + getWidth() - 15, proxyPlate.offset + getY() + 40 + getScrollOffset(), -1);
            FontRenderers.sf_medium_mini.drawString(context.getMatrices(), id + ".", getX() + 3, getY() + 41 + getScrollOffset() + proxyPlate.offset, textColor);

            boolean selected = Managers.PROXY.getActiveProxy() == proxyPlate.proxy;
            boolean hover9 = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 28, getY() + 36 + getScrollOffset() + proxyPlate.offset, 11, 11);
            Render2DEngine.drawRectWithOutline(context.getMatrices(), getX() + getWidth() - 28, getY() + 36 + getScrollOffset() + proxyPlate.offset, 11, 11, hover9 ? hoveredColor : color, color2);
            FontRenderers.icons.drawString(context.getMatrices(), selected ? "i" : "k", getX() + getWidth() - 26, proxyPlate.offset + getY() + 41 + getScrollOffset(), selected ? -1 : Color.GRAY.getRGB());

            boolean hover10 = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 51.5f, getY() + 36 + getScrollOffset() + proxyPlate.offset, 21.5f, 11);
            String ping = proxyPlate.proxy.getPing() <= 0 ? proxyPlate.proxy.getPing() == -1 ? Formatting.RED + "shit" : proxyPlate.proxy.getPing() == -2 ? pinging[(int) (System.currentTimeMillis() / 200 % 3)] : "check" : proxyPlate.proxy.getPing() + "ms";
            Render2DEngine.drawRectWithOutline(context.getMatrices(), getX() + getWidth() - 51.5f, getY() + 36 + getScrollOffset() + proxyPlate.offset, 21.5f, 11, hover10 ? hoveredColor : color, color2);
            FontRenderers.sf_medium_mini.drawCenteredString(context.getMatrices(), ping, getX() + getWidth() - 41, proxyPlate.offset + getY() + 40 + getScrollOffset(), hover10 ? -1 : Color.GRAY.getRGB());
        }
        setMaxElementsHeight(proxyPlates.size() * 20);
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
        float posXWidth = getWidth() / 5f;

        float posYX = posXX + posXWidth + 2;
        float posYWidth = getWidth() / 12f;

        float posZX = posYX + posYWidth + 2;
        float posZWidth = getWidth() / 6.5f;

        float serverX = posZX + posZWidth + 2;
        float serverWidth = getWidth() / 6f;

        {
            boolean hoveringName = Render2DEngine.isHovered(mouseX, mouseY, nameX, getY() + 19, nameWidth, 11);
            boolean hoveringX = Render2DEngine.isHovered(mouseX, mouseY, posXX, getY() + 19, posXWidth, 11);
            boolean hoveringY = Render2DEngine.isHovered(mouseX, mouseY, posYX, getY() + 19, posYWidth, 11);
            boolean hoveringZ = Render2DEngine.isHovered(mouseX, mouseY, posZX, getY() + 19, posZWidth, 11);
            boolean hoveringServer = Render2DEngine.isHovered(mouseX, mouseY, serverX, getY() + 19, serverWidth, 11);
            boolean hoveringAdd = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, getY() + 19, 11, 11);

            if (hoveringName) {
                listeningType = ListeningType.Name;
                addName = "";
            }

            if (hoveringX) {
                listeningType = ListeningType.Ip;
                addIp = "";
            }

            if (hoveringY) {
                listeningType = ListeningType.Port;
                addPort = "";
            }

            if (hoveringZ) {
                listeningType = ListeningType.Login;
                addLogin = "";
            }

            if (hoveringServer) {
                listeningType = ListeningType.Password;
                addPassword = "";
            }

            if (hoveringName || hoveringX || hoveringY || hoveringZ || hoveringServer)
                listeningId = -3;

            if (hoveringAdd) {
                try {
                    Managers.PROXY.addProxy(new ProxyManager.ThProxy(addName, addIp, Integer.parseInt(addPort), addLogin, addPassword));
                } catch (Exception e) {
                }
                refresh();
            }
        }

        ArrayList<ProxyPlate> copy = Lists.newArrayList(proxyPlates);
        for (ProxyPlate proxyPlate : copy) {
            if ((int) (proxyPlate.offset + getY() + 50) + getScrollOffset() > getY() + getHeight())
                continue;

            boolean hoveringName = Render2DEngine.isHovered(mouseX, mouseY, nameX, getY() + 36 + getScrollOffset() + proxyPlate.offset, nameWidth, 11);
            boolean hoveringIp = Render2DEngine.isHovered(mouseX, mouseY, posXX, getY() + 36 + getScrollOffset() + proxyPlate.offset, posXWidth, 11);
            boolean hoveringPort = Render2DEngine.isHovered(mouseX, mouseY, posYX, getY() + 36 + getScrollOffset() + proxyPlate.offset, posYWidth, 11);
            boolean hoveringLogin = Render2DEngine.isHovered(mouseX, mouseY, posZX, getY() + 36 + getScrollOffset() + proxyPlate.offset, posZWidth, 11);
            boolean hoveringPassword = Render2DEngine.isHovered(mouseX, mouseY, serverX, getY() + 36 + getScrollOffset() + proxyPlate.offset, serverWidth, 11);
            boolean hoveringRemove = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 15, getY() + 36 + getScrollOffset() + proxyPlate.offset, 11, 11);
            boolean hoveringSelect = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 28, getY() + 36 + getScrollOffset() + proxyPlate.offset, 11, 11);
            boolean hoveringCheck = Render2DEngine.isHovered(mouseX, mouseY, getX() + getWidth() - 51.5f, getY() + 36 + getScrollOffset() + proxyPlate.offset, 21.5f, 11);

            if (hoveringName)
                listeningType = ListeningType.Name;

            if (hoveringIp)
                listeningType = ListeningType.Ip;

            if (hoveringPort)
                listeningType = ListeningType.Port;

            if (hoveringLogin)
                listeningType = ListeningType.Login;

            if (hoveringPassword)
                listeningType = ListeningType.Password;

            if (hoveringCheck)
                Managers.PROXY.checkPing(proxyPlate.proxy());

            if (hoveringName || hoveringIp || hoveringPort || hoveringLogin || hoveringPassword)
                listeningId = proxyPlate.id;

            if (hoveringSelect) {
                if (Managers.PROXY.getActiveProxy() == proxyPlate.proxy)
                    Managers.PROXY.setActiveProxy(null);
                else
                    Managers.PROXY.setActiveProxy(proxyPlate.proxy);
            }

            if (hoveringRemove) {
                Managers.PROXY.removeProxy(proxyPlate.proxy);
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

            for (ProxyPlate plate : proxyPlates) {
                if (listeningId == plate.id) {
                    switch (listeningType) {
                        case Name -> {
                            plate.proxy.setName(paste);
                            return;
                        }
                        case Ip -> {
                            plate.proxy.setIp(paste);
                            return;
                        }
                        case Port -> {
                            try {
                                plate.proxy.setPort(Integer.parseInt(paste));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        case Login -> {
                            plate.proxy.setL(paste);
                            return;
                        }
                        case Password -> {
                            plate.proxy.setP(paste);
                            return;
                        }
                    }
                }
            }

            if (listeningId == -3) {
                switch (listeningType) {
                    case Name -> addName = paste;
                    case Ip -> addIp = paste;
                    case Port -> addPort = paste;
                    case Login -> addLogin = paste;
                    case Password -> addPassword = paste;
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

                    for (ProxyPlate plate : proxyPlates) {
                        if (listeningId == plate.id) {
                            switch (listeningType) {
                                case Name -> {
                                    plate.proxy.setName(SliderElement.removeLastChar(plate.proxy.getName()));
                                    return;
                                }
                                case Ip -> {
                                    plate.proxy.setIp(SliderElement.removeLastChar(plate.proxy.getIp()));
                                    return;
                                }
                                case Port -> {
                                    String num = SliderElement.removeLastChar(String.valueOf(plate.proxy.getPort()));
                                    if (num.isEmpty())
                                        num = "0";

                                    plate.proxy.setPort(Integer.parseInt(num));
                                    return;
                                }
                                case Login -> {
                                    plate.proxy.setL(SliderElement.removeLastChar(plate.proxy.getL()));
                                    return;
                                }
                                case Password -> {
                                    plate.proxy.setP(SliderElement.removeLastChar(plate.proxy.getP()));
                                    return;
                                }
                            }
                        }
                    }

                    if (listeningId == -3) {
                        switch (listeningType) {
                            case Name -> addName = SliderElement.removeLastChar(addName);
                            case Ip -> addIp = SliderElement.removeLastChar(addIp);
                            case Port -> addPort = SliderElement.removeLastChar(addPort);
                            case Login -> addLogin = SliderElement.removeLastChar(addLogin);
                            case Password -> addPassword = SliderElement.removeLastChar(addPassword);
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

            for (ProxyPlate plate : proxyPlates) {
                if (listeningId == plate.id) {
                    try {
                        switch (listeningType) {
                            case Name -> plate.proxy.setName(plate.proxy.getName() + key);
                            case Ip -> plate.proxy.setIp(plate.proxy.getIp() + key);
                            case Port ->
                                    plate.proxy.setPort(Integer.parseInt(String.valueOf(plate.proxy.getPort()) + key));
                            case Login -> plate.proxy.setL(plate.proxy.getL() + key);
                            case Password -> plate.proxy.setP(plate.proxy.getP() + key);
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
                        case Ip -> addIp = addIp + key;
                        case Port -> addPort = addPort + key;
                        case Login -> addLogin = addLogin + key;
                        case Password -> addPassword = addPassword + key;
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
        proxyPlates.clear();
        int id1 = 0;
        for (ProxyManager.ThProxy p : Managers.PROXY.getProxies())
            if (search.equals("Search") || search.isEmpty() || p.getName().contains(search)) {
                proxyPlates.add(new ProxyPlate(id1, id1 * 20 + 18, p));
                id1++;
            }
    }

    @Override
    public int getMinWidth() {
        return 340;
    }

    private record ProxyPlate(int id, float offset, ProxyManager.ThProxy proxy) {
    }
}
