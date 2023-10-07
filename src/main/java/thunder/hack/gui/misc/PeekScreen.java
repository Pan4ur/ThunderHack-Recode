package thunder.hack.gui.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.modules.render.Tooltips;
import net.minecraft.block.Block;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

public class PeekScreen extends ShulkerBoxScreen {
    private static final Identifier TEXTURE = new Identifier("textures/container.png");

    private static final ItemStack[] ITEMS = new ItemStack[27];

    private final Block block;

    public PeekScreen(ShulkerBoxScreenHandler handler, PlayerInventory inventory, Text title, Block block) {
        super(handler, inventory, title);
        this.block = block;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && focusedSlot != null && !focusedSlot.getStack().isEmpty() && client.player.playerScreenHandler.getCursorStack().isEmpty()) {
            ItemStack itemStack = focusedSlot.getStack();

            if (Tooltips.hasItems(itemStack) && Tooltips.middleClickOpen.getValue()) {

                Arrays.fill(ITEMS, ItemStack.EMPTY);
                NbtCompound nbt = itemStack.getNbt();

                if (nbt != null && nbt.contains("BlockEntityTag")) {
                    NbtCompound nbt2 = nbt.getCompound("BlockEntityTag");
                    if (nbt2.contains("Items")) {
                        NbtList nbt3 = nbt2.getList("Items",10);
                        for (int i = 0; i < nbt3.size(); i++) {
                            ITEMS[nbt3.getCompound(i).getByte("Slot")] = ItemStack.fromNbt(nbt3.getCompound(i));
                        }
                    }
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

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        if (block instanceof ShulkerBoxBlock) {
            if(((ShulkerBoxBlock) block).getColor() != null) {
                float[] colors = ((ShulkerBoxBlock) block).getColor().getColorComponents();
                RenderSystem.setShaderColor(colors[0], colors[1], colors[2], 1.0F);
            }
        } else if (block instanceof EnderChestBlock) {
            RenderSystem.setShaderColor(0F, 50F / 255F, 50F / 255F, 1.0F);
        } else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;

        context.drawTexture(TEXTURE, i + 2,j + 12, 0, 0, 176, 67, 176, 67);

        RenderSystem.setShaderColor(1f,1f,1f,1f);
    }
}