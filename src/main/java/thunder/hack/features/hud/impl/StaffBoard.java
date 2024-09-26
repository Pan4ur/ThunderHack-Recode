package thunder.hack.features.hud.impl;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import thunder.hack.features.cmd.impl.StaffCommand;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StaffBoard extends HudElement {
    private static final Pattern validUserPattern = Pattern.compile("^\\w{3,16}$");
    private List<String> players = new ArrayList<>();
    private List<String> notSpec = new ArrayList<>();
    private Map<String, Identifier> skinMap = new HashMap<>();

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
                String name = Arrays.asList(player.getScoreboardTeam().getPlayerList().toArray()).toString().replace("[", "").replace("]", "");

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
            String name = Arrays.asList(s.getPlayerList().toArray()).toString().replace("[", "").replace("]", "");

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
        return name.contains("helper") || name.contains("moder") || name.contains("admin") || name.contains("owner") || name.contains("curator") || name.contains("куратор") || name.contains("модер") || name.contains("админ") || name.contains("хелпер") || name.contains("поддержка") || name.contains("сотрудник") || name.contains("зам") || name.contains("стажёр");
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        List<String> all = new java.util.ArrayList<>();
        all.addAll(players);
        all.addAll(notSpec);

        int y_offset1 = 0;
        float max_width = 50;

        float pointerX = 0;
        for (String player : all) {
            if (y_offset1 == 0)
                y_offset1 += 4;

            y_offset1 += 9;

            float nameWidth = FontRenderers.sf_bold_mini.getStringWidth(player.split(":")[0]);
            float timeWidth = FontRenderers.sf_bold_mini.getStringWidth((player.split(":")[1].equalsIgnoreCase("vanish") ? Formatting.RED + "V" : player.split(":")[1].equalsIgnoreCase("gm3") ? Formatting.RED + "V " + Formatting.YELLOW + "(GM3)" : Formatting.GREEN + "Z"));

            float width = (nameWidth + timeWidth) * 1.4f;

            if (width > max_width)
                max_width = width;

            if (timeWidth > pointerX)
                pointerX = timeWidth;
        }

        vAnimation = AnimationUtility.fast(vAnimation, 14 + y_offset1, 15);
        hAnimation = AnimationUtility.fast(hAnimation, max_width, 15);

        Render2DEngine.drawHudBase(context.getMatrices(), getPosX(), getPosY(), hAnimation, vAnimation, HudEditor.hudRound.getValue());

        if (HudEditor.hudStyle.is(HudEditor.HudStyle.Glowing)) {
            FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "Staff", getPosX() + hAnimation / 2, getPosY() + 4, HudEditor.textColor.getValue().getColorObject());
        } else {
            FontRenderers.sf_bold.drawGradientCenteredString(context.getMatrices(), "Staff", getPosX() + hAnimation / 2, getPosY() + 4, 10);
        }

        if (y_offset1 > 0) {
            if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
                Render2DEngine.drawRectDumbWay(context.getMatrices(), getPosX() + 4, getPosY() + 13, getPosX() + getWidth() - 8, getPosY() + 14, new Color(0x54FFFFFF, true));
            } else {
                Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2, getPosY() + 13.7f, getPosX() + 2 + hAnimation / 2f - 2, getPosY() + 13.5f, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
                Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2 + hAnimation / 2f - 2, getPosY() + 13.7f, getPosX() + 2 + hAnimation - 4, getPosY() + 14, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));
            }
        }


        Render2DEngine.addWindow(context.getMatrices(), getPosX(), getPosY(), getPosX() + hAnimation, getPosY() + vAnimation, 1f);
        int y_offset = 0;

        for (String player : all) {
            float px = getPosX() + (max_width - pointerX - 10);

            Identifier tex = getTexture(player);
            if (tex != null) {
                context.drawTexture(tex, (int) (getPosX() + 3), (int) (getPosY() + 16 + y_offset), 8, 8, 8, 8, 8, 8, 64, 64);
                context.drawTexture(tex, (int) (getPosX() + 3), (int) (getPosY() + 16 + y_offset), 8, 8, 40, 8, 8, 8, 64, 64);
            }

            FontRenderers.sf_bold_mini.drawString(context.getMatrices(), player.split(":")[0], getPosX() + 13, getPosY() + 19 + y_offset, HudEditor.textColor.getValue().getColor());
            FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), (player.split(":")[1].equalsIgnoreCase("vanish") ? Formatting.RED + "O" : player.split(":")[1].equalsIgnoreCase("gm3") ? Formatting.YELLOW + "O" : Formatting.GREEN + "O"),
                    px + (getPosX() + max_width - px) / 2f, getPosY() + 19 + y_offset, HudEditor.textColor.getValue().getColor());
            Render2DEngine.drawRect(context.getMatrices(), px, getPosY() + 17 + y_offset, 0.5f, 8, new Color(0x44FFFFFF, true));
            y_offset += 9;
        }
        Render2DEngine.popWindow();
        setBounds(getPosX(), getPosY(), hAnimation, vAnimation);
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

    private Identifier getTexture(String n) {
        Identifier id = null;
        if (skinMap.containsKey(n))
            id = skinMap.get(n);

        for (PlayerListEntry ple : mc.getNetworkHandler().getPlayerList())
            if (n.contains(ple.getProfile().getName())) {
                id = ple.getSkinTextures().texture();
                if (!skinMap.containsKey(n))
                    skinMap.put(n, id);
                break;
            }

        return id;
    }
}
