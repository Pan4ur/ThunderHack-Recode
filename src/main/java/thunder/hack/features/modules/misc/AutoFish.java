package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InventoryUtility;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class AutoFish extends Module {
    public AutoFish() {
        super("AutoFish", Category.MISC);
    }

    private final Setting<DetectMode> detectMode = new Setting<>("DetectMode", DetectMode.DataTracker);
    private final Setting<Boolean> rodSave = new Setting<>("RodSave", true);
    private final Setting<Boolean> changeRod = new Setting<>("ChangeRod", false);
    private final Setting<Boolean> autoSell = new Setting<>("AutoSell", false);

    private boolean flag = false;
    private final Timer timeout = new Timer();
    private final Timer cooldown = new Timer();

    private enum DetectMode {
        Sound, DataTracker
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) disable("NPE protection");
    }

    @Override
    public void onDisable() {
        flag = false;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof PlaySoundS2CPacket sound && detectMode.getValue() == DetectMode.Sound)
            if (sound.getSound().value().equals(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH) && mc.player.fishHook != null && mc.player.fishHook.squaredDistanceTo(sound.getX(), sound.getY(), sound.getZ()) < 4f)
                catchFish();
    }

    @Override
    public void onUpdate() {
        if (mc.player.getMainHandStack().getItem() instanceof FishingRodItem) {
            if (mc.player.getMainHandStack().getDamage() > 52) {
                if (rodSave.getValue() && !changeRod.getValue()) {
                    disable(isRu() ? "Удочка почти сломалась!" : "Saving the rod...");
                } else if (changeRod.getValue() && getRodSlot() != -1) {
                    sendMessage(isRu() ? "Свапнулся на новую удочку" : "Swapped to a new rod");
                    InventoryUtility.switchTo(getRodSlot());
                    cooldown.reset();
                } else disable(isRu() ? "Удочка почти сломалась!" : "Saving the rod...");
            }
        }

        if (!cooldown.passedMs(1000)) return;

        if (timeout.passedMs(45000) && mc.player.getMainHandStack().getItem() instanceof FishingRodItem) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            timeout.reset();
            cooldown.reset();
        }

        if (mc.player.fishHook != null && detectMode.getValue() == DetectMode.DataTracker) {
            boolean caughtFish = mc.player.fishHook.getDataTracker().get(FishingBobberEntity.CAUGHT_FISH);
            if (!flag && caughtFish) {
                catchFish();
                flag = true;
            } else if (!caughtFish) flag = false;
        }
    }

    private void catchFish() {
        Managers.ASYNC.run(() -> {

            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

            if (autoSell.getValue() && timeout.passedMs(1000)) mc.player.networkHandler.sendChatCommand("sellfish");

            try {
                Thread.sleep((int) MathUtility.random(899, 1399));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            timeout.reset();
        }, (int) MathUtility.random(199, 349));
    }

    private int getRodSlot() {
        for (int i = 0; i < 9; i++) {
            final ItemStack item = mc.player.getInventory().getStack(i);
            if (item.getItem() == Items.FISHING_ROD && item.getDamage() < 52) return i;
        }
        return -1;
    }
}
