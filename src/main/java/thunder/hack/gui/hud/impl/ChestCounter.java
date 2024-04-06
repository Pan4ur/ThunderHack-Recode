package thunder.hack.gui.hud.impl;

import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;

import static thunder.hack.modules.render.StorageEsp.getBlockEntities;

public class ChestCounter extends HudElement {
    public ChestCounter() {
        super("ChestCounter", 50, 10);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        Pair<Integer, Integer> chests = getChestCount();
        String str = "Chests: " + Formatting.WHITE + "S:" + chests.getLeft() + " D:" + chests.getRight();
        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), str, getPosX(), getPosY(), HudEditor.getColor(1).getRGB());
    }

    public Pair<Integer, Integer> getChestCount() {
        int singleCount = 0;
        int doubleCount = 0;

        for (BlockEntity be : getBlockEntities()) {
            if (be instanceof ChestBlockEntity chest) {
                ChestType chestType = mc.world.getBlockState(chest.getPos()).get(ChestBlock.CHEST_TYPE);
                if(chestType == ChestType.SINGLE) {
                    singleCount++;
                } else {
                    doubleCount++;
                }
            }
        }
        return new Pair<>(singleCount, doubleCount / 2);
    }
}
