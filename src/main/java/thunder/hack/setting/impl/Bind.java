package thunder.hack.setting.impl;


import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.Objects;

public class Bind {
    private int key;
    private boolean hold = false;

    public Bind(int key) {
        this.key = key;
    }

    public static Bind none() {
        return new Bind(-1);
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isEmpty() {
        return this.key < 0;
    }

    public String toString() {
        String kn = this.key > 0 ? GLFW.glfwGetKeyName((int) (this.key + 0), GLFW.glfwGetKeyScancode((int) (this.key + 0))) : "None";
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
                kn = "unknown." + (int) (this.key + 0);
            }
        }

        return this.isEmpty() ? "None" : (kn + "").toUpperCase();
    }

    public String getBBB() {
        String kn = this.key > 0 ? GLFW.glfwGetKeyName((int) (this.key + 0), GLFW.glfwGetKeyScancode((int) (this.key + 0))) : "None";
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
                kn = "unknown." + (int) (this.key + 0);
            }
        }

        return this.isEmpty() ? "None" : (kn + "").toUpperCase();
    }

    private String capitalise(String str) {
        if (str.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(str.charAt(0)) + (str.length() != 1 ? str.substring(1).toLowerCase() : "");
    }

    public boolean isHold() {
        return hold;
    }

    public void setHold(boolean hold) {
        this.hold = hold;
    }

    public static class BindConverter extends Converter<Bind, JsonElement> {
        public JsonElement doForward(Bind bind) {
            return new JsonPrimitive(bind.toString());
        }

        public Bind doBackward(JsonElement jsonElement) {
            String s = jsonElement.getAsString();
            if (s.equalsIgnoreCase("None")) {
                return Bind.none();
            }
            int key = -1;
            try {
                key = InputUtil.fromTranslationKey("key.keyboard." + s.toLowerCase()).getCode();
            } catch (Exception exception) {
                // empty catch block
            }

            if(key == -1 ){
                key = checkKey(s.toUpperCase());
            }
            if (key == 0) {
                return Bind.none();
            }
            return new Bind(key);
        }
    }

    private static int checkKey(String s){
        if(Objects.equals(s, "LEFT_CONTROL")) return GLFW.GLFW_KEY_LEFT_CONTROL;
        if(Objects.equals(s, "LEFT_ALT")) return GLFW.GLFW_KEY_LEFT_ALT;
        if(Objects.equals(s, "LEFT_SHIFT")) return GLFW.GLFW_KEY_LEFT_SHIFT;
        if(Objects.equals(s, "CAPS_LOCK")) return GLFW.GLFW_KEY_CAPS_LOCK;
        if(Objects.equals(s, "TAB")) return GLFW.GLFW_KEY_TAB;
        if(Objects.equals(s, "RIGHT_CONTROL")) return GLFW.GLFW_KEY_RIGHT_CONTROL;
        if(Objects.equals(s, "RIGHT_ALT")) return GLFW.GLFW_KEY_RIGHT_ALT;
        if(Objects.equals(s, "RIGHT_SHIFT")) return GLFW.GLFW_KEY_RIGHT_SHIFT;
        if(Objects.equals(s, "ENTER")) return GLFW.GLFW_KEY_ENTER;
        if(Objects.equals(s, "BACKSPACE")) return GLFW.GLFW_KEY_BACKSPACE;
        if(Objects.equals(s, "DOWN")) return GLFW.GLFW_KEY_DOWN;
        if(Objects.equals(s, "UP")) return GLFW.GLFW_KEY_UP;
        if(Objects.equals(s, "LEFT")) return GLFW.GLFW_KEY_LEFT;
        if(Objects.equals(s, "RIGHT")) return GLFW.GLFW_KEY_RIGHT;

        return 0;
    }
}

