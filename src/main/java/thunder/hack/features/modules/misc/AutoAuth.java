package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.RandomStringUtils;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public final class AutoAuth extends Module {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Custom);
    private final Setting<String> cpass = new Setting<>("Password", "babidjon777", v -> mode.getValue() == Mode.Custom);
    private final Setting<Boolean> show = new Setting<>("ShowPassword", true);

    public AutoAuth() {
        super("AutoAuth", Category.MISC);
    }

    private enum Mode {
        Custom, Random, Qwerty
    }

    @Override
    public void onEnable() {
        String warningMsg = isRu() ?
                Formatting.RED + "Внимание! " + Formatting.RESET + "Пароль сохраняется в конфиге, перед передачей конфига " + Formatting.RED + " ВЫКЛЮЧИ МОДУЛЬ!" :
                Formatting.RED + "Attention! " + Formatting.RESET + "The passwords are stored in the config, so before sharing your configs " + Formatting.RED + " TOGGLE OFF THE MODULE!";
        sendMessage(warningMsg);
    }

    @Override
    public void onDisable() {
        sendMessage("Resetting password...");
        cpass.setValue("none");
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.@NotNull Receive event) {
        if (event.getPacket() instanceof GameMessageS2CPacket pac && mc.getNetworkHandler() != null) {
            String password = "";
            switch (mode.getValue()) {
                case Custom -> {
                    password = cpass.getValue();
                    if (password.isEmpty()) {
                        sendMessage(Formatting.RED + (isRu() ? "Ошибка регистрации: Пароль пуст!" : "Registration error: Password is empty!"));
                        return;
                    }
                }
                case Qwerty -> password = "qwerty123";
                case Random -> password = RandomStringUtils.randomAlphabetic(5) + RandomStringUtils.randomPrint(5);
            }

            String m = pac.content().getString().toLowerCase();

            if (m.contains("/reg") || m.contains("/register") || m.contains("зарегистрируйтесь")) {
                mc.getNetworkHandler().sendChatCommand("reg " + password + " " + password);
                if (show.getValue()) sendMessage((isRu() ? "Твой пароль: " : "Your password: ") + Formatting.RED + password);
                Managers.NOTIFICATION.publicity("AutoAuth", isRu() ? "Выполнена регистрация!" : "Registration completed!", 4, Notification.Type.SUCCESS);
            } else if (m.contains("авторизуйтесь") || m.contains("/l")) {
                mc.getNetworkHandler().sendChatCommand("login " + password);
                Managers.NOTIFICATION.publicity("AutoAuth", isRu() ? "Выполнен вход!" : "Logged in!", 4, Notification.Type.SUCCESS);
            }
        }
    }
}
