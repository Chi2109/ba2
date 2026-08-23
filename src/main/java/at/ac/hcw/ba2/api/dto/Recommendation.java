package at.ac.hcw.ba2.api.dto;

import at.ac.hcw.ba2.domain.Priority;

public record Recommendation(
        Priority priority,
        String text,
        String source,
        String requiredQualification,
        boolean requiresEscalation
) {
}
