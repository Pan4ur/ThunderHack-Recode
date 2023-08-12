package thunder.hack.gui.hud.impl;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;

public class TestHud extends HudElement {


    public TestHud() {
        super("TestHud", "TestHud", 100, 100);

    }

    private final Setting<Float> r = new Setting<>("r", 15f, 1f, 360f);
    private final Setting<Float> s = new Setting<>("s", 15f, 1f, 360f);

}
