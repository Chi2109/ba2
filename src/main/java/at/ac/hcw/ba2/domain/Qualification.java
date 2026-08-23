package at.ac.hcw.ba2.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Locale;

public enum Qualification {

    RS(10),
    NFS(20);

    private final int level;

    Qualification(int level) {
        this.level = level;
    }

    public boolean satisfies(Qualification requiredQualification) {
        return this.level >= requiredQualification.level;
    }

    @JsonCreator
    public static Qualification fromJson(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);

        return Arrays.stream(values())
                .filter(qualification ->
                        qualification.name().equals(normalized))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unsupported qualification: " + value
                        )
                );
    }

    @JsonValue
    public String jsonValue() {
        return name();
    }
}
