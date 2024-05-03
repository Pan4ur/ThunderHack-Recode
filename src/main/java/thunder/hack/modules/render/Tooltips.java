package thunder.hack.modules.render;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.inventory.Inventories;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class Tooltips extends Module {
    public Tooltips() {
        super("Tooltips", Category.MISC);
    }

    public static final Setting<Boolean> middleClickOpen = new Setting<>("MiddleClickOpen", true);
    public static final Setting<Boolean> storage =  new Setting<>("Storage", true);
    public static final Setting<Boolean> maps =  new Setting<>("Maps", true);
    public final Setting<Boolean> shulkerRegear = new Setting<>("ShulkerRegear", true);
    public final Setting<Boolean> shulkerRegearShiftMode = new Setting<>("RegearShift", true);

    public static boolean hasItems(ItemStack itemStack) {
        ContainerComponent compoundTag = itemStack.get(DataComponentTypes.CONTAINER);
        return compoundTag != null && !compoundTag.stream().toList().isEmpty();
    }
}
