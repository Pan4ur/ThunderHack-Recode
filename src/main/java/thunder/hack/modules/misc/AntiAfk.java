package thunder.hack.modules.misc;

import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

import java.util.Random;

public class AntiAfk extends Module {
    public AntiAfk() {
        super("AntiAfk", Category.MISC);
    }
    private final Setting<Boolean> spin = new Setting<>("Spin", false);
    public Setting<Float> speed = new Setting<>("Speed", 5f, 1f, 7f);
    private final Setting<Boolean> jump = new Setting<>("Jump", false);
    private final Setting<Boolean> swing = new Setting<>("Swing", false);
    private final Setting<Boolean> alwayssneak = new Setting<>("AlwaysSneak", false);
    private float prevYaw;
    private final Random random = new Random();

    @Override
    public void onEnable() {
        prevYaw = mc.player.getYaw();
        if(alwayssneak.getValue()){mc.options.sneakKey.setPressed(true);}
    }

    @Override
    public void onUpdate(){
        if(spin.getValue()){
            prevYaw += speed.getValue();
            mc.player.setYaw(prevYaw);
        }
        if(jump.getValue()){
            if (mc.options.jumpKey.isPressed()) mc.options.jumpKey.setPressed(false);
            else if (random.nextInt(99) == 0) mc.options.jumpKey.setPressed(true);
        }
        if(swing.getValue()){
            if (random.nextInt(99) == 0) mc.player.swingHand(mc.player.getActiveHand());
        }
    }

    @Override
    public void onDisable() {
        if(alwayssneak.getValue()){mc.options.sneakKey.setPressed(false);}
    }
}
