package thunder.hack.setting.impl;

public class PositionSetting {
    public float x;
    public float y;

    public PositionSetting(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setX(float posx) {
        this.x = posx;
    }

    public float getX() {
        return this.x;
    }

    public void setY(float posy) {
        this.y = posy;
    }

    public float getY() {
        return this.y;
    }
}
