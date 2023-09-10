package thunder.hack.gui.clickui;


import com.google.common.collect.Lists;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.core.ConfigManager;
import thunder.hack.core.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.utility.render.MSAAFramebuffer;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.List;

import static thunder.hack.modules.Module.mc;


public class ClickUI extends Screen {

    public static List<AbstractWindow> windows;

    private Identifier pic1 = new Identifier("textures/pic1.png");
    private Identifier pic2 = new Identifier("textures/pic2.png");
    private Identifier pic3 = new Identifier("textures/pic3.png");


    private boolean firstOpen;
    private float scrollY;
    private boolean setup = false;
    private int hstep = 0;

    public ClickUI() {
        super(Text.of("ClickGUI"));

        windows = Lists.newArrayList();
        firstOpen = true;
        this.setInstance();
    }

    private static ClickUI INSTANCE = new ClickUI();

    public static ClickUI getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickUI();
        }
        return INSTANCE;
    }

    public static ClickUI getClickGui() {
        return ClickUI.getInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }


    @Override
    protected void init() {
        if (firstOpen) {
            double x = 20, y = 20;
            double offset = 0;
            int windowHeight = 18;

            int i = 0;
            for (final Module.Category category : ThunderHack.moduleManager.getCategories()) {
                if (category.getName().contains("HUD")) continue;
                ModuleWindow window = new ModuleWindow(category, ThunderHack.moduleManager.getModulesByCategory(category), i, x + offset, y, 108, windowHeight);
                window.setOpen(true);
                windows.add(window);
                offset += 110;

                if (offset > mc.getWindow().getScaledWidth()) {
                    offset = 0;
                }
                i++;
            }
            firstOpen = false;
        }
        windows.forEach(AbstractWindow::init);
    }


    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        if (ThunderHack.oldVersion && (mc.player.age % 20) > 10) {
            FontRenderers.thGlitch.drawCenteredString(context.getMatrices(), "New version is available!", mc.getWindow().getScaledWidth() / 2f + 1, mc.getWindow().getScaledHeight() - 39 - FontRenderers.thGlitch.getFontHeight("New version is available!"), Color.BLACK.getRGB());
            FontRenderers.thGlitch.drawCenteredString(context.getMatrices(), "New version is available!", mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() - 40 - FontRenderers.thGlitch.getFontHeight("New version is available!"), -1);
        }

        if (Module.fullNullCheck()) renderBackground(context);

        if(ModuleManager.clickGui.scrollMode.getValue() == ClickGui.scrollModeEn.Old) {
            for (AbstractWindow window : windows) {
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 264)) {
                    window.setY(window.getY() + 2);
                }
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 265)) {
                    window.setY(window.getY() - 2);
                }
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 262)) {
                    window.setX(window.getX() + 2);
                }
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 263)) {
                    window.setX(window.getX() - 2);
                }
                if (scrollY != 0) {
                    window.setY(window.getY() + scrollY);
                }
            }
        } else {
            for (AbstractWindow window : windows) {
                if (scrollY != 0) {
                    window.setModuleOffset(scrollY, mouseX, mouseY);
                }
            }
        }

        scrollY = 0;

        if (ClickGui.getInstance().msaa.getValue()) {
            if (!MSAAFramebuffer.framebufferInUse()) {
                MSAAFramebuffer.use(() -> {
                    for (AbstractWindow window : windows) {
                        window.render(context, mouseX, mouseY, delta, ClickGui.getInstance().hcolor1.getValue().getColorObject());
                    }
                    super.render(context, mouseX, mouseY, delta);
                });
            } else {
                for (AbstractWindow window : windows) {
                    window.render(context, mouseX, mouseY, delta, ClickGui.getInstance().hcolor1.getValue().getColorObject());
                }
                super.render(context, mouseX, mouseY, delta);
            }
        } else {
            for (AbstractWindow window : windows) {
                window.render(context, mouseX, mouseY, delta, ClickGui.getInstance().hcolor1.getValue().getColorObject());
            }
            super.render(context, mouseX, mouseY, delta);
        }
        if (!setup && ConfigManager.firstLaunch) {
            float hx = mc.getWindow().getScaledWidth() / 2f;
            float hy = mc.getWindow().getScaledHeight() / 2f;

            Render2DEngine.drawGradientBlurredShadow(context.getMatrices(), hx + 1 - 100, hy - 100 + 1, 199, 199, 10, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
            Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), hx - 100 - 0.5f, hy - 100 - 0.5f, 200 + 1, 200 + 1, HudEditor.hudRound.getValue());
            Render2DEngine.drawRound(context.getMatrices(), hx - 100, hy - 100, 200, 200, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());
            //FontRenderers.sf_bold.drawCenteredString(context.getMatrices(),"KeyBinds", getPosX() + max_width / 2, getPosY() + 3, HudEditor.textColor.getValue().getColor());

            if (hstep == 0) {
                FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Choose your language", hx, hy - 80, -1);
                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy - 20, 180, 20)) {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy - 20, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 100));
                } else {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy - 20, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 70));

                }

                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy - 50, 180, 20)) {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy - 50, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 100));
                } else {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy - 50, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 70));
                }

                FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Russian", hx, hy - 15, -1);
                FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "English", hx, hy - 45, -1);
                FontRenderers.modules.drawCenteredString(context.getMatrices(), "ThunderHack v" + ThunderHack.version, hx, hy + 90, Render2DEngine.injectAlpha(HudEditor.getColor(180), 70).getRGB());
            } else if (hstep == 1) {
                FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), !MainSettings.isRu() ? "Hi! Thanks for using ThunderHack 1.20.1" : "Привет! Спс что скачал ThunderHack 1.20.1"
                        , hx, hy - 80, -1);
                FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), !MainSettings.isRu() ? "Do you need help with the config?" : "Те помочь с кфг?"
                        , hx, hy - 70, -1);

                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy - 20, 180, 20)) {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy - 20, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 100));
                } else {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy - 20, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 70));

                }

                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy - 50, 180, 20)) {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy - 50, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 100));
                } else {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy - 50, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 70));
                }

                FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), MainSettings.language.getValue() == MainSettings.Language.RU ? "Да" : "Yes", hx, hy - 45, -1);
                FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), MainSettings.language.getValue() == MainSettings.Language.RU ? "Нет, я про" : "Go fuck urself", hx, hy - 15, -1);
                FontRenderers.modules.drawCenteredString(context.getMatrices(), "ThunderHack v" + ThunderHack.version, hx, hy + 90, Render2DEngine.injectAlpha(HudEditor.getColor(180), 70).getRGB());
                context.drawTexture(pic1, (int) hx - 45, (int) hy + 10, 0, 0, 80, 75, 80, 75);

            } else if (hstep == 2) {
                FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), !MainSettings.isRu() ?
                                "Choose your server" : "На какой сервер?"
                        , hx, hy - 80, -1);

                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy - 20, 180, 20)) {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy - 20, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 100));
                } else {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy - 20, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 70));

                }

                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy - 50, 180, 20)) {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy - 50, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 100));
                } else {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy - 50, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 70));
                }

                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy + 10, 180, 20)) {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy + 10, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 100));
                } else {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy + 10, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 70));
                }

                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy + 40, 180, 20)) {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy + 40, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 100));
                } else {
                    Render2DEngine.drawRound(context.getMatrices(), hx - 90, hy + 40, 180, 20, 4, Render2DEngine.injectAlpha(HudEditor.getColor(180), 70));
                }

                FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "crystalpvp.cc", hx, hy - 45, -1);
                FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "strict.2b2tpvp.org", hx, hy - 15, -1);
                FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "mcfunny.su (aka shit)", hx, hy + 15, -1);
                FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "rw, hw, ares, ft (GrimAc)", hx, hy + 45, -1);


                FontRenderers.modules.drawCenteredString(context.getMatrices(), "ThunderHack v" + ThunderHack.version, hx, hy + 90, Render2DEngine.injectAlpha(HudEditor.getColor(180), 70).getRGB());
            } else if (hstep == 3) {
                FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), !MainSettings.isRu() ?
                                "ThunderHack is set up" : "Вперед, сносить кабины))0)"
                        , hx, hy - 80, -1);
                FontRenderers.modules.drawCenteredString(context.getMatrices(), "ThunderHack v" + ThunderHack.version, hx, hy + 90, Render2DEngine.injectAlpha(HudEditor.getColor(180), 70).getRGB());
                context.drawTexture(pic2, (int) hx - 45, (int) hy + 10, 0, 0, 80, 75, 80, 75);
            } else if (hstep == 5) {
                FontRenderers.sf_medium.drawCenteredString(context.getMatrices(),
                        "((("
                        , hx, hy - 80, -1);

                context.drawTexture(pic3, (int) hx - 45, (int) hy + 10, 0, 0, 80, 75, 80, 75);
            }
        }
    }

    // 0 lang
    // 1 help
    // 2 cfg
    // 3 final


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scrollY += (int) (amount * 5D);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!setup && ConfigManager.firstLaunch) {
            float hx = mc.getWindow().getScaledWidth() / 2f;
            float hy = mc.getWindow().getScaledHeight() / 2f;
            if (hstep == 0) {
                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy - 50, 180, 20)) {
                    MainSettings.language.setValue(MainSettings.Language.ENG);
                    hstep = 1;
                }
                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy - 20, 180, 20)) {
                    MainSettings.language.setValue(MainSettings.Language.RU);
                    hstep = 1;
                }
            } else if (hstep == 1) {
                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy - 50, 180, 20)) {
                    hstep = 2;
                }
                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy - 20, 180, 20)) {
                    hstep = 5;
                    ThunderHack.asyncManager.run(() -> setup = true, 3000);
                }
            } else if (hstep == 2) {
                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy - 20, 180, 20)) {
                    // strict
                    ThunderHack.configManager.loadDefault("strict");
                    hstep = 3;
                    ThunderHack.asyncManager.run(() -> setup = true, 3000);
                }

                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy - 50, 180, 20)) {
                    //cc
                    ThunderHack.configManager.loadDefault("cc");
                    hstep = 3;
                    ThunderHack.asyncManager.run(() -> setup = true, 3000);
                }

                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy + 10, 180, 20)) {
                    // fg
                    ThunderHack.configManager.loadDefault("fg");
                    hstep = 3;
                    ThunderHack.asyncManager.run(() -> setup = true, 3000);
                }

                if (Render2DEngine.isHovered(mouseX, mouseY, hx - 90, hy + 40, 180, 20)) {
                    // grim
                    ThunderHack.configManager.loadDefault("grim");
                    hstep = 3;
                    ThunderHack.asyncManager.run(() -> setup = true, 3000);
                }
            }
            return false;
        }
        windows.forEach(w -> {
            w.mouseClicked((int) mouseX, (int) mouseY, button);

            windows.forEach(w1 -> {
                if (w.dragging && w != w1)
                    w1.dragging = false;
            });
        });
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!setup && ConfigManager.firstLaunch) return false;
        windows.forEach(w -> w.mouseReleased((int) mouseX, (int) mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        windows.forEach(w -> {
            w.keyTyped(keyCode);
        });

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return false;
    }

    @Override
    public void removed() {
        ThunderHack.EVENT_BUS.unsubscribe(this);
    }

}
