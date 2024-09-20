package thunder.hack.utility.discord.helpers;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class RPCButton implements Serializable {
    private final String url;
    private final String label;

    public String getLabel() {
        return this.label;
    }

    public String getUrl() {
        return this.url;
    }

    public static @NotNull RPCButton create(String substring, final String s) {
        substring = substring.substring(0, Math.min(substring.length(), 31));
        return new RPCButton(substring, s);
    }

    protected RPCButton(final String label, final String url) {
        this.label = label;
        this.url = url;
    }
}
