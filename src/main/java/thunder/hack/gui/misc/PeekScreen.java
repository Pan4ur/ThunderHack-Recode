package thunder.hack.gui.misc;

import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import thunder.hack.features.modules.render.Tooltips;

import java.util.Arrays;
import java.util.List;

public class PeekScreen extends ShulkerBoxScreen {
    private static final ItemStack[] ITEMS = new ItemStack[27];

    public PeekScreen(ShulkerBoxScreenHandler handler, PlayerInventory inventory, Text title, Block block) {
        super(handler, inventory, title);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && focusedSlot != null && !focusedSlot.getStack().isEmpty() && client.player.playerScreenHandler.getCursorStack().isEmpty()) {
            ItemStack itemStack = focusedSlot.getStack();

            if (Tooltips.hasItems(itemStack) && Tooltips.middleClickOpen.getValue()) {

                Arrays.fill(ITEMS, ItemStack.EMPTY);
                ContainerComponent nbt = itemStack.get(DataComponentTypes.CONTAINER);
                if (nbt != null) {
                    List<ItemStack> list = nbt.stream().toList();
                    for (int i = 0; i < list.size(); i++)
                        ITEMS[i] = list.get(i);
                }


                client.setScreen(new PeekScreen(new ShulkerBoxScreenHandler(0, client.player.getInventory(), new SimpleInventory(ITEMS)), client.player.getInventory(), focusedSlot.getStack().getName(), ((BlockItem) focusedSlot.getStack().getItem()).getBlock()));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }
}