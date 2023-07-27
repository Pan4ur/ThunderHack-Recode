package thunder.hack.setting.impl;


import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public class Bind {
    private int key;
    private boolean hold, mouse;

    public Bind(int key, boolean mouse) {
        this.key = key;
        this.mouse = mouse;
    }

    public int getKey() {
        return this.key;
    }

    public String getBind() {
        if(mouse) return "M"+key;

        String kn = this.key > 0 ? GLFW.glfwGetKeyName(this.key + 0, GLFW.glfwGetKeyScancode(this.key + 0)) : "None";
        if (kn == null) {
            try {
                for (Field declaredField : GLFW.class.getDeclaredFields()) {
                    if (declaredField.getName().startsWith("GLFW_KEY_")) {
                        int a = (int) declaredField.get(null);
                        if (a == this.key) {
                            String nb = declaredField.getName().substring("GLFW_KEY_".length());
                            kn = nb.substring(0, 1).toUpperCase() + nb.substring(1).toLowerCase();
                        }
                    }
                }
            } catch (Exception ignored) {
                kn = "unknown." + (this.key + 0);
            }
        }

        return this.key == -1 ? "None" : (kn + "").toUpperCase();
    }


    public boolean isHold() {
        return hold;
    }

    public boolean isMouse() {
        return mouse;
    }

    public void setHold(boolean hold) {
        this.hold = hold;
    }
}

