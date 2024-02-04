package thunder.hack.injection;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.Core;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.misc.PeekScreen;
import thunder.hack.modules.render.Tooltips;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render2DEngine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static thunder.hack.modules.Module.mc;
import static thunder.hack.modules.render.Tooltips.hasItems;

@Mixin(value = {HandledScreen.class})
public abstract class MixinHandledScreen<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {

    @Unique
    private final Timer delayTimer = new Timer();

    @Unique
    private Runnable postRender;

    protected MixinHandledScreen(Text title) {
        super(title);
    }

    @Shadow
    protected abstract boolean isPointOverSlot(Slot slotIn, double mouseX, double mouseY);

    @Shadow
    protected abstract void onMouseClick(Slot slotIn, int slotId, int mouseButton, SlotActionType type);

    @Inject(method = "render", at = @At("HEAD"))
    private void drawScreenHook(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        for (int i1 = 0; i1 < mc.player.currentScreenHandler.slots.size(); ++i1) {
            Slot slot = mc.player.currentScreenHandler.slots.get(i1);
            if (isPointOverSlot(slot, mouseX, mouseY) && slot.isEnabled()) {
                if (ModuleManager.itemScroller.isEnabled() && shit() && attack() && delayTimer.passedMs(ModuleManager.itemScroller.delay.getValue())) {
                    this.onMouseClick(slot, slot.id, 0, SlotActionType.QUICK_MOVE);
                    delayTimer.reset();
                }
            }
        }
    }

    private boolean shit() {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 340) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 344);
    }

    private boolean attack() {
        return Core.hold_mouse0;
    }


    @Shadow
    @Nullable
    protected Slot focusedSlot;
    @Shadow
    protected int x;
    @Shadow
    protected int y;

    private static final Identifier CONTAINER_BACKGROUND = new Identifier("textures/container.png");
    private static final Identifier MAP_BACKGROUND = new Identifier("textures/map_background.png");
    private static final ItemStack[] ITEMS = new ItemStack[27];

    private Map<Render2DEngine.Rectangle, Integer> clickableRects = new HashMap<>();

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (focusedSlot != null && !focusedSlot.getStack().isEmpty() && client.player.playerScreenHandler.getCursorStack().isEmpty()) {
            if (hasItems(focusedSlot.getStack()) && Tooltips.storage.getValue()) {
                renderShulkerToolTip(context, mouseX, mouseY, 0, 0, focusedSlot.getStack());
            } else if (focusedSlot.getStack().getItem() == Items.FILLED_MAP && Tooltips.maps.getValue()) {
                drawMapPreview(context, focusedSlot.getStack(), mouseX, mouseY);
            }
        }
        int xOffset = 0;
        int yOffset = 20;
        int stage = 0;

        if (ModuleManager.tooltips.isEnabled() && ModuleManager.tooltips.shulkerRegear.getValue()) {
            clickableRects.clear();
            for (int i1 = 0; i1 < mc.player.currentScreenHandler.slots.size(); ++i1) {
                Slot slot = mc.player.currentScreenHandler.slots.get(i1);
                if (slot.getStack().isEmpty()) continue;

                if (slot.getStack().getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
                    if (!renderShulkerToolTip(context, xOffset, yOffset + 67, mouseX, mouseY, slot.getStack()))
                        continue;
                    clickableRects.put(new Render2DEngine.Rectangle(xOffset, yOffset, xOffset + 176, yOffset + 67), slot.id);
                    yOffset += 67;
                    if (stage == 0) {
                        if (yOffset + 67 >= mc.getWindow().getScaledHeight()) {
                            yOffset = 20;
                            xOffset = mc.getWindow().getScaledWidth() - 176;
                            stage = 1;
                        }
                    } else if (stage == 1) {
                        if (yOffset + 67 >= mc.getWindow().getScaledHeight()) {
                            yOffset = 20;
                            xOffset = 170;
                            stage = 2;
                        }
                    } else {
                        if (yOffset + 67 >= mc.getWindow().getScaledHeight()) {
                            yOffset = 20;
                            xOffset = mc.getWindow().getScaledWidth() - 352;
                            stage = 0;
                        }
                    }
                }
            }

            if(postRender != null) {
                postRender.run();
                postRender = null;
            }
        }
    }

    public boolean renderShulkerToolTip(DrawContext context, int offsetX, int offsetY, int mouseX, int mouseY, ItemStack stack) {
        try {
            NbtCompound compoundTag = stack.getSubNbt("BlockEntityTag");
            DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(27, ItemStack.EMPTY);
            Inventories.readNbt(compoundTag, itemStacks);

            float[] colors = new float[]{1F, 1F, 1F};
            Item focusedItem = stack.getItem();
            if (focusedItem instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
                try {
                    colors = Objects.requireNonNull(ShulkerBoxBlock.getColor(stack.getItem())).getColorComponents();
                } catch (NullPointerException npe) {
                    colors = new float[]{1F, 1F, 1F};
                }
            }
            draw(context, itemStacks, offsetX, offsetY, mouseX, mouseY, colors);
        } catch (Exception ignore) {
            return false;
        }
        return true;
    }


    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    private void onDrawMouseoverTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        if (focusedSlot != null && !focusedSlot.getStack().isEmpty() && client.player.playerScreenHandler.getCursorStack().isEmpty()) {
            if (focusedSlot.getStack().getItem() == Items.FILLED_MAP && Tooltips.maps.getValue()) ci.cancel();
        }
    }

    private void draw(DrawContext context, DefaultedList<ItemStack> itemStacks, int offsetX, int offsetY, int mouseX, int mouseY, float[] colors) {
        RenderSystem.disableDepthTest();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        offsetX += 8;
        offsetY -= 82;

        drawBackground(context, offsetX, offsetY, colors);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        DiffuseLighting.enableGuiDepthLighting();
        int row = 0;
        int i = 0;
        for (ItemStack itemStack : itemStacks) {
            context.drawItem(itemStack, offsetX + 8 + i * 18, offsetY + 7 + row * 18);
            context.drawItemInSlot(mc.textRenderer, itemStack, offsetX + 8 + i * 18, offsetY + 7 + row * 18);

            if (mouseX > offsetX + 8 + i * 18 && mouseX < offsetX + 28 + i * 18 && mouseY > offsetY + 7 + row * 18 && mouseY < offsetY + 27 + row * 18)
                postRender = () -> context.drawTooltip(textRenderer, getTooltipFromItem(mc, itemStack), itemStack.getTooltipData(), mouseX, mouseY);

            i++;
            if (i >= 9) {
                i = 0;
                row++;
            }
        }
        DiffuseLighting.disableGuiDepthLighting();
        RenderSystem.enableDepthTest();
    }

    private void drawBackground(DrawContext context, int x, int y, float[] colors) {
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(colors[0], colors[1], colors[2], 1F);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        context.drawTexture(CONTAINER_BACKGROUND, x, y, 0, 0, 176, 67, 176, 67);
        RenderSystem.enableBlend();
    }

    private void drawMapPreview(DrawContext context, ItemStack stack, int x, int y) {
        RenderSystem.enableBlend();
        context.getMatrices().push();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int y1 = y - 12;
        int y2 = y1 + 100;
        int x1 = x + 8;
        int x2 = x1 + 100;
        int z = 300;

        context.drawTexture(MAP_BACKGROUND, x1, y1, x2, y2, 0, 0);

        MapState mapState = FilledMapItem.getMapState(stack, client.world);

        if (mapState != null) {
            mapState.getPlayerSyncData(client.player);

            x1 += 8;
            y1 += 8;
            z = 310;
            double scale = (double) (100 - 16) / 128.0D;
            context.getMatrices().translate(x1, y1, z);
            context.getMatrices().scale((float) scale, (float) scale, 0);
            VertexConsumerProvider.Immediate consumer = client.getBufferBuilders().getEntityVertexConsumers();
            client.gameRenderer.getMapRenderer().draw(context.getMatrices(), consumer, FilledMapItem.getMapId(stack), mapState, false, 0xF000F0);
        }
        context.getMatrices().pop();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && focusedSlot != null && !focusedSlot.getStack().isEmpty() && client.player.playerScreenHandler.getCursorStack().isEmpty()) {
            ItemStack itemStack = focusedSlot.getStack();

            if (hasItems(itemStack) && Tooltips.middleClickOpen.getValue()) {

                Arrays.fill(ITEMS, ItemStack.EMPTY);
                NbtCompound nbt = itemStack.getNbt();

                if (nbt != null && nbt.contains("BlockEntityTag")) {
                    NbtCompound nbt2 = nbt.getCompound("BlockEntityTag");
                    if (nbt2 != null && nbt2.contains("Items")) {
                        NbtList nbt3 = nbt2.getList("Items", 10);
                        for (int i = 0; i < nbt3.size(); i++) {
                            ITEMS[nbt3.getCompound(i).getByte("Slot")] = ItemStack.fromNbt(nbt3.getCompound(i));
                        }
                    }
                }

                client.setScreen(new PeekScreen(new ShulkerBoxScreenHandler(0, client.player.getInventory(), new SimpleInventory(ITEMS)), client.player.getInventory(), focusedSlot.getStack().getName(), ((BlockItem) focusedSlot.getStack().getItem()).getBlock()));
                cir.setReturnValue(true);
            }
        }
        for (Render2DEngine.Rectangle rect : clickableRects.keySet()) {
            if (rect.contains(mouseX, mouseY)) {
                if (ModuleManager.tooltips.shulkerRegearShiftMode.getValue()) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, clickableRects.get(rect), 0, SlotActionType.QUICK_MOVE, mc.player);
                } else {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, clickableRects.get(rect), 0, SlotActionType.PICKUP, mc.player);
                }
            }
        }
    }
}
