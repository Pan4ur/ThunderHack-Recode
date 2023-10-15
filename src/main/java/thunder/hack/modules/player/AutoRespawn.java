package thunder.hack.modules.player;

import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.util.Formatting;
import thunder.hack.ThunderHack;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

public class AutoRespawn extends Module {
    private final Timer timer = new Timer();
    private final Setting<Boolean> deathcoords = new Setting<>("deathcoords", true);
    private final Setting<Boolean> autokit = new Setting<>("Auto Kit", false);
    private final Setting<String> kit = new Setting<>("kit name", "kitname", v -> autokit.getValue());
    private final Setting<Boolean> autohome = new Setting<>("Auto Home", false);

    private boolean flag;

    public AutoRespawn() {
        super("AutoRespawn", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) return;

        if (timer.passedMs(2100)) {
            timer.reset();
        }

        if (mc.currentScreen instanceof DeathScreen) {
            if (flag){
                if(deathcoords.getValue())
                    sendMessage(Formatting.GOLD + "[PlayerDeath] " + Formatting.YELLOW + (int) mc.player.getX() + " " + (int) mc.player.getY() + " " + (int) mc.player.getZ());
                mc.player.requestRespawn();
                mc.setScreen(null);

                ThunderHack.asyncManager.run(()-> {
                    if (autokit.getValue() && mc.player != null) {
                        mc.player.networkHandler.sendChatCommand("kit " + kit.getValue());
                    }
                    if (autohome.getValue() && mc.player != null) {
                        mc.player.networkHandler.sendChatCommand("home");
                    }
                },1000);
                flag = false;
            }
        } else {
            flag = true;
        }
    }
}
