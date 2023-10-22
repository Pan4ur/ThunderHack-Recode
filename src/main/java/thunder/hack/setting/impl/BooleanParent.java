package thunder.hack.setting.impl;

public class BooleanParent {
    private boolean state, extended;

    public BooleanParent(boolean state) {
        this.state = state;
        extended = false;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
