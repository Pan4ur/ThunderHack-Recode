package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import thunder.hack.features.cmd.Command;
import thunder.hack.core.manager.client.ConfigManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class TabParseCommand extends Command {
    public TabParseCommand() {
        super("tabparse");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            String serverIP = "unknown_server";
            if (mc.getNetworkHandler().getServerInfo() != null && mc.getNetworkHandler().getServerInfo().address != null)
                serverIP = mc.getNetworkHandler().getServerInfo().address.replace(':', '_');

            String randomSuffix = generateRandomString(5);

            File dir = new File(ConfigManager.TABPARSER_FOLDER, serverIP);

            if (!dir.exists()) dir.mkdirs();

            String fileName = serverIP + "-" + new SimpleDateFormat("dd.MM.yyyy").format(new Date()) + "-" + randomSuffix + ".txt";
            File file = new File(dir, fileName);

            try {
                file.createNewFile();
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
                writer.write("========================\n\n");
                writer.write("Server: " + mc.getNetworkHandler().getServerInfo().address + "\n");
                writer.write("Date: " + new SimpleDateFormat("dd.MM.yyyy").format(new Date()) + "\n\n");
                writer.write("========================\n\n");

                List<PlayerListEntry> sortedPlayers = new ArrayList<>(mc.getNetworkHandler().getPlayerList());
                sortedPlayers.sort((player1, player2) -> {
                    String prefix1 = player1.getScoreboardTeam().getPrefix().getString();
                    String prefix2 = player2.getScoreboardTeam().getPrefix().getString();
                    return prefix2.compareTo(prefix1);
                });

                for (PlayerListEntry entry : sortedPlayers)
                    writer.write(Team.decorateName(entry.getScoreboardTeam(), Text.literal(entry.getProfile().getName())).getString() + "\n");

                writer.close();
                sendMessage(isRu() ? Formatting.GREEN + "Таб успешно сохранен в " + file.getPath() : Formatting.GREEN + "Tab was successfully saved in " + file.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return SINGLE_SUCCESS;
        });
    }


    private String generateRandomString(int length) {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    private String getPlayerPrefix(PlayerListEntry playerInfo) {
        return playerInfo.getDisplayName() != null ? playerInfo.getDisplayName().getString() : "";
    }
}
