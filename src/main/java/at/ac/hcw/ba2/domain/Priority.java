package at.ac.hcw.ba2.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum Priority {
    CRITICAL,
    HIGH,
    NORMAL,
    LOW;

    @JsonCreator
    public static Priority fromJson(String value) {
        return Priority.valueOf(value.toUpperCase(Locale.ROOT));
    }

    @JsonValue
    public String jsonValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}