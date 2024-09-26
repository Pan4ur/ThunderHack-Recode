package thunder.hack.gui.notification;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.EaseOutBack;

import java.awt.*;

import static thunder.hack.core.manager.client.NotificationManager.isDefault;
import static thunder.hack.features.modules.Module.mc;

public class Notification {
    private final String message, title;
    private final String icon;
    private final int lifeTime;
    public final EaseOutBack animation;
    private float y, width, animationX, height = 25;
    private boolean direction = false;
    private final Timer timer = new Timer();

    public Notification(String title, String message, Type type, int time) {
        lifeTime = time;
        this.title = title;
        this.message = message;

        switch (type) {
            case INFO -> icon = "J";
            case ENABLED -> icon = "K";
            case DISABLED, ERROR -> icon = "I";
            case WARNING -> icon = "L";
            default -> icon = "H";
        }

        width = isDefault() ? FontRenderers.sf_bold_mini.getStringWidth(message) + 38f : FontRenderers.sf_bold_micro.getStringWidth(title + " " + message) + 20f;
        height = isDefault() ? 25 : 13;

        animation = new EaseOutBack(isDefault() ? 10 : 20);

        animationX = width;
        if (isDefault())
            y = mc.getWindow().getScaledHeight() - height;
        else y = mc.getWindow().getScaledHeight() / 2f + 10;
    }

    public void render(MatrixStack matrix, float getY) {
        int animatedAlpha = (int) MathUtility.clamp((1 - animation.getAnimationd()) * 255, 0, 255);
        Color color = new Color(170, 170, 170, animatedAlpha);

        if (isDefault()) {
            direction = isFinished();
            animationX = (float) (width * animation.getAnimationd());

            y = animate(y, getY);
            float x = mc.getWindow().getScaledWidth() - 6 - width + animationX;

            if (HudEditor.hudStyle.is(HudEditor.HudStyle.Glowing)) {
                Render2DEngine.verticalGradient(matrix, x + 25, y + 1, x + 25.5f, y + 12, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
                Render2DEngine.verticalGradient(matrix, x + 25, y + 11, x + 25.5f, y + 22, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));
            } else {
                Render2DEngine.drawRect(matrix, x + 25, y + 2, 0.5f, 20, Render2DEngine.injectAlpha(new Color(0x44FFFFFF, true), (int) (animatedAlpha * 0.1f)));
            }

            FontRenderers.sf_bold_mini.drawString(matrix, title, x + 30, y + 6, HudEditor.textColor.getValue().getColor());
            FontRenderers.sf_bold_mini.drawString(matrix, message, x + 30, y + 15, color.getRGB());
            FontRenderers.mid_icons.drawString(matrix, icon, x + 5, y + 7, color.getRGB());
        } else {
            direction = isFinished();
            animationX = (float) (width * animation.getAnimationd());
            y = animate(y, getY);
            float x = mc.getWindow().getScaledWidth() / 2f - width / 2f;
            if (HudEditor.hudStyle.is(HudEditor.HudStyle.Glowing)) {
                Render2DEngine.verticalGradient(matrix, x + 13, y + 1, x + 13.5f, y + 6, Render2DEngine.injectAlpha(color, 0), Render2DEngine.injectAlpha(color, animatedAlpha));
                Render2DEngine.verticalGradient(matrix, x + 13, y + 6, x + 13.5f, y + 11, Render2DEngine.injectAlpha(color, animatedAlpha), Render2DEngine.injectAlpha(color, 0));
            } else {
                Render2DEngine.drawRect(matrix, x + 13, y + 1, 0.5f, 10, Render2DEngine.injectAlpha(new Color(0x44FFFFFF, true), (int) (animatedAlpha * 0.1f)));
            }
            FontRenderers.sf_bold_micro.drawString(matrix, title + " " + message, x + 16, y + 5, color.getRGB());
            FontRenderers.icons.drawString(matrix, icon, x + 3, y + 5.5f, color.getRGB());
        }
    }

    public void onUpdate() {
        animation.update(direction);
    }

    public void renderShaders(MatrixStack matrix, float getY) {
        direction = isFinished();
        animationX = (float) (width * animation.getAnimationd());
        y = animate(y, getY);
        if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            Render2DEngine.drawHudBase2(matrix, isDefault() ? mc.getWindow().getScaledWidth() - 6 - width + animationX : mc.getWindow().getScaledWidth() / 2f - width / 2f,
                    y, width, height, isDefault() ? 5f : 3f, HudEditor.blurStrength.getValue(), HudEditor.blurOpacity.getValue(), (float) MathUtility.clamp((1 - animation.getAnimationd()), 0f, 1f));
        } else {
            Render2DEngine.drawHudBase(matrix, isDefault() ? mc.getWindow().getScaledWidth() - 6 - width + animationX : mc.getWindow().getScaledWidth() / 2f - width / 2f,
                    y, width, height, isDefault() ? 5f : 3f, (float) MathUtility.clamp((1 - animation.getAnimationd()), 0f, 1f));
        }
    }

    private boolean isFinished() {
        return timer.passedMs(lifeTime);
    }

    public double getHeight() {
        return height;
    }

    public boolean shouldDelete() {
        return isFinished() && animationX >= width - 5;
    }

    public float animate(float value, float target) {
        return value + (target - value) / 8f;
    }

    public enum Type {
        SUCCESS("Success", Formatting.GREEN),
        INFO("Information", Formatting.AQUA),
        WARNING("Warning", Formatting.GOLD),
        ERROR("Error", Formatting.RED),
        ENABLED("Module enabled", Formatting.DARK_GREEN),
        DISABLED("Module disabled", Formatting.DARK_RED);

        final String name;
        final Formatting color;

        Type(String name, Formatting color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public Formatting getColor() {
            return color;
        }
    }
}