package dev.thunderhack.utils.discord.callbacks;

import dev.thunderhack.utils.discord.DiscordUser;
import com.sun.jna.Callback;

public interface JoinRequestCallback extends Callback {
    void apply(final DiscordUser p0);
}
