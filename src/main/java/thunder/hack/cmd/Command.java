package thunder.hack.cmd;

import thunder.hack.Thunderhack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static thunder.hack.modules.Module.fullNullCheck;

public abstract class Command {
    protected String name;

    public static MinecraftClient mc;

    public Command(String name) {
        this.name = name;
        mc = MinecraftClient.getInstance();
    }

    public static void sendMessage(String message) {
        Command.sendSilentMessage(Thunderhack.commandManager.getClientMessage() + " "  + message);
    }

    public static void sendMessageWithoutTH(String message) {
        Command.sendSilentMessage(message);
    }

    public static void sendSilentMessage(String message) {
        if (fullNullCheck()) {
            return;
        }
        mc.player.sendMessage(Text.of(message));
    }

    public static String getCommandPrefix() {
        return Thunderhack.commandManager.getPrefix();
    }

    public abstract void execute(String[] var1);

    public String getName() {
        return this.name;
    }
}

