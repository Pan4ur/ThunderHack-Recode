package dev.thunderhack.notification;

import net.minecraft.client.util.math.MatrixStack;
import dev.thunderhack.gui.font.FontRenderers;
import dev.thunderhack.modules.client.HudEditor;
import dev.thunderhack.utils.Timer;
import dev.thunderhack.utils.math.MathUtility;
import dev.thunderhack.utils.render.Render2DEngine;
import dev.thunderhack.utils.render.animation.BetterAnimation;

import java.awt.*;

import static dev.thunderhack.modules.Module.mc;

public class Notification {
    private final String message;
    private final String title;
    private final Timer timer;
    private final Type type;
    private final float height = 25;
    private final long stayTime;
    public BetterAnimation animation = new BetterAnimation();
    private float posY;
    private final float width;
    private float animationX;
    private boolean direction = false;
    private final Timer animationTimer = new Timer();

    public Notification(String title, String message, Type type, int time) {
        stayTime = time;
        this.title = title;
        this.message = message;
        this.type = type;
        timer = new Timer();
        timer.reset();
        width = FontRenderers.getSettingsRenderer().getStringWidth(message) + 38;
        animationX = width;
        posY = mc.getWindow().getScaledHeight() - height;
    }

    public void render(MatrixStack matrix, float getY) {
        Color icolor2 = new Color(170, 170, 170, (int) MathUtility.clamp((1 - animation.getAnimationd()), 0, 255));

        direction = isFinished();
        animationX = (float) (width * animation.getAnimationd());

        posY = animate(posY, getY);

        int x1 = (int) ((mc.getWindow().getScaledWidth() - 6) - width + animationX);
        int y1 = (int) posY;

        Render2DEngine.verticalGradient(matrix, x1 + 25, y1 + 1, x1 + 25.5f, y1 + 12, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.verticalGradient(matrix, x1 + 25, y1 + 11, x1 + 25.5f, y1 + 22, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));

        FontRenderers.sf_bold_mini.drawString(matrix, title, x1 + 30, y1 + 6, HudEditor.textColor.getValue().getColor());
        FontRenderers.sf_bold_mini.drawString(matrix, message, x1 + 30, (int) y1 + 15, icolor2.getRGB());

        String icon = "I";
        switch (type) {
            case SUCCESS -> icon = "H";
            case INFO -> icon = "J";
            case ENABLED -> icon = "K";
            case DISABLED, ERROR -> icon = "I";
            case WARNING -> icon = "L";
        }

        FontRenderers.mid_icons.drawString(matrix, icon, x1 + 5, y1 + 7, icolor2.getRGB());

        if (animationTimer.passedMs(50)) {
            animation.update(direction);
            animationTimer.reset();
        }
    }

    public void renderShaders(MatrixStack matrix, float getY) {
        direction = isFinished();
        animationX = (float) (width * animation.getAnimationd());

        posY = animate(posY, getY);

        int x1 = (int) ((mc.getWindow().getScaledWidth() - 6) - width + animationX);
        int y1 = (int) posY;

        Render2DEngine.drawGradientGlow(matrix, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), x1, y1, width, height, 5f, 10);
        Render2DEngine.drawGradientRoundShader(matrix, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), x1 - 0.5f, y1 - 0.5f, width + 1, height + 1, 5f);
        Render2DEngine.drawRoundShader(matrix, x1, y1, width, height, 5f, HudEditor.plateColor.getValue().getColorObject());
    }

    private boolean isFinished() {
        return timer.passedMs(stayTime);
    }

    public double getHeight() {
        return height;
    }

    public boolean shouldDelete() {
        return (isFinished()) && animationX >= width - 5;
    }

    public float animate(float value, float target) {
        return value + (target - value) / 8f;
    }

    public enum Type {
        SUCCESS("Success"),
        INFO("Information"),
        WARNING("Warning"),
        ERROR("Error"),
        ENABLED("Module enabled"),
        DISABLED("Module disabled");

        final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}