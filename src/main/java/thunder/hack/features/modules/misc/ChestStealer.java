package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ItemSelectSetting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;

import java.util.ArrayList;
import java.util.Random;

import static thunder.hack.features.modules.render.StorageEsp.getBlockEntities;

public class ChestStealer extends Module {
    public ChestStealer() {
        super("ChestStealer", Category.MISC);
    }

    public final Setting<ItemSelectSetting> items = new Setting<>("Items", new ItemSelectSetting(new ArrayList<>()));
    private final Setting<Integer> delay = new Setting<>("Delay", 100, 0, 1000);
    private final Setting<Boolean> random = new Setting<>("Random", false);
    private final Setting<Boolean> close = new Setting<>("Close", false);
    private final Setting<Boolean> autoMyst = new Setting<>("AutoMyst", false);
    private final Setting<Sort> sort = new Setting<>("Sort", Sort.None);

    private final Timer autoMystDelay = new Timer();
    private final Timer timer = new Timer();
    private final Random rnd = new Random();

    public void onRender3D(MatrixStack stack) {
        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest) {
            for (int i = 0; i < chest.getInventory().size(); i++) {
                Slot slot = chest.getSlot(i);
                if (slot.hasStack() && isAllowed(slot.getStack())
                        && timer.every(delay.getValue() + (random.getValue() && delay.getValue() != 0 ? rnd.nextInt(delay.getValue()) : 0))
                        && !(mc.currentScreen.getTitle().getString().contains("Аукцион") || mc.currentScreen.getTitle().getString().contains("покупки"))) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    autoMystDelay.reset();
                }
            }
            if (isContainerEmpty(chest) && close.getValue())
                mc.player.closeHandledScreen();
        }
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (autoMyst.getValue() && mc.currentScreen == null && autoMystDelay.passedMs(3000)) {
            for (BlockEntity be : getBlockEntities()) {
                if (be instanceof EnderChestBlockEntity) {
                    if (mc.player.squaredDistanceTo(be.getPos().toCenterPos()) > 39)
                        continue;
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(be.getPos().toCenterPos().add(MathUtility.random(-0.4, 0.4), 0.375, MathUtility.random(-0.4, 0.4)), Direction.UP, be.getPos(), false));
                    mc.player.swingHand(Hand.MAIN_HAND);
                    break;
                }
            }
        }
    }

    private boolean isAllowed(ItemStack stack) {
        boolean allowed = items.getValue().contains(stack.getItem().getTranslationKey().replace("block.minecraft.", "").replace("item.minecraft.", ""));
        return switch (sort.getValue()) {
            case None -> true;
            case WhiteList -> allowed;
            default -> !allowed;
        };
    }

    private boolean isContainerEmpty(GenericContainerScreenHandler container) {
        for (int i = 0; i < (container.getInventory().size() == 90 ? 54 : 27); i++)
            if (container.getSlot(i).hasStack()) return false;
        return true;
    }

    private enum Sort {None, WhiteList, BlackList}
}