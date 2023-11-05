package thunder.hack.setting.impl;

public class BooleanParent {
    private boolean enabled, extended;

    public BooleanParent(boolean enabled) {
        this.enabled = enabled;
        extended = false;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
