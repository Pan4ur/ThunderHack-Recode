package thunder.hack.setting;


import com.google.gson.JsonPrimitive;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.SettingEvent;
import thunder.hack.setting.impl.*;

import java.util.function.Predicate;

public class Setting<T> {
    private final String name;
    private final T defaultValue;
    private T value;
    private T plannedValue;
    private T min;
    private T max;
    public Setting<?> parent = null;

    private boolean hasRestriction;
    private Predicate<T> visibility;
    private Module module;

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.plannedValue = defaultValue;
    }


    public Setting(String name, T defaultValue, T min, T max) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.plannedValue = defaultValue;
        this.hasRestriction = true;
    }


    public Setting(String name, T defaultValue, T min, T max, Predicate<T> visibility) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.plannedValue = defaultValue;
        this.visibility = visibility;
        this.hasRestriction = true;
    }

    public Setting(String name, T defaultValue, Predicate<T> visibility) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.visibility = visibility;
        this.plannedValue = defaultValue;
    }

    public static Enum get(Enum clazz) {
        int index = EnumConverter.currentEnum(clazz);
        for (int i = 0; i < clazz.getClass().getEnumConstants().length; ++i) {
            Enum e = clazz.getClass().getEnumConstants()[i];
            if (i != index + 1) continue;
            return e;
        }
        return clazz.getClass().getEnumConstants()[0];
    }

    public String getName() {
        return this.name;
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(T value) {
        this.setPlannedValue(value);
        if (this.hasRestriction) {
            if (((Number) this.min).floatValue() > ((Number) value).floatValue()) {
                this.setPlannedValue(this.min);
            }
            if (((Number) this.max).floatValue() < ((Number) value).floatValue()) {
                this.setPlannedValue(this.max);
            }
        }
        this.value = this.plannedValue;
        ThunderHack.EVENT_BUS.post(new SettingEvent(this));
    }

    public float getPow2Value() {
        if (value instanceof Float) {
            return (float) value * (float) value;
        }
        if (value instanceof Integer) {
            return (int) value * (int) value;
        }
        return 0;
    }

    public void setPlannedValue(T value) {
        this.plannedValue = value;
    }

    public T getMin() {
        return this.min;
    }

    public void setMin(T min) {
        this.min = min;
    }

    public T getMax() {
        return this.max;
    }

    public void setMax(T max) {
        this.max = max;
    }

    public Module getModule() {
        return this.module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public String currentEnumName() {
        return EnumConverter.getProperName((Enum) this.value);
    }

    public String[] getModes() {
        return EnumConverter.getNames((Enum) this.value);
    }

    public void setEnum(Enum mod) {
        this.plannedValue = (T) mod;
    }

    public void increaseEnum() {
        this.plannedValue = (T) EnumConverter.increaseEnum((Enum) this.value);
        this.value = this.plannedValue;
        ThunderHack.EVENT_BUS.post(new SettingEvent(this));
    }

    public void setEnumByNumber(int id) {
        this.plannedValue = (T) EnumConverter.setEnumInt((Enum) this.value, id);
        this.value = this.plannedValue;
        ThunderHack.EVENT_BUS.post(new SettingEvent(this));
    }

    public String getType() {
        if (this.isEnumSetting()) {
            return "Enum";
        }
        if (this.isColorSetting()) {
            return "ColorSetting";
        }
        if (this.isPositionSetting()) {
            return "PositionSetting";
        }
        if (this.isBooleanParent()) {
            return "BooleanParent";
        }
        return this.getClassName(this.defaultValue);
    }

    public boolean isBooleanParent() {
        return this.value instanceof BooleanParent;
    }


    public <T> String getClassName(T value) {
        return value.getClass().getSimpleName();
    }

    public boolean isNumberSetting() {
        return this.value instanceof Double || this.value instanceof Integer || this.value instanceof Short || this.value instanceof Long || this.value instanceof Float;
    }

    public boolean isInteger() {
        return this.value instanceof Integer;
    }

    public boolean isFloat() {
        return this.value instanceof Float;
    }

    public boolean isEnumSetting() {
        return !this.isPositionSetting() && !this.isBooleanParent() && !this.isNumberSetting() && !(this.value instanceof PositionSetting) && !(this.value instanceof JsonPrimitive) && !(this.value instanceof String) && !(this.value instanceof ColorSetting) && !(this.value instanceof Parent) && !(this.value instanceof Bind) && !(this.value instanceof Character) && !(this.value instanceof Boolean);
    }

    public boolean isBindSetting() {
        return this.value instanceof Bind;
    }

    public boolean isStringSetting() {
        return this.value instanceof String;
    }

    public boolean isColorSetting() {
        return this.value instanceof ColorSetting;
    }

    public boolean isPositionSetting() {
        return this.value instanceof PositionSetting;
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }

    public String getValueAsString() {
        return this.value.toString();
    }

    public boolean hasRestriction() {
        return this.hasRestriction;
    }

    public Setting<T> withParent(Setting<?> parent) {
        this.parent = parent;
        return this;
    }

    public boolean isVisible() {
        if (parent != null) {

            if(parent.getValue() instanceof BooleanParent bp)
                if (!bp.isExtended())
                    return false;

            if(parent.getValue() instanceof Parent p)
                if (!p.isExtended())
                    return false;
        }
        if (this.visibility == null) {
            return true;
        }
        return this.visibility.test(this.getValue());
    }
}

