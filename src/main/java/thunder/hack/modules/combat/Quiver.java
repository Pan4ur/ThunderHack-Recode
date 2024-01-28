package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.potion.PotionUtil;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.world.HoleUtility;

import java.util.List;
import java.util.Objects;

import static thunder.hack.modules.client.MainSettings.isRu;

public final class Quiver extends Module {
    private final Setting<Integer> shootCount = new Setting<>("Shoot Count", 1, 1, 10);
    private final Setting<Boolean> onlyInHole = new Setting<>("Only In Hole", false);

    private static Quiver instance;

    private int preBowSlot;
    private int invPreSlot;
    private int count;

    public Quiver() {
        super("Quiver", Category.COMBAT);
        instance = this;
    }

    @Override
    public void onEnable() {
        count = 0;
        invPreSlot = -1;
        preBowSlot = mc.player.getInventory().selectedSlot;
    }

    @Override
    public void onDisable() {
        if (invPreSlot != -1) {
            switchInvSlot(9, invPreSlot);
        }
        if (preBowSlot != -1) {
            InventoryUtility.switchTo(preBowSlot);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onSync(EventSync event) {
        if (count >= shootCount.getValue()) {
            disable();
            return;
        }
        if ((!HoleUtility.isHole(mc.player.getBlockPos()) && onlyInHole.getValue())
                || (mc.player.isUsingItem() && !mc.player.getMainHandStack().getItem().equals(Items.BOW)))
            return;
        if (!getArrow()) {
            disable(isRu() ? "В интвенторе отсутствуют нужные стрелы! Отключение..." : "No arrows in hotbar! Disabling...");
            return;
        }

        SearchInvResult result = InventoryUtility.findItemInHotBar(Items.BOW);
        if (!result.found()) {
            disable(isRu() ? "В хотбаре отсутствует лук! Отключение..." : "No bow in hotbar! Disabling...");
            return;
        }
        result.switchTo();

        if (BowItem.getPullProgress(mc.player.getItemUseTime()) >= 0.15) {
            shoot();
            return;
        }
        mc.options.useKey.setPressed(true);
    }

    private void shoot() {
        sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), -90, mc.player.isOnGround()));
        mc.options.useKey.setPressed(false);
        mc.interactionManager.stopUsingItem(mc.player);
        count++;
    }

    private boolean getArrow() {
        SearchInvResult result = InventoryUtility.findInInventory(stack -> {
            if (stack.getItem() instanceof TippedArrowItem) {
                List<String> effects = PotionUtil.getPotion(stack).getEffects().stream()
                        .map(StatusEffectInstance::getTranslationKey)
                        .toList();
                return effects.contains("effect.minecraft.speed")
                        || effects.contains("effect.minecraft.strength")
                        || effects.contains("effect.minecraft.invisibility");
            }
            return false;
        });
        if (!result.found())
            return false;
        if (result.isInHotBar() || result.slot() == 9)
            return true;
        invPreSlot = result.slot();

        return switchInvSlot(result.slot(), 9);
    }

    private boolean switchInvSlot(int from, int to) {
        if (from == -1 || to == -1) return false;

        sendPacket(new ClientCommandC2SPacket(
                Objects.requireNonNull(mc.player),
                ClientCommandC2SPacket.Mode.STOP_SPRINTING
        ));
        clickSlot(from);
        clickSlot(to);
        clickSlot(from);
        sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));

        return true;
    }

    public static Quiver getInstance() {
        return instance;
    }
}
