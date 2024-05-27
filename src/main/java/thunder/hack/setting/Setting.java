package thunder.hack.setting;


import com.google.gson.JsonPrimitive;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSetting;
import thunder.hack.modules.Module;
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
        this. max = max;
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
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        setPlannedValue(value);
        if (hasRestriction) {
            if (((Number) min).floatValue() > ((Number) value).floatValue()) {
                setPlannedValue(min);
            }
            if (((Number) max).floatValue() < ((Number) value).floatValue()) {
                setPlannedValue(max);
            }
        }
        this.value = plannedValue;
        ThunderHack.EVENT_BUS.post(new EventSetting(this));
    }

    public float getPow2Value() {
        if (value instanceof Float)
            return (float) value * (float) value;

        if (value instanceof Integer)
            return (int) value * (int) value;

        return 0;
    }

    public void setPlannedValue(T value) {
        plannedValue = value;
    }

    public T getMin() {
        return min;
    }

    public void setMin(T min) {
        this.min = min;
    }

    public T getMax() {
        return max;
    }

    public void setMax(T max) {
        this.max = max;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public String currentEnumName() {
        return EnumConverter.getProperName((Enum) value);
    }

    public String[] getModes() {
        return EnumConverter.getNames((Enum) value);
    }

    public void setEnum(Enum mod) {
        plannedValue = (T) mod;
    }

    public void increaseEnum() {
        plannedValue = (T) EnumConverter.increaseEnum((Enum) value);
        value = plannedValue;
        ThunderHack.EVENT_BUS.post(new EventSetting(this));
    }

    public void setEnumByNumber(int id) {
        plannedValue = (T) EnumConverter.setEnumInt((Enum) value, id);
        value = plannedValue;
        ThunderHack.EVENT_BUS.post(new EventSetting(this));
    }

    public String getType() {
        if (isEnumSetting()) {
            return "Enum";
        }
        if (isColorSetting()) {
            return "ColorSetting";
        }
        if (isPositionSetting()) {
            return "PositionSetting";
        }
        if (isBooleanParent()) {
            return "BooleanParent";
        }
        if (value instanceof ItemSelectSetting) {
            return "ItemSelectSetting";
        }
        return getClassName(defaultValue);
    }

    public boolean isBooleanParent() {
        return value instanceof BooleanParent;
    }

    public <T> String getClassName(T value) {
        return value.getClass().getSimpleName();
    }

    public boolean isNumberSetting() {
        return value instanceof Double || value instanceof Integer || value instanceof Short || value instanceof Long || value instanceof Float;
    }

    public boolean isInteger() {
        return value instanceof Integer;
    }

    public boolean isFloat() {
        return value instanceof Float;
    }

    public boolean isEnumSetting() {
        return !isPositionSetting() && !isBooleanParent() && !isNumberSetting() && !(value instanceof ItemSelectSetting) && !(value instanceof PositionSetting) && !(value instanceof JsonPrimitive) && !(value instanceof String) && !(value instanceof ColorSetting) && !(value instanceof SettingGroup) && !(value instanceof Bind) && !(value instanceof Character) && !(value instanceof Boolean);
    }

    public boolean isBindSetting() {
        return value instanceof Bind;
    }

    public boolean isStringSetting() {
        return value instanceof String;
    }
    
    public boolean isItemSelectSetting() {
        return value instanceof ItemSelectSetting;
    }
    
    public boolean isColorSetting() {
        return value instanceof ColorSetting;
    }

    public boolean isPositionSetting() {
        return value instanceof PositionSetting;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public String getValueAsString() {
        return value.toString();
    }

    public boolean hasRestriction() {
        return hasRestriction;
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

            if(parent.getValue() instanceof SettingGroup p)
                if (!p.isExtended())
                    return false;
        }

        if (visibility == null)
            return true;

        return visibility.test(getValue());
    }

    public boolean is(T colorMode) {
        return value == colorMode;
    }
}

