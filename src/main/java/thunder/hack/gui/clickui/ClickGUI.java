package thunder.hack.gui.clickui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.util.List;
import java.util.Objects;

import static thunder.hack.modules.Module.mc;

public class ClickGUI extends Screen {
    public static List<AbstractCategory> windows;
    public static boolean anyHovered;

    private boolean firstOpen;
    private float scrollY, closeAnimation, prevYaw, prevPitch, closeDirectionX, closeDirectionY;
    public static boolean close = false;

    public static String currentDescription = "";
    public static final Identifier arrow = new Identifier("thunderhack", "textures/gui/elements/arrow.png");

    public ClickGUI() {
        super(Text.of("NewClickGUI"));
        windows = Lists.newArrayList();
        firstOpen = true;
        this.setInstance();
    }

    private static ClickGUI INSTANCE = new ClickGUI();

    public static ClickGUI getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGUI();
        }
        return INSTANCE;
    }

    public static ClickGUI getClickGui() {
        windows.forEach(AbstractCategory::init);
        return ClickGUI.getInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    protected void init() {
        if (firstOpen) {
            float offset = 0;
            int windowHeight = 18;

            int halfWidth = mc.getWindow().getScaledWidth() / 2;
            int halfWidthCats = (int) (3 * (ModuleManager.clickGui.moduleWidth.getValue() + 4f));

            for (final Module.Category category : ThunderHack.moduleManager.getCategories()) {
                if (category == Module.Category.HUD) continue;
                Category window = new Category(category, ThunderHack.moduleManager.getModulesByCategory(category), (halfWidth - halfWidthCats) + offset, 20, 100, windowHeight);
                window.setOpen(true);
                windows.add(window);
                offset += ModuleManager.clickGui.moduleWidth.getValue() + 2;
                if (offset > mc.getWindow().getScaledWidth())
                    offset = 0;
            }
            firstOpen = false;
        } else {
            if (windows.getFirst().getX() < 0 || windows.getFirst().getY() < 0) {
                float offset = 0;

                int halfWidth = mc.getWindow().getScaledWidth() / 2;
                int halfWidthCats = (int) (3 * (ModuleManager.clickGui.moduleWidth.getValue() + 4f));

                for (AbstractCategory w : windows) {
                    w.setX((halfWidth - halfWidthCats) + offset);
                    w.setY(20);
                    offset += ModuleManager.clickGui.moduleWidth.getValue() + 2;
                    if (offset > mc.getWindow().getScaledWidth())
                        offset = 0;
                }
            }
        }
        windows.forEach(AbstractCategory::init);
    }

    public static void renderTexture(@NotNull MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth,
                                     double textureHeight) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0;
        renderTexturedQuad(
                matrices.peek().getPositionMatrix(),
                x0,
                x1,
                y0,
                y1,
                z,
                (u + 0.0F) / (float) textureWidth,
                (u + (float) regionWidth) / (float) textureWidth,
                (v + 0.0F) / (float) textureHeight,
                (v + (float) regionHeight) / (float) textureHeight
        );
    }

    private static void renderTexturedQuad(Matrix4f matrix, double x0, double x1, double y0, double y1, double z, float u0, float u1, float v0, float v1) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, (float) x0, (float) y1, (float) z).texture(u0, v1);
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z).texture(u1, v1);
        buffer.vertex(matrix, (float) x1, (float) y0, (float) z).texture(u1, v0);
        buffer.vertex(matrix, (float) x0, (float) y0, (float) z).texture(u0, v0);

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }


    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void tick() {
        windows.forEach(AbstractCategory::tick);

        if (close) {
            if (mc.player != null) {
                if (mc.player.getPitch() > prevPitch)
                    closeDirectionY = (prevPitch - mc.player.getPitch()) * 300;

                if (mc.player.getPitch() < prevPitch)
                    closeDirectionY = (prevPitch - mc.player.getPitch()) * 300;

                if (mc.player.getYaw() > prevYaw)
                    closeDirectionX = (prevYaw - mc.player.getYaw()) * 300;

                if (mc.player.getYaw() < prevYaw)
                    closeDirectionX = (prevYaw - mc.player.getYaw()) * 300;
            }

            if (closeDirectionX < 1 && closeDirectionY < 1 && closeAnimation > 2)
                closeDirectionY = -3000;

            closeAnimation++;
            if (closeAnimation > 6) {
                close = false;
                windows.forEach(AbstractCategory::restorePos);
                close();
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (ModuleManager.clickGui.blur.getValue())
            applyBlur(delta);

        anyHovered = false;

        ClickGui.Image image = ModuleManager.clickGui.image.getValue();

        if (image != ClickGui.Image.None) {

            RenderSystem.setShaderTexture(0, image.file);
            Render2DEngine.renderTexture(context.getMatrices(), image.pos[0], image.pos[1], image.size * ((float) image.fileWidth / image.fileHeight), image.size * ((float) image.fileHeight / image.fileWidth), 0, 0, image.fileWidth, image.fileHeight, image.fileWidth, image.fileHeight);
        }

        if (closeAnimation <= 6) {
            windows.forEach(w -> {
                w.setX((float) (w.getX() + closeDirectionX * AnimationUtility.deltaTime()));
                w.setY((float) (w.getY() + closeDirectionY * AnimationUtility.deltaTime()));
            });
        }


        if (Module.fullNullCheck())
            renderBackground(context, mouseX, mouseY, delta);
        //   Render2DEngine.drawMainMenuShader(context.getMatrices(), 0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());

        if (ModuleManager.clickGui.scrollMode.getValue() == ClickGui.scrollModeEn.Old) {
            for (AbstractCategory window : windows) {
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 264))
                    window.setY(window.getY() + 2);
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 265))
                    window.setY(window.getY() - 2);
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 262))
                    window.setX(window.getX() + 2);
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 263))
                    window.setX(window.getX() - 2);
                if (scrollY != 0)
                    window.setY(window.getY() + scrollY);
            }
        } else for (AbstractCategory window : windows)
            if (scrollY != 0)
                window.setModuleOffset(scrollY, mouseX, mouseY);

        scrollY = 0;
        windows.forEach(w -> w.render(context, mouseX, mouseY, delta));

        if (!Objects.equals(currentDescription, "") && ModuleManager.clickGui.descriptions.getValue()) {
            Render2DEngine.drawHudBase(context.getMatrices(), mouseX + 7, mouseY + 5, FontRenderers.sf_medium.getStringWidth(currentDescription) + 6, 11, 1f, false);
            FontRenderers.sf_medium.drawString(context.getMatrices(), currentDescription, mouseX + 10, mouseY + 8, HudEditor.getColor(0).getRGB());
            currentDescription = "";
        }

        if (ModuleManager.clickGui.tips.getValue() && !close)
            FontRenderers.sf_medium.drawString(context.getMatrices(),
                    "Left Mouse Click to enable module" +
                            "\nRight Mouse Click to open module settings\nMiddle Mouse Click to bind module" +
                            "\nCtrl + F to start searching\nDrag n Drop config there to load" +
                            "\nShift + Left Mouse Click to change module visibility in array list" +
                            "\nMiddle Mouse Click on slider to enter value from keyboard" +
                            "\nDelete + Left Mouse Click on module to reset", 5, mc.getWindow().getScaledHeight() - 80, HudEditor.getColor(0).getRGB());

        if (!HudElement.anyHovered && !ClickGUI.anyHovered)
            GLFW.glfwSetCursor(mc.getWindow().getHandle(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollY += (int) (verticalAmount * 5D);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        windows.forEach(w -> {
            w.mouseClicked((int) mouseX, (int) mouseY, button);
            windows.forEach(w1 -> {
                if (w.dragging && w != w1) w1.dragging = false;
            });
        });
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        //   if (!setup && ConfigManager.firstLaunch) return false;
        windows.forEach(w -> w.mouseReleased((int) mouseX, (int) mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char key, int modifier) {
        windows.forEach(w -> w.charTyped(key, modifier));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        windows.forEach(w -> w.keyTyped(keyCode));

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (mc.player == null || !ModuleManager.clickGui.closeAnimation.getValue()) {
                super.keyPressed(keyCode, scanCode, modifiers);
                return true;
            }

            if (close)
                return true;

            windows.forEach(AbstractCategory::savePos);

            closeDirectionX = 0;
            closeDirectionY = 0;

            close = true;
            mc.mouse.lockCursor();

            closeAnimation = 0;
            if (mc.player != null) {
                prevYaw = mc.player.getYaw();
                prevPitch = mc.player.getPitch();
            }
            return true;
        }

        return false;
    }
}