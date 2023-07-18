package thunder.hack.modules.player;

import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.util.Formatting;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

public class AutoRespawn extends Module {
    public AutoRespawn() {
        super("AutoRespawn", Category.PLAYER);
    }

    private final Timer timer = new Timer();
    public Setting<Boolean> deathcoords = new Setting<>("deathcoords", true);
    public Setting<Boolean> autokit = new Setting<>("Auto Kit", false);
    public Setting<String> kit = new Setting<>("kit name", "kitname", v -> autokit.getValue());
    public Setting<Boolean> autohome = new Setting<>("Auto Home", false);

    private boolean flag;

    @Override
    public void onTick() {
        if (fullNullCheck()) return;

        if (timer.passedMs(2100)) {
            timer.reset();
        }

        if (mc.currentScreen instanceof DeathScreen) {
            if (flag){
                if(deathcoords.getValue())
                    Command.sendMessage(Formatting.GOLD + "[PlayerDeath] " + Formatting.YELLOW + (int) mc.player.getX() + " " + (int) mc.player.getY() + " " + (int) mc.player.getZ());
                mc.player.requestRespawn();
                mc.setScreen(null);

                Thunderhack.asyncManager.run(()-> {
                    if (autokit.getValue()) {
                        mc.player.networkHandler.sendChatCommand("kit " + kit.getValue());
                    }
                    if (autohome.getValue()) {
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
