package thunder.hack.utility.discord;

import java.util.Arrays;
import java.util.List;
import com.sun.jna.Structure;

public class DiscordUser extends Structure {
    public String userId;
    public String username;
    @Deprecated
    public String discriminator;
    public String avatar;
    
    protected List<String> getFieldOrder() {
        return Arrays.asList("userId", "username", "discriminator", "avatar");
    }
}
