package thunder.hack.modules.misc;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.notification.Notification;
import thunder.hack.notification.NotificationManager;
import thunder.hack.setting.Setting;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.RandomStringUtils;

public class AutoAuth extends Module {
    public AutoAuth() {
        super("AutoAuth", "Автоматически-логинится на -серверах", Category.MISC);
    }


    private String password;
    private final Setting<Mode> passwordMode = new Setting<>("Password Mode", Mode.Custom);
    public Setting < String > cpass = new Setting <> ( "Password" , "babidjon777" ,v-> passwordMode.getValue() == Mode.Custom);
    public Setting<Boolean> showPasswordInChat = new Setting<>("Show Pass In Chat", true);

    private enum Mode {
        Custom, Random, Qwerty
    }

    @Override
    public void onEnable(){
        Command.sendMessage(Formatting.RED + "Внимание!!! " + Formatting.RESET + "Пароль сохраняется в конфиге, перед передачей конфига " + Formatting.RED +  " ВЫКЛЮЧИ МОДУЛЬ!");
        Command.sendMessage(Formatting.RED + "Внимание!!! " + Formatting.RESET + "Пароль сохраняется в конфиге, перед передачей конфига " + Formatting.RED +  " ВЫКЛЮЧИ МОДУЛЬ!");
        Command.sendMessage(Formatting.RED + "Внимание!!! " + Formatting.RESET + "Пароль сохраняется в конфиге, перед передачей конфига " + Formatting.RED +  " ВЫКЛЮЧИ МОДУЛЬ!");
    }

    @Override
    public void onDisable(){
        Command.sendMessage(Formatting.RED +  "AutoAuth " + Formatting.RESET + "reseting password...");
        cpass.setValue("none");
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if(event.getPacket() instanceof GameMessageS2CPacket) {
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
                    Command.sendMessage("Твой пароль: " + Formatting.RED + this.password);
                Thunderhack.notificationManager.publicity("AutoAuth","Выполнена регистрация!", 4, Notification.Type.SUCCESS);
            } else if (pac.content().getString().contains("Авторизуйтесь") || pac.content().getString().contains("/l")) {
                mc.getNetworkHandler().sendChatCommand("login " + this.password);
                Thunderhack.notificationManager.publicity("AutoAuth","Выполнен вход!", 4, Notification.Type.SUCCESS);
            }
        }
    }
}
