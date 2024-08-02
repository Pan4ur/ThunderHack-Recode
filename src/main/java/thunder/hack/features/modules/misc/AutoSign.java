package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import thunder.hack.events.impl.EventScreen;
import thunder.hack.injection.accesors.ISignEditScreen;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;

import java.text.SimpleDateFormat;
import java.util.Date;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class AutoSign extends Module {
    public AutoSign() {
        super("AutoSign", Category.MISC);
    }

    private final Setting<String> line1 = new Setting<>("Line1", "<player>");
    private final Setting<String> line2 = new Setting<>("Line2", "was here");
    private final Setting<String> line3 = new Setting<>("Line3", "<------------->");
    private final Setting<String> line4 = new Setting<>("Line4", "<date>");
    private final Setting<String> dateFormat = new Setting<>("DateFormat", "dd/MM/yyyy",
            v -> line1.getValue().contains("<date>") || line2.getValue().contains("<date>") || line3.getValue().contains("<date>") || line4.getValue().contains("<date>"));
    private final Setting<Boolean> glow = new Setting<>("Glowing", false);

    @EventHandler
    public void onScreen(EventScreen e) {
        if (e.getScreen() instanceof SignEditScreen ses) {
            e.cancel();
            sendPacketSilent(new UpdateSignC2SPacket(((ISignEditScreen) ses).getBlockEntity().getPos(), ((ISignEditScreen) ses).isFront(), format(line1.getValue()), format(line2.getValue()), format(line3.getValue()), format(line4.getValue())));

            if (glow.getValue()) {
                SearchInvResult result = InventoryUtility.findItemInHotBar(Items.GLOW_INK_SAC);
                boolean offhand = mc.player.getOffHandStack().getItem() == Items.GLOW_INK_SAC;
                if (result.found() || offhand) {
                    InventoryUtility.saveSlot();
                    result.switchTo();
                    mc.interactionManager.interactBlock(mc.player, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND,
                            new BlockHitResult(((ISignEditScreen) ses).getBlockEntity().getPos().toCenterPos().add(0, 0.5, 0), Direction.UP, ((ISignEditScreen) ses).getBlockEntity().getPos(), false));
                    sendPacket(new HandSwingC2SPacket(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND));
                    InventoryUtility.returnSlot();
                }
            }
        }
    }

    public String format(String s) {
        String format = "dd/MM/yyyy";

        try {
            format = new SimpleDateFormat(dateFormat.getValue()).format(new Date());
        } catch (Exception e) {
            sendMessage(Formatting.RED + (isRu() ? "У тебя не правильный формат даты!" : "Your date format is wrong!"));
        }

        return s.replace("<player>", mc.getSession().getUsername()).replace("<date>", format);
    }
}
