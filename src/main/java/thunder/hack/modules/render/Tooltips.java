package thunder.hack.modules.render;

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

    public static boolean hasItems(ItemStack itemStack) {
        NbtCompound compoundTag = itemStack.getSubNbt("BlockEntityTag");
        return compoundTag != null && compoundTag.contains("Items", 9);
    }
}
