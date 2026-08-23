package at.ac.hcw.ba2.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Priority {
    CRITICAL,
    HIGH,
    NORMAL,
    LOW;

    @JsonValue
    public String jsonValue() {
        return name().toLowerCase();
    }
}
