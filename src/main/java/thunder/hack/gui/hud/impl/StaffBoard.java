package thunder.hack.gui.hud.impl;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import thunder.hack.cmd.impl.StaffCommand;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StaffBoard extends HudElement {
    private static final Pattern validUserPattern = Pattern.compile("^\\w{3,16}$");
    public final Setting<ColorSetting> shadowColor = new Setting<>("ShadowColor", new ColorSetting(0xFF101010));
    public final Setting<ColorSetting> color2 = new Setting<>("Color", new ColorSetting(0xFF101010));
    public final Setting<ColorSetting> color3 = new Setting<>("Color2", new ColorSetting(0xC59B9B9B));
    public final Setting<ColorSetting> textColor = new Setting<>("TextColor", new ColorSetting(0xBEBEBE));
    List<String> players = new ArrayList<>();
    List<String> notSpec = new ArrayList<>();
    private final LinkedHashMap<UUID, String> nameMap = new LinkedHashMap<>();

    public StaffBoard() {
        super("StaffBoard", 50, 50);
    }

    public static List<String> getOnlinePlayer() {
        return mc.player.networkHandler.getPlayerList().stream()
                .map(PlayerListEntry::getProfile)
                .map(GameProfile::getName)
                .filter(profileName -> validUserPattern.matcher(profileName).matches())
                .collect(Collectors.toList());
    }

    public static List<String> getOnlinePlayerD() {
        List<String> S = new ArrayList<>();
        for (PlayerListEntry player : mc.player.networkHandler.getPlayerList()) {
            if (mc.isInSingleplayer() || player.getScoreboardTeam() == null) break;
            String prefix = player.getScoreboardTeam().getPrefix().getString();
            if (check(Formatting.strip(prefix).toLowerCase())
                    || StaffCommand.staffNames.toString().toLowerCase().contains(player.getProfile().getName().toLowerCase())
                    || player.getProfile().getName().toLowerCase().contains("1danil_mansoru1")
                    || player.getProfile().getName().toLowerCase().contains("barslan_")
                    || player.getProfile().getName().toLowerCase().contains("timmings")
                    || player.getProfile().getName().toLowerCase().contains("timings")
                    || player.getProfile().getName().toLowerCase().contains("ruthless")
                    || player.getScoreboardTeam().getPrefix().getString().contains("YT")
                    || (player.getScoreboardTeam().getPrefix().getString().contains("Y") && player.getScoreboardTeam().getPrefix().getString().contains("T"))) {
                String name = Arrays.asList(player.getScoreboardTeam().getPlayerList().stream().toArray()).toString().replace("[", "").replace("]", "");

                if (player.getGameMode() == GameMode.SPECTATOR) {
                    S.add(player.getScoreboardTeam().getPrefix().getString() + name + ":gm3");
                    continue;
                }
                S.add(player.getScoreboardTeam().getPrefix().getString() + name + ":active");
            }
        }
        return S;
    }

    public List<String> getVanish() {
        List<String> list = new ArrayList<>();
        for (Team s : mc.world.getScoreboard().getTeams()) {
            if (s.getPrefix().getString().length() == 0 || mc.isInSingleplayer()) continue;
            String name = Arrays.asList(s.getPlayerList().stream().toArray()).toString().replace("[", "").replace("]", "");

            if (getOnlinePlayer().contains(name) || name.isEmpty())
                continue;
            if (StaffCommand.staffNames.toString().toLowerCase().contains(name.toLowerCase())
                    && check(s.getPrefix().getString().toLowerCase())
                    || check(s.getPrefix().getString().toLowerCase())
                    || name.toLowerCase().contains("1danil_mansoru1")
                    || name.toLowerCase().contains("barslan_")
                    || name.toLowerCase().contains("timmings")
                    || name.toLowerCase().contains("timings")
                    || name.toLowerCase().contains("ruthless")
                    || s.getPrefix().getString().contains("YT")
                    || (s.getPrefix().getString().contains("Y") && s.getPrefix().getString().contains("T"))
            )
                list.add(s.getPrefix().getString() + name + ":vanish");
        }
        return list;
    }

    public static boolean check(String name) {
        if (mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().address.contains("mcfunny")) {
            return name.contains("helper") || name.contains("moder") || name.contains("модер") || name.contains("хелпер");
        }
        return name.contains("helper") || name.contains("moder") || name.contains("admin") || name.contains("owner") || name.contains("curator") || name.contains("куратор") || name.contains("модер") || name.contains("админ") || name.contains("хелпер");
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        int y_offset1 = 11;
        List<String> all = new java.util.ArrayList<>();
        all.addAll(players);
        all.addAll(notSpec);
        float scale_x = 50;
        for (String player : all) {
            if (player != null) {
                String a = player.split(":")[0] + " " + (player.split(":")[1].equalsIgnoreCase("vanish") ? Formatting.RED + "VANISH" : player.split(":")[1].equalsIgnoreCase("gm3") ? Formatting.RED + "VANISH " + Formatting.YELLOW + "(NEAR!)" : Formatting.GREEN + "ACTIVE");
                if (FontRenderers.modules.getStringWidth(a) > scale_x) {
                    scale_x = FontRenderers.modules.getStringWidth(a);
                }
            }
            y_offset1 += 13;
        }

        Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX(), getPosY(), scale_x + 20, 20 + y_offset1, 20, shadowColor.getValue().getColorObject());

        Render2DEngine.drawRound(context.getMatrices(), getPosX(), getPosY(), scale_x + 20, 20 + y_offset1, 7f, color2.getValue().getColorObject());
        FontRenderers.modules.drawCenteredString(context.getMatrices(), "StaffBoard", getPosX() + (scale_x + 20) / 2, getPosY() + 5, textColor.getValue().getColor());
        Render2DEngine.drawRound(context.getMatrices(), getPosX() + 2, getPosY() + 13, scale_x + 16, 1, 0.5f, color3.getValue().getColorObject());

        int y_offset = 11;
        for (String player : all) {
            String a = player.split(":")[0] + " " + (player.split(":")[1].equalsIgnoreCase("vanish") ? Formatting.RED + "VANISH" : player.split(":")[1].equalsIgnoreCase("gm3") ? Formatting.RED + "VANISH " + Formatting.YELLOW + "(NEAR!)" : Formatting.GREEN + "ACTIVE");
            FontRenderers.modules.drawString(context.getMatrices(), a, getPosX() + 5, getPosY() + 18 + y_offset, -1, false);
            y_offset += 13;
        }
    }

    @Override
    public void onDisable() {
        nameMap.clear();
    }

    @Override
    public void onUpdate() {
        if (mc.player.age % 10 == 0) {
            players = getVanish();
            notSpec = getOnlinePlayerD();
            players.sort(String::compareTo);
            notSpec.sort(String::compareTo);
        }
    }
}
