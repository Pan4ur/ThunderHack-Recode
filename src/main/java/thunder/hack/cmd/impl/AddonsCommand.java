package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import thunder.hack.ThunderHack;
import thunder.hack.api.IAddon;
import thunder.hack.cmd.Command;
import thunder.hack.core.impl.AddonManager;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class AddonsCommand extends Command {
    public AddonsCommand() {
        super("addons");
    }
    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            List<IAddon> sortedAddons = ThunderHack.addonManager.getAddons().stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(IAddon::getName))
                    .toList();

            if (sortedAddons.isEmpty()) {
                sendMessage("No addons installed.");
                return SINGLE_SUCCESS;
            }

            sortedAddons.forEach(iAddon -> {
                sendMessage(iAddon.getName() + " by " + iAddon.getAuthor());
            });

            return SINGLE_SUCCESS;
        });
    }
}
