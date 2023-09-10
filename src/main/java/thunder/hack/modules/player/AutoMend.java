package thunder.hack.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.EventKeyPress;
import thunder.hack.events.impl.EventKeyRelease;
import thunder.hack.events.impl.EventMouse;
import thunder.hack.events.impl.EventSync;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.Timer;

import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.BetterDynamicAnimation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class AutoMend extends Module {
    private final Setting<Bind> key = new Setting<>("Key", new Bind(-1, false, false));
    private final Setting<Integer> delay = new Setting<>("Throw Delay", 100, 0, 100);
    private final Setting<Integer> armorDelay = new Setting<>("Armor Delay", 100, 0, 1000);

    private final Timer timer = new Timer();
    private final Timer timer2 = new Timer();
    public static boolean keyState = false;
    public static BetterDynamicAnimation mendAnimation = new BetterDynamicAnimation();
    boolean need_repair = false;

    public AutoMend() {
        super("AutoMend", Category.PLAYER);
    }

    @EventHandler
    public void onKeyPress(@NotNull EventKeyPress e) {
        if (e.getKey() == key.getValue().getKey()) keyState = true;
    }


    @EventHandler
    public void onKeyRelease(@NotNull EventKeyRelease e) {
        if (e.getKey() == key.getValue().getKey()) keyState = false;
    }


    @EventHandler
    public void onMouse(@NotNull EventMouse e) {
        if (e.getButton() == key.getValue().getKey()) keyState = e.getAction() == 1;
    }


    @EventHandler
    public void onSync(EventSync e) {
        if (keyState && mc.currentScreen == null) {
            mc.player.setPitch(90);
            SearchInvResult xpResult = InventoryUtility.getXp();
            ArrayList<ItemStack> stacks = new ArrayList();
            stacks.add(mc.player.getInventory().armor.get(3));
            stacks.add(mc.player.getInventory().armor.get(2));
            stacks.add(mc.player.getInventory().armor.get(1));
            stacks.add(mc.player.getInventory().armor.get(0));

            need_repair = false;

            for (ItemStack stack : stacks)
                if (PlayerUtility.calculatePercentage(stack) < 100) {
                    need_repair = true;
                    break;
                }

            if (need_repair) {
                int prevItem = mc.player.getInventory().selectedSlot;

                xpResult.switchTo();

                final ItemStack helm = mc.player.getInventory().getStack(36);
                if (!helm.isEmpty() && PlayerUtility.calculatePercentage(helm) >= 100) takeOffSlot(8);

                final ItemStack chest = mc.player.getInventory().getStack(37);
                if (!chest.isEmpty() && PlayerUtility.calculatePercentage(chest) >= 100) takeOffSlot(7);

                final ItemStack legging = mc.player.getInventory().getStack(38);
                if (!legging.isEmpty() && PlayerUtility.calculatePercentage(legging) >= 100) takeOffSlot(6);

                final ItemStack feet = mc.player.getInventory().getStack(39);
                if (!feet.isEmpty() && PlayerUtility.calculatePercentage(feet) >= 100) takeOffSlot(5);

                if (xpResult.found()) {
                    if (timer.passedMs(delay.getValue())) {
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        timer.reset();
                    }
                    mc.player.getInventory().selectedSlot = prevItem;
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevItem));
                }
            }
        }
    }

    public void onRender2D(DrawContext context) {
        if (keyState && mc.currentScreen == null) {
            ArrayList<ItemStack> stacks = new ArrayList();
            stacks.add(mc.player.getInventory().armor.get(0));
            stacks.add(mc.player.getInventory().armor.get(1));
            stacks.add(mc.player.getInventory().armor.get(2));
            stacks.add(mc.player.getInventory().armor.get(3));

            int multiplier = 0;
            int totalarmor = 0;
            for (ItemStack armor : stacks) {
                if (armor.isEmpty()) continue;
                totalarmor += (int) PlayerUtility.calculatePercentage(armor);
                multiplier++;
            }
            float progress = (float) totalarmor / (100f * multiplier);

            final int expCount = InventoryUtility.getItemCount(Items.EXPERIENCE_BOTTLE);

            float posX = mc.getWindow().getScaledWidth() / 2f - 69;
            float posY = mc.getWindow().getScaledHeight() / 2f + 50;

            Render2DEngine.drawGradientBlurredShadow(context.getMatrices(), posX, posY, 137, 48, 17, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
            Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), posX, posY, 137, 47.5f, 9);
            Render2DEngine.drawRound(context.getMatrices(), posX + 0.5f, posY + 0.5f, 136f, 46, 9, Render2DEngine.injectAlpha(Color.BLACK, 220));

            float status = Math.min(100, progress * 100);

            mendAnimation.setValue(status);
            status = (float) mendAnimation.getAnimationD();
            status = Math.max(10f, status);

            Render2DEngine.drawGradientRound(context.getMatrices(), posX + 48, posY + 32, 85, 11, 4f, HudEditor.getColor(0).darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker());

            Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(0), HudEditor.getColor(270), posX + 48, posY + 32, MathUtility.clamp(85 * (status / 100f), 8, 85), 11, 4f);

            FontRenderers.modules.drawString(context.getMatrices(), (int) (progress * 100) + "%", posX + 85f, posY + 37f, -1);

            String action = "Mending...  (" + expCount + " xp)";
            if (!InventoryUtility.getXp().found()) action = "No exp in hotbar!";

            if (!need_repair) action = "Armor is OK!";

            FontRenderers.modules.drawString(context.getMatrices(), action, posX + 48, posY + 7, -1, false);


            context.getMatrices().push();
            context.getMatrices().translate(posX + 24, posY + 24, 0.0F);
            context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees((mc.player.age % 360) * 3));
            context.getMatrices().translate(-(posX + 24), -(posY + 24), 0.0F);

            FontRenderers.big_icons.drawString(context.getMatrices(), "Q", posX + 12, posY + 12, Render2DEngine.applyOpacity(new Color(0xFF646464, true).getRGB(), 170));


            context.getMatrices().translate(posX + 24, posY + 24, 0.0F);
            context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-((mc.player.age % 360)) * 3));
            context.getMatrices().translate(-(posX + 24), -(posY + 24), 0.0F);
            context.getMatrices().pop();

            List<ItemStack> armor = mc.player.getInventory().armor;
            ItemStack[] items = new ItemStack[]{armor.get(3), armor.get(2), armor.get(1), armor.get(0)};

            float xItemOffset = posX + 48;
            for (ItemStack itemStack : items) {
                context.getMatrices().push();
                context.getMatrices().translate(xItemOffset, posY + 15, 0);
                context.getMatrices().scale(0.75f, 0.75f, 0.75f);
                context.drawItem(itemStack, 0, 0);
                context.drawItemInSlot(mc.textRenderer, itemStack, 0, 0);
                context.getMatrices().pop();
                xItemOffset += 12;
            }
        }
    }

    private void takeOffSlot(int slot) {
        if (!timer2.passedMs(armorDelay.getValue())) return;

        int target = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) {
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
}
