package thunder.hack.modules.misc;

import thunder.hack.modules.Module;
import static thunder.hack.modules.client.MainSettings.isRu;

public class GetNbtTags extends Module {
    public GetNbtTags() {
        super("GetNbtTags", Module.Category.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.player.getMainHandStack().hasNbt()) {
            disable(mc.player.getMainHandStack().getNbt().toString());
        }
        else{
            disable(isRu() ? "У этого предмета нет nbt тегов!" : "This item dont contains nbt tags!");
        }
    }
}
