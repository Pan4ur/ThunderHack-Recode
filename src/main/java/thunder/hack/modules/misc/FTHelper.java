package thunder.hack.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
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
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;

import static thunder.hack.modules.client.ClientSettings.isRu;

public class FTHelper extends Module {

    public FTHelper() {
        super("FTHelper", Category.MISC);
    }

    public final Setting<Boolean> trueSight = new Setting<>("TrueSight", true);
    private final Setting<Boolean> spek = new Setting<>("SpekNotify", true);
    private final Setting<Bind> desorient = new Setting<>("Desorient", new Bind(-1, false, false));
    private final Setting<Bind> trap = new Setting<>("Trap", new Bind(-1, false, false));

    private final Timer disorientTimer = new Timer();
    private final Timer trapTimer = new Timer();

    @EventHandler
    private void onSync(EventSync event) {
        if (fullNullCheck()) return;

        if (isKeyPressed(desorient.getValue().getKey()) && disorientTimer.passedMs(3000) && mc.currentScreen == null) {
            use(InventoryUtility.findInHotBar(i -> i.getItem() == Items.ENDER_EYE && i.getName().getString().contains("Дезориентация")),
                    InventoryUtility.findInInventory(i -> i.getItem() == Items.ENDER_EYE && i.getName().getString().contains("Дезориентация")));
            disorientTimer.reset();
        }

        if (isKeyPressed(trap.getValue().getKey()) && trapTimer.passedMs(3000) && mc.currentScreen == null) {
            use(InventoryUtility.findInHotBar(i -> i.getItem() == Items.NETHERITE_SCRAP && i.getName().getString().contains("Трапка")),
                    InventoryUtility.findInInventory(i -> i.getItem() == Items.NETHERITE_SCRAP && i.getName().getString().contains("Трапка")));
            trapTimer.reset();
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof GameMessageS2CPacket pac && spek.getValue()) {
            String content = pac.content().getString().toLowerCase();
            if (content.contains("спек") || content.contains("ызус") || content.contains("spec") || content.contains("spek") || content.contains("ызул")) {
                String name = ThunderUtility.solveName(pac.content().getString());
                ThunderHack.notificationManager.publicity("SpekNotification", isRu() ? name + " хочет чтобы за ним проследили" : name + " wants to be followed", 3, Notification.Type.WARNING);
            }
        }
    }

    private void use(SearchInvResult result, SearchInvResult invResult) {
        if (result.found()) {
            InventoryUtility.saveAndSwitchTo(result.slot());
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
            InventoryUtility.returnSlot();
        } else if (invResult.found()) {
            clickSlot(invResult.slot(), mc.player.getInventory().selectedSlot, SlotActionType.SWAP);
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
            clickSlot(invResult.slot(), mc.player.getInventory().selectedSlot, SlotActionType.SWAP);
            sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        }
        disorientTimer.reset();
    }
}
