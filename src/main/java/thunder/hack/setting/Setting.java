package thunder.hack.setting;

import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSetting;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.impl.*;

import java.util.function.Predicate;

public class Setting<T> {
    private final String name;
    private final T defaultValue;
    private T value;
    private T plannedValue;
    private T min;
    private T max;
    public Setting<?> group = null;

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
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        setValueSilent(value);
        ThunderHack.EVENT_BUS.post(new EventSetting(this));
    }

    public void setValueSilent(T value) {
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

    @SuppressWarnings("unchecked")
    public void setEnum(Enum mod) {
        plannedValue = (T) mod;
    }

    @SuppressWarnings("unchecked")
    public void increaseEnum() {
        plannedValue = (T) EnumConverter.increaseEnum((Enum) value);
        value = plannedValue;
        ThunderHack.EVENT_BUS.post(new EventSetting(this));
    }

    @SuppressWarnings("unchecked")
    public void setEnumByNumber(int id) {
        plannedValue = (T) EnumConverter.setEnumInt((Enum) value, id);
        value = plannedValue;
        ThunderHack.EVENT_BUS.post(new EventSetting(this));
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
        return value.getClass().isEnum();
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

    public boolean isPositionSetting() {
        return value instanceof PositionSetting;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public boolean hasRestriction() {
        return hasRestriction;
    }

    public Setting<T> addToGroup(Setting<?> group) {
        this.group = group;
        return this;
    }

    public boolean isVisible() {
        if (group != null) {
            if (group.getValue() instanceof BooleanSettingGroup bp)
                if (!bp.isExtended())
                    return false;

            if (group.getValue() instanceof SettingGroup p)
                if (!p.isExtended())
                    return false;
        }

        if (visibility == null)
            return true;

        return visibility.test(getValue());
    }

    public boolean is(T v) {
        return value == v;
    }

    public boolean not(T v) {
        return value != v;
    }
}

