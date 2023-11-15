package thunder.hack.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Formatting;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;

public class MystFinder extends Module {
    public MystFinder() {
        super("MystFinder", Category.MISC);
    }

    // Перезалив на юг карается потерей матери

    private final Setting<Integer> anDelay = new Setting<>("/an delay", 300, 10, 3000);
    private final Setting<Integer> mystDelay = new Setting<>("/myst delay", 10, 10, 500);
    private final Setting<Integer> timeOutV = new Setting<>("TimeOut", 60, 10, 600);
    private final Setting<Anka> anka = new Setting<>("Anka", Anka.X1);
    private final Setting<Boolean> mystik = new Setting<>("Mystik", true);
    private final Setting<Boolean> mayak = new Setting<>("Mayak", true);
    private final Setting<Boolean> meteorit = new Setting<>("Meteorit", true);
    private final Setting<Boolean> sundukSmerti = new Setting<>("Sunduk Smerti", true);

    private final Timer anTimer = new Timer();
    private final Timer mystTimer = new Timer();
    public final Timer timeOut = new Timer();

    public List<String> events = new ArrayList<>();

    private int currentAn = 101;

    // 101 - 113
    // 201 - 236
    // 301 - 318
    // 501 - 510
    // 601 - 608

    @Override
    public void onEnable() {
        timeOut.reset();
        mc.setScreen(new DownloadingTerrainScreen());
        events.clear();
    }

    public void handleESC() {
        if(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.GLFW_KEY_ESCAPE))
            disable("Поиск прекращен!");
    }

    @Override
    public void onUpdate() {
        if(timeOut.passedS(timeOutV.getValue()))
            disable("Не удалось найти мистик(");

        if (currentAn > ankaBounds()[1]) currentAn = ankaBounds()[0];
        else currentAn++;

        if (anTimer.every(anDelay.getValue()))
            mc.player.networkHandler.sendChatCommand("an" + currentAn);

        if (mystTimer.every(mystDelay.getValue()))
            mc.player.networkHandler.sendChatCommand("myst delay");
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof GameMessageS2CPacket pac) {
            if (pac.content.getString().contains("Мистический сундук:")) {
                String event = Formatting.LIGHT_PURPLE + "Мистический сундук на " + Formatting.RESET + currentAn + Formatting.LIGHT_PURPLE +  " анке";
                if(!events.contains(event)) events.add(event);
                if(mystik.getValue()) disable("Мистик обнаружен");
            }
            if (pac.content.getString().contains("Маяк убийца:")) {
                String event = Formatting.AQUA + "Маяк убийца на " + Formatting.RESET + currentAn + Formatting.AQUA + " анке";
                if(!events.contains(event)) events.add(event);
                if(mayak.getValue()) disable("Мистик обнаружен");
            }
            if (pac.content.getString().contains("Сундук смерти:")) {
                String event = Formatting.GOLD + "Сундук смерти на " + Formatting.RESET + currentAn + Formatting.GOLD + " анке";
                if(!events.contains(event)) events.add(event);
                if(sundukSmerti.getValue()) disable("Мистик обнаружен");
            }
            if (pac.content.getString().contains("Метеоритный дождь:")) {
                String event = Formatting.RED + "Метеоритный дождь на " + Formatting.RESET + currentAn + Formatting.RED + " анке";
                if(!events.contains(event)) events.add(event);
                if(meteorit.getValue()) disable("Мистик обнаружен");
            }
        }
    }

    private int[] ankaBounds() {
        switch (anka.getValue()) {
            default -> {
                return new int[]{101, 113};
            }
            case X2 -> {
                return new int[]{201, 236};
            }
            case X3 -> {
                return new int[]{301, 318};
            }
            case X5 -> {
                return new int[]{501, 510};
            }
            case X10 -> {
                return new int[]{601, 608};
            }
        }
    }

    private enum Anka {X1, X2, X3, X5, X10}
}
