package thunder.hack.features.modules.player;

import net.minecraft.item.*;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

import static thunder.hack.features.modules.client.ClientSettings.isRu;
import static thunder.hack.features.modules.combat.AutoTotem.findNearestCurrentItem;

public class ToolSaver extends Module {
    public ToolSaver() {
        super("ToolSaver", Category.PLAYER);
    }

    private final Setting<Integer> savePercent = new Setting<>("Save %", 10, 1, 50);

    @Override
    public void onUpdate() {
        ItemStack tool = mc.player.getMainHandStack();
        if(!(tool.getItem() instanceof MiningToolItem))
            return;

        float durability = tool.getMaxDamage() - tool.getDamage();
        int percent = (int) ((durability / (float) tool.getMaxDamage()) * 100F);

        if(percent <= savePercent.getValue()) {
            mc.player.getInventory().selectedSlot = findNearestCurrentItem();
            sendMessage(isRu() ? "Твой инструмент почти сломался!" : "Your tool is almost broken!");
        }
    }
}
