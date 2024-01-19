package thunder.hack.modules.misc;

import thunder.hack.setting.Setting;
import thunder.hack.modules.Module;
import static thunder.hack.modules.client.MainSettings.isRu;

public class GetNbtTags extends Module {

    private final Setting<Boolean> copy = new Setting<>("Copy", false);
    
    public GetNbtTags() {
        super("GetNbtTags", Module.Category.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.player.getMainHandStack().hasNbt()) {
            String nbt = mc.player.getMainHandStack().getNbt().toString();
            if (copy.getValue()) mc.keyboardHandler.setClipboard(nbt);
            disable(nbt);
        }
        else{
            disable(isRu() ? "У этого предмета нет nbt тегов!" : "This item doesn't have any NBT!");
        }
    }
}
