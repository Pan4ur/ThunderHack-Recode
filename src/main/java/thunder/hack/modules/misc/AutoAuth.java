package thunder.hack.modules.misc;

import meteordevelopment.orbit.EventHandler;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.RandomStringUtils;

import static thunder.hack.modules.client.ClientSettings.isRu;

public final class AutoAuth extends Module {
    private final Setting<Mode> passwordMode = new Setting<>("Password Mode", Mode.Custom);
    private final Setting<String> cpass = new Setting<>("Password", "babidjon777", v -> passwordMode.getValue() == Mode.Custom);
    private final Setting<Boolean> showPasswordInChat = new Setting<>("Show Pass In Chat", true);

    private String password;

    public AutoAuth() {
        super("AutoAuth", Category.MISC);
    }

    private enum Mode {
        Custom, Random, Qwerty
    }

    @Override
    public void onEnable() {
        String warningMsg = isRu() ?
                Formatting.RED + "Внимание!!! " + Formatting.RESET + "Пароль сохраняется в конфиге, перед передачей конфига " + Formatting.RED + " ВЫКЛЮЧИ МОДУЛЬ!" :
                Formatting.RED + "Attention!!! " + Formatting.RESET + "The passwords are stored in the config, so before sharing your configs " + Formatting.RED + " TOGGLE OFF THE MODULE!";
        sendMessage(warningMsg);
    }

    @Override
    public void onDisable() {
        sendMessage("Resetting password...");
        cpass.setValue("none");
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.@NotNull Receive event) {
        if (event.getPacket() instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket pac = event.getPacket();
            if (passwordMode.getValue() == Mode.Custom) {
                this.password = cpass.getValue();
            } else if (passwordMode.getValue() == Mode.Qwerty) {
                this.password = "qwerty123";
            } else if (passwordMode.getValue() == Mode.Random) {
                String str1 = RandomStringUtils.randomAlphabetic(5);
                String str2 = RandomStringUtils.randomPrint(5);
                this.password = str1 + str2;
            }
            if (passwordMode.getValue() == Mode.Custom && (this.password == null || this.password.isEmpty()))
                return;
            if (pac.content().getString().contains("/reg") || pac.content().getString().contains("/register") || pac.content().getString().contains("Зарегистрируйтесь")) {
                mc.getNetworkHandler().sendChatCommand("reg " + this.password + " " + this.password);
                if (this.showPasswordInChat.getValue())
                    sendMessage("Твой пароль: " + Formatting.RED + this.password);
                ThunderHack.notificationManager.publicity("AutoAuth", "Выполнена регистрация!", 4, Notification.Type.SUCCESS);
            } else if (pac.content().getString().contains("Авторизуйтесь") || pac.content().getString().contains("/l")) {
                mc.getNetworkHandler().sendChatCommand("login " + this.password);
                ThunderHack.notificationManager.publicity("AutoAuth", "Выполнен вход!", 4, Notification.Type.SUCCESS);
            }
        }
    }
}
