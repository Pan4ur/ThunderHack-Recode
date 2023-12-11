package thunder.hack.gui.autobuy;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;

import static thunder.hack.modules.Module.mc;

public class ItemPlate {

    float scroll_animation = 0f;
    private final AutoBuyItem item;
    private int posX;
    private float posY;
    private float scrollPosY;
    private float prevPosY;
    private int progress;
    private int fade;
    private final int index;
    private boolean first_open = true;
    private boolean listening_bind = false;
    private boolean holdbind = false;


    public ItemPlate(AutoBuyItem item, int posX, int posY, int index) {
        this.item = item;
        this.posX = posX;
        this.posY = posY;
        fade = 0;
        this.index = index * 5;
        scrollPosY = posY;
        scroll_animation = 0;
    }

    public void render(DrawContext context, int MouseX, int MouseY) {
        if (scrollPosY != posY) {
            scroll_animation = AnimationUtility.fast(scroll_animation, 1, 15f);
            posY = (float) Render2DEngine.interpolate(prevPosY, scrollPosY, scroll_animation);
        }

        if ((posY > AutoBuyGui.getInstance().main_posY + AutoBuyGui.getInstance().height) || posY < AutoBuyGui.getInstance().main_posY) {
            return;
        }

        MatrixStack stack = context.getMatrices();

        if (item.getItem() != Items.AIR) {
            Render2DEngine.addWindow(stack, new Render2DEngine.Rectangle(posX + 1, posY + 1, posX + 90, posY + 30));
            Render2DEngine.drawRound(stack, posX + 1, posY, 89, 30, 4f, Render2DEngine.applyOpacity(new Color(25, 20, 30, 255), getFadeFactor()));
            if (first_open) {
                Render2DEngine.drawBlurredShadow(stack, MouseX - 20, MouseY - 20, 40, 40, 60, Render2DEngine.applyOpacity(new Color(0xC3555A7E, true), getFadeFactor()));
                first_open = false;
            }
            if (isHovered(MouseX, MouseY))
                Render2DEngine.drawBlurredShadow(stack, MouseX - 20, MouseY - 20, 40, 40, 60, Render2DEngine.applyOpacity(new Color(0xC3555A7E, true), getFadeFactor()));
            if (AutoBuyGui.selected_plate != this)
                FontRenderers.icons.drawString(stack, "H", (posX + 80f), (posY + 22f), Render2DEngine.applyOpacity(new Color(0xFFECECEC, true).getRGB(), getFadeFactor()));
            else {
                stack.push();
                stack.translate((posX + 91f), (posY + 15f), 0.0F);
                stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(mc.player.age * 4));
                stack.translate(-(posX + 91f), -(posY + 15f), 0.0F);
                FontRenderers.big_icons.drawString(stack, "H", (posX + 78f), (posY + 5f), Render2DEngine.applyOpacity(new Color(0xFF646464, true).getRGB(), getFadeFactor()));
                stack.translate((posX + 91f), (posY + 15f), 0.0F);
                stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-mc.player.age * 4));
                stack.translate(-(posX + 91f), -(posY + 15f), 0.0F);
                stack.pop();
            }

            if (item.getItem() != null)
                FontRenderers.sf_medium.drawString(stack, I18n.translate(item.getItem().getTranslationKey()), posX + 5, posY + 5, Render2DEngine.applyOpacity(-1, getFadeFactor()), false);

            String ench =item.getEnchantmentsToArray().length == 0 ? "Любые" : String.join(" ", item.getEnchantmentsToArray());

            FontRenderers.sf_medium_mini.drawString(stack, "Цена: " + item.getPrice(), posX + 5, posY + 13.5f, Render2DEngine.applyOpacity(-1, 0.7f), false);
            FontRenderers.sf_medium_mini.drawString(stack, "Мин.Кол-во: " + item.getCount(), posX + 5, posY + 19.5f, Render2DEngine.applyOpacity(-1, 0.7f), false);
            FontRenderers.sf_medium_mini.drawString(stack, "Зачары: " + ench, posX + 5, posY + 25f, Render2DEngine.applyOpacity(-1, 0.7f), false);

            Render2DEngine.popWindow();

            if (item.getItem() != null) {
                stack.translate(posX + 60f, posY + 11f, 0);
                context.drawItem(new ItemStack(item.getItem()), 0, 0);
                stack.translate(-(posX + 60f), -(posY + 11f), 0);
            }

        } else {
            Render2DEngine.drawRound(stack, posX + 1, posY, 89, 30, 4f, new Color(25, 20, 30, 255));
            Render2DEngine.drawRound(stack, posX + 31, posY + 2, 26, 26, 13f, new Color(51, 38, 56, 255));
            Render2DEngine.drawRect(stack, posX + 43, posY + 6, 2, 18, new Color(25, 20, 30, 255));
            Render2DEngine.drawRect(stack, posX + 35, posY + 14, 18, 2, new Color(25, 20, 30, 255));

            // Render2DEngine.drawRound(stack, posX + 1, posY, 89, 30, 4f, new Color(25, 20, 30, 255));

        }
    }

    private float getFadeFactor() {
        return 1f;
    }


    public void onTick() {
        if (progress > 4) {
            progress = 0;
        }
        progress++;

        if (fade < 10 + index) {
            fade++;
        }
    }

    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX > posX && mouseX < posX + 90 && mouseY > posY && mouseY < posY + 30;
    }

    public void movePosition(float deltaX, float deltaY) {
        this.posY += deltaY;
        this.posX += deltaX;
        scrollPosY = posY;
    }

    public void scrollElement(float deltaY) {
        scroll_animation = 0;
        prevPosY = posY;
        this.scrollPosY += deltaY;
    }

    public void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        if ((posY > AutoBuyGui.getInstance().main_posY + AutoBuyGui.getInstance().height) || posY < AutoBuyGui.getInstance().main_posY)
            return;

        if (mouseX > posX && mouseX < posX + 90 && mouseY > posY && mouseY < posY + 30) {
            if (clickedButton == 2) item.setPrice(-999);
            else AutoBuyGui.selected_plate = this;
        }
    }

    public float getPosX() {
        return this.posX;
    }

    public float getPosY() {
        return this.posY;
    }

    public void setPosY(float v) {
        this.posY = v;
        scrollPosY = posY;
    }

    public AutoBuyItem getAutoBuyItem() {
        return this.item;
    }
}
