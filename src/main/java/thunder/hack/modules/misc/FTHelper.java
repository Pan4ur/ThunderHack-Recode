package thunder.hack.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.gui.notification.Notification;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.PlayerUtility;

import java.util.Objects;

import static thunder.hack.modules.client.MainSettings.isRu;

public class FTHelper extends Module {

    public FTHelper() {
        super("FTHelper", Category.MISC);
    }

    public final Setting<Boolean> trueSight = new Setting<>("TrueSight", true);
    public final Setting<Boolean> spek = new Setting<>("SpecNotification", true);
    private final Setting<Bind> desorient = new Setting<>("Desorient", new Bind(-1, false, false));
    private final Setting<Bind> trap = new Setting<>("Trap", new Bind(-1, false, false));

    private final Timer disorientTimer = new Timer();
    private final Timer trapTimer = new Timer();


    @EventHandler
    @SuppressWarnings("unused")
    private void onSync(EventSync event) {
        if (fullNullCheck()) return;

        if (isKeyPressed(desorient.getValue().getKey()) && disorientTimer.passedMs(3000) && mc.currentScreen == null) {
            use(getDisorientAtHotBar(), getDisorientAtInventory());
            disorientTimer.reset();
        }

        if (isKeyPressed(trap.getValue().getKey()) && trapTimer.passedMs(3000) && mc.currentScreen == null) {
            use(getTrapAtHotBar(), getTrapAtInventory());
            trapTimer.reset();
        }
    }
    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof GameMessageS2CPacket && spek.getValue()) {
            final GameMessageS2CPacket packet = event.getPacket();
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            String playerName = player.getName().toString();
            int startIndex = playerName.indexOf("{") + 1;
            int endIndex = playerName.indexOf("}");
            if (packet.content().getString().contains("спек")){
                String nickname = playerName.substring(startIndex, endIndex);
                ThunderHack.notificationManager.publicity("SpekNotification", isRu() ? nickname +  " хочет чтобы за ним проследили" : "Someone wants to be followed", 3, Notification.Type.WARNING);
            }
        }
    }

    private void use(int slot, int islot) {
        if (slot != -1) {
            int originalSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
            sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtility.getWorldActionId(mc.world)));
            mc.player.getInventory().selectedSlot = originalSlot;
            sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
        } else if (islot != -1) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, islot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtility.getWorldActionId(mc.world)));
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, islot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
        }
        disorientTimer.reset();
    }

    private int getDisorientAtHotBar() {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!(itemStack.getItem() == Items.ENDER_EYE)) continue;
            if (!(itemStack.getName().getString().contains("Дезориентация"))) continue;
            return i;
        }
        return -1;
    }

    private int getTrapAtHotBar() {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!(itemStack.getItem() == Items.NETHERITE_SCRAP)) continue;
            if (!(itemStack.getName().getString().contains("Трапка"))) continue;
            return i;
        }
        return -1;
    }

    private int getDisorientAtInventory() {
        for (int i = 36; i >= 0; i--) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!(itemStack.getItem() == Items.ENDER_EYE)) continue;
            if (!(itemStack.getName().getString().contains("Дизориентация"))) continue;
            if (i < 9) i += 36;
            return i;
        }
        return -1;
    }

    private int getTrapAtInventory() {
        for (int i = 36; i >= 0; i--) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!(itemStack.getItem() == Items.NETHERITE_SCRAP)) continue;
            if (!(itemStack.getName().getString().contains("Трапка"))) continue;
            if (i < 9) i += 36;
            return i;
        }
        return -1;
    }
}
