package thunder.hack.setting.impl;

public class Parent {
    private boolean extended;
    private final int hierarchy;

    public Parent(boolean extended, int hierarchy) {
        this.extended = extended;
        this.hierarchy = hierarchy;
    }

    public boolean isExtended() {
        return extended;
    }

    public int getHierarchy() {
        return hierarchy;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }
}