package thunder.hack.modules.player;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Keyboard;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.opengl.GL40C;
import thunder.hack.core.ModuleManager;
import thunder.hack.events.impl.*;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.impl.TargetHud;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtil;
import thunder.hack.utility.player.InventoryUtil;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.BetterDynamicAnimation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class AutoMend extends Module {
    public AutoMend() {
        super("AutoMend", Category.PLAYER);
    }

    public Setting<Bind> key = new Setting<>("Key", new Bind(-1,false));
    private final Setting<Integer> threshold = new Setting<>("Percent", 100, 0, 100);
    private final Setting<Integer> dlay = new Setting<>("ThrowDelay", 100, 0, 100);
    private final Setting<Integer> armdlay = new Setting<>("ArmorDelay", 100, 0, 1000);

    private final Timer timer = new Timer();
    private final Timer timer2 = new Timer();
    public static boolean keyState = false;
    public static BetterDynamicAnimation mendAnimation = new BetterDynamicAnimation();
    boolean need_repair = false;

    @Subscribe
    public void onKeyPress(EventKeyPress e){
        if(e.getKey() == key.getValue().getKey()) keyState = true;
    }

    @Subscribe
    public void onKeyRelease(EventKeyRelease e){
        if(e.getKey() == key.getValue().getKey()) keyState = false;
    }

    @Subscribe
    public void onMouse(EventMouse e){
        if(e.getButton() == key.getValue().getKey()) keyState = e.getAction() == 1;
    }

    @Subscribe
    public void onSync(EventSync event) {
        if (keyState) mc.player.setPitch(90);
    }

    @Subscribe
    public void onPostSync(EventPostSync e) {
        if(keyState) {
            int xp_slot = InventoryUtil.getXpSlot();

            ArrayList<ItemStack> stacks = new ArrayList();
            stacks.add(mc.player.getInventory().armor.get(3));
            stacks.add(mc.player.getInventory().armor.get(2));
            stacks.add(mc.player.getInventory().armor.get(1));
            stacks.add(mc.player.getInventory().armor.get(0));

            need_repair = false;

            for(ItemStack stack : stacks)
                if(calculatePercentage(stack) < 100){
                    need_repair = true;
                    break;
                }

            if (need_repair) {
                int prevItem = mc.player.getInventory().selectedSlot;

                if(xp_slot != -1) {
                    mc.player.getInventory().selectedSlot = xp_slot;
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(xp_slot));
                }

                final ItemStack helm = mc.player.getInventory().getStack(36);
                if (!helm.isEmpty() && calculatePercentage(helm) >= 100) takeOffSlot(8);

                final ItemStack chest = mc.player.getInventory().getStack(37);
                if (!chest.isEmpty() && calculatePercentage(chest) >= 100) takeOffSlot(7);

                final ItemStack legging = mc.player.getInventory().getStack(38);
                if (!legging.isEmpty() && calculatePercentage(legging) >= 100) takeOffSlot(6);

                final ItemStack feet = mc.player.getInventory().getStack(39);
                if (!feet.isEmpty() &&  calculatePercentage(feet) >= 100) takeOffSlot(5);

                if(xp_slot != -1) {
                    if (timer.passedMs(dlay.getValue())) {
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        timer.reset();
                    }
                    mc.player.getInventory().selectedSlot = prevItem;
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevItem));
                }
            }
        }
    }

    @Subscribe
    public void onRender2D(Render2DEvent e) {
        if (keyState) {
            ArrayList<ItemStack> stacks = new ArrayList();
            stacks.add(mc.player.getInventory().armor.get(0));
            stacks.add(mc.player.getInventory().armor.get(1));
            stacks.add(mc.player.getInventory().armor.get(2));
            stacks.add(mc.player.getInventory().armor.get(3));

            int multiplier = 0;
            int totalarmor = 0;
            for(ItemStack armor : stacks){
                if(armor.isEmpty()) continue;
                totalarmor += (int) calculatePercentage(armor);
                multiplier++;
            }
            float progress = (float) totalarmor / (100f * multiplier);

            final int expCount = InventoryUtil.getExpCount();

            float posX = mc.getWindow().getScaledWidth() / 2f - 69;
            float posY = mc.getWindow().getScaledHeight() / 2f + 50;


            Render2DEngine.drawGradientBlurredShadow(e.getMatrixStack(),posX, posY, 137, 48,17, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
            Render2DEngine.renderRoundedGradientRect(e.getMatrixStack(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90),posX, posY , 137, 47.5f,9);
            Render2DEngine.drawRound(e.getMatrixStack(),posX + 0.5f, posY + 0.5f, 136f, 46,9, Render2DEngine.injectAlpha(Color.BLACK,220));

            float status = Math.min(100, progress * 100);

            mendAnimation.setValue(status);
            status = (float) mendAnimation.getAnimationD();
            status = Math.max(10f,status);
            Render2DEngine.drawGradientRound(e.getMatrixStack(),posX + 48, posY + 32, 85, 11, 4f, HudEditor.getColor(0).darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker());

            Render2DEngine.renderRoundedGradientRect(e.getMatrixStack(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(0), HudEditor.getColor(270),posX + 48, posY + 32, MathUtil.clamp(85 * (status/100f),8,85), 11, 4f);

            FontRenderers.modules.drawString( e.getMatrixStack(), (int)(progress * 100) + "%", posX + 85f, posY + 37f, -1);

            String action = "Mending...  (" + expCount + " xp)";
            if(InventoryUtil.getXpSlot() == -1) action = "No exp in hotbar!";
            if(!need_repair) action = "Armor is OK!";

            FontRenderers.modules.drawString(e.getMatrixStack(), action, posX + 48, posY + 7, -1, false);


            e.getMatrixStack().push();
            e.getMatrixStack().translate(posX + 24, posY + 24, 0.0F);
            e.getMatrixStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees((mc.player.age % 360) * 3));
            e.getMatrixStack().translate(-(posX + 24), -(posY + 24), 0.0F);

            FontRenderers.big_icons.drawString(e.getMatrixStack(),"Q",posX + 12, posY + 12, Render2DEngine.applyOpacity(new Color(0xFF646464, true).getRGB(), 170));

            e.getMatrixStack().translate(posX + 24, posY + 24, 0.0F);
            e.getMatrixStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-((mc.player.age % 360)) * 3));
            e.getMatrixStack().translate(-(posX + 24), -(posY + 24), 0.0F);
            e.getMatrixStack().pop();

            List<ItemStack> armor = mc.player.getInventory().armor;
            ItemStack[] items = new ItemStack[]{armor.get(3), armor.get(2), armor.get(1), armor.get(0)};

            float xItemOffset = posX + 48;
            for (ItemStack itemStack : items) {
                e.getMatrixStack().push();
                e.getMatrixStack().translate(xItemOffset, posY + 15, 0);
                e.getMatrixStack().scale(0.75f, 0.75f, 0.75f);
                e.getContext().drawItem(itemStack, 0, 0);
                e.getContext().drawItemInSlot(mc.textRenderer, itemStack, 0, 0);
                e.getMatrixStack().pop();
                xItemOffset += 12;
            }
        }
    }

    private void takeOffSlot(int slot) {
        if(!timer2.passedMs(armdlay.getValue())) return;

        int target = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()){
                target = i;
                break;
            }
        }
        if (target != -1) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, target, 0, SlotActionType.PICKUP, mc.player);
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            timer2.reset();
        }
    }

    public static float calculatePercentage(ItemStack stack) {
        float durability = stack.getMaxDamage() - stack.getDamage();
        return (durability / (float) stack.getMaxDamage()) * 100F;
    }

}
