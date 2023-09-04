// 
// Decompiled by Procyon v0.5.36
// 

package thunder.hack.utility.discord.callbacks;

import thunder.hack.utility.discord.DiscordUser;
import com.sun.jna.Callback;

public interface ReadyCallback extends Callback {
    void apply(final DiscordUser p0);
}
