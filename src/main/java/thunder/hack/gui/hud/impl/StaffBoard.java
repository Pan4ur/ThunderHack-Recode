package thunder.hack.gui.hud.impl;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import thunder.hack.cmd.impl.StaffCommand;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StaffBoard extends HudElement {
    private static final Pattern validUserPattern = Pattern.compile("^\\w{3,16}$");
    private List<String> players = new ArrayList<>();
    private List<String> notSpec = new ArrayList<>();

    private float vAnimation, hAnimation;

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
            if (s.getPrefix().getString().isEmpty() || mc.isInSingleplayer()) continue;
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
        return name.contains("helper") || name.contains("moder") || name.contains("admin") || name.contains("owner") || name.contains("curator") || name.contains("куратор") || name.contains("модер") || name.contains("админ") || name.contains("хелпер") || name.contains("поддержка");
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        List<String> all = new java.util.ArrayList<>();
        all.addAll(players);
        all.addAll(notSpec);


        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "StaffBoard", getPosX() + hAnimation / 2f, getPosY() + 2, HudEditor.textColor.getValue().getColor());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2, getPosY() + 13.7f, getPosX() + hAnimation / 2f, getPosY() + 14, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + hAnimation / 2f, getPosY() + 13.7f, getPosX() + hAnimation - 2, getPosY() + 14, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));

        Render2DEngine.addWindow(context.getMatrices(), getPosX(), getPosY(), getPosX() + hAnimation, getPosY() + vAnimation, 1f);
        int y_offset = 5;
        for (String player : all) {
            String a = player.split(":")[0] + " " + (player.split(":")[1].equalsIgnoreCase("vanish") ? Formatting.RED + "SPEC" : player.split(":")[1].equalsIgnoreCase("gm3") ? Formatting.RED + "SPEC " + Formatting.YELLOW + "(GM3)" : Formatting.GREEN + "Z");

            if (a.contains("SPEC") && ModuleManager.autoLeave.isEnabled())
                ModuleManager.autoLeave.onStaff();

            FontRenderers.sf_bold_mini.drawString(context.getMatrices(), a, getPosX() + 5, getPosY() + 18 + y_offset, -1, false);
            y_offset += 11;
        }
        Render2DEngine.popWindow();
    }

    public void onRenderShaders(DrawContext context) {
        int y_offset1 = 5;
        float scale_x = 35;
        List<String> all = new java.util.ArrayList<>();
        all.addAll(players);
        all.addAll(notSpec);

        for (String player : all) {
            if (player != null) {
                String a = player.split(":")[0] + " " + (player.split(":")[1].equalsIgnoreCase("vanish") ? Formatting.RED + "SPEC" : player.split(":")[1].equalsIgnoreCase("gm3") ? Formatting.RED + "SPEC " + Formatting.YELLOW + "(GM3)" : Formatting.GREEN + "Z");
                if (FontRenderers.sf_bold_mini.getStringWidth(a) > scale_x) {
                    scale_x = FontRenderers.sf_bold_mini.getStringWidth(a);
                }
            }
            y_offset1 += 11;
        }

        vAnimation = AnimationUtility.fast(vAnimation, 20 + y_offset1, 15);
        hAnimation = AnimationUtility.fast(hAnimation, scale_x + 15, 15);

        Render2DEngine.drawHudBase(context.getMatrices(), getPosX(), getPosY(), hAnimation, vAnimation, HudEditor.hudRound.getValue());
        setBounds((int) (scale_x + 20), 20 + y_offset1);
    }

    @Override
    public void onUpdate() {
        if (mc.player != null && mc.player.age % 10 == 0) {
            players = getVanish();
            notSpec = getOnlinePlayerD();
            players.sort(String::compareTo);
            notSpec.sort(String::compareTo);
        }
    }
}
