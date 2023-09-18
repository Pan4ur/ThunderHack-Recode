package thunder.hack.utility.discord.callbacks;

import com.sun.jna.Callback;

public interface ErroredCallback extends Callback {
    void apply(final int p0, final String p1);
}
