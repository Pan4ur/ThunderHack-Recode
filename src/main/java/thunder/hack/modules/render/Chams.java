package thunder.hack.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import thunder.hack.events.impl.EventHeldItemRenderer;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;

import java.awt.*;

public class Chams extends Module {
    public Chams() {
        super("Chams", Category.RENDER);
    }

    private final Setting<Boolean> handItems = new Setting<>("HandItems", false);
    private final Setting<ColorSetting> handItemsColor = new Setting<>("HandItemsColor", new ColorSetting(new Color(0x9317DE5D, true)));

    @EventHandler
    public void onRenderHands(EventHeldItemRenderer e) {
        if (handItems.getValue())
            RenderSystem.setShaderColor(handItemsColor.getValue().getRed() / 255f,handItemsColor.getValue().getGreen() / 255f,handItemsColor.getValue().getBlue() / 255f,handItemsColor.getValue().getAlpha() / 255f);
    }
}