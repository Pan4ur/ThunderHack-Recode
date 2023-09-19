package thunder.hack.setting.impl;

public class PositionSetting {
    public float x;
    public float y;

    public PositionSetting(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getX() {
        return this.x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getY() {
        return this.y;
    }
}
