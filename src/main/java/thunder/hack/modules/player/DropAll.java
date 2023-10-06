package thunder.hack.modules.player;

import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

import static thunder.hack.modules.client.MainSettings.isRu;

public class DropAll extends Module {
    public DropAll() {
        super("DropAll", Category.PLAYER);
    }

    private Thread dropThread;

    public Setting<Integer> delay = new Setting<>("Delay", 70, 0, 500);

    @Override
    public void onEnable() {
        if (dropThread != null) {
            dropThread.interrupt();
            dropThread = null;
        }

        dropThread = new Thread(() -> {
            for (int i = 5; i <= 45; i++) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 1, SlotActionType.THROW, mc.player);
                try {
                    Thread.sleep(delay.getValue());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        });
        dropThread.start();
        disable(isRu() ? "Выбросил" : "Done");
    }
}