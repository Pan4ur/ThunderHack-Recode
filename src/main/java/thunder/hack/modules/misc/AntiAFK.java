package thunder.hack.modules.misc;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.SettingEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

import java.util.concurrent.ThreadLocalRandom;

public class AntiAFK extends Module {

    public AntiAFK() {
        super("AntiAFK", Category.MISC);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Simple);
    private final Setting<Boolean> spin = new Setting<>("Spin", false, v -> mode.getValue() == Mode.Simple);
    private final Setting<Float> speed = new Setting<>("Speed", 5f, 1f, 7f, v -> mode.getValue() == Mode.Simple);
    private final Setting<Boolean> jump = new Setting<>("Jump", false, v -> mode.getValue() == Mode.Simple);
    private final Setting<Boolean> swing = new Setting<>("Swing", false, v -> mode.getValue() == Mode.Simple);
    private final Setting<Boolean> alwayssneak = new Setting<>("AlwaysSneak", false, v -> mode.getValue() == Mode.Simple);
    private final Setting<Integer> radius = new Setting<>("Radius", 64, 1, 128, v -> mode.getValue() == Mode.Baritone);

    private int step;
    private Timer inactiveTime = new Timer();

    private enum Mode {
        Simple, Baritone
    }

    @Override
    public void onEnable() {
        if (alwayssneak.getValue())
            mc.options.sneakKey.setPressed(true);

        step = 0;
    }

    @EventHandler
    public void onSettingChange(SettingEvent e) {
        if(e.getSetting() == mode)
            step = 0;
    }

    @Override
    public void onUpdate() {
        if(mode.getValue() == Mode.Simple) {
            if (spin.getValue()) {
                double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
                float newYaw = mc.player.getYaw() + speed.getValue();
                mc.player.setYaw((float) (newYaw - (newYaw - mc.player.getYaw()) % gcdFix));
            }

            if (jump.getValue() && mc.player.isOnGround())
                mc.player.jump();

            if (swing.getValue() && ThreadLocalRandom.current().nextInt(99) == 0)
                mc.player.swingHand(mc.player.getActiveHand());
        } else {
           if(inactiveTime.every(5000)) {
               if(step > 3)
                   step = 0;

               switch (step) {
                   case 0: {
                       mc.player.networkHandler.sendChatMessage("#goto ~ ~" + radius.getValue());
                       break;
                   }
                   case 1: {
                       mc.player.networkHandler.sendChatMessage("#goto ~" + radius.getValue() + " ~");
                       break;
                   }
                   case 2: {
                       mc.player.networkHandler.sendChatMessage("#goto ~ ~-" + radius.getValue());
                       break;
                   }
                   case 3: {
                       mc.player.networkHandler.sendChatMessage("#goto ~-" + radius.getValue() + " ~");
                       break;
                   }
               }
               step++;
           }
        }

        if(ThunderHack.playerManager.currentPlayerSpeed > 0.07)
            inactiveTime.reset();
    }

    @Override
    public void onDisable() {
        if (alwayssneak.getValue())
            mc.options.sneakKey.setPressed(false);

        if(mode.getValue() == Mode.Baritone)
            mc.player.networkHandler.sendChatMessage("#stop");
    }
}
