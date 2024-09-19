package thunder.hack.features.hud;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventMouse;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.hud.HudEditorGui;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.PositionSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

public class HudElement extends Module {
    private final Setting<PositionSetting> pos = new Setting<>("Position", new PositionSetting(0.5f, 0.5f));
    private boolean mouseState = false, mouseButton = false;
    private float x, y, dragX, dragY, hitX, hitY;
    private float height, width;
    public static boolean anyHovered = false;

    public HudElement(String name, int width, int height) {
        super(name, Category.HUD);
        this.height = height;
        this.width = width;
    }

    @Override
    public void onRender2D(DrawContext context) {
        y = mc.getWindow().getScaledHeight() * pos.getValue().getY();
        x = mc.getWindow().getScaledWidth() * pos.getValue().getX();

        if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HudEditorGui) {
            if (mouseButton && mouseState) {
                pos.getValue().setX(Math.clamp(Render2DEngine.scrollAnimate((normaliseX() - dragX) / mc.getWindow().getScaledWidth(), pos.getValue().getX(), .1f),
                        0, 1f));
                pos.getValue().setY(Math.clamp(Render2DEngine.scrollAnimate((normaliseY() - dragY) / mc.getWindow().getScaledHeight(), pos.getValue().getY(), .1f),
                        0, 1f - (height / mc.getWindow().getScaledHeight())));

                float finalX = 0;
                float finalY = 0;

                if (HudEditor.sticky.getValue())
                    for (Module m : Managers.MODULE.getEnabledModules())
                        if (m instanceof HudElement hudElement && hudElement != this && !(hudElement.getPosX() == 0 && hudElement.getPosY() == 0)) {
                            if (getPosX() > mc.getWindow().getScaledWidth() / 2f) {
                                if (isNear(hudElement.getHitX() + hudElement.getWidth(), getHitX() + getWidth()))
                                    finalX = hudElement.getHitX() + hudElement.getWidth() - getWidth();

                            } else {
                                if (isNear(hudElement.getHitX(), getHitX()))
                                    finalX = hudElement.getHitX();

                            }
                            if (isNear(hudElement.getHitY(), getHitY()))
                                finalY = hudElement.getHitY();
                        }

                if (finalX != 0 || finalY != 0)
                    Render2DEngine.drawRound(context.getMatrices(), finalX == 0 ? getHitX() : finalX, finalY == 0 ? getHitY() : finalY, width, height, 3, new Color(0x7B2F2F2F, true));

                if (finalX != 0)
                    Render2DEngine.drawLine(finalX, 0, finalX, mc.getWindow().getScaledHeight(), -1);

                if (finalY != 0)
                    Render2DEngine.drawLine(0, finalY, mc.getWindow().getScaledWidth(), finalY, -1);
            }
        }

        if (mouseButton) {
            if (!mouseState && isHovering()) {
                dragX = (int) (normaliseX() - (pos.getValue().getX() * mc.getWindow().getScaledWidth()));
                dragY = (int) (normaliseY() - (pos.getValue().getY() * mc.getWindow().getScaledHeight()));
                mouseState = true;
            }
        } else {
            mouseState = false;
        }

        if (isHovering() && (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HudEditorGui)) {
            if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_WAYLAND) {
                GLFW.glfwSetCursor(mc.getWindow().getHandle(), mouseState ? GLFW.glfwCreateStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR) : GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR));
            }
            anyHovered = true;
        }
        // Render2DEngine.drawRect(context.getMatrices(),getPosX(), getPosY(), width, height, Color.RED);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onMouse(@NotNull EventMouse event) {
        if (event.getAction() == 0 && event.getButton() == 1 && isHovering() && mc.currentScreen instanceof HudEditorGui)
            HudEditorGui.getHudGui().hudClicked(this);

        if (event.getAction() == 0) {

            if (mouseButton && mouseState)
                if (HudEditor.sticky.getValue())
                    for (Module m : Managers.MODULE.getEnabledModules()) {
                        if (m instanceof HudElement hudElement && hudElement != this && !(hudElement.getHitX() == 0 && hudElement.getHitY() == 0)) {

                            float hitDifX = getPosX() - getHitX();
                            float hitDifY = getPosY() - getHitY();

                            if (getPosX() > mc.getWindow().getScaledWidth() / 2f) {
                                if (isNear(hudElement.getHitX() + hudElement.getWidth(), getHitX() + getWidth()))
                                    pos.getValue().setX((hudElement.getHitX() + hitDifX + hudElement.getWidth() - getWidth()) / mc.getWindow().getScaledWidth());
                            } else {
                                if (isNear(hudElement.getHitX(), getHitX()))
                                    pos.getValue().setX((hudElement.getHitX() + hitDifX) / mc.getWindow().getScaledWidth());
                            }
                            if (isNear(hudElement.getHitY(), getHitY()))
                                pos.getValue().setY((hudElement.getHitY() + hitDifY) / mc.getWindow().getScaledHeight());
                        }
                    }

            HudEditorGui.currentlyDragging = null;
            mouseButton = false;
        }
        if (event.getAction() == 1 && isHovering() && HudEditorGui.currentlyDragging == null) {
            HudEditorGui.currentlyDragging = this;
            mouseButton = true;
        }
    }

    public int normaliseX() {
        return (int) (mc.mouse.getX() / Render3DEngine.getScaleFactor());
    }

    public int normaliseY() {
        return (int) (mc.mouse.getY() / Render3DEngine.getScaleFactor());
    }

    public boolean isHovering() {
        return normaliseX() > Math.min(hitX, hitX + width) && normaliseX() < Math.max(hitX, hitX + width) && normaliseY() > Math.min(hitY, hitY + height) && normaliseY() < Math.max(hitY, hitY + height);
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setHitX(float hitX) {
        this.hitX = hitX;
    }

    public void setHitY(float hitY) {
        this.hitY = hitY;
    }

    public void setBounds(float x, float y, float w, float h) {
        setHitX(x);
        setHitY(y);
        setWidth(w);
        setHeight(h);
    }

    public float getPosX() {
        return x;
    }

    public float getHitX() {
        return hitX;
    }

    public float getHitY() {
        return hitY;
    }

    public float getPosY() {
        return y;
    }

    public float getX() {
        return pos.getValue().x;
    }

    public float getY() {
        return pos.getValue().y;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    private boolean isNear(float n1, float n2) {
        return Math.abs(n1 - n2) < 10;
    }
}
