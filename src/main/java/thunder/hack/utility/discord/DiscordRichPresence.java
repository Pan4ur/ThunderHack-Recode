package thunder.hack.utility.discord;

import java.time.OffsetDateTime;
import java.util.Collections;
import thunder.hack.utility.discord.helpers.RPCButton;
import java.util.Arrays;
import java.util.List;
import com.sun.jna.Structure;

public class DiscordRichPresence extends Structure {
    public String largeImageKey;
    public String largeImageText;
    public String smallImageText;
    public String partyPrivacy;
    public long startTimestamp;
    public String button_label_1;
    public int instance;
    public String partyId;
    public int partySize;
    public long endTimestamp;
    public String details;
    public String joinSecret;
    public String spectateSecret;
    public String smallImageKey;
    public String matchSecret;
    public String button_url_2;
    public String button_label_2;
    public String state;
    public String button_url_1;
    public int partyMax;
    
    public DiscordRichPresence() {
        this.setStringEncoding("UTF-8");
    }
    
    protected List<String> getFieldOrder() {
        return Arrays.asList("state", "details", "startTimestamp", "endTimestamp", "largeImageKey", "largeImageText", "smallImageKey", "smallImageText", "partyId", "partySize", "partyMax", "partyPrivacy", "matchSecret", "joinSecret", "spectateSecret", "button_label_1", "button_url_1", "button_label_2", "button_url_2", "instance");
    }
    
    public static class Builder {
        private final DiscordRichPresence rpc;
        
        public Builder setSmallImage(final String s) {
            return this.setSmallImage(s, "");
        }
        
        public Builder setDetails(final String s) {
            if (s != null && !s.isEmpty()) {
                this.rpc.details = s.substring(0, Math.min(s.length(), 128));
            }
            return this;
        }
        
        public Builder setLargeImage(final String largeImageKey, final String largeImageText) {
            this.rpc.largeImageKey = largeImageKey;
            this.rpc.largeImageText = largeImageText;
            return this;
        }
        
        public Builder setState(final String s) {
            if (s != null && !s.isEmpty()) {
                this.rpc.state = s.substring(0, Math.min(s.length(), 128));
            }
            return this;
        }
        
        public Builder setInstance(final boolean instance) {
            if ((this.rpc.button_label_1 != null && this.rpc.button_label_1.isEmpty()) || (this.rpc.button_label_2 != null && this.rpc.button_label_2.isEmpty())) {
                return this;
            }
            this.rpc.instance = (instance ? 1 : 0);
            return this;
        }
        
        public Builder setButtons(final RPCButton o) {
            return this.setButtons(Collections.singletonList(o));
        }
        
        public Builder setSmallImage(final String smallImageKey, final String smallImageText) {
            this.rpc.smallImageKey = smallImageKey;
            this.rpc.smallImageText = smallImageText;
            return this;
        }
        
        public Builder setParty(final String partyId, final int partySize, final int partyMax) {
            if ((this.rpc.button_label_1 != null && this.rpc.button_label_1.isEmpty()) || (this.rpc.button_label_2 != null && this.rpc.button_label_2.isEmpty())) {
                return this;
            }
            this.rpc.partyId = partyId;
            this.rpc.partySize = partySize;
            this.rpc.partyMax = partyMax;
            return this;
        }
        
        public Builder setButtons(final List<RPCButton> list) {
            if (list != null && !list.isEmpty()) {
                final int min = Math.min(list.size(), 2);
                this.rpc.button_label_1 = list.get(0).getLabel();
                this.rpc.button_url_1 = list.get(0).getUrl();
                if (min == 2) {
                    this.rpc.button_label_2 = list.get(1).getLabel();
                    this.rpc.button_url_2 = list.get(1).getUrl();
                }
            }
            return this;
        }
        
        public Builder setStartTimestamp(final OffsetDateTime offsetDateTime) {
            this.rpc.startTimestamp = offsetDateTime.toEpochSecond();
            return this;
        }
        
        public Builder setSecrets(final String matchSecret, final String joinSecret, final String spectateSecret) {
            if ((this.rpc.button_label_1 != null && this.rpc.button_label_1.isEmpty()) || (this.rpc.button_label_2 != null && this.rpc.button_label_2.isEmpty())) {
                return this;
            }
            this.rpc.matchSecret = matchSecret;
            this.rpc.joinSecret = joinSecret;
            this.rpc.spectateSecret = spectateSecret;
            return this;
        }
        
        public Builder setButtons(final RPCButton rpcButton, final RPCButton rpcButton2) {
            return this.setButtons(Arrays.asList(rpcButton, rpcButton2));
        }
        
        public Builder setStartTimestamp(final long startTimestamp) {
            this.rpc.startTimestamp = startTimestamp;
            return this;
        }
        
        public Builder() {
            this.rpc = new DiscordRichPresence();
        }
        
        public Builder setSecrets(final String joinSecret, final String spectateSecret) {
            if ((this.rpc.button_label_1 != null && this.rpc.button_label_1.isEmpty()) || (this.rpc.button_label_2 != null && this.rpc.button_label_2.isEmpty())) {
                return this;
            }
            this.rpc.joinSecret = joinSecret;
            this.rpc.spectateSecret = spectateSecret;
            return this;
        }
        
        public Builder setEndTimestamp(final long endTimestamp) {
            this.rpc.endTimestamp = endTimestamp;
            return this;
        }
        
        public Builder setEndTimestamp(final OffsetDateTime offsetDateTime) {
            this.rpc.endTimestamp = offsetDateTime.toEpochSecond();
            return this;
        }
        
        public Builder setLargeImage(final String s) {
            return this.setLargeImage(s, "");
        }
        
        public DiscordRichPresence build() {
            return this.rpc;
        }
    }
}
