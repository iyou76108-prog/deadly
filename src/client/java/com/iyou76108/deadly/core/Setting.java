package com.iyou76108.deadly.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Locale;
import java.util.Objects;

public final class Setting<T> {
    public enum Kind {
        BOOLEAN, INTEGER, DECIMAL
    }

    private final String id;
    private final String label;
    private final Kind kind;
    private final T defaultValue;
    private final double min;
    private final double max;
    private final double step;
    private T value;
    private Runnable changeListener = () -> { };

    private Setting(String id, String label, Kind kind, T defaultValue, double min, double max, double step) {
        this.id = Objects.requireNonNull(id);
        this.label = Objects.requireNonNull(label);
        this.kind = Objects.requireNonNull(kind);
        this.defaultValue = Objects.requireNonNull(defaultValue);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public static Setting<Boolean> bool(String id, String label, boolean defaultValue) {
        return new Setting<>(id, label, Kind.BOOLEAN, defaultValue, 0, 1, 1);
    }

    public static Setting<Integer> integer(String id, String label, int defaultValue, int min, int max, int step) {
        return new Setting<>(id, label, Kind.INTEGER, defaultValue, min, max, step);
    }

    public static Setting<Double> decimal(String id, String label, double defaultValue, double min, double max, double step) {
        return new Setting<>(id, label, Kind.DECIMAL, defaultValue, min, max, step);
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Kind getKind() {
        return kind;
    }

    public T getValue() {
        return value;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener == null ? () -> { } : changeListener;
    }

    public void reset() {
        setValue(defaultValue);
    }

    public void setValue(T next) {
        T normalized = normalize(next);
        if (!Objects.equals(value, normalized)) {
            value = normalized;
            changeListener.run();
        }
    }

    @SuppressWarnings("unchecked")
    private T normalize(T candidate) {
        if (candidate == null) {
            return defaultValue;
        }
        return switch (kind) {
            case BOOLEAN -> (T) Boolean.valueOf((Boolean) candidate);
            case INTEGER -> (T) Integer.valueOf((int) Math.round(clamp(((Number) candidate).doubleValue())));
            case DECIMAL -> (T) Double.valueOf(clamp(((Number) candidate).doubleValue()));
        };
    }

    private double clamp(double number) {
        return Math.max(min, Math.min(max, number));
    }

    @SuppressWarnings("unchecked")
    public void cycle() {
        switch (kind) {
            case BOOLEAN -> setValue((T) Boolean.valueOf(!((Boolean) value)));
            case INTEGER -> {
                int next = (int) Math.round(((Number) value).doubleValue() + step);
                setValue((T) Integer.valueOf(next > max ? (int) min : next));
            }
            case DECIMAL -> {
                double next = ((Number) value).doubleValue() + step;
                setValue((T) Double.valueOf(next > max + 1.0E-6 ? min : next));
            }
        }
    }

    public String formattedValue() {
        return switch (kind) {
            case BOOLEAN -> (Boolean) value ? "ON" : "OFF";
            case INTEGER -> Integer.toString((Integer) value);
            case DECIMAL -> String.format(Locale.ROOT, "%.1f", (Double) value);
        };
    }

    public JsonElement toJson() {
        return switch (kind) {
            case BOOLEAN -> new JsonPrimitive((Boolean) value);
            case INTEGER -> new JsonPrimitive((Integer) value);
            case DECIMAL -> new JsonPrimitive((Double) value);
        };
    }

    @SuppressWarnings("unchecked")
    public void readJson(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        try {
            switch (kind) {
                case BOOLEAN -> setValue((T) Boolean.valueOf(element.getAsBoolean()));
                case INTEGER -> setValue((T) Integer.valueOf(element.getAsInt()));
                case DECIMAL -> setValue((T) Double.valueOf(element.getAsDouble()));
            }
        } catch (RuntimeException ignored) {
            // Invalid values are ignored; the default/current value remains usable.
        }
    }
}
